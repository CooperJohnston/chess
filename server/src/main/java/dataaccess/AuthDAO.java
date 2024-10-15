package dataaccess;

import model.AuthData;

public interface AuthDAO {
  void createAuthData(AuthData NewAuth) throws DataAccessException;

  boolean checkAuthData(String AutToken) throws DataAccessException;

  AuthData getAuthData(String AutToken) throws DataAccessException;

  void deleteAuthData(String AutToken) throws DataAccessException;

  void clear() throws DataAccessException;

}
