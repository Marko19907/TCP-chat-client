package no.ntnu.datakomm.chat;

/**
 * Interface for a listener that can handle events received from the server
 */
public interface ChatListener {

    /**
     * This method is called when connection (socket) is closed by the remote
     * end (server).
     */
    void onDisconnect();

    /**
     * This method is called when a login procedure is done: either it succeeded
     * or failed.
     *
     * @param success when true, the client has logged in, when false, login
     * failed
     * @param errMsg Error message in case of failure, or null on successful
     * login
     */
    void onLoginResult(boolean success, String errMsg);

    /**
     * This method is called when an incoming text message is received
     *
     * @param message message received (private or public)
     */
    void onMessageReceived(TextMessage message);

    /**
     * This method is called when an error happened when we tried to send
     * message to the server (the message was not sent to necessary recipients)
     *
     * @param errMsg Error message
     */
    void onMessageError(String errMsg);

    /**
     * This method is called when a list of currently connected users is
     * received
     *
     * @param usernames List of users
     */
    void onUserList(String[] usernames);

    /**
     * This method is called when a list of currently supported commands is
     * received
     *
     * @param commands List of supported commands
     */
    void onSupportedCommands(String[] commands);

    /**
     * This method is called when the server did not understand the last command
     *
     * @param errMsg Error message
     */
    void onCommandError(String errMsg);

}
