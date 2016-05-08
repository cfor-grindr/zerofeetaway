package com.zerofeetaway.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zerofeetaway.R;

public class EventViewHolder extends RecyclerView.ViewHolder {

    protected ImageView mThumbnail;
    protected TextView mDateView, mDistanceView, mNameView, mOrganizerView;
    protected TextView mAddr1View, mAddr2View;

    public EventViewHolder(View itemView) {
        super(itemView);

        mThumbnail = (ImageView) itemView.findViewById(R.id.event_thumbnail);
        mDateView = (TextView) itemView.findViewById(R.id.event_date);
        mDistanceView = (TextView) itemView.findViewById(R.id.event_distance);
        mNameView = (TextView) itemView.findViewById(R.id.event_name);
        mOrganizerView = (TextView) itemView.findViewById(R.id.event_organizer);
        mAddr1View = (TextView) itemView.findViewById(R.id.event_addr1);
        mAddr2View = (TextView) itemView.findViewById(R.id.event_addr2);
    }
}
