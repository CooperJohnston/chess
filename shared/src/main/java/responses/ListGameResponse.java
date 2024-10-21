package responses;

import model.GameData;

import java.util.ArrayList;

public class ListGameResponse {

  private final ArrayList<GameData> games;

  public ListGameResponse(ArrayList<GameData> games) {
    this.games=games;
  }

  public ArrayList<GameData> getGames() {
    return games;
  }
}
