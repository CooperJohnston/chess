package ui;

import chess.ChessGame;
import com.google.gson.Gson;
import exception.ResponseException;
import model.GameData;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.LoginRequest;
import requests.RegisterRequest;
import responses.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class ServerFacade {
  private final String url;
  static String authToken=null;

  public ServerFacade(String url) {
    this.url=url;
  }

  private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
    try {
      URL url=(new URI(this.url + path)).toURL();
      HttpURLConnection http=(HttpURLConnection) url.openConnection();
      http.setRequestMethod(method);
      if (authToken != null) {
        http.setRequestProperty("Authorization", authToken);
      }
      if ("POST".equals(method) || "PUT".equals(method)) {
        http.setRequestProperty("Content-Type", "application/json");
        http.setDoOutput(true);
        writeBody(request, http);
      }


      throwIfNotSuccessful(http);
      return readBody(http, responseClass);
    } catch (Exception ex) {
      throw new ResponseException(500, ex.getMessage());
    }
  }


  private static void writeBody(Object request, HttpURLConnection http) throws IOException {
    if (request != null) {
      http.addRequestProperty("Content-Type", "application/json");
      String reqData=new Gson().toJson(request);
      try (OutputStream reqBody=http.getOutputStream()) {
        reqBody.write(reqData.getBytes());
      }
    }

  }

  public void register(String username, String password, String email) throws ResponseException {
    RegisterResponse registerResponse=this.makeRequest("POST", "/user",
            new RegisterRequest(username, password, email), RegisterResponse.class);
    authToken=registerResponse.getAuthToken();
  }

  public void logout() throws ResponseException {
    this.makeRequest("DELETE", "/session", null, null);
    authToken=null;
  }

  public void login(String username, String password) throws ResponseException {
    LoginResponse loginResponse=this.makeRequest("POST", "/session",
            new LoginRequest(username, password), LoginResponse.class);
    authToken=loginResponse.getAuth();
  }

  public void create(String name) throws ResponseException {
    this.makeRequest("POST", "/game",
            new CreateGameRequest(name, authToken), CreateGameResponse.class);
  }

  public ArrayList<GameData> list() throws ResponseException {
    return this.makeRequest("GET", "/game", null,
            ListGameResponse.class).games();

  }

  public void join(int gameID, ChessGame.TeamColor playerColor) throws ResponseException {
    this.makeRequest("PUT", "/game",
            new JoinGameRequest(playerColor, gameID), JoinGameResponse.class);
  }

  public void observe(int gameID) throws ResponseException {
    JoinGameRequest joinGameRequest=new JoinGameRequest(null, gameID);
    joinGameRequest.setObserve(true);
    this.makeRequest("PUT", "/game",
            joinGameRequest, JoinGameResponse.class);
  }

  public void clear() throws ResponseException {
    this.makeRequest("DELETE", "/db", null, null);
  }


  private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
    var status=http.getResponseCode();
    if (!isSuccessful(status)) {
      throw new ResponseException(status, "failure: " + status);
    }
  }

  private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
    T response=null;
    if (http.getContentLength() < 0) {
      try (InputStream respBody=http.getInputStream()) {
        InputStreamReader reader=new InputStreamReader(respBody);
        if (responseClass != null) {
          response=new Gson().fromJson(reader, responseClass);
        }
      }
    }
    return response;
  }


  private boolean isSuccessful(int status) {
    return status / 100 == 2;
  }

  public String getAuthToken() {
    return authToken;
  }
}

