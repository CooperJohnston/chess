package requests;

import chess.ChessGame;

public class JoinGameRequest {

  private final ChessGame.TeamColor playerColor;
  private String authToken;
  private final int gameID;

  public JoinGameRequest(ChessGame.TeamColor playerColor, int ID) {
    this.playerColor=playerColor;
    this.gameID=ID;
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
}
