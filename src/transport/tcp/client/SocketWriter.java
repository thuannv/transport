
package transport.tcp.client;

import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.LinkedList;

/**
 *
 * @author thuannv
 * @since Sept. 06, 2017
 */
public class SocketWriter extends Thread {

    private final Object mLock = new Object();

    private volatile boolean mIsRunning = false;

    private final LinkedList<byte[]> mQueue = new LinkedList<>();

    private final Socket mSocket;

    public SocketWriter(Socket socket) {
        mSocket = socket;
    }

    public void stopWriter() {
        mIsRunning = false;
        synchronized (mLock) {
            mLock.notifyAll();
        }
    }

    public void write(byte[] data) {
        if (data == null || data.length <= 0) {
            return;
        }

        synchronized (mLock) {
            if (mIsRunning) {
                mQueue.addFirst(data);
                mLock.notifyAll();
            }
        }
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    @Override
    public void run() {
        mIsRunning = true;

        byte[] data;
        try (BufferedOutputStream writer = new BufferedOutputStream(mSocket.getOutputStream())) {
            while (mIsRunning) {
                synchronized (mLock) {
                    while (mIsRunning && mQueue.isEmpty()) {
                        try {
                            mLock.wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                    if (!mQueue.isEmpty()) {
                        System.out.println("Get message from queue.");
                        data = mQueue.removeLast();
                        try {
                            System.out.println("Sending...");
                            writer.write(data);
                            writer.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mIsRunning = false;
    }

}
