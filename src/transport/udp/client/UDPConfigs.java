
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

    public UDPConfigs(String host, int port, int bufferSize) {
        mHost = host;
        mPort = port;
        mBufferSize = bufferSize;
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

    /**
     *
     */
    public static class Builder {

        private int mPort;

        private String mHost;

        private int mBufferSize;

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

        public int getPort() {
            return mPort;
        }

        public String getHost() {
            return mHost;
        }

        public int getBufferSize() {
            return mBufferSize;
        }

        public UDPConfigs build() {
            if (mConfigs == null) {
                mConfigs = new UDPConfigs(mHost, mPort, mBufferSize);
            }
            return mConfigs;
        }

    }
}
