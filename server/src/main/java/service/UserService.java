package service;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;
import requests.LoginRequest;
import requests.RegisterRequest;
import responses.LoginResponse;
import responses.RegisterResponse;

public class UserService {
  private final UserDAO dao;

  public UserService(UserDAO dao) {
    this.dao=dao;
  }

  public void clear() throws DataAccessException {
    this.dao.clear();
  }

  public RegisterResponse registerUser(RegisterRequest regRequest) throws DataAccessException {
    if (regRequest.username() == null || regRequest.username().isEmpty() ||
            regRequest.password() == null || regRequest.password().isEmpty() ||
            regRequest.email() == null || regRequest.email().isEmpty()) {
      throw new DataAccessException("Error: bad request");
    }

    UserData userData=new UserData(regRequest.username(), regRequest.password(), regRequest.email());
    if (dao.checkUser(userData)) {
      throw new DataAccessException("Error: already taken");
    }

    dao.insertUser(userData);
    return new RegisterResponse(null, regRequest.username());

  }

  public LoginResponse loginUser(LoginRequest loginRequest) throws DataAccessException {
    if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
      throw new DataAccessException("Error: authorized");
    }
    UserData userData=new UserData(loginRequest.getUsername(), loginRequest.getPassword(), null);
    if (!dao.checkUser(userData) || !dao.getUser(userData).password().equals(loginRequest.getPassword())) {
      throw new DataAccessException("Error: unauthorized");
    }
    return new LoginResponse(loginRequest.getUsername(), null);

  }


}