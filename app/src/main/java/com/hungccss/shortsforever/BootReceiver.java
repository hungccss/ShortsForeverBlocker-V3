package com.hungccss.shortsforever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // AccessibilityService is re-bound by Android when it is enabled.
        // This receiver exists so the package is awakened after boot/update.
    }
}
