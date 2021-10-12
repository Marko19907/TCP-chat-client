package no.ntnu.datakomm.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class TCPClient {
    private PrintWriter toServer;
    private BufferedReader fromServer;
    private Socket connection;

    // Hint: if you want to store a message for the last error, store it here
    private String lastError = null;

    private final List<ChatListener> listeners = new LinkedList<>();

    /**
     * Connect to a chat server.
     *
     * @param host host name or IP address of the chat server
     * @param port TCP port of the chat server
     * @return True on success, false otherwise
     */
    public boolean connect(String host, int port) {
        boolean result = false;
        try {
            this.connection = new Socket(host, port);
            this.toServer = new PrintWriter(this.connection.getOutputStream(), true);
            this.fromServer = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
            result = true;
        } catch (IOException e) {
            this.log("Failed to connect: " + e.getMessage());
        }

        return result;
    }

    /**
     * Close the socket. This method must be synchronized, because several
     * threads may try to call it. For example: When "Disconnect" button is
     * pressed in the GUI thread, the connection will get closed. Meanwhile, the
     * background thread trying to read server's response will get error in the
     * input stream and may try to call this method when the socket is already
     * in the process of being closed. with "synchronized" keyword we make sure
     * that no two threads call this method in parallel.
     */
    public synchronized void disconnect() {
        if (this.isConnectionActive()) {
            this.closeConnection();
            this.onDisconnect();
        }
    }

    /**
     * @return true if the connection is active (opened), false if not.
     */
    public boolean isConnectionActive() {
        return this.connection != null;
    }

    /**
     * Send a command to server.
     *
     * @param cmd A command. It should include the command word and optional attributes, according to the protocol.
     * @return true on success, false otherwise
     */
    private boolean sendCommand(String cmd) {
        // Guard conditions
        if (cmd == null || !this.isConnectionActive()) {
            return false;
        }

        this.toServer.println(cmd);

        return true;
    }

    /**
     * Send a public message to all the recipients.
     *
     * @param message Message to send, not null
     * @return true if message sent, false on error
     */
    public boolean sendPublicMessage(String message) {
        // Guard condition
        if (message == null) {
            this.lastError = "Could not send the message";
            return false;
        }
        // TODO: Can a String be blank, not empty?
        return this.sendCommand("msg " + message);
    }

    /**
     * Send a login request to the chat server.
     *
     * @param username Username to use, can't be blank or null
     */
    public void tryLogin(String username) {
        // Guard condition
        if (username == null || username.isBlank()) {
            this.lastError = "Username cant be blank!";
            return;
        }
        this.sendCommand("login " + username);
    }

    /**
     * Send a request for latest user list to the server. To get the new users,
     * clear your current user list and use events in the listener.
     */
    public void refreshUserList() {
        // TODO Step 5: implement this method
        // Hint: Use Wireshark and the provided chat client reference app to find out what commands the
        // client and server exchange for user listing.
        this.sendCommand("users");
    }

    /**
     * Send a private message to a single recipient.
     *
     * @param recipient username of the chat user who should receive the message
     * @param message   Message to send
     * @return true if message sent, false on error
     */
    public boolean sendPrivateMessage(String recipient, String message) {
        if (recipient == null || message == null){
            this.log("Message can't be null");
            return false;
        }
        return this.sendCommand("privmsg " + recipient + " " + message);
    }


    /**
     * Send a request for the list of commands that server supports.
     */
    public void askSupportedCommands() {
        // TODO Step 8: Implement this method
        // Hint: Reuse sendCommand() method
    }


    /**
     * Wait for chat server's response
     *
     * @return one line of text (one command) received from the server
     */
    private String waitServerResponse() {
        // Guard condition
        if (!this.isConnectionActive()) {
            this.log("No server connection");
            return null;
        }

        String serverResponse = null;

        try {
            serverResponse = this.fromServer.readLine();
        } catch (IOException e) {
            this.log("Could not receive message from server" + e.getMessage());
            this.onDisconnect();
        }

        return serverResponse;
    }

    /**
     * Closes the connection to the server
     */
    private void closeConnection() {
        try {
            this.connection.close();
            this.connection = null;
            this.toServer = null;
            this.fromServer = null;
        } catch (IOException e) {
            this.log("Couldn't close the connection: " + e.getMessage());
        }
    }

    /**
     * Get the last error message
     *
     * @return Error message or "" if there has been no error
     */
    public String getLastError() {
        if (this.lastError != null) {
            return this.lastError;
        } else {
            return "";
        }
    }

    /**
     * Start listening for incoming commands from the server in a new CPU thread.
     */
    public void startListenThread() {
        // Call parseIncomingCommands() in the new thread.
        Thread t = new Thread(() -> {
            this.parseIncomingCommands();
        });
        t.start();
    }

    /**
     * Placeholder function for lambda switch case
     */
    private void ignore() {
    }

    /**
     * Check if the message type is private
     * @param messageCommand command from the server response
     * @return true if private, else otherwise
     */
    private boolean isMessagePrivate(String messageCommand) {
        return messageCommand.equals("privmsg");
    }

    /**
     * Read incoming messages one by one, generate events for the listeners. A loop that runs until
     * the connection is closed.
     */
    private void parseIncomingCommands() {
        while (this.isConnectionActive()) {
            // TODO Step 3: Implement this method
            // Hint: Reuse waitServerResponse() method
            // Hint: Have a switch-case (or other way) to check what type of response is received from the server
            // and act on it.
            // Hint: In Step 3 you need to handle only login-related responses.
            // Hint: In Step 3 reuse onLoginResult() method

            String response = this.waitServerResponse();
            if (response != null) {

                final String serverCommand = this.extractFirstWord(response);
                final String serverMessage = this.excludeFirstWord(response);

                switch (serverCommand) {
                    case "loginok" -> this.onLoginResult(true, "");
                    case "loginerr" -> this.onLoginResult(false, serverMessage);
                    case "modeok" -> this.ignore();
                    case "msg" -> this.onMsgReceived(false,
                            this.extractFirstWord(serverMessage),
                            this.excludeFirstWord(serverMessage));
                    case "privmsg" -> this.onMsgReceived(true,
                            this.extractFirstWord(serverMessage),
                            this.excludeFirstWord(serverMessage));
                    case "msgok" -> this.ignore();
                    case "msgerr" -> this.onMsgError(serverMessage);
                    case "inbox" -> this.ignore();
                    case "supported" -> this.ignore();
                    case "cmderr" -> this.ignore();
                    case "users" -> this.onUsersList(this.extractUsers(serverMessage));
                    default -> this.log("Unsupported command: " + serverMessage);
                }
            }

            // TODO Step 5: update this method, handle user-list response from the server
            // Hint: In Step 5 reuse onUserList() method

            // TODO Step 7: add support for incoming chat messages from other users (types: msg, privmsg)
            // TODO Step 7: add support for incoming message errors (type: msgerr)
            // TODO Step 7: add support for incoming command errors (type: cmderr)
            // Hint for Step 7: call corresponding onXXX() methods which will notify all the listeners

            // TODO Step 8: add support for incoming supported command list (type: supported)

        }
    }

    /**
     * Extracts the first word of a String
     * @param text The String to extract the first words from
     * @return The first word
     */
    private String extractFirstWord(String text) {
        return text.split(" ")[0];
    }

    /**
     * Extracts the users String array from a given users String
     * @param serverMessage The String to extract users from
     * @return The extracted users String array
     */
    private String[] extractUsers(String serverMessage) {
        return serverMessage.split(" ");
    }

    /**
     * Extracts the server message from the response
     *
     * @param response response to extract from
     * @return the message in the response
     */
    private String excludeFirstWord(String response) {
        String[] splitString = response.split(" ");
        int length = splitString.length;
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < length; i++){
            builder.append(splitString[i]).append(" ");
        }
        return builder.toString();
    }

    /**
     * Register a new listener for events (login result, incoming message, etc)
     *
     * @param listener
     */
    public void addListener(ChatListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    /**
     * Unregister an event listener
     *
     * @param listener
     */
    public void removeListener(ChatListener listener) {
        this.listeners.remove(listener);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // The following methods are all event-notificators - notify all the listeners about a specific event.
    // By "event" here we mean "information received from the chat server".
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Notify listeners that login operation is complete (either with success or
     * failure)
     *
     * @param success When true, login successful. When false, it failed
     * @param errMsg  Error message if any
     */
    private void onLoginResult(boolean success, String errMsg) {
        this.listeners.forEach(l -> l.onLoginResult(success, errMsg));
    }

    /**
     * Notify listeners that socket was closed by the remote end (server or
     * Internet error)
     */
    private void onDisconnect() {
        this.listeners.forEach(ChatListener::onDisconnect);
    }

    /**
     * Notify listeners that server sent us a list of currently connected users
     *
     * @param users List with usernames
     */
    private void onUsersList(String[] users) {
        this.listeners.forEach(l -> l.onUserList(users));
    }

    /**
     * Notify listeners that a message is received from the server
     *
     * @param priv   When true, this is a private message
     * @param sender Username of the sender
     * @param text   Message text
     */
    private void onMsgReceived(boolean priv, String sender, String text) {
        final TextMessage message = new TextMessage(sender, priv, text);
        this.listeners.forEach(listener -> listener.onMessageReceived(message));
    }

    /**
     * Notify listeners that our message was not delivered
     *
     * @param errMsg Error description returned by the server
     */
    private void onMsgError(final String errMsg) {
        this.listeners.forEach(listener -> listener.onMessageError(errMsg));
    }

    /**
     * Notify listeners that command was not understood by the server.
     *
     * @param errMsg Error message
     */
    private void onCmdError(String errMsg) {
        // TODO Step 7: Implement this method
    }

    /**
     * Notify listeners that a help response (supported commands) was received
     * from the server
     *
     * @param commands Commands supported by the server
     */
    private void onSupported(String[] commands) {
        // TODO Step 8: Implement this method
    }

    /**
     * Prints message to the terminal
     * @param message the message to print
     */
    private void log(String message) {
        System.out.println(message);
    }
}
