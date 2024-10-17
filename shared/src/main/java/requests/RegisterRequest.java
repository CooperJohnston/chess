package requests;

public class RegisterRequest {
  private final String email;
  private final String password;
  private String username;

  public RegisterRequest(String username, String password, String email) {
    this.email=email;
    this.password=password;
    this.username=username;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getEmail() {
    return email;
  }

}
