
package transport.utils;

/**
 *
 * @author thuannv
 * @since 15/09/2017
 */
public final class ThreadUtils {
    
    private ThreadUtils() {}
    
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
        }
    }
    
    public static void sleep(long millis, int nanos) {
        try {
            Thread.sleep(millis, nanos);
        } catch (InterruptedException ex) {
        }
    }
}
