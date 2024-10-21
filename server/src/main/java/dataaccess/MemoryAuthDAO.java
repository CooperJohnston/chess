package dataaccess;

import model.AuthData;

import java.util.ArrayList;

public class MemoryAuthDAO implements AuthDAO {
  public ArrayList<AuthData> authData;

  public MemoryAuthDAO() {
    authData=new ArrayList<>();
  }

  @Override
  public void createAuthData(AuthData authData) {
    this.authData.add(authData);
  }

  @Override
  public boolean checkAuthData(String autToken) throws DataAccessException {
    for (AuthData authData : authData) {
      if (authData.authToken().equals(autToken)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public AuthData getAuthData(String autToken) {
    for (AuthData authData : authData) {
      if (authData.authToken().equals(autToken)) {
        return authData;
      }
    }
    return null;
  }

  @Override
  public void deleteAuthData(String autToken) throws DataAccessException {
    if (getAuthData(autToken) != null) {
      authData.remove(getAuthData(autToken));
      return;
    }
    throw new DataAccessException("Error: unauthorized");
  }

  @Override
  public void clear() {
    authData.clear();
  }


  @Override
  public ArrayList<AuthData> getAllAuthData() {
    return authData;
  }


}
