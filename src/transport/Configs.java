
package transport;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thuannv
 */
public final class Configs {
    
    private final int mPingTime;
    
    private final int mRetryConnect;
    
    private final List<Address> mServers;
    
    public Configs(List<Address> servers, int pingTime, int retryCount) {
        if (servers == null || servers.isEmpty()) {
            throw new IllegalArgumentException("servers must NOT be empty.");
        }
        
        mServers = new ArrayList<>(servers);
        mPingTime = pingTime;
        mRetryConnect = retryCount;
    }
    
    public Address getServer(int index) {
        return mServers.get(index);
    }
    
    public int getServerCount() {
        return mServers.size();
    }
    
    public int getPingTime() {
        return mPingTime;
    }
    
    public int getRetryCount() {
        return mRetryConnect;
    }
}
