package no.ntnu.datakomm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * A Simple TCP client, used as a warm-up exercise for assignment A4.
 */
public class SimpleTcpClient {
    // Remote host where the server will be running
    private static final String HOST = "localhost";
    // TCP port
    private static final int PORT = 1301;
    private Socket socket = null;
    private PrintWriter toServer = null;

    /**
     * Run the TCP Client.
     *
     * @param args Command line arguments. Not used.
     */
    public static void main(String[] args) {
        SimpleTcpClient client = new SimpleTcpClient();
        try {
            client.run();
        } catch (InterruptedException e) {
            log("Client interrupted");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Run the TCP Client application. The logic is already implemented, no need to change anything in this method.
     * You can experiment, of course.
     *
     * @throws InterruptedException The method sleeps to simulate long client-server conversation.
     *                              This exception is thrown if the execution is interrupted halfway.
     */
    public void run() throws InterruptedException {
        log("Simple TCP client started");

        if (!this.connectToServer(HOST, PORT)) {
            log("ERROR: Failed to connect to the server");
            return;
        }
        log("Connection to the server established");

        int a = (int) (1 + Math.random() * 10);
        int b = (int) (1 + Math.random() * 10);
        String request = a + "+" + b;

        this.sendRequestToServer("test");
        this.sendRequestToServer("exit");

        if (!this.sendRequestToServer(request)) {
            log("ERROR: Failed to send valid message to server!");
            return;
        }
        log("Sent " + request + " to server");

        String response = this.readResponseFromServer();
        if (response == null) {
            log("ERROR: Failed to receive server's response!");
            return;
        }
        log("Server responded with: " + response);

        this.sleepRandomTime();
        request = "bla+bla";
        if (!this.sendRequestToServer(request)) {
            log("ERROR: Failed to send invalid message to server!");
            return;
        }
        log("Sent " + request + " to server");

        response = this.readResponseFromServer();
        if (response == null) {
            log("ERROR: Failed to receive server's response!");
            return;
        }
        log("Server responded with: " + response);

        if (!this.sendRequestToServer("game over") || !this.closeConnection()) {
            log("ERROR: Failed to stop conversation");
            return;
        }
        log("Game over, connection closed");

        // When the connection is closed, try to send one more message. It should fail.
        if (!this.sendRequestToServer("2+2")) {
            log("Sending another message after closing the connection failed as expected");
        } else {
            log("ERROR: sending a message after closing the connection did not fail!");
        }

        log("Simple TCP client finished");
    }

    /**
     * Put the main thread to sleep for a random number of seconds (between 2 and 5 seconds)
     */
    private void sleepRandomTime() {
        long secondsToSleep = 2 + (long) (Math.random() * 5);
        log("Sleeping " + secondsToSleep + " seconds to allow simulate long client-server connection...");
        try {
            Thread.sleep(secondsToSleep * 1000);
        } catch (InterruptedException e) {
            System.out.println("Thread sleep interrupted... Oh, well...");
        }
    }

    /**
     * Try to establish TCP connection to the server (the three-way handshake).
     *
     * @param host The remote host to connect to. Can be domain (localhost, ntnu.no, etc), or IP address
     * @param port TCP port to use
     * @return True when connection established, false on error
     */
    private boolean connectToServer(String host, int port) {
        if (host == null) {
            throw new IllegalArgumentException("Host cannot be null!");
        }

        boolean toReturn = false;

        try {
            this.socket = new Socket(host, port);
            this.toServer = new PrintWriter(this.socket.getOutputStream(), true);
            toReturn = true;
        } catch (IOException e) {
            log("Failed to connect: " + e.getMessage());
        }
        return toReturn;
    }

    /**
     * Close the TCP connection to the remote server.
     *
     * @return True on success, false otherwise. Note: if the connection was already closed (not established),
     * return true as well.
     */
    private boolean closeConnection() {
        // Guard condition
        if (!this.isConnected()) {
            throw new IllegalStateException("Not connected to the server!");
        }

        boolean result = false;
        try {
            this.socket.close();
            this.socket = null;
            this.toServer = null;
            result = true;
        } catch (IOException e) {
            log("Could not close the connection with the server " + e.getMessage());
        }

        return result;
    }


    /**
     * Send a request message to the server (newline will be added automatically)
     *
     * @param request The request message to send. Do NOT include the newline in the message!
     * @return True when message successfully sent, false on error.
     */
    private boolean sendRequestToServer(String request) {
        // Guard condition
        if (!this.isConnected()) {
            log("Connection is missing");
            return false;
        }
        if (request == null) {
            log("The request cannot be null");
            return false;
        }

        this.toServer.println(request);

        return true;
    }

    /**
     * Wait for one response from the remote server.
     *
     * @return The response received from the server, null on error. The newline character is stripped away
     * (not included in the returned value).
     */
    private String readResponseFromServer() {
        // Guard condition
        if (!this.isConnected()) {
            log("Could not read the response, not connected to the server . . .");
            return null;
        }

        String response = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            response = reader.readLine();
        } catch (IOException e) {
            log("Failed to read the response from the server, reason: " + e.getMessage());
        }
        return response;
    }

    /**
     * Returns true if the client is connected to the server.
     *
     * @return True if the client is connected to the server
     */
    private boolean isConnected() {
        return !((this.socket == null) || (this.toServer == null));
    }

    /**
     * Log a message to the system console.
     *
     * @param message The message to be logged (printed).
     */
    private static void log(String message) {
        String threadId = "THREAD #" + Thread.currentThread().getId() + ": ";
        System.out.println(threadId + message);
    }
}
