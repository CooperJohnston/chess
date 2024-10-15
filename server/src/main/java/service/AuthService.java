package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

public class AuthService {

  private final AuthDAO DAO;

  AuthService(AuthDAO DAO) {
    this.DAO=DAO;
  }

  void clear() throws DataAccessException {
    DAO.clear();
  }

}
