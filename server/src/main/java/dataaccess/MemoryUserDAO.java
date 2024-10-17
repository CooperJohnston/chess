package dataaccess;

import model.UserData;

import java.util.ArrayList;

public class MemoryUserDAO implements UserDAO {

  public ArrayList<UserData> userDataList;

  public MemoryUserDAO() {
    userDataList=new ArrayList<>();
  }

  @Override
  public void clear() {
    userDataList.clear();
  }

  @Override
  public void insertUser(UserData user) {
    userDataList.add(user);
  }

  @Override
  public boolean checkUser(UserData user) throws DataAccessException {
    for (UserData userData : userDataList) {
      if (userData.equals(user)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public UserData getUser(UserData user) throws DataAccessException {
    if (userDataList.contains(user)) {
      return user;
    }
    throw new DataAccessException("User not found");
  }

  @Override
  public ArrayList<UserData> getAllUsers() {
    return userDataList;
  }
}