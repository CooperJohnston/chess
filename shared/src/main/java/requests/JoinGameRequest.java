package requests;

public class JoinGameRequest {

  private final String playerColor;
  private String auth;
  private final int ID;

  public JoinGameRequest(String playerColor, String auth, int ID) {
    this.playerColor=playerColor;
    this.auth=auth;
    this.ID=ID;
  }

  public String getPlayerColor() {
    return playerColor;
  }

  public String getAuth() {
    return auth;
  }

  public void setAuth(String authToken) {
    this.auth=authToken;
  }

  public int getID() {
    return ID;
  }
}
