package model;

/**
 * Represents a simple string message within the network simulation.
 */
public class StringMessage implements Message {
    String body;

    /**
     * Constructs a StringMessage with specified content.
     *
     * @param body The string content of the message.
     */
    public StringMessage(String body) {
        this.body = body;
    }

    /**
     * Default constructor for creating an empty StringMessage.
     */
    public StringMessage() {}

    /**
     * Retrieves the body of the message.
     *
     * @return The string content of the message.
     */
    public String getBody() {
        return body;
    }
}
