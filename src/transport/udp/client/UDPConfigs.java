
package transport.udp.client;

/**
 *
 * @author thuannv
 * @since Sept. 06, 2017
 */
public final class UDPConfigs {

    private final int mPort;

    private final String mHost;

    private final int mBufferSize;
    
    private final int mSocketTimeout;

    private final boolean mIsReuseAddress;

    public UDPConfigs(String host, int port, int bufferSize, int socketTimeout, boolean isReuseAddress) {
        mHost = host;
        mPort = port;
        mBufferSize = bufferSize;
        mSocketTimeout = socketTimeout;
        mIsReuseAddress = isReuseAddress;
    }

    public int getPort() {
        return mPort;
    }

    public String getHost() {
        return mHost;
    }

    public int getBufferSize() {
        return mBufferSize;
    }

    public int getSocketTimeout() {
        return mSocketTimeout;
    }

    public boolean isReuseAddress() {
        return mIsReuseAddress;
    }

    /**
     *
     */
    public static class Builder {

        private int mPort;

        private String mHost;

        private int mBufferSize;
        
        private int mSocketTimeout;

        private boolean mIsReuseAddress;

        private UDPConfigs mConfigs;

        public Builder setHost(String host) {
            mHost = host;
            return this;
        }

        public Builder setPort(int port) {
            mPort = port;
            return this;
        }

        public Builder setBufferSize(int bufferSize) {
            mBufferSize = bufferSize;
            return this;
        }
        
        public Builder setSocketTimeout(int timeout) {
            mSocketTimeout = timeout;
            return this;
        }
        
        public int getPort() {
            return mPort;
        }

        public String getHost() {
            return mHost;
        }

        public int getBufferSize() {
            return mBufferSize;
        }

        public int getSocketTimeout() {
            return mSocketTimeout;
        }

        public boolean isReuseAddress() {
            return mIsReuseAddress;
        }

        public void setReuseAddress(boolean isReuseAddress) {
            mIsReuseAddress = isReuseAddress;
        }

        public UDPConfigs build() {
            if (mConfigs == null) {
                mConfigs = new UDPConfigs(mHost, mPort, mBufferSize, mSocketTimeout, mIsReuseAddress);
            }
            return mConfigs;
        }

    }
}
