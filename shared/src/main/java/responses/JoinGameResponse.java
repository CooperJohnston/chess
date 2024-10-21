package responses;

import chess.ChessGame;

public class JoinGameResponse {
  private final int gameID;
  private final ChessGame.TeamColor name;
  private String auth;

  public JoinGameResponse(int gameID, ChessGame.TeamColor playerColor) {
    this.gameID=gameID;
    this.name=playerColor;

  }

  public int getGameID() {
    return gameID;
  }

  public String getAuth() {
    return auth;
  }

  public void setAuth(String auth) {
    this.auth=auth;
  }
}
