package service;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import requests.LoginRequest;
import requests.RegisterRequest;
import responses.LoginResponse;
import responses.RegisterResponse;

public class UserService {
  private final UserDAO DAO;

  public UserService(UserDAO DAO) {
    this.DAO=DAO;
  }

  public void clear() throws DataAccessException {
    this.DAO.clear();
  }

  public RegisterResponse registerUser(RegisterRequest regRequest) throws DataAccessException {
    if ((regRequest.getPassword() == null) || (regRequest.getEmail() == null) || (regRequest.getUsername() == null)) {
      throw new DataAccessException("Username and password are required");
    }
    UserData userData=new UserData(regRequest.getUsername(), regRequest.getPassword(), regRequest.getEmail());
    if (DAO.checkUser(userData)) {
      throw new DataAccessException("User already exists in the database");
    }

    DAO.insertUser(userData);
    return new RegisterResponse(null, regRequest.getUsername());

  }

  public LoginResponse loginUser(LoginRequest loginRequest) throws DataAccessException {
    if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
      throw new DataAccessException("Username and password are required");
    }
    UserData userData=new UserData(loginRequest.getUsername(), loginRequest.getPassword(), null);
    if (!DAO.checkUser(userData) || !DAO.getUser(userData).password().equals(loginRequest.getPassword())) {
      throw new DataAccessException("User is not recognized");
    }
    return new LoginResponse(loginRequest.getUsername(), null);

  }


}