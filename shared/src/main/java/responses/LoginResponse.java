package responses;

public class LoginResponse {
  private String username;
  private String auth;

  public LoginResponse(String username, String auth) {
    this.username=username;
    this.auth=auth;
  }

  public String getUsername() {
    return username;
  }

  public String getAuth() {
    return auth;
  }
}
