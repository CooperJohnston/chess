package requests;

import chess.ChessGame;

public class JoinGameRequest {

  private final ChessGame.TeamColor playerColor;
  private String authToken;
  private final int gameID;
  boolean observe;

  public JoinGameRequest(ChessGame.TeamColor playerColor, int id) {
    this.playerColor=playerColor;
    this.gameID=id;
    this.observe=false;
  }

  public ChessGame.TeamColor getPlayerColor() {
    return playerColor;
  }

  public String getAuth() {
    return authToken;
  }

  @Override
  public String toString() {
    return "JoinGameRequest{" +
            "playerColor='" + playerColor + '\'' +
            ", authToken='" + authToken + '\'' +
            ", ID=" + gameID +
            '}';
  }

  public void setAuth(String authToken) {
    this.authToken=authToken;
  }

  public int getGameID() {
    return gameID;
  }

  public void setObserve(boolean observe) {
    this.observe=observe;
  }

  public boolean isObserve() {
    return observe;
  }
}
