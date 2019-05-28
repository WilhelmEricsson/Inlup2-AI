import processing.core.PVector;

public class TankMessage {

    // Tank id
    private int sender;

    // Kanske ändra till int sen istället
    private String message;
    private PVector position;

    public TankMessage(int sender, String message, PVector position) {
        this.sender = sender;
        this.message = message;
        this.position = position;
    }

    public String getMessage() {
        return message;
    }

    public PVector getPosition() {
        return position;
    }

    public int getSender() {
        return sender;
    }

}
