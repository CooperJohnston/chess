package requests;

public class CreateGameRequest {
  private final String name;
  private String authToken;

  public CreateGameRequest(String name, String authToken) {
    this.name=name;
    this.authToken=authToken;
  }

  public String getName() {
    return name;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken=authToken;
  }

}
