package net.marevalo.flowsmanager;

import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.util.XmppStringUtils;

/**
 * XMPP Connection Singleton
 *
 * This class will manage a single connection shared for all the activities
 */
public class XMPPConnectionManager {

    private static final String LOGTAG = "XMPPConnectionManager";

    // XMPP Connection Object from Smack
    static AbstractXMPPConnection connection = null;

    // XMPP Connection Configuration
    static String xmppUserName = "" ;
    static String xmppUserDomain = ""  ;
    static String xmppPassword = ""  ;

    // Configuration status for the singleton
    static Boolean isConfigured = false ;
    static Boolean isConnected = false ;

    public static Void setConfiguration ( String xmppUser , String xmppPassword ) {
        // TODO: validate xmppUser
        XMPPConnectionManager.isConfigured = true;
        XMPPConnectionManager.xmppPassword = xmppPassword ;
        XMPPConnectionManager.xmppUserName = XmppStringUtils.parseLocalpart( xmppUser ) ;
        XMPPConnectionManager.xmppUserDomain = XmppStringUtils.parseDomain( xmppUser ) ;
        return null;
    }

    public static AbstractXMPPConnection getConnection() {

        if ( XMPPConnectionManager.isConfigured == false ) {
            Log.w(LOGTAG, "Instating unconfigured connection" );
            return null;
        }
        if ( XMPPConnectionManager.isConnected == false ) {

            // Send the configuration
            // TODO: Yes I know, this is restrictive, DNS must be working
            XMPPTCPConnectionConfiguration.Builder configBuilder =
                    XMPPTCPConnectionConfiguration.builder();
            configBuilder.setUsernameAndPassword(
                    XMPPConnectionManager.xmppUserName,
                    XMPPConnectionManager.xmppPassword);
            configBuilder.setServiceName( XMPPConnectionManager.xmppUserDomain  );
            configBuilder.setResource( "FlowsManager" );

            XMPPConnectionManager.connection = new XMPPTCPConnection(configBuilder.build());

            try {
                // Create a connection to the XMPP server.
                XMPPConnectionManager.connection.connect();
                // Log into the server
                XMPPConnectionManager.connection.login();
            } catch (Exception ex) {
                Log.w(LOGTAG, "XMPP Connection error " + ex);
                return null ;
            }
            XMPPConnectionManager.isConnected = true ;

        }

        return XMPPConnectionManager.connection ;

    }

    static public Boolean isConfigured () {
        return XMPPConnectionManager.isConfigured;
    }
}
