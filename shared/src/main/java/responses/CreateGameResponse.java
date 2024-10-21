package responses;

public class CreateGameResponse {
  private final int gameID;
  private String auth;

  public CreateGameResponse(int gameId, String auth) {
    this.gameID=gameId;
    this.auth=auth;
  }

  public int getGameId() {
    return gameID;
  }

  public String getAuth() {
    return auth;
  }

  public void setAuth(String auth) {
    this.auth=auth;
  }
}
