package net.marevalo.flowsmanager;

import android.os.Parcel;
import android.os.Parcelable;

import org.jivesoftware.smackx.disco.packet.DiscoverInfo;

import java.util.Iterator;
import java.util.List;

public class Entity implements Parcelable,Comparable {

    private Integer id = null ;
    private String jid  = "" ;
    private String node = "" ;
    private String name = "" ;
    private List<DiscoverInfo.Identity> identities = null ;
    private List<DiscoverInfo.Feature> features = null ;


    //
    // Creators
    //
    public Entity(String jid, String node, String name) {
        this.id = 0 ;
        this.jid = jid ;
        this.node = node ;
        this.name = name ;
    }

    //
    // Getters & Setters
    //
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //
    // Some pretty printing
    //
    public String getDisplayName() {
        if ( this.name != null && this.name != "" ) {
            return this.name;
        }
        if ( this.node != null && this.node != "" ) {
            return this.node;
        }
        return this.jid;
    }

    public String getIdName() {
        if ( this.jid != null && this.jid != "" ) {
            return this.jid;
        }
        return this.node;
    }

    public void setIdentities (  List<DiscoverInfo.Identity> identities ) {
        this.identities = identities ;
    }

    public void setFeatures (  List<DiscoverInfo.Feature> features ) {
        this.features = features ;
    }

    public int getIconResource () {
        if (this.identities == null) {
            return R.drawable.object_loading;
        }

        Iterator<DiscoverInfo.Identity> iI = identities.iterator();
        while (iI.hasNext()) {
            DiscoverInfo.Identity item = (DiscoverInfo.Identity) iI.next();
            switch (item.getCategory()) {
                case "account":
                    return R.drawable.icon_account ;
                case "automation":
                    return R.drawable.icon_automation ;
                case "auth":
                    return R.drawable.icon_auth ;
                case "client":
                    return R.drawable.icon_client ;
                case "collaboration":
                    return R.drawable.icon_collaboration ;
                case "server":
                    return R.drawable.icon_server ;
                case "component":
                    return R.drawable.icon_component ;
                case "directory":
                    return R.drawable.icon_directory ;
                case "conference":
                    return R.drawable.icon_conference ;
                case "gateway":
                    return R.drawable.icon_gateway ;
                case "headline":
                    return R.drawable.icon_headline ;
                case "hierarchy":
                    return R.drawable.icon_hierarchy ;
                case "proxy":
                    return R.drawable.icon_proxy ;
                case "pubsub":
                    return R.drawable.icon_pubsub ;
                case "store":
                    return R.drawable.icon_store ;
            }

        }

        return R.drawable.icon_default ;
    }

    //
    // Parcelable support
    //

    // Unclear function :-P
    @Override
    public int describeContents() {
        return 0;
    }

    // Parceler
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.jid);
        dest.writeString(this.node);
        dest.writeString(this.name);
    }

    // Creator
    public static final Parcelable.Creator<Entity> CREATOR
            = new Parcelable.Creator<Entity>() {
        public Entity createFromParcel(Parcel in) {
            return new Entity(in);
        }

        public Entity[] newArray(int size) {
            return new Entity[size];
        }
    };

    // De-parceler
    public Entity(Parcel in) {
        this.id = in.readInt();
        this.jid = in.readString();
        this.node = in.readString();
        this.name = in.readString();
    }

    //
    // Comparable support
    //

    @Override
    public int compareTo(Object another) {
        return this.getDisplayName().compareToIgnoreCase( ((Entity) another).getDisplayName() );
    }
}
