package responses;

import model.GameData;

import java.util.ArrayList;

public record ListGameResponse(ArrayList<GameData> games) {

}
