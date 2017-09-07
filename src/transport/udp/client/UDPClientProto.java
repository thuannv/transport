package transport.udp.client;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import transport.DataListener;
import transport.ZLive;

/**
 *
 * @author thuannv
 * @since Sept. 06, 2017
 */
public class UDPClientProto {

    private final UDPConfigs mConfigs;

    private SocketReader mReader;

    private SocketWriter mWriter;

    private DataListener mListener;

    public UDPClientProto(UDPConfigs configs) {
        mConfigs = configs;
    }

    public void setListener(DataListener listener) {
        mListener = listener;
    }

    public void start() {
        try {
            final DatagramChannel datagramChannel = connect();
            createReader(datagramChannel);
            createWriter(datagramChannel);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private DatagramChannel connect() throws UnknownHostException, IOException {
        InetSocketAddress address = new InetSocketAddress(mConfigs.getHost(), mConfigs.getPort());
        DatagramChannel datagramChannel = DatagramChannel.open();
        DatagramSocket socket = datagramChannel.socket();
        socket.connect(address);
        return datagramChannel;
    }

    private void createReader(DatagramChannel datagramChannel) {
        if (mReader == null) {
            mReader = new SocketReader(mConfigs, datagramChannel);
            mReader.setListener(new DataListener() {
                @Override
                public void onReceived(byte[] data) {
                    if (mListener != null) {
                        mListener.onReceived(data);
                    }
                }
            });
            mReader.start();
        }
    }

    private void createWriter(DatagramChannel datagramChannel) {
        if (mWriter == null) {
            mWriter = new SocketWriter(mConfigs, datagramChannel);
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

        if (mWriter != null && mWriter.isRunning()) {
            mWriter.write(data);
        }
    }

    private static byte[] generateMessage() {
        ZLive.ZAPIMessage.Builder builder = ZLive.ZAPIMessage.newBuilder();
        builder.setCmd(1);
        builder.setDeviceId("steven-pc@vng.com.vn");
        builder.setData(ByteString.copyFrom("16f63762155910c13b6a3877a70e943395d565ef66e7dff9", Charset.defaultCharset()));
        ZLive.ZAPIMessage data = builder.build();
        return data.toByteArray();
    }

    private static String parse(byte[] data) {
        String result = "";
        try {
            ZLive.ZAPIMessage msg = ZLive.ZAPIMessage.parseFrom(data);
            ByteString responsedData = msg.getData();
            result = responsedData.toString(Charset.defaultCharset()) + "";
        } catch (InvalidProtocolBufferException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            final UDPConfigs configs = new UDPConfigs("49.213.118.166", 11114, 1024, 15000);
            final UDPClientProto client = new UDPClientProto(configs);
            client.setListener(new DataListener() {
                @Override
                public void onReceived(byte[] data) {
                    String reponsedMessage = parse(data);
                    if (reponsedMessage != null && !reponsedMessage.isEmpty()) {
                        System.out.println(reponsedMessage);
                    } else {
                        System.out.println("parse data failed.");
                    }

                    System.out.println("Sending new message...");
                    client.send(generateMessage());
                }
            });
            client.start();

            client.send(generateMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
