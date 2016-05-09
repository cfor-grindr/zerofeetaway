package com.zerofeetaway.recyclerview;

import android.os.Parcel;
import android.os.Parcelable;

public class EventModel implements Parcelable {
    private String mId;
    private String mDate, mName, mOrganizer;
    private String mAddr1, mAddr2;
    private double mDistance;
    private String mUrl;

    private String mThumbnail;

    public EventModel(String id, String date, String name, String org, double dist, String addr1,
                      String addr2, String img, String url) {
        mId = id;
        mDate = date; mName = name; mOrganizer = org; mAddr1 = addr1; mAddr2 = addr2;
        mDistance = dist;
        mThumbnail = img;
        mUrl = url;
    }

    /**
     * Parcelable data restoration
     * @param in Parcel to restore information from
     */
    public EventModel(Parcel in) {
        mId = in.readString();
        mDate = in.readString(); mName = in.readString(); mOrganizer = in.readString();
        mAddr1 = in.readString(); mAddr2 = in.readString();

        mDistance = in.readDouble();
        mThumbnail= in.readString(); mUrl = in.readString();
    }

    public boolean equals(EventModel event) { return mId.equals(event.getId()); }

    public String getId() { return mId; }
    public String getDate() { return mDate; }
    public String getEventName() { return mName; }
    public String getOrganizer() { return mOrganizer;}
    public String getAddr1() { return mAddr1; }
    public String getAddr2() { return mAddr2; }
    public double getDistance() { return mDistance; }
    public String getThumbnailURL() { return mThumbnail; }
    public String getUrl() { return mUrl; }

    public void update(EventModel event) {
        mDate = event.getDate(); mName = event.getEventName();
        mOrganizer = event.getOrganizer(); mAddr1 = event.getAddr1(); mAddr2 = event.getAddr2();
        mDistance = event.getDistance();
        mThumbnail = event.getThumbnailURL();
        mUrl = event.getUrl();
    }

    /**************************/
    /***** PARCEL METHODS *****/
    /**************************/
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mId);
        out.writeString(mDate); out.writeString(mName); out.writeString(mOrganizer);
        out.writeString(mAddr1); out.writeString(mAddr2);

        out.writeDouble(mDistance);
        out.writeString(mThumbnail); out.writeString(mUrl);
    }

    public static final Parcelable.Creator<EventModel> CREATOR
            = new Parcelable.Creator<EventModel>() {
        public EventModel createFromParcel(Parcel in) {
            return new EventModel(in);
        }

        public EventModel[] newArray(int size) {
            return new EventModel[size];
        }
    };
}
