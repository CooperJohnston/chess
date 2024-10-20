package service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
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

  public int generateGameID(String gameName) throws DataAccessException {
    if (gameName.isEmpty()) {
      throw new DataAccessException("Game name cannot be empty");
    }
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
      throw new DataAccessException("No algorithm available");
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
      GameData newGame=new GameData(newID, null, null, gameRequest.getName(), new ChessGame());
      gameDAO.createGame(newGame);
      return new CreateGameResponse(newID, gameRequest.getAuthToken());

    } catch (DataAccessException e) {
      throw new DataAccessException("Error: NoSuchAlgorithmException");
    }
  }

  public JoinGameResponse joinGame(JoinGameRequest gameRequest) throws DataAccessException {
    if (gameRequest.getID() <= 0) {
      throw new DataAccessException("Error: bad request");
    }
    GameData gameData=gameDAO.getGame(gameRequest.getID());
    if (gameData == null) {
      throw new DataAccessException("Error: unauthorized");
    }
    if (gameRequest.getPlayerColor() == null || gameRequest.getPlayerColor().isEmpty()) {
      return new JoinGameResponse(gameRequest.getID(), null, gameRequest.getAuth());
    }
    AuthData authData=authDAO.getAuthData(gameRequest.getAuth());
    if (authData == null) {
      throw new DataAccessException("Error: Invalid authorization token");
    }
    String userName=authData.Username();
    if ("WHITE".equals(gameRequest.getPlayerColor()) && gameData.whiteUsername() == null) {
      GameData updateData=new GameData(gameData.gameID(), userName, gameData.blackUsername(), gameData.gameName(), gameData.game());
      gameDAO.updateGame(updateData);
      return new JoinGameResponse(gameRequest.getID(), "WHITE", gameRequest.getAuth());
    }
    if ("BLACK".equals(gameRequest.getPlayerColor()) && gameData.blackUsername() == null) {
      GameData updateData=new GameData(gameData.gameID(), gameData.whiteUsername(), userName, gameData.gameName(), gameData.game());
      gameDAO.updateGame(updateData);
      return new JoinGameResponse(gameRequest.getID(), "BLACK", gameRequest.getAuth());
    }
    throw new DataAccessException("Error: already taken");
  }


  public ArrayList<GameData> getAllGames() throws DataAccessException {
    return gameDAO.getAllGames();
  }


}
