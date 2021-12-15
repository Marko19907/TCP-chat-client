package no.ntnu;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Main class for the TCP chat server. Accepts new client connections, hands each new client over to a
 * separate ClientHandler. Maintains list of active connections.
 */
public class Server {
    private static final int TCP_PORT = 1300; // TCP port to listen to
    private static final int SECONDS_IN_ONE_HOUR = 60 * 60;
    // The socket will be close when inactive for this many milliseconds
    private static final int SOCKET_TIMEOUT_MS = SECONDS_IN_ONE_HOUR * 1000;

    private final List<ClientHandler> clientHandlers = new LinkedList<>();

    /**
     * Entrypoint for the application
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.run();
        log("Server exiting...");
    }

    /**
     * Start the TCP chat server
     */
    private void run() {
        ServerSocket welcomeSocket = this.openWelcomingSocket();
        if (welcomeSocket != null) {
            while (true) {
                Socket clientSocket = this.acceptNextClient(welcomeSocket);
                if (clientSocket != null) {
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    clientHandler.start();
                    this.storeClientHandler(clientHandler);
                }
            }
        }
    }

    /**
     * Store client handler in the register
     *
     * @param clientHandler The client handler thread
     */
    private void storeClientHandler(ClientHandler clientHandler) {
        this.clientHandlers.add(clientHandler);
    }

    /**
     * Remove client handler from the register
     *
     * @param clientHandler The client handler thread
     */
    public void removeClientHandler(ClientHandler clientHandler) {
        this.clientHandlers.remove(clientHandler);
    }

    /**
     * Open a server socket which listens on the predefined TCP port
     *
     * @return the welcoming server socket, null on error
     */
    private ServerSocket openWelcomingSocket() {
        ServerSocket welcomeSocket = null;
        try {
            welcomeSocket = new ServerSocket(TCP_PORT);
            log("Server listening on port " + TCP_PORT);
        } catch (IOException e) {
            log("Could not open a listening socket: " + e.getMessage());
        }
        return welcomeSocket;
    }

    /**
     * Block execution until the next client connects
     *
     * @param welcomeSocket The listening server socket
     * @return Socket for the newly connected client, null on error
     */
    private Socket acceptNextClient(ServerSocket welcomeSocket) {
        Socket clientSocket = null;
        try {
            clientSocket = welcomeSocket.accept();
            clientSocket.setSoTimeout(SOCKET_TIMEOUT_MS);
            log("New client connected from " + clientSocket.getRemoteSocketAddress());
        } catch (IOException e) {
            log("Failed to accept a client connection: " + e.getMessage());
        }
        return clientSocket;
    }

    /**
     * Log a message to standard output
     */
    public static void log(String message) {
        System.out.println(message);
    }

    /**
     * Forward a message to all client sockets, except the socket belonging to the original sender
     *
     * @param message The message to forward
     * @param sender  The original sender who will not receive this message
     * @return The number of clients who received the message
     */
    public int forwardToAllClientsExcept(String message, ClientHandler sender) {
        int recipientCount = 0;
        for (ClientHandler c : this.clientHandlers) {
            if (c != sender) {
                c.send(message);
                recipientCount++;
            }
        }
        return recipientCount;
    }

    /**
     * Check if a given username is already available or a logged-in users has already taken it
     *
     * @param username The username to check
     * @return True if username available, false if someone already uses it
     */
    public boolean isUsernameAvailable(String username) {
        ClientHandler clientHandler = this.getHandlerByUsername(username);
        return clientHandler == null;
    }

    /**
     * Find a client handler with the given username
     *
     * @param username The username of desired client handler
     * @return The desired client handler or null if none found with that username
     */
    private ClientHandler getHandlerByUsername(String username) {
        ClientHandler clientHandler = null;
        Iterator<ClientHandler> it = this.clientHandlers.iterator();
        while (clientHandler == null && it.hasNext()) {
            ClientHandler c = it.next();
            if (c.hasUsername(username)) {
                clientHandler = c;
            }
        }
        return clientHandler;
    }

    /**
     * Return all usernames currently in use (all logged-in users), separated by spaces
     *
     * @return All active usernames, separated by spaces
     */
    public String getActiveUsernames() {
        List<String> usernames = new LinkedList<>();
        this.clientHandlers.forEach(c -> usernames.add(c.getUsername()));
        return String.join(" ", usernames);
    }

    /**
     * Forward a private message to a specific recipient
     *
     * @param recipient    The username of the recipient
     * @param wholeMessage The whole message to forward, including the privmsg, recipient, etc
     * @return true on success, false on error (user not found)
     */
    public boolean forwardPrivateMessage(String recipient, String wholeMessage) {
        ClientHandler clientHandler = this.getHandlerByUsername(recipient);
        if (clientHandler != null && clientHandler.isLoggedIn()) {
            clientHandler.send(wholeMessage);
            return true;
        } else {
            return false;
        }
    }
}
