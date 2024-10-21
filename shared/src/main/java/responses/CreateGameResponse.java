package responses;

public class CreateGameResponse {
  private final int gameId;
  private String auth;

  public CreateGameResponse(int gameId, String auth) {
    this.gameId=gameId;
    this.auth=auth;
  }

  public int getGameId() {
    return gameId;
  }

  public String getAuth() {
    return auth;
  }

  public void setAuth(String auth) {
    this.auth=auth;
  }
}
