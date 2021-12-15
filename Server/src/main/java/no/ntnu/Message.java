package no.ntnu;

/**
 * A message sent and received over the socket
 */
public class Message {
    private final String command;
    private final String arguments;

    public Message(String command, String arguments) {
        this.command = command;
        this.arguments = arguments;
    }

    /**
     * Create message from one line of text received on the socket
     *
     * @param input The received text input
     * @return An interpreted message or null on error
     */
    public static Message createFromInput(String input) {
        Message message = null;
        if (input != null) {
            String[] parts = input.split(" ", 2);
            if (parts.length >= 1) {
                String command = parts[0];
                String arguments = null;
                if (parts.length == 2) {
                    arguments = parts[1];
                }
                message = new Message(command, arguments);
            }
        }
        return message;
    }

    /**
     * Convert the message to a string - the form that can be sent over the socket
     *
     * @return
     */
    @Override
    public String toString() {
        if (this.arguments != null) {
            return this.command + " " + this.arguments;
        } else {
            return this.command;
        }
    }

    /**
     * Get all the message arguments as a single string
     *
     * @return
     */
    public String getArguments() {
        return this.arguments;
    }

    /**
     * Get the command word of the message
     *
     * @return
     */
    public String getCommand() {
        return this.command;
    }
}
