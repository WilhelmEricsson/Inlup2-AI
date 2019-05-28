public class TankMessage {

    // Tank id
    private int sender;

    private String message;

    public TankMessage(int sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getSender() {
        return sender;
    }

}
