package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.LoginRequest;
import requests.RegisterRequest;
import responses.CreateGameResponse;
import responses.JoinGameResponse;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
  private GameService gameService;
  private UserService userService;
  private AuthService authService;
  private MemoryUserDAO memoryUserDAO;
  private MemoryGameDAO memoryGameDAO;
  private MemoryAuthDAO memoryAuthDAO;
  private UserData userData=new UserData("TestUser", "TestPass", "TestEmail");
  private GameData gameData=new GameData(1, "TestWhite", "TestBlack", "TestGameName", new ChessGame());
  private AuthData authData=new AuthData("TestAuth", "TestUser");
  private DatabaseGameDAO databaseGameDAO;
  private DatabaseAuthDAO databaseAuthDAO;
  private DatabaseUserDAO databaseUserDAO;


  @BeforeEach
  public void setUp() {
    memoryUserDAO=new MemoryUserDAO();
    memoryGameDAO=new MemoryGameDAO();
    memoryAuthDAO=new MemoryAuthDAO();
    gameService=new GameService(memoryGameDAO, memoryAuthDAO);
    userService=new UserService(memoryUserDAO);
    authService=new AuthService(memoryAuthDAO);
  }

  @Test
  @DisplayName("Service Clear")
  public void testClear() {

    memoryAuthDAO.createAuthData(authData);
    memoryGameDAO.createGame(gameData);
    memoryUserDAO.insertUser(userData);

    memoryUserDAO.clear();
    memoryGameDAO.clear();
    memoryAuthDAO.clear();

    assert memoryAuthDAO.getAllAuthData().isEmpty();
    assert memoryUserDAO.getAllUsers().isEmpty();
    assert memoryGameDAO.getAllGames().isEmpty();

  }

  @Test
  @DisplayName("RegisterPass")
  public void testRegisterPass() throws DataAccessException {
    RegisterRequest registerRequest=new RegisterRequest(userData.username(), userData.password(), userData.email());
    assert userService.registerUser(registerRequest).getUsername().equals(userData.username());
  }

  @Test
  @DisplayName("RegisterFail")
  public void testRegisterFail() throws DataAccessException {
    RegisterRequest registerRequest=new RegisterRequest("", "", "");
    assertThrows(Exception.class, () -> userService.registerUser(registerRequest));
  }

  @Test
  @DisplayName("LoginPass")
  public void testLoginPass() throws DataAccessException {

    RegisterRequest registerRequest=new RegisterRequest(userData.username(), userData.password(), userData.username());
    LoginRequest loginRequest=new LoginRequest(userData.username(), userData.password());
    assert (loginRequest).getUsername().equals(userData.username());
  }

  @Test
  @DisplayName("LoginFail")
  public void testLoginFail() throws DataAccessException {
    RegisterRequest registerRequest=new RegisterRequest(userData.username(), userData.password(), userData.username());
    userService.registerUser(registerRequest);
    LoginRequest loginRequest=new LoginRequest(userData.username(), "WrongPassword");
    assertThrows(Exception.class, () -> userService.loginUser(loginRequest));

  }

  @Test
  @DisplayName("TestMakeAuthPass")
  public void testAuthPass() throws DataAccessException {
    RegisterRequest registerRequest=new RegisterRequest(userData.username(), userData.password(), userData.username());
    userService.registerUser(registerRequest);
    String auth=authService.makeAuth(registerRequest.username());
    assert authService.authenticate(auth);


  }

  @Test
  @DisplayName("TestMakeAuthFail")
  public void testAuthFail() throws DataAccessException {
    RegisterRequest registerRequest=new RegisterRequest(null, userData.password(), userData.username());
    assertThrows(Exception.class, () -> authService.makeAuth(registerRequest.username()));
  }


  @Test
  @DisplayName("AuthenticateFail")
  public void testAuthenticateFail() throws DataAccessException {
    RegisterRequest registerRequest=new RegisterRequest(userData.username(), userData.password(), userData.username());
    userService.registerUser(registerRequest);
    assertThrows(DataAccessException.class, () -> authService.authenticate(userData.username()));
  }

  @Test
  @DisplayName("LogoutPass")
  public void testLogoutPass() throws DataAccessException {
    RegisterRequest registerRequest=new RegisterRequest(userData.username(), userData.password(), userData.username());
    userService.registerUser(registerRequest);
    String authToken=authService.makeAuth(registerRequest.username());
    authService.logout(authToken);
    assertThrows(Exception.class, () -> authService.authenticate(authToken));

  }

  @Test
  @DisplayName("LogoutFail")
  public void testLogoutFail() throws DataAccessException {
    RegisterRequest registerRequest=new RegisterRequest(userData.username(), userData.password(), userData.username());
    userService.registerUser(registerRequest);
    assertThrows(Exception.class, () -> authService.logout(""));

  }


  @Test
  @DisplayName("CreateGamePass")
  public void testCreateGamePass() throws DataAccessException {
    CreateGameRequest createGameRequest=new CreateGameRequest(userData.username(), null);
    CreateGameResponse createGameResponse=gameService.createGame(createGameRequest);
    assert createGameResponse.getGameID() == gameService.generateGameID(createGameRequest.getGameName());
  }

  @Test
  @DisplayName("CreateGameFail")
  public void testCreateGameFail() throws DataAccessException {
    CreateGameRequest createGameRequest=new CreateGameRequest(null, null);
    assertThrows(Exception.class, () -> gameService.createGame(createGameRequest));
  }

  @Test
  @DisplayName("JoinGamePass")
  public void testJoinGamePass() throws DataAccessException {
    RegisterRequest registerRequest=new RegisterRequest(userData.username(), userData.password(), userData.username());
    userService.registerUser(registerRequest);
    String auth=authService.makeAuth(registerRequest.username());
    CreateGameRequest createGameRequest=new CreateGameRequest(gameData.gameName(), auth);
    CreateGameResponse createGameResponse=gameService.createGame(createGameRequest);
    JoinGameRequest joinGameRequest=new JoinGameRequest(ChessGame.TeamColor.WHITE, createGameResponse.getGameID());
    joinGameRequest.setAuth(auth);
    JoinGameResponse joinGameResponse=gameService.joinGame(joinGameRequest);
    assert joinGameResponse.getGameID() == createGameResponse.getGameID();


  }


  @Test
  @DisplayName("JoinGameFail")
  public void testJoinGameFail() throws DataAccessException {
    CreateGameRequest createGameRequest=new CreateGameRequest(userData.username(), null);
    CreateGameResponse createGameResponse=gameService.createGame(createGameRequest);
    JoinGameRequest joinGameRequest=new JoinGameRequest(ChessGame.TeamColor.WHITE, createGameResponse.getGameID());
    assertThrows(Exception.class, () -> gameService.joinGame(joinGameRequest));

  }

  @Test
  @DisplayName("ListAllGamesPass")
  public void testListAllGamesPass() throws DataAccessException {
    CreateGameRequest createGameRequest=new CreateGameRequest(userData.username(), null);
    CreateGameResponse createGameResponse=gameService.createGame(createGameRequest);
    assertNotNull(gameService.getAllGames());

  }

  @Test
  @DisplayName("TestGenerateIDPass")
  public void testGenerateIDPass() throws DataAccessException {
    assert gameService.generateGameID(gameData.gameName()) > 0;
  }

  @Test
  @DisplayName("GenerateIDFail")
  public void testGenerateIDFail() throws DataAccessException {
    assertThrows(Exception.class, () -> gameService.generateGameID(null));
  }


}
