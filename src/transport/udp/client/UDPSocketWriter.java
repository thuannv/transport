package transport.udp.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author thuannv
 * @since Sept. 06, 2017
 */
public final class UDPSocketWriter extends Thread {

    private final UDPConfigs mConfigs;

    private final DatagramChannel mDatagramChannel;

    private final BlockingQueue<byte[]> mQueue;

    private final SocketAddress mTargetAddress;

    private volatile boolean mIsRunning = false;
    
    private CountDownLatch mStartSignal;
    
    private CountDownLatch mStopSignal;
    
    public UDPSocketWriter(UDPConfigs configs, DatagramChannel channel, CountDownLatch startSignal, CountDownLatch stopSignal) {
        super("DatagramSocketWriter");
        mConfigs = configs;
        mTargetAddress = new InetSocketAddress(configs.getHost(), configs.getPort());
        mDatagramChannel = channel;
        mQueue = new LinkedBlockingDeque<>();
        mStartSignal = startSignal;
        mStopSignal = stopSignal;
    }

    public void stopWriter() {
        mIsRunning = false;
    }

    public void write(byte[] data) {
        if (data != null && data.length > 0 && mIsRunning) {
            try {
                mQueue.offer(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        mStartSignal.countDown();
        mIsRunning = true;
        byte[] data;
        final ByteBuffer buffer = ByteBuffer.allocate(mConfigs.getBufferSize());
        while (mIsRunning) {
            try {
                data = mQueue.poll(500, TimeUnit.MILLISECONDS);
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mIsRunning = false;
        System.out.println("Writer is stopped.");
        mStopSignal.countDown();
    }

}
