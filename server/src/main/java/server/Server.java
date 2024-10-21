package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.*;
import requests.LoginRequest;
import requests.RegisterRequest;
import responses.LoginResponse;
import responses.RegisterResponse;
import service.AuthService;
import service.GameService;
import service.UserService;
import spark.*;

public class Server {
  private final UserService userService;
  UserDAO userDAO;
  private final AuthService authService;
  AuthDAO authDAO;
  private final GameService gameService;
  GameDAO gameDAO;

  public Server() {
    authDAO=new MemoryAuthDAO();
    gameDAO=new MemoryGameDAO();
    userDAO=new MemoryUserDAO();
    userService=new UserService(userDAO);
    authService=new AuthService(authDAO);
    gameService=new GameService(gameDAO, authDAO);

  }

  public int run(int desiredPort) {
    Spark.port(desiredPort);

    Spark.staticFiles.location("web");

    // Register your endpoints and handle exceptions here.
    Spark.delete("/db", this::clear);
    Spark.post("/user", this::register);
    Spark.post("/session", this::login);

    //This line initializes the server and can be removed once you have a functioning endpoint
    Spark.init();

    Spark.awaitInitialization();
    return Spark.port();
  }

  private Object clear(Request req, Response res) throws DataAccessException {
    userService.clear();
    authService.clear();
    gameService.clear();
    return "{}";
  }

  private Object register(Request req, Response res) throws DataAccessException {
    try {
      var regReq=new Gson().fromJson(req.body(), RegisterRequest.class);
      RegisterResponse regResp=userService.registerUser(regReq);
      regResp.setAuth(authService.makeAuth(regReq));

      return new Gson().toJson(regResp);
    } catch (DataAccessException e) {
      if (e.getMessage().equals("Error: bad request")) {
        res.status(400);
        JsonObject error=new JsonObject();
        error.addProperty("error", "Register Bad Request");
        error.addProperty("message", e.getMessage());
        return new Gson().toJson(error);
      } else if (e.getMessage().equals("Error: already taken")) {
        res.status(403);
        JsonObject error=new JsonObject();
        error.addProperty("error", "Register");
        error.addProperty("message", e.getMessage());
        return new Gson().toJson(error);
      } else {
        res.status(500);
        JsonObject error=new JsonObject();
        error.addProperty("error", "Register");
        error.addProperty("message", e.getMessage());
        return new Gson().toJson(error);

      }
    }
  }

  private Object login(Request req, Response res) throws DataAccessException {
    try {
      var loginReq=new Gson().fromJson(req.body(), LoginRequest.class);
      LoginResponse loginResp=userService.loginUser(loginReq);
      loginResp.setAuthToken(authService.makeAuth(loginReq));
      return new Gson().toJson(loginResp);
    } catch (DataAccessException e) {
      if (e.getMessage().equals("Error: unauthorized")) {
        res.status(401);
        JsonObject error=new JsonObject();
        error.addProperty("error", "Login");
        error.addProperty("message", e.getMessage());
        return new Gson().toJson(error);
      } else {
        res.status(500);
        JsonObject error=new JsonObject();
        error.addProperty("error", "Login");
        error.addProperty("message", e.getMessage());
        return new Gson().toJson(error);
      }
    }
  }

  public void stop() {
    Spark.stop();
    Spark.awaitStop();
  }
}
