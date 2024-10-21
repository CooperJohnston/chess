package responses;

public class LoginResponse {
  private final String username;
  private String authToken;

  public LoginResponse(String username, String auth) {
    this.username=username;
    this.authToken=auth;
  }

  public String getUsername() {
    return username;
  }

  public void getAuthToken(String authToken) {
    this.authToken=authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken=authToken;
  }

  public String getAuth() {
    return authToken;
  }
}
