package transport.udp.client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.nio.channels.DatagramChannel;
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

    public UDPClient(UDPConfigs configs) {
        mConfigs = configs;
    }

    public void setProcessor(IoProcessor listener) {
        mProcessor = listener;
    }

    public void start() throws IOException {
        createChannel();
        createReader();
        createWriter();
    }

    private void createChannel() throws IOException {
        mChannel = DatagramChannel.open();
        DatagramSocket socket = mChannel.socket();
        socket.setSoTimeout(mConfigs.getSocketTimeout());
    }

    private void createReader() {
        if (mReader == null) {
            mReader = new UDPSocketReader(mConfigs, mChannel);
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
            mWriter = new UDPSocketWriter(mConfigs, mChannel);
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

    public void stop() {
        stopReader();
        stopWriter();
        closeChannel();
    }

    public void send(byte[] data) {
        if (mWriter != null) {
            mWriter.write(data);
        }
    }
}
