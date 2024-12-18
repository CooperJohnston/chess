package dataaccess;

import model.GameData;

import java.util.ArrayList;

public interface GameDAO {
  void createGame(GameData game) throws DataAccessException;

  GameData getGame(int id) throws DataAccessException;

  void updateGame(GameData game) throws DataAccessException;

  ArrayList<GameData> getAllGames() throws DataAccessException;

  void clear() throws DataAccessException;

  boolean checkGame(String gameName) throws DataAccessException;

  boolean checkGame(int gameID) throws DataAccessException;

  public void remove(int gameID) throws DataAccessException;
}
