package transport.udp.client.v2;

import transport.IoProcessor;
import transport.udp.client.UDPConfigs;

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

    private ConnectionListener mListener;

    UDPConnection(UDPConfigs configs) {
        mClient = new UDPClient(configs);
        mClient.setProcessor(new IoProcessor() {
            @Override
            public void process(byte[] data) {
                if (mListener != null) {
                    mListener.onMessage(data);
                }
            }
        });
    }

    private void onConnected() {
        System.out.println("UDPConnection: onConnected()");
        if (mListener != null) {
            mListener.onConnected();
        }
    }

    private void onDisconnected() {
        System.out.println("UDPConnection: onDisconnected()");
        if (mListener != null) {
            mListener.onDisconnected();
        }
    }

    private void onError(Throwable t) {
        System.out.println(t.toString());
        if (mListener != null) {
            mListener.onError(t);
        }
    }

    public boolean isConnected() {
        return mClient.isReady();
    }

    public boolean isConnecting() {
        return mIsConnecting.get() || mClient.isStarting();
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

    public void setConnectionListener(ConnectionListener listener) {
        mListener = listener;
    }

    public void send(byte[] data) {
        mClient.send(data);
    }

    /**
     * {@link Connector}
     */
    private final class Connector extends Thread {
        @Override
        public void run() {
            try {
                mClient.start();
                onConnected();
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
                onError(e);
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
                onDisconnected();
            }
            mIsDisconnecting.set(false);
        }
    }

    /**
     * {@link ConnectionListener}
     */
    public interface ConnectionListener {
        void onConnected();

        void onError(Throwable t);

        void onDisconnected();

        void onMessage(byte[] data);
    }
}
