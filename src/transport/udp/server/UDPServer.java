
package transport.udp.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thuannv
 * @since Sept. 06, 2017
 */
public class UDPServer {

    private static final Logger LOGGER = Logger.getLogger(UDPServer.class.getName());

    public void start() throws IOException, UnknownHostException {
        InetAddress hostIP = InetAddress.getLocalHost();
        InetSocketAddress receivingAddress = new InetSocketAddress(hostIP, 3333);
        InetSocketAddress sendingAddress = new InetSocketAddress(hostIP, 4444);
        DatagramChannel datagramChannel = DatagramChannel.open();
        DatagramSocket socket = datagramChannel.socket();
        socket.bind(receivingAddress);

        String message, confirm;
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        byte[] data;
        boolean stop = false;
        while (!stop) {
            System.out.println("Waiting for message...");
            datagramChannel.receive(buffer);
            buffer.flip();
            data = buffer.array();
            message = new String(data, 0, buffer.limit(), Charset.defaultCharset());
            if ("STOP".equals(message)) {
                System.out.println("Stopping server...");
                stop = true;
            } else {
                System.out.format("Client Message: \"%s\"\n", message);
                buffer.clear();

                confirm = ("Received " + message);
                buffer.put(confirm.getBytes(Charset.defaultCharset()));
                buffer.flip();

                datagramChannel.send(buffer, sendingAddress);
                buffer.clear();
            }
//            System.out.format("Client Message: \"%s\"\n", message);
//            buffer.clear();
//
//            confirm = ("Received " + message);
//            buffer.put(confirm.getBytes(Charset.defaultCharset()));
//            buffer.flip();
//
//            datagramChannel.send(buffer, sendingAddress);
//            buffer.clear();
        }
        System.out.println("Server is stopped.");
    }

    public static void main(String[] args) {
        try {
            new UDPServer().start();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

}
