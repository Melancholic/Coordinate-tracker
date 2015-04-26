package com.coordinate_tracker.anagorny;

/**
 * Created by sosnov on 17.03.15.
 */
public class Configuration {
    public static String ID = StorageAdapter.usersStorage().getString(Configuration.UUID_TOKEN_KEY_NAME, "");
    public final static String PRIVATE_STORE_NAME="CurrentUser";
    public final static String AUTH_TOKEN_KEY_NAME="AuthToken";
    public final static String UUID_TOKEN_KEY_NAME="UUID";

    private final static String HOST = "coordinate.anagorny.com";
    private final static String RECIVE_ACTION = "api/v1/geodata/recive";
    private final static String LOGIN_ACTION = "/api/v1/login";
    private final static String DEFAULT_PROTOCOL = "http";
    private final static int DEFAULT_PORT = 80;

    public static String getReciveURL() {
        return getURL(RECIVE_ACTION);
    }

    public static String getReciveURL(int port) {
        return getURL(port, LOGIN_ACTION);
    }

    public static String getLoginURL() {
        return getURL(LOGIN_ACTION);
    }

    public static String getLoginURL(int port) {
        return getURL(port, LOGIN_ACTION);
    }

    public static String getURL(String action) {
        return getURL(DEFAULT_PROTOCOL, HOST, DEFAULT_PORT, action);
    }

    public static String getURL(int port, String action) {
        return getURL(DEFAULT_PROTOCOL, HOST, port, action);
    }

    public static String getURL(String protocol, String host, int port, String action) {
        return protocol + "://" + host + ":" + port + "/" + action;
    }

    public static String geRootURL(String protocol, String host, int port) {
        return protocol + "://" + host + ":" + port;
    }

    public static String geRootURL() {
        return DEFAULT_PROTOCOL + "://" + HOST + ":" + DEFAULT_PORT;
    }
}
