package service;

import java.util.UUID;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import requests.LoginRequest;
import requests.RegisterRequest;

public class AuthService {

  private final AuthDAO DAO;

  AuthService(AuthDAO DAO) {
    this.DAO=DAO;
  }

  void clear() throws DataAccessException {
    DAO.clear();
  }

  public String makeAuth(String username) throws DataAccessException {
    String token=UUID.randomUUID().toString();
    AuthData authData=new AuthData(username, token);
    DAO.createAuthData(authData);
    return token;
  }

  public String makeAuth(RegisterRequest registerRequest) throws DataAccessException {
    return makeAuth(registerRequest.getUsername());
  }

  public String makeAuth(LoginRequest loginRequest) throws DataAccessException {
    return makeAuth(loginRequest.getUsername());
  }

  public void logout(String token) throws DataAccessException {
    DAO.deleteAuthData(token);
  }

  public boolean authenticate(String token) throws DataAccessException {
    if (DAO.checkAuthData(token)) {
      return true;
    } else {
      throw new DataAccessException("User is unauthorized");
    }
  }


}
