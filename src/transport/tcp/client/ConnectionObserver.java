
package transport.tcp.client;

/**
 *
 * @author thuannv
 * @since Sept. 06, 2017
 */
public interface ConnectionObserver {
    void onConnected();
    void onDisconnected();
    void onError(Throwable t);
}
