
package transport.tcp.client;

/**
 *
 * @author thuannv
 * @since Sept. 06, 2017
 */
public class TCPConfigs {

    private final int mPort;

    private final String mHost;

    private final int mPayloadSize;

    private final int mSocketTimeout;

    private final int mReadTimeout;

    private final int mWriteTimeout;

    public TCPConfigs(String host, int port, int payloadSize,
            int socketTimeout, int readTimeout, int writeTimeout) {
        mHost = host;
        mPort = port;
        mPayloadSize = payloadSize;
        mSocketTimeout = socketTimeout;
        mReadTimeout = readTimeout;
        mWriteTimeout = writeTimeout;
    }

    public String getHost() {
        return mHost;
    }

    public int getPort() {
        return mPort;
    }

    public int getPayloadSize() {
        return mPayloadSize;
    }

    public int getReadTimeout() {
        return mReadTimeout;
    }

    public int getWriteTimeout() {
        return mWriteTimeout;
    }

    public int getSocketTimeout() {
        return mSocketTimeout;
    }

    /**
     *
     */
    public static final class Builder {

        private int mPort;

        private String mHost;

        private int mPayloadSize;

        private TCPConfigs mConfigs;

        private int mSocketTimeout;

        private int mReadTimeout;

        private int mWriteTimeout;

        public Builder setPort(int port) {
            mPort = port;
            return this;
        }

        public Builder setHost(String host) {
            mHost = host;
            return this;
        }

        public Builder setPayloadSize(int size) {
            mPayloadSize = size;
            return this;
        }

        public Builder setSocketTimout(int timeout) {
            mSocketTimeout = timeout;
            return this;
        }

        public Builder setReadTimout(int timeout) {
            mReadTimeout = timeout;
            return this;
        }

        public Builder setWriteTimout(int timeout) {
            mWriteTimeout = timeout;
            return this;
        }

        public String getHost() {
            return mHost;
        }

        public int getPort() {
            return mPort;
        }

        public int getPayloadSize() {
            return mPayloadSize;
        }

        public int getReadTimeout() {
            return mReadTimeout;
        }

        public int getWriteTimeout() {
            return mWriteTimeout;
        }

        public int getSocketTimeout() {
            return mSocketTimeout;
        }

        public TCPConfigs build() {
            if (mConfigs == null) {
                mConfigs = new TCPConfigs(mHost, mPort, mPayloadSize,
                        mSocketTimeout, mReadTimeout, mWriteTimeout);
            }
            return mConfigs;
        }
    }
}
