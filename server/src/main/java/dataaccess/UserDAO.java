package dataaccess;

import model.UserData;

public interface UserDAO {
  void insertUser(UserData user) throws DataAccessException;  // Create a new user

  UserData getUser(UserData user) throws DataAccessException;

  boolean checkUser(UserData user) throws DataAccessException;

  void clear() throws DataAccessException;

}
