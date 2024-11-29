package websocket.messages;

public class Notification extends ServerMessage {
  public final String message;

  public Notification(ServerMessageType type, String message) {
    super(type);
    this.message=message;
  }

  public String getMesssage() {
    return this.message;
  }

  public String message() {
    return this.message;
  }
}