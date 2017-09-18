package transport.udp.client.v2;

import transport.udp.client.UDPConfigs;

import java.net.*;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author thuannv
 * @since 09/17/2017
 */
public final class UDPSocketWriter extends Thread {

    private static final String NAME = "UDPSocketWriter";

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final UDPConfigs mConfigs;

    private final DatagramSocket mSocket;

    private final BlockingQueue<byte[]> mQueue;

    private final SocketAddress mTargetAddress;

    private CountDownLatch mStartLatch;

    private volatile boolean mIsRunning = false;

    public UDPSocketWriter(UDPConfigs configs, DatagramSocket socket, CountDownLatch startLatch) throws UnknownHostException {
        super(String.format(Locale.US, "%s-%d", NAME, COUNTER.incrementAndGet()));
        mConfigs = configs;
        mTargetAddress = new InetSocketAddress(InetAddress.getByName(configs.getHost()), configs.getPort());
        mSocket = socket;
        mQueue = new LinkedBlockingDeque<>();
        mStartLatch = startLatch;
    }

    public void stopWriter(boolean blocking) {
        mIsRunning = false;
        interrupt();
        if (blocking) {
            try {
                join();
            } catch (InterruptedException e) {
            }
        }
    }

    public void write(byte[] data) {
        if (data != null && data.length > 0 && isAlive() && !isInterrupted()) {
            try {
                mQueue.offer(data);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void run() {
        notifyStart();
        loop();
        notifyStop();
    }

    private void notifyStart() {
        System.out.println(Thread.currentThread().getName() + " is started.");
        if (mStartLatch != null) {
            mStartLatch.countDown();
        }
    }

    private void loop() {
        mIsRunning = true;
        byte[] data;
        final DatagramPacket packet = new DatagramPacket(new byte[0], 0, mTargetAddress);
        while (!isInterrupted() && mIsRunning) {
            try {
                data = mQueue.poll(1000, TimeUnit.MILLISECONDS);
                if (data != null && data.length > 0) {
                    packet.setData(data, 0, data.length);
                    mSocket.send(packet);
                }
            } catch (Exception e) {
            }
        }
    }

    private void notifyStop() {
        System.out.println(Thread.currentThread().getName() + " is finished.");
    }

}
