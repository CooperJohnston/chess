package responses;

public class RegisterResponse {

  private String authToken;
  private final String username;

  public RegisterResponse(String auth, String username) {
    this.authToken=auth;
    this.username=username;
  }

  public String getAuthToken() {
    return authToken;
  }

  @Override
  public String toString() {
    return "RegisterResponse{" +
            "auth='" + authToken + '\'' +
            ", username='" + username + '\'' +
            '}';
  }

  public void setAuth(String authToken) {
    this.authToken=authToken;
  }

  public String getUsername() {
    return username;
  }


}
