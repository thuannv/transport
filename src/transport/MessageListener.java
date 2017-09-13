/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transport;

/**
 *
 * @author steven
 */
public interface MessageListener {
    void onMessage(ZLive.ZAPIMessage message);
}
