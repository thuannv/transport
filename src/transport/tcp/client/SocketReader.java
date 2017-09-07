
package transport.tcp.client;

import java.io.BufferedInputStream;
import java.net.Socket;
import java.util.Arrays;
import transport.DataListener;

/**
 *
 * @author thuannv
 * @since Sept. 06, 2017
 */
public class SocketReader extends Thread {

    private DataListener mListener;

    private volatile boolean mIsRunning = false;

    private final Socket mSocket;
    
    private final TCPConfigs mConfigs;

    public SocketReader(Socket socket, TCPConfigs configs) {
        mSocket = socket;
        mConfigs = configs;
    }

    public void stopReader() {
        mIsRunning = false;
    }

    public void setListener(DataListener listener) {
        mListener = listener;
    }

    @Override
    public void run() {
        mIsRunning = true;
        int read = 0;
        final int size = mConfigs.getPayloadSize();
        final byte[] data = new byte[size];
        try (BufferedInputStream in = new BufferedInputStream(mSocket.getInputStream())) {
            while (mIsRunning) {
                try {
                    read = in.read(data, 0, size);
                    if (read > 0) {
                        if (mListener != null) {
                            mListener.onReceived(Arrays.copyOfRange(data, 0, read));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mIsRunning = false;
    }

}
