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
  public boolean checkAuthData(String AutToken) throws DataAccessException {
    for (AuthData authData : authData) {
      if (authData.AuthToken().equals(AutToken)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public AuthData getAuthData(String AutToken) {
    for (AuthData authData : authData) {
      if (authData.AuthToken().equals(AutToken)) {
        return authData;
      }
    }
    return null;
  }

  @Override
  public void deleteAuthData(String AutToken) throws DataAccessException {
    if (getAuthData(AutToken) != null) {
      authData.remove(getAuthData(AutToken));
      return;
    }
    throw new DataAccessException("You are not allowed to delete this auth data");
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
