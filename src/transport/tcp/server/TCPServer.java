
package transport.tcp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author thuannv
 * @since Sept. 06, 2017
 */
public class TCPServer {

    public void start() {
        
        final int port = 2017;

        try (ServerSocket server = new ServerSocket(port)) {
            TCPHandler handler = new TCPHandler();
            System.out.println("Server is waiting for connections port " + port);
            while (true) {
                try {
                    Socket connection = server.accept();
                    System.out.println("Accepted connection from client " + connection);
                    handler.handle(connection);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TCPServer().start();
    }
}
