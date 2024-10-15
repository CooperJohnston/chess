package service;

import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

import java.util.ArrayList;

public class GameService {

  private final GameDAO gameDAO;

  public GameService(GameDAO gameDAO) {
    this.gameDAO=gameDAO;
  }

  public void clear() throws DataAccessException {
    this.gameDAO.clear();
  }

}
