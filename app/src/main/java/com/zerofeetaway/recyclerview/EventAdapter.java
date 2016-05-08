package com.zerofeetaway.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.zerofeetaway.MainActivity;
import com.zerofeetaway.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventViewHolder> {

    private List<EventModel> mDataset;
    private MainActivity mParentActivity;

    public EventAdapter(MainActivity parent) {
        mDataset = new ArrayList<>();

        mParentActivity = parent;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.event_item, viewGroup, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventViewHolder itemViewHolder, int i) {
        EventModel event = mDataset.get(i);

        if (!event.getThumbnailURL().isEmpty()) {
            Picasso.with(mParentActivity)
                    .load(event.getThumbnailURL())
                    .placeholder(R.drawable.empty_photo)
                    .error(R.drawable.empty_photo)
                    .into(itemViewHolder.mThumbnail);
        }
        itemViewHolder.mDateView.setText(event.getDate());
        itemViewHolder.mNameView.setText(event.getEventName());
        itemViewHolder.mNameView.setHorizontallyScrolling(true);
        itemViewHolder.mOrganizerView.setText(event.getOrganizer());
        itemViewHolder.mOrganizerView.setHorizontallyScrolling(true);
        itemViewHolder.mAddr1View.setText(event.getAddr1());
        itemViewHolder.mAddr2View.setText(event.getAddr2());

        // Format distance
        String dist = String.format(Locale.US, "%.2f mi", event.getDistance());
        itemViewHolder.mDistanceView.setText(dist);
    }

    @Override
    public int getItemCount() { return mDataset.size(); }

    /**
     * Either adds an event model item to the data set or updates an existing item in the dataset if
     * there is a model with the same ID.
     *
     * @param event EventModel to add to data set
     * @return True if the event was added, false otherwise (including if item was updated)
     */
    public boolean addEvent(EventModel event) {
        for (EventModel e : mDataset) {
            if (e.equals(event)) {
                e.update(event);
                return false;
            }
        }

        mDataset.add(event);
        return true;
    }
}