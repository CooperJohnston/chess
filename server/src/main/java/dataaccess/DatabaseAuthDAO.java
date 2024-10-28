package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseAuthDAO implements AuthDAO {

  public DatabaseAuthDAO() throws DataAccessException {
    DatabaseManager.fillTables();
  }

  @Override
  public void createAuthData(AuthData authData) throws DataAccessException {
    try (Connection connection=DatabaseManager.getConnection()) {
      String query="INSERT INTO AuthData (authToken, username) VALUES (?, ?)";
      try (PreparedStatement preparedStatement=connection.prepareStatement(query)) {
        preparedStatement.setString(1, authData.authToken());
        preparedStatement.setString(2, authData.username());
        preparedStatement.executeUpdate();
      }
    } catch (SQLException | DataAccessException e) {
      e.printStackTrace();
      throw new DataAccessException("Error: SQL");
    }

  }

  @Override
  public AuthData getAuthData(String authToken) throws DataAccessException {
    try (Connection connection=DatabaseManager.getConnection()) {
      String query="SELECT * FROM AuthData WHERE authToken = ?";
      try (PreparedStatement preparedStatement=connection.prepareStatement(query)) {
        preparedStatement.setString(1, authToken);
        ResultSet resultSet=preparedStatement.executeQuery();
        if (resultSet.next()) {
          return new AuthData(resultSet.getString("authToken"), resultSet.getString("username"));
        }
      }
    } catch (SQLException | DataAccessException e) {
      throw new DataAccessException("Error: SQL");
    }
    return null;
  }

  @Override
  public boolean checkAuthData(String authToken) throws DataAccessException {
    try (Connection connection=DatabaseManager.getConnection()) {
      String query="SELECT COUNT(*) AS count FROM AuthData WHERE authToken = ?";
      try (PreparedStatement preparedStatement=connection.prepareStatement(query)) {
        preparedStatement.setString(1, authToken);
        ResultSet resultSet=preparedStatement.executeQuery();
        if (resultSet.next()) {
          return resultSet.getInt("count") > 0;
        }
      }
    } catch (SQLException | DataAccessException e) {
      throw new DataAccessException("Error: SQL");
    }
    return false;
  }

  @Override
  public void deleteAuthData(String autToken) throws DataAccessException {
    if (!checkAuthData(autToken)) {
      throw new DataAccessException("Error: unauthorized access");
    }
    try (Connection connection=DatabaseManager.getConnection()) {
      String query="DELETE FROM AuthData WHERE authToken = ?";
      try (PreparedStatement preparedStatement=connection.prepareStatement(query)) {
        preparedStatement.setString(1, autToken);
        preparedStatement.executeUpdate();
      }
    } catch (SQLException | DataAccessException e) {
      throw new DataAccessException("Error: SQL");
    }
  }

  @Override
  public void clear() throws DataAccessException {
    try (Connection connection=DatabaseManager.getConnection()) {
      String query="DELETE FROM AuthData";
      try (PreparedStatement preparedStatement=connection.prepareStatement(query)) {
        preparedStatement.executeUpdate();

      }
    } catch (SQLException | DataAccessException e) {
      throw new DataAccessException("Error: SQL");
    }
  }

  @Override
  public ArrayList<AuthData> getAllAuthData() throws DataAccessException {
    ArrayList<AuthData> authDataList=new ArrayList<>();
    try (Connection connection=DatabaseManager.getConnection()) {
      String query="SELECT * FROM AuthData";
      try (PreparedStatement preparedStatement=connection.prepareStatement(query)) {
        ResultSet resultSet=preparedStatement.executeQuery();
        while (resultSet.next()) {
          authDataList.add(new AuthData(resultSet.getString("authToken"), resultSet.getString("username")));

        }

      }

    } catch (DataAccessException | SQLException e) {
      throw new DataAccessException("Error: SQL");
    }
    return authDataList;
  }
}
