package responses;

public class RegisterResponse {

  private String auth;
  private String username;

  public RegisterResponse(String auth, String username) {
    this.auth=auth;
    this.username=username;
  }

  public String getAuth() {
    return auth;
  }

  public String getUsername() {
    return username;
  }


}
