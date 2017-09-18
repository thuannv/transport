package transport.udp.client.v2;

import transport.IoProcessor;
import transport.udp.client.UDPConfigs;

import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author thuannv
 * @since 09/17/2017
 */
public final class UDPSocketReader extends Thread {

    private static final String NAME = "UDPSocketReader";

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final UDPConfigs mConfigs;

    private final DatagramSocket mSocket;

    private final CountDownLatch mStartLatch;

    private WeakReference<IoProcessor> mProcessorRef;

    private volatile boolean mIsRunning = false;

    UDPSocketReader(UDPConfigs configs, DatagramSocket socket, CountDownLatch startLatch, IoProcessor processor) {
        super(String.format(Locale.US, "%s-%d", NAME, COUNTER.incrementAndGet()));
        if (configs == null) {
            throw new IllegalArgumentException("configs must NOT be null.");
        }

        if (socket == null) {
            throw new IllegalArgumentException("socket must NOT be null.");
        }

        if (processor == null) {
            throw new IllegalArgumentException("processor must NOT be null.");
        }

        mConfigs = configs;
        mSocket = socket;
        mStartLatch = startLatch;
        mProcessorRef = new WeakReference<>(processor);
    }

    @Override
    public void run() {
        notifyStart();
        loop();
        notifyStop();
    }

    public void stopReader(boolean blocking) {
        mIsRunning = false;
        interrupt();
        if (blocking) {
            try {
                join();
            } catch (InterruptedException e) {
            }
        }
    }

    private void notifyReceive(byte[] data) {
        IoProcessor processor = mProcessorRef.get();
        if (processor != null) {
            processor.process(data);
        }
    }

    private void loop() {
        mIsRunning = true;
        InetAddress packetAddress = null;
        final int size = mConfigs.getBufferSize();
        final byte[] buffer = new byte[size];
        final DatagramPacket packet = new DatagramPacket(buffer, size);
        final String server = mConfigs.getHost();
        while (!isInterrupted() && mIsRunning) {
            try {
                packet.setData(buffer, 0, size);
                mSocket.receive(packet);
                packetAddress = packet.getAddress();
                if (packetAddress != null
                        && (server.equals(packetAddress.getHostName())
                        || server.equals(packetAddress.getHostAddress())
                        || server.equals(packetAddress.getCanonicalHostName()))) {
                    notifyReceive(Arrays.copyOfRange(buffer, packet.getOffset(), packet.getLength()));
                }
            } catch (Exception ex) {
                //ex.printStackTrace();
            }
        }
    }

    private void notifyStart() {
        System.out.println(Thread.currentThread().getName() + " is started.");
        if (mStartLatch != null) {
            mStartLatch.countDown();
        }
    }

    private void notifyStop() {
        System.out.println(Thread.currentThread().getName() + " is finished.");
    }

}
