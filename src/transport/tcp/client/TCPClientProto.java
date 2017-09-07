package transport.tcp.client;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;
import transport.DataListener;
import transport.ZLive;

/**
 *
 * @author thuannv
 * @since Sept. 07, 2017
 */
public class TCPClientProto {

    private final Object mSocketLock = new Object();

    private final TCPConfigs mConfigs;

    private Socket mSocket;

    private ConnectionObserver mConnectionObserver;

    private DataListener mDataListener;

    private SocketReader mReader;

    private SocketWriter mWriter;

    public TCPClientProto(TCPConfigs configs) {
        mConfigs = configs;
    }

    public void setConnectionListener(ConnectionObserver observer) {
        mConnectionObserver = observer;
    }

    public void setDataListener(DataListener listener) {
        mDataListener = listener;
    }

    private void onConnected() {
        if (mConnectionObserver != null) {
            mConnectionObserver.onConnected();
        }
    }

    private void onDisconnected() {
        if (mConnectionObserver != null) {
            mConnectionObserver.onDisconnected();
        }
    }

    private void onError(Throwable t) {
        if (mConnectionObserver != null) {
            mConnectionObserver.onError(t);
        }
    }

    private void createWriter() {
        if (mWriter == null) {
            mWriter = new SocketWriter(mSocket);
            mWriter.start();
        }
    }

    private void createReader() {
        if (mReader == null) {
            mReader = new SocketReader(mSocket, mConfigs);
            mReader.setListener(new DataListener() {
                @Override
                public void onReceived(byte[] data) {
                    if (mDataListener != null) {
                        mDataListener.onReceived(data);
                    }
                }
            });
            mReader.start();
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
            if (mWriter.isRunning()) {
                mWriter.stopWriter();
            }
            mWriter = null;
        }
    }

    public void send(byte[] data) {
        if (data == null || data.length <= 0) {
            return;
        }

        if (mWriter != null && mWriter.isRunning()) {
            mWriter.write(data);
        }
    }

    private void closeSocket() throws IOException {
        synchronized (mSocketLock) {
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
        }
    }

    public boolean isConnected() {
        boolean isConnected;
        synchronized (mSocketLock) {
            isConnected = mSocket != null && mSocket.isConnected() && !mSocket.isClosed();
        }
        return isConnected;
    }

    public void disconnect() {
        try {
            if (isConnected()) {
                try {
                    stopWriter();
                    stopReader();
                    closeSocket();
                } catch (Exception e) {
                    onError(e);
                }
                onDisconnected();
            }
        } catch (Exception e) {
            onError(e);
        }
    }

    public void start() {
        try {
            mSocket = new Socket(mConfigs.getHost(), mConfigs.getPort());
            //mSocket.setSoTimeout(mConfigs.getSocketTimeout());
            createWriter();
            createReader();
            onConnected();
        } catch (Exception ex) {
            onError(ex);
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
//        return new String(data);
        String result = "";
        try {
            ZLive.ZAPIMessage msg = ZLive.ZAPIMessage.parseFrom(data);
            ByteString responsedData = msg.getData();
            result = responsedData.toString(Charset.defaultCharset()) + "";
        } catch (InvalidProtocolBufferException ex) {
            //ex.printStackTrace();
            System.err.println("Parse data failed.");
        }
        return result;
    }

    public static void main(String[] args) {
        final TCPConfigs configs = new TCPConfigs.Builder()
                .setHost("49.213.118.166")
                .setPort(11113)
                .setPayloadSize(1024 * 128 /* 128kb */)
                .setSocketTimeout(5000)
                .setReadTimeout(15000)
                .setWriteTimeout(15000)
                .build();

        final TCPClientProto client = new TCPClientProto(configs);
        client.setConnectionListener(new ConnectionObserver() {
            @Override
            public void onConnected() {
                System.out.println("Socket connected.");
                client.send(generateMessage());
            }

            @Override
            public void onDisconnected() {
                System.out.println("Socket disconnected");
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Socket error: \n" + t.toString());
            }
        });
        client.setDataListener(new DataListener() {
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

        final TimerTask cancel = new TimerTask() {
            @Override
            public void run() {
                client.disconnect();
            }
        };

        final Timer timer = new Timer();
        timer.schedule(cancel, 5 * 60 * 1000);
    }
}
