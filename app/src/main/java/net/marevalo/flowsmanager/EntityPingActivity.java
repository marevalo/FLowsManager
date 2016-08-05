package net.marevalo.flowsmanager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smackx.ping.PingManager;

import java.util.Iterator;
import java.util.List;

public class EntityPingActivity extends ActionBarActivity {
    private Entity targetEntity ;
    private static final String LOGTAG = "EntityPingActivity" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        this.targetEntity = intent.getParcelableExtra("entity");

        this.setTitle( "Ping " + this.targetEntity.getDisplayName() );

        setContentView(R.layout.activity_entity_ping);

        // Add click listener for the Ping Button
        Button pgButton = (Button) findViewById(R.id.pg_button);
        pgButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new PingEntity().execute( EntityPingActivity.this.targetEntity.getJid() );

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class PingEntity extends AsyncTask<String, Void, Boolean> {

        private PingManager pingManager;
        private String errorMessage = "Got no Pong.";

        // This happens on another thread
        protected Boolean doInBackground(String... jid) {

            // Get the XMMPConnection from the manager
            AbstractXMPPConnection conn = XMPPConnectionManager.getConnection();
            // Obtain the PingManager associated with my XMPP connection
            this.pingManager = PingManager.getInstanceFor(conn);
            // I do not want background pings but a single foreground ping
            pingManager.setPingInterval( -1 );

            // Do the ping
            try {
                return pingManager.ping( jid[0] );
            } catch (Exception ex) {
                Log.w(LOGTAG, "XMPP error " + ex);
                this.errorMessage = "XMPP error:" + ex ;
                return false ;
            }

        }

        // This happens on the thread UI
        protected void onPostExecute( Boolean pingOK ) {
            // If got no info try to exit the activity
            if (pingOK) {
                Toast toast = Toast.makeText(
                        getApplicationContext(),
                        "Pong!",
                        Toast.LENGTH_SHORT );
                toast.show();
            } else {
                Toast toast = Toast.makeText(
                        getApplicationContext(),
                        this.errorMessage ,
                        Toast.LENGTH_LONG );
                toast.show();

            }

        }
    }

}
