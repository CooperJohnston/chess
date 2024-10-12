package dataaccess;

import model.AuthData;

public interface AuthDAO {
  AuthData creatAuthData(String Username) throws DataAccessException;
  boolean checkAuthData(String AutToken) throws DataAccessException;
  void deleteAuthData(String AutToken) throws DataAccessException;
}
