
package transport.udp.client;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
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
        final ByteBuffer buffer = ByteBuffer.allocate(mConfigs.getBufferSize());
        try {
            while (mIsRunning) {
                try {
                    buffer.clear();
                    mDatagramChannel.receive(buffer);
                    buffer.flip();
                    if (mListener != null) {
                        mListener.onReceived(buffer.array(), buffer.position(), buffer.limit());
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
