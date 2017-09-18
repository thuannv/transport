package transport;

/**
 * @author thuannv
 * @since 09/18/2017
 */
public interface DataListener {
    void onReceived(byte[] data);
}
