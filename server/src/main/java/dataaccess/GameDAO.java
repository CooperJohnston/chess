package dataaccess;

import model.GameData;

import java.util.ArrayList;

public interface GameDAO {
  String createGame(GameData game);
  GameData getGame(int id);
  void updateGame(GameData game);
  ArrayList<GameData> getAllGames();
}
