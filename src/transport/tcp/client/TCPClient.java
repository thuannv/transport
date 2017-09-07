package transport.tcp.client;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import transport.DataListener;

/**
 *
 * @author thuannv
 * @since Sept. 06, 2017
 */
public class TCPClient {

    private final Object mSocketLock = new Object();

    private final TCPConfigs mConfigs;

    private Socket mSocket;

    private ConnectionObserver mConnectionObserver;

    private DataListener mDataListener;

    private SocketReader mReader;

    private SocketWriter mWriter;

    public TCPClient(TCPConfigs configs) {
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
            mSocket.setSoTimeout(mConfigs.getSocketTimeout());
            createWriter();
            createReader();
            onConnected();
        } catch (Exception ex) {
            onError(ex);
        }
    }

    public static void main(String[] args) {
        final TCPConfigs configs = new TCPConfigs.Builder()
                .setHost("localhost")
                .setPort(2017)
                .setPayloadSize(1024 * 128 /* 128kb */)
                .setSocketTimeout(5000)
                .setReadTimeout(15000)
                .setWriteTimeout(15000)
                .build();

        final TCPClient client = new TCPClient(configs);
        client.setConnectionListener(new ConnectionObserver() {
            @Override
            public void onConnected() {
                System.out.println("Socket connected.");
                client.send("Client is connected".getBytes());
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
                String msg = new String(data);
                System.out.println("Received message from server: " + msg);
                System.out.println("Send new messaage to server");
                client.send(("Timestamp message ts=" + System.currentTimeMillis()).getBytes());
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
        timer.schedule(cancel, 60000);
    }
}
