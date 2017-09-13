package transport.udp.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.LinkedList;

/**
 *
 * @author thuannv
 * @since Sept. 06, 2017
 */
public final class UDPSocketWriter extends Thread {

    private final Object mLock = new Object();

    private final UDPConfigs mConfigs;

    private final DatagramChannel mDatagramChannel;

    private final LinkedList<byte[]> mQueue;

    private volatile boolean mIsRunning = false;

    private final SocketAddress mTargetAddress;

    public UDPSocketWriter(UDPConfigs configs, DatagramChannel channel) {
        super("DatagramSocketWriter");
        mConfigs = configs;
        mTargetAddress = new InetSocketAddress(configs.getHost(), configs.getPort());
        mDatagramChannel = channel;
        mQueue = new LinkedList<>();
    }

    public void stopWriter() {
        mIsRunning = false;
        synchronized (mLock) {
            mLock.notifyAll();
        }
        this.interrupt();
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public void write(byte[] data) {
        if (data == null || data.length <= 0) {
            return;
        }

        if (mIsRunning && !isInterrupted()) {
            synchronized (mLock) {
                mQueue.addFirst(data);
                mLock.notifyAll();
            }
        }
    }

    @Override
    public void run() {
        mIsRunning = true;
        try {
            byte[] data = null;
            final ByteBuffer buffer = ByteBuffer.allocate(mConfigs.getBufferSize());
            while (mIsRunning && !interrupted()) {
                synchronized (mLock) {
                    while (mIsRunning && mQueue.isEmpty()) {
                        try {
                            mLock.wait();
                        } catch (InterruptedException ie) {
                        }
                    }
                    if (!mQueue.isEmpty()) {
                        data = mQueue.removeLast();
                    }
                }
                if (data != null) {
                    try {
                        buffer.clear();
                        buffer.put(data, 0, data.length);
                        buffer.flip();
                        mDatagramChannel.send(buffer, mTargetAddress);
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
            }
            System.out.println("Writer is stopping...");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mIsRunning = false;
        }
        System.out.println("Writer is stopped.");
    }

}
