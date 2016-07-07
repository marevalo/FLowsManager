package net.marevalo.flowsmanager;

import android.os.Parcel;
import android.os.Parcelable;

public class HistoryElement implements Parcelable {
    public Entity entity = null ;
    public int firstViewablePosition = 0 ;

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
        entity.writeToParcel(dest, flags);
        dest.writeInt(this.firstViewablePosition);
    }

    // Creator
    public static final Parcelable.Creator<HistoryElement> CREATOR
            = new Parcelable.Creator<HistoryElement>() {
        public HistoryElement createFromParcel(Parcel in) {
            return new HistoryElement(in);
        }

        public HistoryElement[] newArray(int size) {
            return new HistoryElement[size];
        }
    };

    // De-parceler
    public HistoryElement(Parcel in) {
        this.entity = new Entity(in);
        this.firstViewablePosition = in.readInt();
    }

    public HistoryElement( Entity entity, int firstViewablePosition ) {
        this.entity = entity;
        this.firstViewablePosition = firstViewablePosition;
    }
}
