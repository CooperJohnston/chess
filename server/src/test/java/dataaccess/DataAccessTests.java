package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.AuthService;
import service.GameService;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessTests {

  static GameService gameService;
  static UserService userService;
  static AuthService authService;
  static final DatabaseAuthDAO authDAO;
  static final DatabaseGameDAO gameDAO;
  static final DatabaseUserDAO userDAO;

  static {
    try {
      authDAO=new DatabaseAuthDAO();
      gameDAO=new DatabaseGameDAO();
      userDAO=new DatabaseUserDAO();
      gameService=new GameService(gameDAO, authDAO);

    } catch (DataAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeEach
  public void clear() throws DataAccessException {
    authDAO.clear();
    gameDAO.clear();
    userDAO.clear();
  }

  @Test
  public void testCreateAuth_Pass() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    authDAO.createAuthData(authData);
    assertNotNull(authDAO.getAuthData("testAuth"));

  }

  @Test
  public void testCreateAuth_Fail() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    authDAO.createAuthData(authData);
    assertThrows(Exception.class, () -> authDAO.createAuthData(authData));
  }

  @Test
  public void testGetAuthData_Pass() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    authDAO.createAuthData(authData);
    AuthData result=authDAO.getAuthData("testAuth");
    assertNotNull(result);
  }

  @Test
  public void testGetAuthData_Fail() throws DataAccessException {
    assertNull(authDAO.getAuthData("poop"));
  }

  @Test
  public void testCheckAuthData_Pass() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    authDAO.createAuthData(authData);
    assertTrue(authDAO.checkAuthData("testAuth"));
  }

  @Test
  public void testCheckAuthData_Fail() throws DataAccessException {
    assertFalse(authDAO.checkAuthData("testAuth"));
  }

  @Test
  public void testDeleteAuthData_Pass() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    authDAO.createAuthData(authData);
    authDAO.deleteAuthData("testAuth");
    assertNull(authDAO.getAuthData("testAuth"));
  }

  @Test
  public void testDeleteAuthData_Fail() throws DataAccessException {
    assertFalse(authDAO.checkAuthData("poop"));
  }

  @Test
  public void testClearAuthData_Pass() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    authDAO.createAuthData(authData);
    authDAO.clear();
    assertNull(authDAO.getAuthData("testAuth"));
  }

  @Test
  public void testCreateGame_Pass() throws DataAccessException {
    GameData gameData=new GameData(1, null, null,
            "testName", new ChessGame());
    gameDAO.createGame(gameData);
    assertNotNull(gameDAO.getAllGames());
  }

  @Test
  public void testCreateGame_Fail() {
    assertThrows(Exception.class, () -> gameDAO.createGame(null));
  }

  @Test
  public void testGetGame_Pass() throws DataAccessException {
    GameData gameData=new GameData(1, null, null,
            "testName", new ChessGame());
    gameDAO.createGame(gameData);
    assertNotNull(gameDAO.getGame(1));

  }

  @Test
  public void testGetGame_Fail() throws DataAccessException {
    assertNull(gameDAO.getGame(1));
  }

  @Test
  public void testCLearGameData() throws DataAccessException {
    GameData gameData=new GameData(1, null, null,
            "testName", new ChessGame());
    gameDAO.createGame(gameData);
    gameDAO.clear();
    assertNull(gameDAO.getGame(1));

  }

  @Test
  public void CheckGame_Pass() throws DataAccessException {
    GameData gameData=new GameData(1, null, null,
            "testName", new ChessGame());
    gameDAO.createGame(gameData);
    assertTrue(gameDAO.checkGame(1));

  }

  @Test
  public void CheckGame_Fail() throws DataAccessException {
    assertFalse(gameDAO.checkGame(1));
  }

  @Test
  public void UpdateGame_Pass() throws DataAccessException {
    GameData gameData=new GameData(1, null, null,
            "testName", new ChessGame());
    gameDAO.createGame(gameData);
    GameData updateGame=new GameData(1, "testUser",
            null, "testName", gameData.game());
    gameDAO.updateGame(updateGame);
    assertEquals(updateGame.whiteUsername(), gameDAO.getGame(1).whiteUsername());


  }

  @Test
  public void UpdateGame_Fail() throws DataAccessException {
    GameData gameData=new GameData(1, null, null,
            "testName", new ChessGame());
    gameDAO.createGame(gameData);
    GameData updateGame=new GameData(500, "testUser",
            null, "testName", gameData.game());
    assertThrows(Exception.class, () -> gameDAO.updateGame(updateGame));
  }

  @Test
  public void CreateUser_Pass() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    userDAO.insertUser(userData);
    assertNotNull(userDAO.getAllUsers());
  }

  @Test
  public void CreateUser_Fail() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    userDAO.insertUser(userData);
    assertThrows(Exception.class, () -> userDAO.insertUser(userData));
  }

  @Test
  public void CheckUser_Pass() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    userDAO.insertUser(userData);
    assertTrue(userDAO.checkUser(userData));
  }

  @Test
  public void CheckUser_Fail() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    assertFalse(userDAO.checkUser(userData));
  }

  @Test
  public void DeleteUser_Pass() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    userDAO.insertUser(userData);
    userDAO.clear();
    assertFalse(userDAO.checkUser(userData));
  }

  @Test
  public void GetUser_Pass() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    userDAO.insertUser(userData);
    assert userDAO.getUser(userData).equals(userData);
  }

  @Test
  public void GetUser_Fail() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    assertNull(userDAO.getUser(userData));
  }


}
