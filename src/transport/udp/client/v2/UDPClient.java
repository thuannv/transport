package transport.udp.client.v2;

import transport.Address;
import transport.BuildConfig;
import transport.Configs;
import transport.IoProcessor;
import transport.udp.client.UDPConfigs;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author thuannv
 * @since 09/17/2017
 */
public final class UDPClient {

    private final AtomicBoolean mIsReady = new AtomicBoolean(false);

    private final AtomicBoolean mIsStarting = new AtomicBoolean(false);

    private final AtomicBoolean mIsStopping = new AtomicBoolean(false);

    private final CountDownLatch mStartSignal = new CountDownLatch(2);

    private final UDPConfigs mConfigs;

    private DatagramSocket mSocket;

    private UDPSocketReader mReader;

    private UDPSocketWriter mWriter;

    private IoProcessor mProcessor;

    private final IoProcessor mProcessDelegator = new IoProcessor() {
        @Override
        public void process(byte[] data) {
            if (mProcessor != null) {
                mProcessor.process(data);
            }
        }
    };

    UDPClient(UDPConfigs configs) {
        mConfigs = configs;
    }

    private DatagramSocket createSocket() throws SocketException {
        mSocket = new DatagramSocket(0);
        mSocket.setSoTimeout(mConfigs.getSocketTimeout());
        mSocket.setReuseAddress(mConfigs.isReuseAddress());
        return mSocket;
    }

    private void createReader() {
        if (mReader == null) {
            mReader = new UDPSocketReader(mConfigs, mSocket, mStartSignal, mProcessDelegator);
            mReader.start();
        }
    }

    private void createWriter() throws UnknownHostException {
        if (mWriter == null) {
            mWriter = new UDPSocketWriter(mConfigs, mSocket, mStartSignal);
            mWriter.start();
        }
    }

    private void stopWriter() {
        if (mWriter != null) {
            mWriter.stopWriter(true, 1000);
            mWriter = null;
        }
    }

    private void stopReader() {
        if (mReader != null) {
            mReader.stopReader(true, 1000);
            mReader = null;
        }
    }

    private void closeSocket() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (Exception e) {
            }
        }
    }

    public void start() throws SocketException, UnknownHostException {
        if (mIsReady.get() || mIsStarting.get()) {
            System.out.println("UDPClient is already started.");
            return;
        }

        System.out.println("UDPClient is starting...");
        try {
            createSocket();
            createReader();
            createWriter();
            mIsReady.set(true);
            System.out.println("UDPClient is ready.");
        } finally {
            mIsStarting.set(false);
        }
    }

    public void stop() {
        if (mIsReady.compareAndSet(true, false)) {
            System.out.println("UDPClient is shutting down...");
            mIsStopping.set(true);
            stopWriter();
            stopReader();
            closeSocket();
            mIsStopping.set(false);
            System.out.println("UDPClient is shutdown.");
        }
    }

    public boolean isReady() {
        return mIsReady.get();
    }

    public boolean isStarting() {
        return mIsStarting.get();
    }

    public boolean isStopping() {
        return mIsStopping.get();
    }

    public void setProcessor(IoProcessor processor) {
        mProcessor = processor;
    }


    /*************************************************************
     * For testing purpose.
     *************************************************************/
    static Configs getConfigs() {
        int pingTime;
        final List<Address> servers = new ArrayList<>();
        if (BuildConfig.DEBUG) {
            pingTime = 10000;
            servers.add(new Address("127.0.0.1", 3333));
            servers.add(new Address("localhost", 4444));
        } else {
            pingTime = 15000;
            servers.add(new Address("49.213.118.166", 11114));
        }
        return new Configs(servers, pingTime, 10);
    }

    public static void main(String[] args) {

    }
}
