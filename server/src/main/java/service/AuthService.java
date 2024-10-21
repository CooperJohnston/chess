package service;

import java.util.UUID;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import requests.LoginRequest;
import requests.RegisterRequest;

public class AuthService {

  private final AuthDAO dao;

  public AuthService(AuthDAO DAO) {
    this.dao=DAO;
  }

  public void clear() throws DataAccessException {
    dao.clear();
  }

  public String makeAuth(String username) throws DataAccessException {
    if (username == null || username.isEmpty()) {
      throw new DataAccessException("Username cannot be null or empty");
    }
    String token=UUID.randomUUID().toString();
    AuthData authData=new AuthData(token, username);
    dao.createAuthData(authData);
    return token;
  }

  public String makeAuth(RegisterRequest registerRequest) throws DataAccessException {
    return makeAuth(registerRequest.username());
  }

  public String makeAuth(LoginRequest loginRequest) throws DataAccessException {
    return makeAuth(loginRequest.getUsername());
  }

  public void logout(String token) throws DataAccessException {
    dao.deleteAuthData(token);
  }

  public boolean authenticate(String token) throws DataAccessException {
    if (dao.checkAuthData(token)) {
      return true;
    } else {
      throw new DataAccessException("Error: unauthorized");
    }
  }


}
