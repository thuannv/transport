package transport.udp.client.v2;

import transport.DataListener;
import transport.ErrorResolver;
import transport.MessageHelper;
import transport.ZLive;
import transport.udp.client.UDPConfigs;
import transport.utils.TextUtils;
import transport.utils.ThreadUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author thuannv
 * @since 09/18/2017
 */
public final class UDPConnectionManager {

    private static final int UDP_HANDSHAKE = 300;

    private static final int UDP_PING = 301;

    private static final int UDP_PONG = 302;

    private static final int UDP_CLIENT_CLOSED = 303;

    private static final int SUB_UDP_HANDSHAKE_SUCCESS = 300;

    private static final int SUB_UDP_HANDSHAKE_FAILURE = 301;

    private UDPConfigs mConfigs;

    private UDPConnection mConnection;

    private volatile boolean mIsReady = false;

    private Timer mPingScheduler;

    private Timer mCheckingHandshakeTimer;

    private final Set<DataListener> mListeners = Collections.synchronizedSet(new HashSet<DataListener>());

    private final UDPConnection.ConnectionListener mConnectionListener = new UDPConnection.ConnectionListener() {
        @Override
        public void onConnected() {
            System.out.println("UDPConnectionManager.onConnected()");
            startHandshake();
        }

        @Override
        public void onError(Throwable t) {
            System.err.println("UDPConnectionManager.onError() -> e=" + t.toString());
        }

        @Override
        public void onDisconnected() {
            System.out.println("UDPConnectionManager.onDisconnected()");
            stopPingScheduler();
        }

        @Override
        public void onMessage(byte[] data) {
            if (mIsReady) {
                if (!isPongMessage(data)) {
                    notifyListeners(data);
                }
            } else {
                processHandshake(data);
            }
        }
    };

    private void startHandshake() {
        mConnection.send(MessageHelper.createProtoMessage(UDP_HANDSHAKE));
        startCheckingHandshake();
    }

    private void startCheckingHandshake() {
        if (mCheckingHandshakeTimer == null) {
            mCheckingHandshakeTimer = new Timer();
            mCheckingHandshakeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("Handshake timeout.");
                }
            }, 5000);
        }
    }

    private void stopCheckingHandshake() {
        if (mCheckingHandshakeTimer != null) {
            mCheckingHandshakeTimer.cancel();
            mCheckingHandshakeTimer = null;
        }
    }

    private void onHandshakeSuccess() {
        System.out.println("Handshake succeeded.");
        mIsReady = true;
        startPingScheduler();
    }

    private void onHandshakeFailure(int errorCode, String reason) {
        System.out.format("Handshake failed. errorCode=%d, reason=%s\n", errorCode, reason);
    }

    private void processHandshake(byte[] data) {
        try {
            ZLive.ZAPIMessage message = ZLive.ZAPIMessage.parseFrom(data);
            if (message.getCmd() == UDP_HANDSHAKE) {
                stopCheckingHandshake();
                int subCommand = message.getSubCmd();
                if (subCommand == SUB_UDP_HANDSHAKE_SUCCESS) {
                    onHandshakeSuccess();
                } else if (subCommand == SUB_UDP_HANDSHAKE_FAILURE) {
                    int errorCode = -1;
                    try {
                        errorCode = ByteBuffer.wrap(message.getData().toByteArray()).getInt();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    onHandshakeFailure(errorCode, ErrorResolver.resolve(errorCode));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPingScheduler() {
        if (mPingScheduler == null) {
            System.out.println("Starting ping scheduler...");
            mPingScheduler = new Timer();
            mPingScheduler.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isReady()) {
                        System.out.println("Ping to server.");
                        mConnection.send(MessageHelper.createProtoMessage(UDP_PING));
                    }
                }
            }, 15000, 15000);
        }
    }

    private void stopPingScheduler() {
        if (mPingScheduler != null) {
            mPingScheduler.cancel();
            mPingScheduler = null;
        }
    }

    private boolean isPongMessage(byte[] data) {
        try {
            ZLive.ZAPIMessage msg = ZLive.ZAPIMessage.parseFrom(data);
            return msg.getCmd() == UDP_PONG;
        } catch (Exception e) {
        }
        return false;
    }

    public void init(UDPConfigs configs) {
        mConfigs = configs;
    }

    public synchronized void connect() {
        if (mConnection == null) {
            mConnection = new UDPConnection(mConfigs);
            mConnection.setConnectionListener(mConnectionListener);
        }
        mConnection.connect();
    }

    public synchronized void disconnect() {
        if (mConnection != null) {
            mIsReady = false;
            mConnection.disconnect();
            mConnection = null;
        }
    }

    public synchronized boolean isConnected() {
        return mConnection != null && mConnection.isConnected();
    }

    public synchronized boolean isConnecting() {
        return mConnection != null && mConnection.isConnecting();
    }

    public boolean isReady() {
        return mIsReady;
    }

    public void addListener(DataListener listener) {
        if (listener != null) {
            mListeners.add(listener);
        }
    }

    public void removeListener(DataListener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
    }

    public void notifyListeners(byte[] data) {
        for (DataListener listener : mListeners) {
            listener.onReceived(data);
        }
    }

    public void send(byte[] data) {
        if (mConnection != null) {
            mConnection.send(data);
        }
    }


    static DataListener dummyListener() {

        return new DataListener() {

            int i = 0;

            @Override
            public void onReceived(byte[] data) {
                ZLive.ZAPIMessage message = null;
                try {
                    System.out.println("onReceived()");
                    message = ZLive.ZAPIMessage.parseFrom(data);
                    String sdata = message.getData().toString(Charset.defaultCharset());
                    if (!TextUtils.isEmpty(sdata)) {
                        System.out.format("%d. %s\n", i++, sdata);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static void main(String[] args) throws InterruptedException {
        UDPConfigs configs = new UDPConfigs("49.213.118.166", 11114, 4096, 5000, true);
        DataListener dataListener = dummyListener();
        UDPConnectionManager manager = new UDPConnectionManager();
        manager.init(configs);
        manager.addListener(dataListener);
        manager.connect();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!manager.isReady()) {
                    ThreadUtils.sleep(100);
                }

                for (int i = 0; i < 10; i++) {
                    ThreadUtils.sleep(200);
                    //System.out.println("Sending message...");
                    manager.send(MessageHelper.createProtoMessage(1000));
                }
            }
        });

        t.start();
        t.join();

        manager.disconnect();
        manager.removeListener(dataListener);
    }
}
