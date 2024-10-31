package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
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
  public void createAuthPass() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    authDAO.createAuthData(authData);
    assertNotNull(authDAO.getAuthData("testAuth"));

  }

  @Test
  public void createAuthFail() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    authDAO.createAuthData(authData);
    assertThrows(Exception.class, () -> authDAO.createAuthData(authData));
  }

  @Test
  public void getAuthDataPass() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    authDAO.createAuthData(authData);
    AuthData result=authDAO.getAuthData("testAuth");
    assertNotNull(result);
  }

  @Test
  public void testGetAuthDataFail() throws DataAccessException {
    assertNull(authDAO.getAuthData("poop"));
  }

  @Test
  public void testCheckAuthDataPass() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    authDAO.createAuthData(authData);
    assertTrue(authDAO.checkAuthData("testAuth"));
  }

  @Test
  public void testCheckAuthDataFail() throws DataAccessException {
    assertFalse(authDAO.checkAuthData("testAuth"));
  }

  @Test
  public void testDeleteAuthDataPass() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    authDAO.createAuthData(authData);
    authDAO.deleteAuthData("testAuth");
    assertNull(authDAO.getAuthData("testAuth"));
  }

  @Test
  public void testDeleteAuthDataFail() throws DataAccessException {
    assertFalse(authDAO.checkAuthData("poop"));
  }

  @Test
  public void testClearAuthDataPass() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    authDAO.createAuthData(authData);
    authDAO.clear();
    assertNull(authDAO.getAuthData("testAuth"));
  }

  @Test
  public void testCreateGamePass() throws DataAccessException {
    GameData gameData=new GameData(1, null, null,
            "testName", new ChessGame());
    gameDAO.createGame(gameData);
    assertNotNull(gameDAO.getAllGames());
  }

  @Test
  public void testCreateGameFail() {
    assertThrows(Exception.class, () -> gameDAO.createGame(null));
  }

  @Test
  public void testGetGamePass() throws DataAccessException {
    GameData gameData=new GameData(1, null, null,
            "testName", new ChessGame());
    gameDAO.createGame(gameData);
    assertNotNull(gameDAO.getGame(1));

  }

  @Test
  public void testGetGameFail() throws DataAccessException {
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
  public void testCheckGamePass() throws DataAccessException {
    GameData gameData=new GameData(1, null, null,
            "testName", new ChessGame());
    gameDAO.createGame(gameData);
    assertTrue(gameDAO.checkGame(1));

  }

  @Test
  public void testCheckGameFail() throws DataAccessException {
    assertFalse(gameDAO.checkGame(1));
  }

  @Test
  public void testUpdateGamePass() throws DataAccessException {
    GameData gameData=new GameData(1, null, null,
            "testName", new ChessGame());
    gameDAO.createGame(gameData);
    GameData updateGame=new GameData(1, "testUser",
            null, "testName", gameData.game());
    gameDAO.updateGame(updateGame);
    assertEquals(updateGame.whiteUsername(), gameDAO.getGame(1).whiteUsername());


  }

  @Test
  public void testUpdateGameFail() throws DataAccessException {
    GameData gameData=new GameData(1, null, null,
            "testName", new ChessGame());
    gameDAO.createGame(gameData);
    GameData updateGame=new GameData(500, "testUser",
            null, "testName", gameData.game());
    assertThrows(Exception.class, () -> gameDAO.updateGame(updateGame));
  }

  @Test
  public void testCreateUserPass() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    userDAO.insertUser(userData);
    assertNotNull(userDAO.getAllUsers());
  }

  @Test
  public void testCreateUserFail() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    userDAO.insertUser(userData);
    assertThrows(Exception.class, () -> userDAO.insertUser(userData));
  }

  @Test
  public void testCheckUserPass() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    userDAO.insertUser(userData);
    assertTrue(userDAO.checkUser(userData));
  }

  @Test
  public void testCheckUserFail() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    assertFalse(userDAO.checkUser(userData));
  }

  @Test
  public void testDeleteUserPass() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    userDAO.insertUser(userData);
    userDAO.clear();
    assertFalse(userDAO.checkUser(userData));
  }

  @Test
  public void testGetUserPass() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    userDAO.insertUser(userData);
    UserData userData1=userDAO.getUser(userData);
    assert BCrypt.checkpw(userData.password(), userData1.password());
  }

  @Test
  public void testGetUserFail() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    assertNull(userDAO.getUser(userData));
  }


}
