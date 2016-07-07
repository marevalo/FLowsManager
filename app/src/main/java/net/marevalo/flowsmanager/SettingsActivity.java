package net.marevalo.flowsmanager;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jxmpp.util.XmppStringUtils;

import java.util.Set;

public class SettingsActivity extends ActionBarActivity {

    private static final String LOGTAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOGTAG , "Settings started" );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    protected void onResume() {
        SharedPreferences sharedPref;
        String user="";
        String password;

        super.onResume();
        // The activity has become visible (it is now "resumed").
        // Get the initial configuration
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        user     = sharedPref.getString( "xmpp_user"     , "" );
        password = sharedPref.getString( "xmpp_password" , "" );

        TextView userTV = (TextView) this.findViewById(R.id.userEditText );
        TextView passwordTV = (TextView) this.findViewById(R.id.passwordEditText);
        TextView messageTV = (TextView) this.findViewById(R.id.message_text_view );

        userTV.setText( user );
        passwordTV.setText( password );
        messageTV.setText( "Connection not tested" );

        // Add click listener for the Test Button
        Button testButton = (Button) findViewById(R.id.test_button);
        testButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TextView userTV = (TextView) SettingsActivity.this.findViewById(R.id.userEditText );
                TextView passwordTV = (TextView) SettingsActivity.this.findViewById(R.id.passwordEditText);

                String user = userTV.getText().toString();
                String password = passwordTV.getText().toString();

                new TestConnectionTask().execute(user, password);
            }
        });


        // Add click listener for the Ok Button
        Button okButton = (Button) findViewById(R.id.ok_button);
        okButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref;

                sharedPref = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                TextView userTV = (TextView) SettingsActivity.this.findViewById(R.id.userEditText );
                TextView passwordTV = (TextView) SettingsActivity.this.findViewById(R.id.passwordEditText);

                String user = userTV.getText().toString();
                String password = passwordTV.getText().toString();

                SharedPreferences.Editor prefedit = sharedPref.edit();
                prefedit
                    .putString("xmpp_password", password )
                    .putString("xmpp_user", user )
                    .commit();

                SettingsActivity.this.onBackPressed();
            }
        });

    }

    // Connection testing task
    private class TestConnectionTask extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... params) {

            String user = params[0];
            String password =  params[1];
            Log.d(LOGTAG , "Testing: " + user + " , " + password );
            XMPPConnectionManager.setConfiguration(user, password);
            return XMPPConnectionManager.getConnection() != null ;
        }

        protected void onPostExecute(Boolean result) {
            TextView messageTV = (TextView) findViewById(R.id.message_text_view );
            Button okButton = (Button) findViewById(R.id.ok_button);
            if ( result ) {
                messageTV.setText("Connection OK");
            } else {
                messageTV.setText("Connection KO");
            }
            // De/Activate OK button
            okButton.setEnabled(result);
        }
    }

}
