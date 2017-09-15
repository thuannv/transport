package transport.udp.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import transport.Address;
import transport.Configs;
import transport.ErrorResolver;
import transport.IoProcessor;
import transport.MessageHelper;
import transport.MessageListener;
import transport.ZLive;
import transport.utils.TextUtils;
import transport.utils.ThreadUtils;

/**
 *
 * @author thuannv
 * @since 11/09/2017
 */
public final class UDPManager {

    private static final int UDP_HANDSHAKE = 300;

    private static final int UDP_PING = 301;

    private static final int UDP_PONG = 302;

    private static final int UDP_CLIEN_CLOSED = 303;

    private static final int SUB_UDP_HANDSHAKE_SUCCESS = 300;

    private static final int SUB_UDP_HANDSHAKE_FAILURE = 301;

    private final long HANDSHAKE_TIMEOUT_MILLIS = 5000; // 5 seconds

    private static final boolean LOCAL = false;

    private static volatile UDPManager sInstance = null;

    private volatile boolean mIsInitializing = false;

    private Configs mConfigs;

    private UDPClient mClient;

    private UDPConnector mConnector;

    private MessageListener mListener;

    private int mServerIndex = 0;

    private volatile boolean mIsReady = false;

    private volatile Timer mPingScheduler;

    private Timer mHandShakeScheduler;

    private long mLastPong = 0;

    private int mRetryCount = 0;

    public static UDPManager getsInstance() {
        UDPManager localInstance = sInstance;
        if (localInstance == null) {
            synchronized (UDPManager.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    localInstance = sInstance = new UDPManager();
                }
            }
        }
        return localInstance;
    }

    public void init(Configs configs) {
        if (configs == null) {
            throw new IllegalArgumentException("configs must NOT be null.");
        }
        System.out.println("Initializing UDPManager...");

        mConfigs = configs;
        mServerIndex = 0;
    }

    public void startClient() {
        if (mIsInitializing || mIsReady) {
            return;
        }
        connect();
    }

    public void stopClient() {
        if (mIsReady || mIsInitializing) {
            mIsInitializing = false;
            mIsReady = false;
            mServerIndex = 0;
            stopCheckingHandshake();
            stopPingScheduler();
            resetConnector();
            resetClient();
        }
    }

    private synchronized void connect() {
        mIsInitializing = true;
        final Address server = mConfigs.getServer(mServerIndex);
        final UDPConfigs udpConfigs = new UDPConfigs(server.getHost(), server.getPort(), 64 * 1024, 15000);
        System.out.format("Connect to: %s\n", server.toString());
        mConnector = new UDPConnector(udpConfigs);
        mConnector.start();
    }

    private void resetConnector() {
        if (mConnector != null) {
            if (mConnector.isAlive() && !mConnector.isInterrupted()) {
                mConnector.interrupt();
            }
            mConnector = null;
        }
    }

    private void tryConnectToNextServer() {
        System.out.println("Try connecting to next server...");

        mIsReady = false;

        resetConnector();

        if (++mServerIndex == mConfigs.getServerCount()) {
            System.out.println("No more servers to try.");
            mServerIndex = 0;
            stopPingScheduler();
            resetClient();
        } else {
            connect();
        }
    }

    public void startHandshake() {
        if (mClient != null) {
            mClient.send(MessageHelper.createProtoMessage(UDP_HANDSHAKE));
            startCheckingHandshake();
        }
    }

    private synchronized void startCheckingHandshake() {
        if (mHandShakeScheduler == null) {
            mHandShakeScheduler = new Timer();
            mHandShakeScheduler.schedule(new TimerTask() {
                @Override
                public void run() {
                    stopCheckingHandshake();
                    onHandshakeTimeout();
                }
            }, HANDSHAKE_TIMEOUT_MILLIS);
        }
    }

    private synchronized void stopCheckingHandshake() {
        if (mHandShakeScheduler != null) {
            mHandShakeScheduler.cancel();
            mHandShakeScheduler = null;
        }
    }

    private boolean processHanshake(ZLive.ZAPIMessage message) {
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
                onHandshakeFailed(errorCode, ErrorResolver.resolve(errorCode));
            }
            return true;
        }

        return false;
    }

    private void onHandshakeTimeout() {
        System.out.println("Hanshake timeout.");
        tryConnectToNextServer();
    }

    private void onHandshakeFailed(int code, String reason) {
        System.out.format("Hanshake failed code=%d, reason=%s\n", code, reason);
        if (ErrorResolver.isServerError(code)) {
            tryConnectToNextServer();
        }
    }

    private void onHandshakeSuccess() {
        System.out.println("Hanshake success");
        mIsReady = true;
        startPingScheduler();
    }

    private void ping() {
        final long time = System.currentTimeMillis() - mLastPong;
        final int pingTime = mConfigs.getPingTime();
        final int maxRetry = mConfigs.getRetryCount();
        if (time > pingTime) {
            ++mRetryCount;
        }
        if (mRetryCount <= maxRetry) {
            System.out.format("Ping retry count: %d\n", mRetryCount);
            sendPing();
            scheduleNextPing();
        } else {
            mRetryCount = 0;
            mLastPong = 0;
            tryConnectToNextServer();
        }
    }

    public boolean processPong(ZLive.ZAPIMessage message) {
        if (message != null && message.getCmd() == UDP_PONG) {
            mRetryCount = 0;
            mLastPong = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public void startPingScheduler() {
        if (mPingScheduler == null) {
            mPingScheduler = new Timer();
            scheduleNextPing();
        }
    }

    private void scheduleNextPing() {
        if (mPingScheduler != null) {
            mPingScheduler.schedule(new TimerTask() {
                @Override
                public void run() {
                    ping();
                }
            }, mConfigs.getPingTime());
        }
    }

    public void stopPingScheduler() {
        if (mPingScheduler != null) {
            mPingScheduler.cancel();
            mPingScheduler = null;
        }
    }

    public void setListener(MessageListener listener) {
        mListener = listener;
    }

    private void notifyListener(ZLive.ZAPIMessage message) {
        if (mListener != null) {
            mListener.onMessage(message);
        }
    }

    public synchronized void send(byte[] data) {
        if (mIsReady && mClient != null) {
            mClient.send(data);
        }
    }

    private void sendPing() {
        byte[] data = ZLive.ZAPIMessage.newBuilder()
                .setCmd(UDP_PING)
                .build()
                .toByteArray();
        send(data);
        System.out.println("Sent ping.");
    }

    private synchronized void resetClient() {
        System.out.println("Stopping client...");
        if (mClient != null) {
            mClient.setProcessor(null);
            mClient.stop();
            mClient = null;
        }
    }

    private synchronized void createClient(UDPConfigs configs) throws IOException {
        mClient = new UDPClient(configs);
        mClient.setProcessor(new InternalProcessor());
        mClient.start();
    }

    /**
     *
     */
    private final class InternalProcessor implements IoProcessor {

        @Override
        public void process(byte[] data) {
            ZLive.ZAPIMessage message = null;
            try {
                message = ZLive.ZAPIMessage.parseFrom(data);
                if (mIsReady) {
                    boolean isPongMessage = processPong(message);
                    if (!isPongMessage) {
                        notifyListener(message);
                    }
                } else {
                    processHanshake(message);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     */
    private final class UDPConnector extends Thread {

        private final UDPConfigs mUDPConfigs;

        public UDPConnector(UDPConfigs configs) {
            mUDPConfigs = configs;
        }

        private void onError(Throwable t) {
            mIsInitializing = false;
            System.out.format("onError()/Error=%s\n", t.toString());
        }

        private void onStarted() {
            System.out.println("Starting handshake...");
            mIsInitializing = false;
            startHandshake();
        }

        @Override
        public void run() {
            try {
                resetClient();
                createClient(mUDPConfigs);
                onStarted();
            } catch (IOException ex) {
                onError(ex);
            }
        }
    }

    static Configs getConfigs() {
        int pingTime;
        final List<Address> servers = new ArrayList<>();
        if (LOCAL) {
            pingTime = 10000;
            servers.add(new Address("127.0.0.1", 3333));
            servers.add(new Address("localhost", 4444));
        } else {
            pingTime = 15000;
            servers.add(new Address("49.213.118.166", 11114));
        }
        return new Configs(servers, pingTime, 10);
    }
    
    static void testUdpManager(Configs configs) {
        UDPManager.getsInstance().init(configs);
        UDPManager.getsInstance().startClient();
        UDPManager.getsInstance().setListener(new MessageListener() {
            int i = 1;

            @Override
            public void onMessage(ZLive.ZAPIMessage message) {
                String data = message.getData().toString(Charset.defaultCharset());
                if (!TextUtils.isEmpty(data)) {
                    System.out.format("%d. %s\n", i++, data);
                }
            }
        });

        ThreadUtils.sleep(5000);
        
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    ThreadUtils.sleep(200);
                    UDPManager.getsInstance().send(MessageHelper.createProtoMessage(10000));
                }
                ThreadUtils.sleep(2000);
                UDPManager.getsInstance().stopClient();

            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException ex) {
        }
    }
    
    public static void main(String[] args) {
        Configs configs = getConfigs();
        testUdpManager(configs);
        testUdpManager(configs);
        testUdpManager(configs);
    }
}
