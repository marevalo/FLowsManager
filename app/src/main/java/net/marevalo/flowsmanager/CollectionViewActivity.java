package net.marevalo.flowsmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jxmpp.util.XmppStringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CollectionViewActivity extends ActionBarActivity {

    // TODO 0.1: Beautify settings ativity
    private static final String LOGTAG = "CollectionViewActivity";
    private Entity currentEntity ;
    private int startPosition ;
    private List<HistoryElement> history ;
    private LoadChildEntitiesTask childLoader ;
    private CompleteChildEntitiesTask childCompleter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOGTAG , "onCreate()" );

        super.onCreate(savedInstanceState);

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            this.currentEntity = savedInstanceState.getParcelable("currentEntity");
            this.startPosition = savedInstanceState.getInt("startPosition");
            this.history = (List) savedInstanceState.getParcelableArrayList("history");
        } else {
            // Probably initialize members with default values for a new instance
            this.history = new ArrayList<HistoryElement>();
        }

    }

    @Override
    protected void onResume() {
        SharedPreferences sharedPref;
        String user="";
        String password="";

        Log.d(LOGTAG , "onResume()" );
        super.onResume();
        // The activity has become visible (it is now "resumed").

        // Get the initial configuration
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        user = sharedPref.getString("xmpp_user", "");
        password = sharedPref.getString("xmpp_password", "");
        Log.d(LOGTAG , "user:" + user );
        XMPPConnectionManager.setConfiguration(user, password);

        // retry until we get a valid configuration
        if ( XMPPConnectionManager.getConnection() == null ) {
            Log.d(LOGTAG , "No connection" );
            //Call the settings activity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent , 1 );
            Log.d(LOGTAG , "Subactivity launched" );
            return;

        }

        Log.d(LOGTAG , "Now we have a working connection" );

        // TODO: Do this in a safe way
        // Anyway if the connection is configured it should be already a current entity
        if ( this.currentEntity == null ) {
            this.currentEntity = new Entity(XmppStringUtils.parseDomain(user), null, null);
        }

        this.setTitle( this.currentEntity.getDisplayName() );

        this.childLoader = new LoadChildEntitiesTask() ;
        this.childLoader.execute(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Stop all background tasks
        if ( this.childCompleter != null ) {
            this.childCompleter.cancel(true);
        }
        if ( this.childLoader != null ) {
            this.childLoader.cancel(true);
        }

        // Get the listview
        ListView collectionListView = (ListView) findViewById(R.id.collectionListView);

        if ( collectionListView != null ) {
            // Save the user's current state
            savedInstanceState.putParcelable("currentEntity", this.currentEntity);
            savedInstanceState.putInt("startPosition", collectionListView.getFirstVisiblePosition());
            savedInstanceState.putParcelableArrayList("history", (ArrayList) this.history);
        }

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_collection_view, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if ( this.history.isEmpty() ) {
            // If we have no history defer to system default behavior and exit app
            super.onBackPressed();
        } else {
            // If we have some back history pop the last entity
            this.childCompleter.cancel(true);
            Entity entity = this.history.get( this.history.size() - 1 ).entity ;
            this.currentEntity = entity;
            this.startPosition = this.history.get( this.history.size() - 1 ).firstViewablePosition;
            this.history.remove(this.history.size() - 1);
            this.setTitle( entity.getDisplayName() );
            this.childLoader = new LoadChildEntitiesTask();
            this.childLoader.execute(this);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_edit_target:
                AlertDialog.Builder builder = new AlertDialog.Builder( this );
                builder.setTitle(R.string.dialog_target_title);
                // Add the content
                LayoutInflater inflater = this.getLayoutInflater();
                builder.setView(inflater.inflate(R.layout.dialog_target, null));
                // Add the buttons
                builder.setPositiveButton(R.string.target_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        TextView targetTV = (TextView)
                                ((Dialog) dialog).findViewById(R.id.editTextTarget);
                        String newTarget = targetTV.getText().toString();
                        // Load the Entity for the new target
                        Entity newEntity = new Entity( newTarget , null , null );
                        CollectionViewActivity.this.navigateTo( newEntity , 0 );

                    }
                });
                builder.setNegativeButton(R.string.target_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.action_info:
                if ( this.childCompleter != null ) {
                    this.childCompleter.cancel(true);
                }
                Intent intent_info = new Intent(this, EntityViewActivity.class);
                intent_info.putExtra( "entity" , this.currentEntity);
                startActivity(intent_info);
                return true;
            case R.id.action_settings:
                if ( this.childCompleter != null ) {
                    this.childCompleter.cancel(true);
                }
                Intent intent_settings = new Intent(this, SettingsActivity.class);
                startActivity(intent_settings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class LoadChildEntitiesTask extends AsyncTask<CollectionViewActivity, Void, CollectionViewActivity> {

        private ServiceDiscoveryManager discoManager;
        private ArrayList<Entity> entityList;
        private String errorMessage = null ;
        private ProgressBar myProgress = null ;

        // This happens on the ui thread
        protected void onPreExecute( ){
            setContentView(R.layout.activity_collection_view);
            this.myProgress = (ProgressBar) CollectionViewActivity.this.findViewById(R.id.progressBar);
            this.myProgress.setMax(100);
            this.myProgress.setProgress(1);
        }

        // THis happens on another thread
        protected CollectionViewActivity doInBackground(CollectionViewActivity... activity) {

            // Get the XMMPConnection from the manager
            AbstractXMPPConnection conn = XMPPConnectionManager.getConnection();
            // Obtain the ServiceDiscoveryManager associated with my XMPP connection
            this.discoManager = ServiceDiscoveryManager.getInstanceFor(conn);

            DiscoverItems discoItems;
            // This example gets the items associated with online catalog service
            try {
                discoItems = this.discoManager.discoverItems(
                        activity[0].currentEntity.getJid() ,
                        activity[0].currentEntity.getNode() );
            } catch (Exception ex) {
                Log.w(LOGTAG, "XMPP Disco error " + ex);
                errorMessage = ex.getMessage() ;
                return activity[0];
            }

            // Get the discovered items of the queried XMPP entity
            Iterator it = discoItems.getItems().iterator();
            Log.i(LOGTAG, "Got items:" + it.toString());

            this.entityList = new ArrayList<>();
            this.entityList.clear();

            // Display the items of the remote XMPP entity
            int id = 0;
            while (it.hasNext()) {
                DiscoverItems.Item item = (DiscoverItems.Item) it.next();

                Entity entity = new Entity(item.getEntityID(), item.getNode() , item.getName() );
                Log.i(LOGTAG, "Got item:" + item.getName() + " : " + item.getEntityID());
                entity.setId(id);
                entity.setName(item.getName());
                this.entityList.add(entity);
                id++;
            }
            Collections.sort(this.entityList);

            return activity[0];
        }

        // This happens on the thread UI
        protected void onPostExecute(final CollectionViewActivity activity) {

            // If we got an error show the error view
            if ( errorMessage != null ) {
                setContentView(R.layout.error_view);
                TextView tvError = (TextView) activity.findViewById(R.id.errorTextView );
                tvError.setText( errorMessage );
                return;
            }

            // Update a little the progress bar
            this.myProgress.setMax( entityList.size() + 2 );
            this.myProgress.setProgress( 2 );

            // Get the listview
            ListView collectionListView = (ListView) findViewById(R.id.collectionListView);

            CollectionAdapter collectionAdapter = new CollectionAdapter(
                    CollectionViewActivity.this ,
                    this.entityList
            );

            collectionListView.setAdapter(collectionAdapter);
            Log.i(LOGTAG, "Reinstatig position: " + activity.startPosition);
            collectionListView.setSelection( activity.startPosition );

            // Create a message handling object as an anonymous class.
            AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView parent, View v, int position, long id) {
                    // Do something in response to the click
                    Entity entity = (Entity) parent.getAdapter().getItem(position);
                    Log.i(LOGTAG, "Click on: " + entity.getJid());
                    if ( activity.currentEntity.getJid() != entity.getJid() ||
                         activity.currentEntity.getNode() != entity.getNode() ){
                        CollectionViewActivity.this.navigateTo(entity, parent.getFirstVisiblePosition());
                    } else {
                        CollectionViewActivity.this.startPosition = parent.getFirstVisiblePosition();
                        CollectionViewActivity.this.childCompleter.cancel(true);
                        Intent intent_entity_view = new Intent( activity,  EntityViewActivity.class );
                        intent_entity_view.putExtra( "entity" , entity);
                        startActivity(intent_entity_view);
                    }
                }
            };

            // Create a message handling object as an anonymous class.
            AdapterView.OnItemLongClickListener mMessageLongClickedHandler =
                new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView parent, View v, int position, long id) {
                    // Do something in response to the long click
                    CollectionViewActivity.this.startPosition = parent.getFirstVisiblePosition();
                    CollectionViewActivity.this.childCompleter.cancel(true);
                    Entity entity = (Entity) parent.getAdapter().getItem(position);
                    Log.i(LOGTAG, "Long Click on: " + entity.getJid());
                    Intent intent_entity_view = new Intent( activity,  EntityViewActivity.class );
                    intent_entity_view.putExtra( "entity" , entity);
                    startActivity(intent_entity_view);
                    return true;
                }
            };

            collectionListView.setOnItemClickListener(mMessageClickedHandler);
            collectionListView.setOnItemLongClickListener(mMessageLongClickedHandler);

            // Trigger the population of icons
            CollectionViewActivity.this.childCompleter = new CompleteChildEntitiesTask() ;
            CollectionViewActivity.this.childCompleter.execute(entityList);
        }

    }

    class CompleteChildEntitiesTask extends AsyncTask<ArrayList<Entity>, Boolean, Boolean> {

        private ServiceDiscoveryManager discoManager;
        private String errorMessage = "";
        private DiscoverInfo entityInfo;
        private ProgressBar myProgress = null ;

        // This happens on the ui thread
        @Override
        protected void onPreExecute( ){
            this.myProgress = (ProgressBar) CollectionViewActivity.this.findViewById(R.id.progressBar);
        }

        @Override
        protected Boolean doInBackground(ArrayList<Entity>... entityList) {

            Entity entity = null ;

            Iterator entityIterator = entityList[0].iterator();

            // Get the XMMPConnection from the manager
            AbstractXMPPConnection conn = XMPPConnectionManager.getConnection();
            // Obtain the ServiceDiscoveryManager associated with my XMPP connection
            this.discoManager = ServiceDiscoveryManager.getInstanceFor(conn);

            while ( entityIterator.hasNext() ) {
                if (this.isCancelled()) {
                    Log.d(LOGTAG , "Cancelling" );
                    return null;
                }
                Log.d(LOGTAG , "Loading info for icons" );
                entity = (Entity) entityIterator.next();
                try {
                    if ( entity.getNode() != null && entity.getNode() != "" ) {
                        entityInfo = discoManager.discoverInfo( entity.getJid() , entity.getNode() );
                    } else {
                        entityInfo = discoManager.discoverInfo( entity.getJid() );
                    }
                } catch (Exception ex) {
                    Log.w(LOGTAG, "XMPP Disco error " + ex);
                    errorMessage = ex.toString() ;
                    this.publishProgress( false );
                    continue;
                }
                entity.setIdentities( entityInfo.getIdentities() );
                this.publishProgress( true );
            }
            return null ;
        }

        @Override
        protected void onProgressUpdate( Boolean... dataSetChanged) {
            Log.d(LOGTAG, "Progress update");
            this.myProgress.incrementProgressBy(1);
            if ( dataSetChanged[0] ) {
                ListView collectionListView = (ListView) findViewById(R.id.collectionListView);
                CollectionAdapter myAdapter = (CollectionAdapter) collectionListView.getAdapter();
                if (myAdapter != null ) {
                    myAdapter.notifyDataSetChanged();
                }
            }
        }

        // This happens on the thread UI
        @Override
        protected void onPostExecute(final Boolean noMeaning ) {
            this.myProgress.setVisibility(View.INVISIBLE);
        }

    }

    private void navigateTo( Entity targetEntity , int currentFirstViewablePos ) {
        if ( this.childCompleter != null ) {
            this.childCompleter.cancel(true);
        }
        this.history.add( new HistoryElement(this.currentEntity , currentFirstViewablePos ) );
        this.currentEntity = targetEntity;
        this.startPosition = 0 ;
        // activity.setFeatureDrawable( , R.drawable.object_loading );
        this.setTitle(targetEntity.getDisplayName() );
        this.childLoader = new LoadChildEntitiesTask();
        this.childLoader.execute(this);
    }

}
