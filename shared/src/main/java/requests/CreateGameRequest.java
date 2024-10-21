package requests;

public class CreateGameRequest {
  private final String gameName;
  private String authToken;

  public CreateGameRequest(String name, String authToken) {
    this.gameName=name;
    this.authToken=authToken;
  }

  public String getGameName() {
    return gameName;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken=authToken;
  }

}
