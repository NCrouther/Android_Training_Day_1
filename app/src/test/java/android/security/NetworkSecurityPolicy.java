package android.security;

import android.annotation.SuppressLint;

@SuppressLint("NewApi")
public class NetworkSecurityPolicy {
    private static final NetworkSecurityPolicy INSTANCE = new NetworkSecurityPolicy();

    public static NetworkSecurityPolicy getInstance() {
        return INSTANCE;
    }

    public boolean isCleartextTrafficPermitted() {
        return true;
    }
}