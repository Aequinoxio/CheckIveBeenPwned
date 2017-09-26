/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package checkivebeenpwned;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author GLLGRL68H26H282H
 */
public class NetworkProxy {
    private String host;
    private int port;
    private Proxy proxy;

    public void init() {
        System.setProperty("java.net.useSystemProxies", "true");
        proxy = getProxy();
        if (proxy != null && proxy.type() != Type.DIRECT ) {
            InetSocketAddress addr = (InetSocketAddress) proxy.address();
            host = addr.getHostName();
            port = addr.getPort();

            System.setProperty("java.net.useSystemProxies", "false");
            System.setProperty("http.proxyHost", host);
            System.setProperty("http.proxyPort", "" + port);

        }
        System.setProperty("java.net.useSystemProxies", "false");
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    private Proxy getProxy() {
        List<Proxy> l = null;
        try {
            ProxySelector def = ProxySelector.getDefault();

            l = def.select(new URI("https://www.google.com"));
            ProxySelector.setDefault(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (l != null) {
            for (Iterator<Proxy> iter = l.iterator(); iter.hasNext();) {
                java.net.Proxy proxy = iter.next();
                return proxy;
            }
        }
        return null;
    }
    
    public Proxy getNetworkProxy(){
        return proxy;
    }
}
