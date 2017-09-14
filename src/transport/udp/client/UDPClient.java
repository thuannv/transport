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

    private CountDownLatch mStopSignal;

    public UDPClient(UDPConfigs configs) {
        mConfigs = configs;
        mStartSignal = new CountDownLatch(2);
        mStopSignal = new CountDownLatch(2);
    }

    public void setProcessor(IoProcessor listener) {
        mProcessor = listener;
    }

    public void start() throws IOException {
        createChannel();
        createReader();
        createWriter();

        try {
            System.out.println("Wait for threads start...");
            mStartSignal.await();
            System.out.println("Threads already started.");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void stop() {
        stopReader();
        stopWriter();

        System.out.println("Wait for threads stop...");
        try {
            mStopSignal.await();
            System.out.println("Threads already stopped.");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        closeChannel();
    }

    private void createChannel() throws IOException {
        mChannel = DatagramChannel.open();
        DatagramSocket socket = mChannel.socket();
        socket.setSoTimeout(mConfigs.getSocketTimeout());
    }

    private void createReader() {
        if (mReader == null) {
            mReader = new UDPSocketReader(mConfigs, mChannel, mStartSignal, mStopSignal);
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
            mWriter = new UDPSocketWriter(mConfigs, mChannel, mStartSignal, mStopSignal);
            mWriter.start();
        }
    }

    private void stopReader() {
        if (mReader != null) {
            mReader.stopReader();
            mReader = null;
        }
    }

    private void closeChannel() {
        try {
            mChannel.close();
        } catch (Exception e) {
        }
    }

    private void stopWriter() {
        if (mWriter != null) {
            mWriter.stopWriter();
            mWriter = null;
        }
    }

    public void send(byte[] data) {
        if (mWriter != null) {
            mWriter.write(data);
        }
    }
}
