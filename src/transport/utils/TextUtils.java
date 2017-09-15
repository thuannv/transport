
package transport.utils;

/**
 *
 * @author thuannv
 * @since 15/09/2017
 */
public final class TextUtils {
    
    private TextUtils() {}
    
    public static boolean isEmpty(CharSequence text) {
        return null == text || text.length() == 0;
    }
}
