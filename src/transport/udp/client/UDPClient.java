package transport.udp.client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.CountDownLatch;
import transport.IoProcessor;

/**
 *
 * @author thuannv
 * @since Sept. 06, 2017
 */
public class UDPClient {

    private final UDPConfigs mConfigs;

    private UDPSocketReader mReader;

    private UDPSocketWriter mWriter;

    private IoProcessor mProcessor;

    private DatagramChannel mChannel;

    private CountDownLatch mStartSignal;

    private volatile boolean mIsRunning;

    private Thread mStopThread;

    public UDPClient(UDPConfigs configs) {
        mConfigs = configs;
        mIsRunning = false;
        mStartSignal = new CountDownLatch(2);
    }

    public void setProcessor(IoProcessor listener) {
        mProcessor = listener;
    }

    public void start() throws IOException {
        if (mIsRunning) {
            return;
        }
        mIsRunning = true;

        createChannel();
        createReader();
        createWriter();

        try {
            System.out.println("Wait for threads start...");
            mStartSignal.await();
            System.out.println("Threads already started.");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            mIsRunning = false;
        }
    }

    public void stop() {
        if (mIsRunning) {
            if (mStopThread == null || !mStopThread.isAlive() || mStopThread.isInterrupted()) {
                mStopThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            stopReader();
                            stopWriter();
                            System.out.println("Waiting for Reader and Writer to stop...");
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        closeChannel();

                        mReader = null;
                        mWriter = null;
                        mStopThread = null;
                        mIsRunning = false;
                        System.out.println("Reader and Writer are stopped.");
                    }
                });
                mStopThread.start();
            }
        }
    }

    private void createChannel() throws IOException {
        mChannel = DatagramChannel.open();
        DatagramSocket socket = mChannel.socket();
        socket.setSoTimeout(mConfigs.getSocketTimeout());
    }

    private void createReader() {
        if (mReader == null) {
            mReader = new UDPSocketReader(mConfigs, mChannel, mStartSignal);
            mReader.setProcessor(new IoProcessor() {
                @Override
                public void process(byte[] data) {
                    if (mProcessor != null) {
                        mProcessor.process(data);
                    }
                }
            });
            mReader.start();
        }
    }

    private void createWriter() {
        if (mWriter == null) {
            mWriter = new UDPSocketWriter(mConfigs, mChannel, mStartSignal);
            mWriter.start();
        }
    }

    private void stopReader() throws InterruptedException {
        if (mReader != null) {
            mReader.stopReader();
            mReader.join();
        }
    }

    private void stopWriter() throws InterruptedException {
        if (mWriter != null) {
            mWriter.stopWriter();
            mWriter.join();
        }
    }

    private void closeChannel() {
        try {
            mChannel.close();
        } catch (IOException ex) {
        }
    }

    public void send(byte[] data) {
        if (mWriter != null) {
            mWriter.write(data);
        }
    }
}
