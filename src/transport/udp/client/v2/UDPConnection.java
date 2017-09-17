package transport.udp.client.v2;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author thuannv
 * @since 09/17/2017
 */
public final class UDPConnection {

    private final UDPClient mClient;

    private final AtomicBoolean mIsConnecting = new AtomicBoolean(false);

    private final AtomicBoolean mIsDisconnecting = new AtomicBoolean(false);

    UDPConnection(UDPClient client) {
        mClient = client;
    }

    public boolean isConnected() {
        return mClient != null && mClient.isReady();
    }

    public boolean isConnecting() {
        return mIsConnecting.get() || (mClient != null && mClient.isStarting());
    }

    public void connect() {
        if (mIsConnecting.compareAndSet(false, true)) {
            new Connector().start();
        }
    }

    public void disconnect() {
        if (mIsDisconnecting.compareAndSet(false, true)) {
            new Disconnector().start();
        }
    }

    public boolean isDisconnected() {
        return !isConnected();
    }

    /**
     * {@link Connector}
     */
    private final class Connector extends Thread {
        @Override
        public void run() {
            try {
                mClient.start();
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            }
            mIsConnecting.set(false);
        }
    }

    /**
     * {@link Disconnector}
     */
    private final class Disconnector extends Thread {

        @Override
        public void run() {
            if (mClient != null && mClient.isReady() && !mClient.isStopping()) {
                mClient.stop();
            }
            mIsDisconnecting.set(false);
        }
    }
}
