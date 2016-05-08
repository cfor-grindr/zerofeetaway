package com.zerofeetaway;

import android.app.ProgressDialog;
import android.location.Location;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

import com.zerofeetaway.EventbriteApiParams;
import com.zerofeetaway.recyclerview.EventAdapter;
import com.zerofeetaway.recyclerview.EventModel;
import com.zerofeetaway.ui.DividerItemDecoration;
import com.zerofeetaway.util.AsyncTask;
import com.zerofeetaway.util.ImageCache;
import com.zerofeetaway.util.ImageResizer;
import com.zerofeetaway.util.Utils;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    /** Search for results within this distance */
    private static final String WITHIN_DISTANCE = "5mi";

    private static final String EVENT_SEARCH = "events/search";
    private static final String EVENT_RESPONSE = "events";

    private static final String IMAGE_CACHE_DIR = "images";

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    // UI Widgets
    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected TextView mLastUpdateTimeTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;

    // Labels
    protected String mLastUpdateTimeLabel;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    protected RecyclerView mRecyclerView;
    protected EventAdapter mAdapter;

    /** Image resizer from DisplayingBitmaps sample */
    private ImageResizer mImageResizer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Locate the UI widgets.
        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);

        // Set labels.
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_label);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        /** Button handlers */
        Button searchButton = (Button) findViewById(R.id.search_button);
        if (searchButton != null) {
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        startSearch();
                    } catch (JSONException e) {
                        Log.e(TAG, "Error in response", e);
                    }
                }
            });
        }
        if (mStartUpdatesButton != null) {
            mStartUpdatesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startUpdatesButtonHandler(view);
                }
            });
        }
        if (mStopUpdatesButton != null) {
            mStopUpdatesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    stopUpdatesButtonHandler(view);
                }
            });
        }

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();

        /** Set up ImageResizer */
        // For the sake of simplicity, only cache one smaller resolution version of the
        // preview image
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        // For now, use 1/2 of the longest width to resize image. Since the larger thumbnail in
        // the detail fragment takes up roughly 1/4 of the screen anyway, this should suffice.
        final int longest = (height > width ? height : width) / 2;

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageResizer = new ImageResizer(this, longest);
        mImageResizer.addImageCache(getSupportFragmentManager(), cacheParams);
        mImageResizer.setImageFadeIn(true);
        mImageResizer.setLoadingImage(R.drawable.empty_photo);

        /** Set up recycler view */
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new EventAdapter(this);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // Before Honeycomb pause image loading on scroll to help with performance
                    if (!Utils.hasHoneycomb()) {
                        mImageResizer.setPauseWork(true);
                    }
                } else {
                    mImageResizer.setPauseWork(false);
                }
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.list_divider_h)));
    }

    /**
     * Called by the child fragments to load resized images
     */
    public ImageResizer getImageResizer() {
        return mImageResizer;
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
                setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    public void startUpdatesButtonHandler(View view) {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates. Does nothing if
     * updates were not previously requested.
     */
    public void stopUpdatesButtonHandler(View view) {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
            stopLocationUpdates();
        }
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    /**
     * Ensures that only one button is enabled at any time. The Start Updates button is enabled
     * if the user is not requesting location updates. The Stop Updates button is enabled if the
     * user is requesting location updates.
     */
    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    private void updateUI() {
        mLatitudeTextView.setText(String.format("%f", mCurrentLocation.getLatitude()));
        mLongitudeTextView.setText(String.format("%f", mCurrentLocation.getLongitude()));
        mLastUpdateTimeTextView.setText(String.format("%s: %s", mLastUpdateTimeLabel,
                mLastUpdateTime));
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // Stop location updates on pause
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        if (mImageResizer != null) {
            mImageResizer.setExitTasksEarly(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }

        if (mImageResizer != null) {
            mImageResizer.setExitTasksEarly(true);
            mImageResizer.flushCache();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mImageResizer != null) {
            mImageResizer.closeCache();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        Toast.makeText(this, getResources().getString(R.string.location_updated_message),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**************************/
    /****** JSON REQUEST ******/
    /**************************/
    public void startSearch() throws JSONException {

        RequestParams params = new RequestParams();
        params.put(EventbriteApiParams.EVENT_PARAM_LOCATION_LATITUDE, mLatitudeTextView.getText());
        params.put(EventbriteApiParams.EVENT_PARAM_LOCATION_LONGITUDE, mLongitudeTextView.getText());
        params.put(EventbriteApiParams.EVENT_PARAM_LOCATION_WITHIN, WITHIN_DISTANCE);
        params.put(EventbriteApiParams.EVENT_PARAM_SORT_BY, EventbriteApiParams.EVENT_PARAM_SORT_BY_DISTANCE);
        params.put(EventbriteApiParams.EXPAND, EventbriteApiParams.EXPAND_PARAM_BOTH);

        EventbriteRestClient.get(EVENT_SEARCH, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "Response is JSONObject");

                try {
                    // TODO: Get other pages if result list > 50
                    // TODO: Use "pagination" field in response
                    JSONArray eventArray = response.getJSONArray(EVENT_RESPONSE);
                    new ParseJSONResponse().execute(eventArray);
                } catch (JSONException e) {
                    Log.e(TAG, "Error getting events JSONArray!", e);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d(TAG, "Response is JSONArray");

                Log.e(TAG, "JSONArray response is not handled!");
            }
        });
    }


    /*************************/
    /****** JSON PARSER ******/
    /*************************/
    private class ParseJSONResponse extends AsyncTask<JSONArray, Void, Void> {

        private final String TASK_TAG = ParseJSONResponse.class.getSimpleName();

        private ProgressDialog mmProgressDialog;

        public ParseJSONResponse() {

        }

        @Override
        public void onPreExecute() {
            mmProgressDialog = new ProgressDialog(MainActivity.this);
            mmProgressDialog.setCancelable(false);
            mmProgressDialog.setMessage(getString(R.string.loading_events_dialog));
            mmProgressDialog.setIndeterminate(true);
            mmProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }

        @Override
        public Void doInBackground(JSONArray... param) {
            JSONArray respArray = param[0];

            for (int i = 0; i < respArray.length(); ++i) {
                JSONObject event = null;

                try {
                    event = respArray.getJSONObject(i);
                } catch (JSONException er) {
                    Log.e(TAG, "Error when parsing response!", er);
                }

                if (event != null) {
                    mAdapter.addEvent(createEventModel(event));
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            if (mmProgressDialog.isShowing()) {
                mmProgressDialog.dismiss();
            }

            mAdapter.notifyDataSetChanged();
        }

        private EventModel createEventModel(JSONObject event) {
            String id, name, startLocal, org, addr1, city, region, zip;
            double lat, lon;

            id = name = startLocal = org = addr1 = city = region = zip = "";
            lat = lon = 0.d;

            try {
                id = event.getString(EventbriteApiParams.EVENT_FIELD_ID);
            } catch (JSONException e) {
                Log.e(TASK_TAG, "Error parsing JSONObject!", e);
            }

            try {
                JSONObject nameObj = event.getJSONObject(EventbriteApiParams.EVENT_FIELD_NAME);
                name = nameObj.getString(EventbriteApiParams.EVENT_FIELD_NAME_TEXT);
            } catch (JSONException e) {
                Log.e(TASK_TAG, "Error parsing JSONObject!", e);
            }

            try {
                JSONObject start = event.getJSONObject(EventbriteApiParams.EVENT_FIELD_START);
                startLocal = start.getString(EventbriteApiParams.EVENT_FIELD_START_LOCAL);
            } catch (JSONException e) {
                Log.e(TASK_TAG, "Error parsing JSONObject!", e);
            }

            try {
                JSONObject organizer = event.getJSONObject(EventbriteApiParams.EVENT_FIELD_ORGANIZER);
                org = organizer.getString(EventbriteApiParams.EVENT_FIELD_ORGANIZER_NAME);
            } catch (JSONException e) {
                Log.e(TASK_TAG, "Error parsing JSONObject!", e);
            }

            try {
                JSONObject venue = event.getJSONObject(EventbriteApiParams.EVENT_FIELD_VENUE);
                JSONObject address = venue.getJSONObject(EventbriteApiParams.EVENT_FIELD_VENUE_ADDRESS);
                addr1 = address.getString(EventbriteApiParams.EVENT_FIELD_VENUE_ADDRESS_1);
                city = address.getString(EventbriteApiParams.EVENT_FIELD_VENUE_CITY);
                region = address.getString(EventbriteApiParams.EVENT_FIELD_VENUE_REGION);
                zip = address.getString(EventbriteApiParams.EVENT_FIELD_VENUE_ZIP);
                lat = address.getDouble(EventbriteApiParams.EVENT_FIELD_VENUE_LAT);
                lon = address.getDouble(EventbriteApiParams.EVENT_FIELD_VENUE_LON);
            } catch (JSONException e) {
                Log.e(TASK_TAG, "Error parsing JSONObject!", e);
            }
            // TODO: Get image URL
            Uri img = null;

            // Fill in info obtained from parsing
            String addr2 = city + ", " + region + " " + zip;
            double dist = haversine(Double.parseDouble(mLatitudeTextView.getText().toString()),
                    Double.parseDouble(mLongitudeTextView.getText().toString()),
                    lat,
                    lon);
            String startDate = "ERR";
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(startLocal);
                startDate = new SimpleDateFormat("EEE'.' LLL d h:mm a", Locale.US).format(date);
            } catch (ParseException err) {
                Log.e(TASK_TAG, "Error parsing date!", err);
            }

            return new EventModel(id, startDate, name, org, dist, addr1, addr2, img);
        }

        private double haversine(double lat1, double lng1, double lat2, double lng2) {
            int r = 6371; // average radius of the earth in km
//            int r = 3959; // average radius of the earth in mi
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lng2 - lng1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return r * c;
        }
    }
}