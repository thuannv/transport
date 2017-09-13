package transport.udp.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import transport.IoProcessor;

/**
 *
 * @author thuannv
 * @since Sept. 06, 2017
 */
public final class UDPSocketReader extends Thread {

    private final DatagramChannel mDatagramChannel;

    private volatile boolean mIsRunning = false;

    private final UDPConfigs mConfigs;

    private IoProcessor mProcessor;

    public UDPSocketReader(UDPConfigs configs, DatagramChannel datagramChannel) {
        super("DatagramSocketReader");
        mConfigs = configs;
        mDatagramChannel = datagramChannel;
    }

    public void stopReader() {
        this.interrupt();
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public void setProcessor(IoProcessor processor) {
        mProcessor = processor;
    }

    @Override
    public void run() {
        mIsRunning = true;
        try {
            InetAddress packetAddress = null;
            final int size = mConfigs.getBufferSize();
            final byte[] buffer = new byte[size];
            final DatagramPacket packet = new DatagramPacket(buffer, size);
            final DatagramSocket socket = mDatagramChannel.socket();
            final String server = mConfigs.getHost();
            
            while (mIsRunning && !isInterrupted()) {
                try {
                    packet.setData(buffer, 0, size);
                    socket.receive(packet);
                    packetAddress = packet.getAddress();
                    if (packetAddress != null
                            && (server.equals(packetAddress.getHostName())
                            || server.equals(packetAddress.getHostAddress())
                            || server.equals(packetAddress.getCanonicalHostName()))) {
                        if (mProcessor != null) {
                            mProcessor.process(Arrays.copyOfRange(buffer, packet.getOffset(), packet.getLength()));
                        }
                    }
                } catch (Exception ex) {
                    //ex.printStackTrace();
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
