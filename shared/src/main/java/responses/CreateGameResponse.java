package responses;

public class CreateGameResponse {
  private final int gameID;
  private String auth;

  public CreateGameResponse(int gameId) {
    this.gameID=gameId;
  }

  public int getGameID() {
    return gameID;
  }

  public String getAuthToken() {
    return auth;
  }

  public void setAuthToken(String auth) {
    this.auth=auth;
  }

  @Override
  public String toString() {
    return "CreateGameResponse{" +
            "gameID=" + gameID +
            ", auth='" + auth + '\'' +
            '}';
  }
}
