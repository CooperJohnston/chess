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
    if (regRequest.getUsername() == null || regRequest.getUsername().isEmpty() ||
            regRequest.getPassword() == null || regRequest.getPassword().isEmpty() ||
            regRequest.getEmail() == null || regRequest.getEmail().isEmpty()) {
      throw new DataAccessException("Error: bad request");
    }

    UserData userData=new UserData(regRequest.getUsername(), regRequest.getPassword(), regRequest.getEmail());
    if (DAO.checkUser(userData)) {
      throw new DataAccessException("Error: already taken");
    }

    DAO.insertUser(userData);
    return new RegisterResponse(null, regRequest.getUsername());

  }

  public LoginResponse loginUser(LoginRequest loginRequest) throws DataAccessException {
    if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
      throw new DataAccessException("Error: unauthorized");
    }
    UserData userData=new UserData(loginRequest.getUsername(), loginRequest.getPassword(), null);
    if (!DAO.checkUser(userData) || !DAO.getUser(userData).password().equals(loginRequest.getPassword())) {
      throw new DataAccessException("Error: unauthorized");
    }
    return new LoginResponse(loginRequest.getUsername(), null);

  }


}