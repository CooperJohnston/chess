package dataaccess;

import model.AuthData;

import java.util.ArrayList;

public interface AuthDAO {
  void createAuthData(AuthData newAuth) throws DataAccessException;

  boolean checkAuthData(String autToken) throws DataAccessException;

  AuthData getAuthData(String autToken) throws DataAccessException;

  void deleteAuthData(String autToken) throws DataAccessException;

  void clear() throws DataAccessException;

  ArrayList<AuthData> getAllAuthData() throws DataAccessException;


}
