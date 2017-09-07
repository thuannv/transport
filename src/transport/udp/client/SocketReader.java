package transport.udp.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import transport.DataListener;

/**
 *
 * @author thuannv
 * @since Sept. 06, 2017
 */
public final class SocketReader extends Thread {

    private final DatagramChannel mDatagramChannel;

    private volatile boolean mIsRunning = false;

    private final UDPConfigs mConfigs;

    private DataListener mListener;

    public SocketReader(UDPConfigs configs, DatagramChannel datagramChannel) {
        super("DatagramSocketReader");
        mConfigs = configs;
        mDatagramChannel = datagramChannel;
    }

    public void stopReader() {
        mIsRunning = false;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public void setListener(DataListener listener) {
        mListener = listener;
    }

    @Override
    public void run() {
        mIsRunning = true;
        try {
            InetAddress serverAddress = null;
            final int size = mConfigs.getBufferSize();
            final byte[] buffer = new byte[size];
            final DatagramPacket packet = new DatagramPacket(buffer, size);
            final DatagramSocket socket = mDatagramChannel.socket();
            final String server = mConfigs.getHost();
            while (mIsRunning) {
                try {
                    packet.setData(buffer, 0, size);
                    socket.receive(packet);
                    serverAddress = packet.getAddress();
                    if (serverAddress != null
                            && (server.equals(serverAddress.getHostName())
                            || server.equals(serverAddress.getAddress())
                            || server.equals(serverAddress.getHostAddress())
                            || server.equals(serverAddress.getCanonicalHostName()))) {
                        if (mListener != null) {
                            mListener.onReceived(Arrays.copyOfRange(buffer, packet.getOffset(), packet.getLength()));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("Reader is stopping...");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mIsRunning = false;
        }
        System.out.println("Reader is stopped.");
    }
}
