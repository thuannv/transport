package transport.udp.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import transport.ZLive;

/**
 *
 * @author thuannv
 * @since Sept. 06, 2017
 */
public class UDPServer {

    private static final int UDP_HANDSHAKE = 300;
    private static final int UDP_PING = 301;
    private static final int UDP_PONG = 302;
    private static final int UDP_CLIEN_CLOSED = 303;

    private static final int SUB_UDP_HANDSHAKE_SUCCESS = 300;
    private static final int SUB_UDP_HANDSHAKE_FAILURE = 301;

    private static final Logger LOGGER = Logger.getLogger(UDPServer.class.getName());

    public void start() throws IOException, UnknownHostException {

        byte[] data;
        int command;
        SocketAddress clientAddress;
        ZLive.ZAPIMessage message;
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        InetSocketAddress receivingAddress = new InetSocketAddress("localhost", 3333);
        DatagramChannel datagramChannel = DatagramChannel.open();
        DatagramSocket socket = datagramChannel.socket();
        socket.bind(receivingAddress);
        boolean stop = false;
        while (!stop) {
            System.out.println("Waiting for message...");
            try {
                buffer.clear();
                clientAddress = datagramChannel.receive(buffer);
                buffer.flip();
                data = Arrays.copyOfRange(buffer.array(), buffer.position(), buffer.limit());
                try {
                    message = ZLive.ZAPIMessage.parseFrom(data);
                    command = message.getCmd();
                    switch (command) {
                        case UDP_HANDSHAKE:
                            System.out.println("Received handshake. Sending handshake success...");
                            ByteBuffer buf = ByteBuffer.allocate(4);
                            buf.putInt(102);
                            buf.flip();
                            data = ZLive.ZAPIMessage.newBuilder(message)
                                    .setSubCmd(SUB_UDP_HANDSHAKE_SUCCESS)
                                    .setData(ByteString.copyFrom(buf))
                                    .build()
                                    .toByteArray();
                            buffer.clear();
                            buffer.put(data);
                            buffer.flip();
                            datagramChannel.send(buffer, clientAddress);
                            System.out.println("Handshake success is sent.");
                            break;

                        case UDP_CLIEN_CLOSED:
                            System.out.println("Client closed.");
                            break;

                        case UDP_PING:
                            System.out.println("Received Ping.");
                            data = ZLive.ZAPIMessage.newBuilder(message)
                                    .setCmd(UDP_PONG)
                                    .build()
                                    .toByteArray();
                            buffer.clear();
                            buffer.put(data);
                            buffer.flip();
                            datagramChannel.send(buffer, clientAddress);
                            System.out.println("Sent Pong.");
                            break;
                        default:
                            System.out.println("Received message from client.");
                            buffer.clear();
                            buffer.put(message.toByteArray());
                            buffer.flip();
                            datagramChannel.send(buffer, clientAddress);
                            System.out.println("Sent reponse message to client.");
                            break;
                    }
                } catch (InvalidProtocolBufferException ie) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
