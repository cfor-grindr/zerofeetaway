package com.zerofeetaway;

public class EventbriteApiParams {
    /** Expansions */
    public static final String EXPAND = "expand";
    public static final String EXPAND_PARAM_ORGANIZER = "organizer";
    public static final String EXPAND_PARAM_VENUE = "venue";
    public static final String EXPAND_PARAM_BOTH = EXPAND_PARAM_ORGANIZER + "," + EXPAND_PARAM_VENUE;

    /** Event Parameters */
    public static final String EVENT_PARAM_Q = "q";
    public static final String EVENT_PARAM_SINCE_ID = "since_id";
    public static final String EVENT_PARAM_POPULAR = "popular";
    public static final String EVENT_PARAM_SORT_BY = "sort_by";
    public static final String EVENT_PARAM_LOCATION_ADDRESS = "location.address";
    public static final String EVENT_PARAM_LOCATION_WITHIN = "location.within";
    public static final String EVENT_PARAM_LOCATION_LATITUDE = "location.latitude";
    public static final String EVENT_PARAM_LOCATION_LONGITUDE = "location.longitude";
    public static final String EVENT_PARAM_LOCATION_VIEWPORT_NORTHEAST_LATITUDE = "location.viewport.northeast.latitude";
    public static final String EVENT_PARAM_LOCATION_VIEWPORT_NORTHEAST_LONGITUDE = "location.viewport.northeast.longitude";
    public static final String EVENT_PARAM_LOCATION_VIEWPORT_SOUTHWEST_LATITUDE = "location.viewport.southwest.latitude";
    public static final String EVENT_PARAM_LOCATION_VIEWPORT_SOUTHWEST_LONGITUDE	 = "location.viewport.southwest.longitude";
    public static final String EVENT_PARAM_VENUE_CITY = "venue.city";
    public static final String EVENT_PARAM_VENUE_REGION = "venue.region";
    public static final String EVENT_PARAM_VENUE_COUNTRY = "venue.country";
    public static final String EVENT_PARAM_PERSONALIZATION_USER_ID = "personalization_user.id";
    public static final String EVENT_PARAM_ORGANIZER_ID = "organizer.id";
    public static final String EVENT_PARAM_USER_ID = "user.id";
    public static final String EVENT_PARAM_TRACKING_CODE = "tracking_code";
    public static final String EVENT_PARAM_CATEGORIES = "categories";
    public static final String EVENT_PARAM_SUBCATEGORIES = "subcategories";
    public static final String EVENT_PARAM_FORMATS = "formats";
    public static final String EVENT_PARAM_PRICE = "price";
    public static final String EVENT_PARAM_START_DATE_RANGE_START = "start_date.range_start";
    public static final String EVENT_PARAM_START_DATE_RANGE_END = "start_date.range_end";
    public static final String EVENT_PARAM_START_DATE_KEYWORD = "start_date.keyword";
    public static final String EVENT_PARAM_DATE_CREATED_RANGE_START = "date_created.range_start";
    public static final String EVENT_PARAM_DATE_CREATED_RANGE_END = "date_created.range_end";
    public static final String EVENT_PARAM_DATE_CREATED_KEYWORD = "date_created.keyword";
    public static final String EVENT_PARAM_DATE_MODIFIED_RANGE_START = "date_modified.range_start";
    public static final String EVENT_PARAM_DATE_MODIFIED_RANGE_END = "date_modified.range_end";
    public static final String EVENT_PARAM_DATE_MODIFIED_KEYWORD = "date_modified.keyword";
    public static final String EVENT_PARAM_SEARCH_TYPE = "search_type";
    public static final String EVENT_PARAM_INCLUDE_ALL_SERIES_INSTANCES = "include_all_series_instances";

    /** Event Parameter values */
    public static final String EVENT_PARAM_SORT_BY_REVERSE_PREFIX = "-";
    public static final String EVENT_PARAM_SORT_BY_ID = "id";
    public static final String EVENT_PARAM_SORT_BY_DATE = "date";
    public static final String EVENT_PARAM_SORT_BY_NAME = "name";
    public static final String EVENT_PARAM_SORT_BY_CITY = "city";
    public static final String EVENT_PARAM_SORT_BY_DISTANCE = "distance";
    public static final String EVENT_PARAM_SORT_BY_BEST = "best";

}
