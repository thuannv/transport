/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transport.tcp.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 *
 * @author steven
 */
public class TCPHandler {

    private final Executor mWorker = Executors.newFixedThreadPool(50);

    public void handle(Socket connection) {
        mWorker.execute(new TCPHandlerJob(connection));
    }

    private static final class TCPHandlerJob implements Runnable {

        private final Socket mConnection;

        public TCPHandlerJob(Socket connection) {
            mConnection = connection;
        }

        @Override
        public void run() {
            try {
                int read;
                final int size = 1024;
                final byte[] buffer = new byte[size];
                BufferedInputStream reader = new BufferedInputStream(mConnection.getInputStream());
                BufferedOutputStream writer = new BufferedOutputStream(mConnection.getOutputStream());
                while (true) {
                    read = reader.read(buffer, 0, size);
                    if (read > 0) {
                        String msg = String.format("Received: %s", new String(buffer, 0, read));
                        System.out.println(msg);
                        byte[] response = msg.getBytes();
                        try {
                            writer.write(response, 0, response.length);
                            writer.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    mConnection.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}
