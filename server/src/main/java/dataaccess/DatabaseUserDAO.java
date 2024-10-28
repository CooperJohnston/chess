package dataaccess;

import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseUserDAO implements UserDAO {


  public DatabaseUserDAO() throws DataAccessException {
    DatabaseManager.fillTables();
  }

  @Override
  public void insertUser(UserData user) throws DataAccessException {
    try (Connection connection=DatabaseManager.getConnection()) {
      String query="INSERT INTO UserData (username, password, email) VALUES (?, ?, ?)";
      try (PreparedStatement preparedStatement=connection.prepareStatement(query)) {
        preparedStatement.setString(1, user.username());
        preparedStatement.setString(2, user.password());
        preparedStatement.setString(3, user.email());
        preparedStatement.executeUpdate();
      }
    } catch (SQLException | DataAccessException e) {
      throw new DataAccessException(e.getMessage());
    }
  }

  @Override
  public UserData getUser(UserData user) throws DataAccessException {
    try (Connection connection=DatabaseManager.getConnection()) {
      String query="SELECT * FROM UserData WHERE username = ?";
      try (PreparedStatement preparedStatement=connection.prepareStatement(query)) {
        preparedStatement.setString(1, user.username());
        ResultSet resultSet=preparedStatement.executeQuery();
        if (resultSet.next()) {
          return new UserData(resultSet.getString("username"), resultSet.getString("password"),
                  resultSet.getString("email"));
        }
      }
    } catch (SQLException | DataAccessException e) {
      throw new DataAccessException(e.getMessage());
    }
    return null;
  }

  @Override
  public boolean checkUser(UserData user) throws DataAccessException {
    try (Connection connection=DatabaseManager.getConnection()) {
      String query="SELECT COUNT(*) FROM UserData WHERE username = ?";
      try (PreparedStatement preparedStatement=connection.prepareStatement(query)) {
        preparedStatement.setString(1, user.username());
        ResultSet resultSet=preparedStatement.executeQuery();
        if (resultSet.next()) {
          return resultSet.getInt("count") > 0;
        }
      }
    } catch (SQLException | DataAccessException e) {
      throw new DataAccessException(e.getMessage());
    }
    return false;
  }

  @Override
  public ArrayList<UserData> getAllUsers() throws DataAccessException {
    ArrayList<UserData> users=new ArrayList<>();
    try (Connection connection=DatabaseManager.getConnection()) {
      String query="SELECT * FROM UserData";
      try (PreparedStatement preparedStatement=connection.prepareStatement(query)) {
        ResultSet resultSet=preparedStatement.executeQuery();
        while (resultSet.next()) {
          UserData curr=new UserData(resultSet.getString("username"),
                  resultSet.getString("password"),
                  resultSet.getString("email"));
          users.add(curr);
        }
      }
    } catch (SQLException | DataAccessException e) {
      throw new DataAccessException(e.getMessage());
    }
    return users;
  }

  @Override
  public void clear() throws DataAccessException {
    try (Connection connection=DatabaseManager.getConnection()) {
      String query="DELETE FROM UserData";
      try (PreparedStatement preparedStatement=connection.prepareStatement(query)) {
        preparedStatement.executeUpdate();

      }
    } catch (SQLException | DataAccessException e) {
      throw new DataAccessException(e.getMessage());
    }

  }
}
