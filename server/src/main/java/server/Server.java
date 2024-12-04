package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.*;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.LoginRequest;
import requests.RegisterRequest;
import responses.*;
import org.eclipse.jetty.websocket.api.Session;
import server.websocket.WebSocketHandler;
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
  private final WebSocketHandler webSocketHandler;


  public Server() {
    try {
      authDAO=new DatabaseAuthDAO();
      gameDAO=new DatabaseGameDAO();
      userDAO=new DatabaseUserDAO();
      userService=new UserService(userDAO);
      authService=new AuthService(authDAO);
      gameService=new GameService(gameDAO, authDAO);
      webSocketHandler=new WebSocketHandler(authService, gameService, userService);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }

  }

  public int run(int desiredPort) {
    Spark.port(desiredPort);

    Spark.staticFiles.location("web");

    Spark.webSocket("/ws", webSocketHandler);

    // Register your endpoints and handle exceptions here.
    Spark.delete("/db", this::clear);
    Spark.post("/user", this::register);
    Spark.post("/session", this::login);
    Spark.delete("/session", this::logout);
    Spark.post("/game", this::createGame);
    Spark.get("/game", this::listGames);
    Spark.put("/game", this::joinGame);


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

  private Object logout(Request req, Response res) throws DataAccessException {
    try {
      authService.logout(req.headers("Authorization"));
      return "{}";
    } catch (Exception e) {
      if (e.getMessage().equals("Error: unauthorized")) {
        res.status(401);
        JsonObject error=new JsonObject();
        error.addProperty("error", "Logout");
        error.addProperty("message", e.getMessage());
        return new Gson().toJson(error);
      } else {
        res.status(500);
        JsonObject error=new JsonObject();
        error.addProperty("error", "Logout");
        error.addProperty("message", e.getMessage());
        return new Gson().toJson(error);

      }
    }
  }


  private Object createGame(Request req, Response res) throws DataAccessException {
    try {
      String auth=req.headers("Authorization");
      var createGameReq=new Gson().fromJson(req.body(), CreateGameRequest.class);
      CreateGameResponse createGameResp=gameService.createGame(createGameReq);
      authService.authenticate(auth);
      return new Gson().toJson(createGameResp);

    } catch (DataAccessException e) {

      if (e.getMessage().equals("Error: bad request")) {
        res.status(400);
        JsonObject error=new JsonObject();
        error.addProperty("error", "CreateGame Bad Request");
        error.addProperty("message", e.getMessage());
        return new Gson().toJson(error);
      } else if (e.getMessage().equals("Error: unauthorized")) {
        res.status(401);
        JsonObject error=new JsonObject();
        error.addProperty("error", "CreateGame");
        error.addProperty("message", e.getMessage());
        return new Gson().toJson(error);

      } else {
        res.status(500);
        JsonObject error=new JsonObject();
        error.addProperty("error", "CreateGame");
        error.addProperty("message", e.getMessage());
        return new Gson().toJson(error);

      }
    }
  }

  private Object listGames(Request req, Response res) throws DataAccessException {
    try {
      String auth=req.headers("Authorization");
      ListGameResponse listGameResponse=new ListGameResponse(gameService.getAllGames());
      authService.authenticate(auth);
      return new Gson().toJson(listGameResponse);

    } catch (DataAccessException e) {
      if (e.getMessage().equals("Error: unauthorized")) {
        res.status(401);
        JsonObject error=new JsonObject();
        error.addProperty("error", "ListGames");
        error.addProperty("message", e.getMessage());
        return new Gson().toJson(error);
      } else {
        res.status(500);
        JsonObject error=new JsonObject();
        error.addProperty("error", "ListGames");
        error.addProperty("message", e.getMessage());
        return new Gson().toJson(error);
      }
    }
  }

  private Object joinGame(Request req, Response res) throws DataAccessException {
    try {
      String auth=req.headers("Authorization");
      var joinGameReq=new Gson().fromJson(req.body(), JoinGameRequest.class);
      joinGameReq.setAuth(auth);
      authService.authenticate(auth);
      JoinGameResponse joinGameResp=gameService.joinGame(joinGameReq);
      return new Gson().toJson(joinGameResp);
    } catch (DataAccessException e) {
      if (e.getMessage().equals("Error: unauthorized")) {
        res.status(401);
        JsonObject error=new JsonObject();
        error.addProperty("error", "JoinGame");
        error.addProperty("message", e.getMessage());
        return new Gson().toJson(error);
      } else if (e.getMessage().equals("Error: already taken")) {
        res.status(403);
        JsonObject error=new JsonObject();
        error.addProperty("error", "JoinGame");
        error.addProperty("message", e.getMessage());
        return new Gson().toJson(error);
      } else if (e.getMessage().equals("Error: bad request")) {
        res.status(400);
        JsonObject error=new JsonObject();
        error.addProperty("error", "JoinGame");
        error.addProperty("message", e.getMessage());
        return new Gson().toJson(error);
      } else {
        res.status(500);
        JsonObject error=new JsonObject();
        error.addProperty("error", "JoinGame");
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
