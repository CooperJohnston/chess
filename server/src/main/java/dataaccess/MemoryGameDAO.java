package dataaccess;

import model.GameData;

import java.util.ArrayList;

public class MemoryGameDAO implements GameDAO {

  public ArrayList<GameData> games;

  public MemoryGameDAO() {
    games=new ArrayList<>();
  }

  @Override
  public void clear() {
    games.clear();
  }

  @Override
  public void createGame(GameData game) {
    games.add(game);
  }

  @Override
  public GameData getGame(int id) {
    for (GameData game : games) {
      if (game.gameID() == id) {
        return game;
      }
    }
    return null;
  }

  @Override
  public ArrayList<GameData> getAllGames() {
    return games;
  }

  @Override
  public void updateGame(GameData game) {
    for (GameData g : games) {
      if (g.gameID() == game.gameID()) {
        games.set(games.indexOf(g), game);
      }
    }
  }

  @Override
  public boolean checkGame(String game) throws DataAccessException {
    for (GameData gameData : games) {
      if (gameData.gameName().equals(game)) {
        return true;
      }

    }
    return false;
  }

  @Override
  public boolean checkGame(int gameID) throws DataAccessException {
    for (GameData gameData : games) {
      if (gameData.gameID() == gameID) {
        return true;
      }

    }
    return false;
  }

  @Override
  public void remove(int gameID) throws DataAccessException {
    for (GameData gameData : games) {
      if (gameData.gameID() == gameID) {
        games.remove(gameData);
        return;
      }
    }
  }
}

