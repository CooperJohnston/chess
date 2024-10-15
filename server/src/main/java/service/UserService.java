package service;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

public class UserService {
  private final UserDAO DAO;

  public UserService(UserDAO DAO) {
    this.DAO=DAO;
  }

  public void clear() throws DataAccessException {
    this.DAO.clear();
  }
}