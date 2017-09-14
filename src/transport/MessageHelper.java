/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transport;

import com.google.protobuf.ByteString;
import java.nio.charset.Charset;

/**
 *
 * @author steven
 */
public final class MessageHelper {
    
    private MessageHelper() {}
    
    public static byte[] createProtoMessage(int cmd) {
        ZLive.ZAPIMessage.Builder builder = ZLive.ZAPIMessage.newBuilder();
        builder.setCmd(cmd);
        builder.setDeviceId("steven-pc@vng.com.vn");
        builder.setData(ByteString.copyFrom("a055d349b59c759ec03a163d9e91907a95d565ef66e7dff9", Charset.defaultCharset()));
        ZLive.ZAPIMessage data = builder.build();
        return data.toByteArray();
    }
}
