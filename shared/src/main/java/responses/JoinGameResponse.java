package responses;

public class JoinGameResponse {
  private final int ID;
  private final String name;
  private String auth;

  public JoinGameResponse(int ID, String name, String auth) {
    this.ID=ID;
    this.name=name;
    this.auth=auth;
  }

  public int getID() {
    return ID;
  }

  public String getName() {
    return name;
  }

  public String getAuth() {
    return auth;
  }

  public void setAuth(String auth) {
    this.auth=auth;
  }
}
