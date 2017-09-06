/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transport.tcp.client;

/**
 *
 * @author steven
 */
public interface ConnectionObserver {
    void onConnected();
    void onDisconnected();
    void onError(Throwable t);
}
