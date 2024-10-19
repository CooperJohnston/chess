package service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import responses.CreateGameResponse;
import responses.JoinGameResponse;

import java.util.ArrayList;

public class GameService {

  private final GameDAO gameDAO;
  private final AuthDAO authDAO;

  public GameService(GameDAO gameDAO, AuthDAO authDAO) {
    this.gameDAO=gameDAO;
    this.authDAO=authDAO;
  }

  public void clear() throws DataAccessException {
    this.gameDAO.clear();
  }

  public int generateGameID(String gameName) throws NoSuchAlgorithmException {
    try {
      MessageDigest digest=MessageDigest.getInstance("SHA-256");
      byte[] hash=digest.digest(gameName.getBytes(StandardCharsets.UTF_8));

      // Take the first 4 bytes of the hash and convert to an integer
      int gameID=Arrays.hashCode(Arrays.copyOfRange(hash, 0, 4));
      // Ensure the ID is positive
      if (gameID <= 0) {
        gameID=-gameID;
      }

      return gameID;
    } catch (NoSuchAlgorithmException e) {
      return 0;
    }
  }

  public CreateGameResponse createGame(CreateGameRequest gameRequest) throws DataAccessException {
    if (gameRequest.getName() == null || gameRequest.getName().isEmpty()) {
      throw new DataAccessException("Error: Name cannot be empty");
    }

    if (gameDAO.checkGame(gameRequest.getName())) {
      throw new DataAccessException("Error: Game already exists");
    }
    try {

      int newID=generateGameID(gameRequest.getName());
      GameData newGame=new GameData(newID, null, null, gameRequest.getName(), null);
      gameDAO.createGame(newGame);
      return new CreateGameResponse(newID, null);

    } catch (NoSuchAlgorithmException e) {
      throw new DataAccessException("Error: NoSuchAlgorithmException");
    }
  }

  public JoinGameResponse joinGame(JoinGameRequest gameRequest) throws DataAccessException {
    if (gameRequest.getID() <= 0) {
      throw new DataAccessException("Error: bad request");
    }
    if (!gameDAO.checkGame(gameRequest.getID())) {
      throw new DataAccessException("Error: unauthorized");
    }
    if (gameRequest.getPlayerColor() == null || gameRequest.getPlayerColor().isEmpty()) {
      return new JoinGameResponse(gameRequest.getID(), null, gameRequest.getAuth());
    }
    if (gameDAO.getGame(gameRequest.getID()).blackUsername() != null && gameDAO.getGame(gameRequest.getID()).whiteUsername() != null) {
      throw new DataAccessException("Error: already taken");
    }
    if (gameRequest.getPlayerColor().equals("WHITE") && gameDAO.getGame(gameRequest.getID()).whiteUsername() == null) {
      GameData gameData=gameDAO.getGame(gameRequest.getID());
      String userName=authDAO.getAuthData(gameRequest.getAuth()).Username();
      GameData updateData=new GameData(gameData.gameID(), userName, gameData.blackUsername(), gameData.gameName(), gameData.game());
      gameDAO.updateGame(updateData);
      return new JoinGameResponse(gameRequest.getID(), gameRequest.getPlayerColor(), gameRequest.getAuth());

    }
    if (gameRequest.getPlayerColor().equals("BLACK") && gameDAO.getGame(gameRequest.getID()).blackUsername() == null) {
      GameData gameData=gameDAO.getGame(gameRequest.getID());
      String userName=authDAO.getAuthData(gameRequest.getAuth()).Username();
      GameData updateData=new GameData(gameData.gameID(), gameData.whiteUsername(), userName, gameData.gameName(), gameData.game());
      gameDAO.updateGame(updateData);
      return new JoinGameResponse(gameRequest.getID(), gameRequest.getPlayerColor(), gameRequest.getAuth());
    } else {
      throw new DataAccessException("Error: already taken");
    }
  }


  public ArrayList<GameData> getAllGames() throws DataAccessException {
    return gameDAO.getAllGames();
  }


}
