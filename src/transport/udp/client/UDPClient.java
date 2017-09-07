package transport.udp.client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import transport.DataListener;

/**
 *
 * @author thuannv
 * @since Sept. 06, 2017
 */
public class UDPClient {

    private final UDPConfigs mConfigs;

    private SocketReader mReader;

    private SocketWriter mWriter;

    private DataListener mListener;

    public UDPClient(UDPConfigs configs) {
        mConfigs = configs;
    }

    public void setListener(DataListener listener) {
        mListener = listener;
    }

    public void start() {
        try {
            final DatagramChannel datagramChannel = connect();
            createReader(datagramChannel);
            createWriter(datagramChannel);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private DatagramChannel connect() throws UnknownHostException, IOException {
        DatagramChannel datagramChannel = DatagramChannel.open();
        DatagramSocket socket = datagramChannel.socket();
        socket.setSoTimeout(mConfigs.getSocketTimeout());
        return datagramChannel;
    }

    private void createReader(DatagramChannel datagramChannel) {
        if (mReader == null) {
            mReader = new SocketReader(mConfigs, datagramChannel);
            mReader.setListener(new DataListener() {
                @Override
                public void onReceived(byte[] data) {
                    if (mListener != null) {
                        mListener.onReceived(data);
                    }
                }
            });
            mReader.start();
        }
    }

    private void createWriter(DatagramChannel datagramChannel) {
        if (mWriter == null) {
            mWriter = new SocketWriter(mConfigs, datagramChannel);
            mWriter.start();
        }
    }

    private void stopReader() {
        if (mReader != null) {
            mReader.stopReader();
            mReader = null;
        }
    }

    private void stopWriter() {
        if (mWriter != null) {
            mWriter.stopWriter();
            mWriter = null;
        }
    }

    public void stop() {
        stopReader();
        stopWriter();
    }

    public void send(byte[] data) {
        if (data == null || data.length <= 0) {
            return;
        }

        if (mWriter != null && mWriter.isRunning()) {
            mWriter.write(data);
        }
    }

    public static void main(String[] args) {
        try {
//            final UDPConfigs configs = new UDPConfigs("localhost", 3333, 1024, 15000);
            final UDPConfigs configs = new UDPConfigs("127.0.0.1", 3333, 1024, 15000);
            final UDPClient client = new UDPClient(configs);
            client.setListener(new DataListener() {
                int id = 1;

                @Override
                public void onReceived(byte[] data) {
                    if (id > 10) {
                        client.send("STOP".getBytes(Charset.defaultCharset()));
                        client.stop();
                    } else {
                        String msg = new String(data, 0, data.length, Charset.defaultCharset());
                        System.out.format("Server responsed: \"%s\"\n", msg);

                        System.out.println("sending new message...");
                        client.send(("Message " + (id++)).getBytes(Charset.defaultCharset()));
                    }
                }
            });
            client.start();
            client.send("Hello!".getBytes(Charset.defaultCharset()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
