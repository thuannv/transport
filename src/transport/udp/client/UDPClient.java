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

    public UDPClient(UDPConfigs configs) {
        mConfigs = configs;
    }

    public void setProcessor(IoProcessor listener) {
        mProcessor = listener;
    }

    public void start() throws IOException {
        final DatagramChannel datagramChannel = connect();
        createReader(datagramChannel);
        createWriter(datagramChannel);
    }

    private DatagramChannel connect() throws IOException {
        DatagramChannel datagramChannel = DatagramChannel.open();
        DatagramSocket socket = datagramChannel.socket();
        socket.setSoTimeout(mConfigs.getSocketTimeout());
        return datagramChannel;
    }

    private void createReader(DatagramChannel datagramChannel) {
        if (mReader == null) {
            mReader = new UDPSocketReader(mConfigs, datagramChannel);
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

    private void createWriter(DatagramChannel datagramChannel) {
        if (mWriter == null) {
            mWriter = new UDPSocketWriter(mConfigs, datagramChannel);
            mWriter.start();
        }
    }

    private void stopReader() {
        if (mReader != null) {
            mReader.stopReader();
            mReader = null;
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
    }

    public void send(byte[] data) {
        if (data == null || data.length <= 0) {
            return;
        }

        if (mWriter != null) {
            mWriter.write(data);
        }
    }
}
