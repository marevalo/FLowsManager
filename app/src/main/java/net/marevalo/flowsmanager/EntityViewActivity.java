package net.marevalo.flowsmanager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;

import java.util.Iterator;
import java.util.List;

public class EntityViewActivity extends ActionBarActivity {

    private static final String LOGTAG = "EntityViewActivity";
    private Entity myEntity;
    private DiscoverInfo myLeafInfo ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        Intent intent = getIntent();
        this.myEntity = intent.getParcelableExtra("entity");

        this.setTitle(this.myEntity.getDisplayName());
        new GetInfoAndPopulateViewsTask().execute(this);
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

    class GetInfoAndPopulateViewsTask extends AsyncTask<EntityViewActivity, Void, EntityViewActivity> {

        private ServiceDiscoveryManager discoManager;
        private String errorMessage = "";

        // This happens on another thread
        protected EntityViewActivity doInBackground(EntityViewActivity... activity) {

            // Get the XMMPConnection from the manager
            AbstractXMPPConnection conn = XMPPConnectionManager.getConnection();
            // Obtain the ServiceDiscoveryManager associated with my XMPP connection
            this.discoManager = ServiceDiscoveryManager.getInstanceFor(conn);
            Entity entity = activity[0].myEntity;
            try {
                if ( entity.getNode() != null && entity.getNode() != "" ) {
                    activity[0].myLeafInfo = discoManager.discoverInfo( entity.getJid() , entity.getNode() );
                 } else {
                    activity[0].myLeafInfo = discoManager.discoverInfo( entity.getJid() );
                }
            } catch (Exception ex) {
                Log.w(LOGTAG, "XMPP Disco error " + ex);
                errorMessage = ex.toString() ;
            }

            return activity[0];
        }

        // This happens on the thread UI
        protected void onPostExecute(final EntityViewActivity activity) {
            // If got no info try to exit the activity
            if ( activity.myLeafInfo == null ) {
                Log.w(LOGTAG, "Got no info" );
                setContentView(R.layout.error_view);
                TextView tvError = (TextView) activity.findViewById(R.id.errorTextView );
                tvError.setText( errorMessage );
                return;
            }

            //Create the listview
            List<DiscoverInfo.Identity> identitiesList = activity.myLeafInfo.getIdentities();
            List<DiscoverInfo.Feature> featuresList = activity.myLeafInfo.getFeatures();
            activity.myEntity.setIdentities( identitiesList );
            activity.myEntity.setFeatures(featuresList);

            setContentView(R.layout.activity_entity_view);

            // Change the icon
            ImageView ivIcon = (ImageView) activity.findViewById(R.id.imageViewLeafIcon );
            ivIcon.setImageDrawable(
                    getResources().getDrawable( activity.myEntity.getIconResource() ) );

            // Display the coordinates
            String coordinatesText = "";
            if ( activity.myEntity.getJid() != null && activity.myEntity.getJid() != "" ) {
                coordinatesText += "JID: " + activity.myEntity.getJid() + "\n";
            }
            if ( activity.myEntity.getNode() != null && activity.myEntity.getNode() != "" ) {
                coordinatesText += "Node: " + activity.myEntity.getNode() + "\n" ;
            }
            if ( activity.myEntity.getName() != null && activity.myEntity.getName() != "" ) {
                coordinatesText += "Name: " + activity.myEntity.getName() + "\n" ;
            }
            TextView tvCoordinates = (TextView) activity.findViewById(R.id.textViewCoordinatesText );
            tvCoordinates.setText( coordinatesText );

            // Display the identities
            // Create the identities text
            String identitiesText = "";
            Iterator<DiscoverInfo.Identity> iI = identitiesList.iterator() ;
            while ( iI.hasNext() ) {
                DiscoverInfo.Identity item = (DiscoverInfo.Identity) iI.next();
                identitiesText += item.getCategory() + " -> " + item.getType() + "\n";
            }
            TextView tvIdentities = (TextView) activity.findViewById(R.id.textViewIdentitiesText );
            tvIdentities.setText( identitiesText );

            // Display the features
            // Create the features text
            String featuresText = "";
            Iterator<DiscoverInfo.Feature> iF = featuresList.iterator() ;
            while ( iF.hasNext() ) {
                DiscoverInfo.Feature item = (DiscoverInfo.Feature) iF.next();
                featuresText += item.getVar() + "\n";
            }
            TextView tvFeatures = (TextView) activity.findViewById(R.id.textViewFeaturesText );
            tvFeatures.setText( featuresText );

        }
    }
}
