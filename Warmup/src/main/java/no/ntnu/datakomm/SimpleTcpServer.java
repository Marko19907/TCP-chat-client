package no.ntnu.datakomm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;

/**
 * A Simple TCP server, used as a warm-up exercise for assignment A4.
 */
public class SimpleTcpServer {
    private static final int PORT = 1301;

    public static void main(String[] args) {
        SimpleTcpServer server = new SimpleTcpServer();
        log("Simple TCP server starting");
        server.run();
        log("ERROR: the server should never go out of the run() method! After handling one client");
    }

    public void run() {
        try {
            ServerSocket socket = new ServerSocket(PORT);
            while (true) {
                final Socket client = socket.accept();
                Thread t = new Thread(() -> {
                    log("New client connected on thread: " + Thread.currentThread().getId());

                    try {
                        while (!socket.isClosed()) {
                            BufferedReader clientInput = new BufferedReader(new InputStreamReader(client.getInputStream()));
                            String message = clientInput.readLine();

                            log("Message from client: " + message);

                            if (message != null && message.equals("exit")) {
                                client.close();
                            }
                        }
                    } catch (IOException e) {
                        log(e.getMessage());
                    }
                });
                t.start();
            }
        } catch (IOException e) {
            log(e.getMessage());
        }
    }

    /**
     * Log a message to the system console.
     *
     * @param message The message to be logged (printed).
     */
    private static void log(String message) {
        System.out.println(message);
    }
}
