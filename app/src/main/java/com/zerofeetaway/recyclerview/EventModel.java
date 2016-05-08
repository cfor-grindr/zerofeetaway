package com.zerofeetaway.recyclerview;

import android.net.Uri;

public class EventModel {
    private String mId;
    private String mDate, mName, mOrganizer;
    private String mAddr1, mAddr2;
    private double mDistance;

    private Uri mThumbnail;

    // Default "dummy" constructor
    public EventModel() {
        mId = mDate = mName = mOrganizer = mAddr1 = mAddr2 = "0";
        mDistance = 0.d;
        mThumbnail = null;
    }

    public EventModel(String id, String date, String name, String org, double dist, String addr1,
                      String addr2, Uri img) {
        mId = id;
        mDate = date; mName = name; mOrganizer = org; mAddr1 = addr1; mAddr2 = addr2;
        mDistance = dist;
        mThumbnail = img;
    }

    public boolean equals(EventModel event) { return mId.equals(event.getId()); }

    public String getId() { return mId; }
    public String getDate() { return mDate; }
    public String getEventName() { return mName; }
    public String getOrganizer() { return mOrganizer;}
    public String getAddr1() { return mAddr1; }
    public String getAddr2() { return mAddr2; }
    public double getDistance() { return mDistance; }
    public Uri getThumbnail() { return mThumbnail; }

    public void update(EventModel event) {
        mDate = event.getDate(); mName = event.getEventName();
        mOrganizer = event.getOrganizer(); mAddr1 = event.getAddr1(); mAddr2 = event.getAddr2();
        mDistance = event.getDistance();
        mThumbnail = event.getThumbnail();
    }
}
