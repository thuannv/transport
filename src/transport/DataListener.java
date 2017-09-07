package transport;

/**
 *
 * @author thuannv
 * @since Sept. 06, 2017
 */
public interface DataListener {

    void onReceived(byte[] data);
}
