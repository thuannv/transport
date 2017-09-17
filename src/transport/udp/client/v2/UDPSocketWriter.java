package transport.udp.client.v2;

import transport.udp.client.UDPConfigs;

import java.net.*;
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

    private static final AtomicInteger INSTANCES_COUNT = new AtomicInteger();

    private final UDPConfigs mConfigs;

    private final DatagramSocket mSocket;

    private final BlockingQueue<byte[]> mQueue;

    private final SocketAddress mTargetAddress;

    private CountDownLatch mStartLatch;

    public UDPSocketWriter(UDPConfigs configs, DatagramSocket socket, CountDownLatch startLatch) throws UnknownHostException {
        super("DatagramSocketWriter");
        mConfigs = configs;
        mTargetAddress = new InetSocketAddress(InetAddress.getByName(configs.getHost()), configs.getPort());
        mSocket = socket;
        mQueue = new LinkedBlockingDeque<>();
        mStartLatch = startLatch;
    }

    public void stopWriter(boolean blocking, long timeout) {
        this.interrupt();
        if (blocking) {
            try {
                this.join(timeout);
            } catch (InterruptedException e) {
            }
        }
    }

    public void write(byte[] data) {
        if (data != null && data.length > 0 && isAlive() && !isInterrupted()) {
            try {
                mQueue.offer(data);
            } catch (Exception e) {
                e.printStackTrace();
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
        byte[] data;
        final DatagramPacket packet = new DatagramPacket(new byte[0], 0, mTargetAddress);
        while (!isInterrupted()) {
            try {
                data = mQueue.poll(500, TimeUnit.MILLISECONDS);
                if (data != null || data.length > 0) {
                    packet.setData(data, 0, data.length);
                    try {
                        mSocket.send(packet);
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyStop() {
        System.out.println(Thread.currentThread().getName() + " is finished.");
    }


    @Override
    protected void finalize() throws Throwable {
        INSTANCES_COUNT.decrementAndGet();
        super.finalize();
    }

    private static String getInstanceName(int id) {
        return String.format("UDPSocketReader_%d", id);
    }
}
