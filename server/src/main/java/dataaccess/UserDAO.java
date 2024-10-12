package dataaccess;

import model.UserData;

public interface UserDAO {
  void insertUser(UserData u) throws DataAccessException;  // Create a new user
  UserData getUser(String username) throws DataAccessException;
}
