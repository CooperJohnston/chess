package service;

import chess.ChessGame;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

  @Test
  @DisplayName("Service Clear")
  public void testClear() {
    memoryAuthDAO=new MemoryAuthDAO();
    memoryUserDAO=new MemoryUserDAO();
    memoryGameDAO=new MemoryGameDAO();

    //check setup of methods
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
}
