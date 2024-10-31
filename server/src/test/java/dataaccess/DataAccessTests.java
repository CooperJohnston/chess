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
  static final DatabaseAuthDAO AUTHDAO;
  static final DatabaseGameDAO GAMEDAO;
  static final DatabaseUserDAO USERDAO;

  static {
    try {
      AUTHDAO=new DatabaseAuthDAO();
      GAMEDAO=new DatabaseGameDAO();
      USERDAO=new DatabaseUserDAO();
      gameService=new GameService(GAMEDAO, AUTHDAO);

    } catch (DataAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeEach
  public void clear() throws DataAccessException {
    AUTHDAO.clear();
    GAMEDAO.clear();
    USERDAO.clear();
  }

  @Test
  public void createAuthPass() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    AUTHDAO.createAuthData(authData);
    assertNotNull(AUTHDAO.getAuthData("testAuth"));

  }

  @Test
  public void createAuthFail() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    AUTHDAO.createAuthData(authData);
    assertThrows(Exception.class, () -> AUTHDAO.createAuthData(authData));
  }

  @Test
  public void getAuthDataPass() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    AUTHDAO.createAuthData(authData);
    AuthData result=AUTHDAO.getAuthData("testAuth");
    assertNotNull(result);
  }

  @Test
  public void testGetAuthDataFail() throws DataAccessException {
    assertNull(AUTHDAO.getAuthData("poop"));
  }

  @Test
  public void testCheckAuthDataPass() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    AUTHDAO.createAuthData(authData);
    assertTrue(AUTHDAO.checkAuthData("testAuth"));
  }

  @Test
  public void testCheckAuthDataFail() throws DataAccessException {
    assertFalse(AUTHDAO.checkAuthData("testAuth"));
  }

  @Test
  public void testDeleteAuthDataPass() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    AUTHDAO.createAuthData(authData);
    AUTHDAO.deleteAuthData("testAuth");
    assertNull(AUTHDAO.getAuthData("testAuth"));
  }

  @Test
  public void testDeleteAuthDataFail() throws DataAccessException {
    assertFalse(AUTHDAO.checkAuthData("poop"));
  }

  @Test
  public void testClearAuthDataPass() throws DataAccessException {
    AuthData authData=new AuthData("testAuth", "testUser");
    AUTHDAO.createAuthData(authData);
    AUTHDAO.clear();
    assertNull(AUTHDAO.getAuthData("testAuth"));
  }

  @Test
  public void testCreateGamePass() throws DataAccessException {
    GameData gameData=new GameData(1, null, null,
            "testName", new ChessGame());
    GAMEDAO.createGame(gameData);
    assertNotNull(GAMEDAO.getAllGames());
  }

  @Test
  public void testCreateGameFail() {
    assertThrows(Exception.class, () -> GAMEDAO.createGame(null));
  }

  @Test
  public void testGetGamePass() throws DataAccessException {
    GameData gameData=new GameData(1, null, null,
            "testName", new ChessGame());
    GAMEDAO.createGame(gameData);
    assertNotNull(GAMEDAO.getGame(1));

  }

  @Test
  public void testGetGameFail() throws DataAccessException {
    assertNull(GAMEDAO.getGame(1));
  }

  @Test
  public void testCLearGameData() throws DataAccessException {
    GameData gameData=new GameData(1, null, null,
            "testName", new ChessGame());
    GAMEDAO.createGame(gameData);
    GAMEDAO.clear();
    assertNull(GAMEDAO.getGame(1));

  }

  @Test
  public void testCheckGamePass() throws DataAccessException {
    GameData gameData=new GameData(1, null, null,
            "testName", new ChessGame());
    GAMEDAO.createGame(gameData);
    assertTrue(GAMEDAO.checkGame(1));

  }

  @Test
  public void testCheckGameFail() throws DataAccessException {
    assertFalse(GAMEDAO.checkGame(1));
  }

  @Test
  public void testUpdateGamePass() throws DataAccessException {
    GameData gameData=new GameData(1, null, null,
            "testName", new ChessGame());
    GAMEDAO.createGame(gameData);
    GameData updateGame=new GameData(1, "testUser",
            null, "testName", gameData.game());
    GAMEDAO.updateGame(updateGame);
    assertEquals(updateGame.whiteUsername(), GAMEDAO.getGame(1).whiteUsername());


  }

  @Test
  public void testUpdateGameFail() throws DataAccessException {
    GameData gameData=new GameData(1, null, null,
            "testName", new ChessGame());
    GAMEDAO.createGame(gameData);
    GameData updateGame=new GameData(500, "testUser",
            null, "testName", gameData.game());
    assertThrows(Exception.class, () -> GAMEDAO.updateGame(updateGame));
  }

  @Test
  public void testCreateUserPass() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    USERDAO.insertUser(userData);
    assertNotNull(USERDAO.getAllUsers());
  }

  @Test
  public void testCreateUserFail() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    USERDAO.insertUser(userData);
    assertThrows(Exception.class, () -> USERDAO.insertUser(userData));
  }

  @Test
  public void testCheckUserPass() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    USERDAO.insertUser(userData);
    assertTrue(USERDAO.checkUser(userData));
  }

  @Test
  public void testCheckUserFail() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    assertFalse(USERDAO.checkUser(userData));
  }

  @Test
  public void testDeleteUserPass() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    USERDAO.insertUser(userData);
    USERDAO.clear();
    assertFalse(USERDAO.checkUser(userData));
  }

  @Test
  public void testGetUserPass() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    USERDAO.insertUser(userData);
    UserData userData1=USERDAO.getUser(userData);
    assert BCrypt.checkpw(userData.password(), userData1.password());
  }

  @Test
  public void testGetUserFail() throws DataAccessException {
    UserData userData=new UserData("testUser", "testPass", "testEmail");
    assertNull(USERDAO.getUser(userData));
  }


}
