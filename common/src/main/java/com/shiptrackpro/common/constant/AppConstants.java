package com.shiptrackpro.common.constant;

/**
 * Application-wide constants shared across all services.
 * Centralizing constants prevents magic strings scattered across the codebase.
 */
public final class AppConstants {

    private AppConstants() {
        // Prevent instantiation
    }

    // --- Pagination Defaults ---
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIR = "desc";
    public static final int MAX_PAGE_SIZE = 100;

    // --- Gateway Headers (injected by API Gateway after JWT validation) ---
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";

    // --- Roles ---
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    // --- Shipment Statuses ---
    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_PICKED_UP = "PICKED_UP";
    public static final String STATUS_IN_TRANSIT = "IN_TRANSIT";
    public static final String STATUS_OUT_FOR_DELIVERY = "OUT_FOR_DELIVERY";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_DELAYED = "DELAYED";
    public static final String STATUS_FAILED_DELIVERY = "FAILED_DELIVERY";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_RETURNED = "RETURNED";

    // --- Notification Types ---
    public static final String NOTIF_SHIPMENT_CREATED = "SHIPMENT_CREATED";
    public static final String NOTIF_STATUS_CHANGED = "STATUS_CHANGED";
    public static final String NOTIF_OUT_FOR_DELIVERY = "OUT_FOR_DELIVERY";
    public static final String NOTIF_DELIVERED = "DELIVERED";
    public static final String NOTIF_DELAYED = "DELAYED";
    public static final String NOTIF_FAILED_DELIVERY = "FAILED_DELIVERY";

    // --- Tracking Number ---
    public static final String TRACKING_PREFIX = "STP";
    public static final int TRACKING_CODE_LENGTH = 6;

    // --- Redis Key Prefixes ---
    public static final String REDIS_REFRESH_TOKEN = "refresh:";
    public static final String REDIS_BLACKLIST = "blacklist:";
    public static final String REDIS_VERIFY_EMAIL = "verify:";
    public static final String REDIS_RESET_PASSWORD = "reset:";
    public static final String REDIS_RATE_LIMIT = "rate:";
    public static final String REDIS_LOGIN_ATTEMPTS = "login_attempts:";
    public static final String REDIS_LOCATION = "location:";
    public static final String REDIS_LOCATION_BUFFER = "location_buffer:";
}
