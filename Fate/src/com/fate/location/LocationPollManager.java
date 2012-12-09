package com.fate.location;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.commonsware.cwac.locpoll.LocationPoller;
import com.commonsware.cwac.locpoll.LocationPollerParameter;
import com.commonsware.cwac.locpoll.LocationPollerResult;

public class LocationPollManager {
    private static final int PERIOD = 3600000; // 1 hour

    public static void setupLocationPoll(Context context) {
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context, LocationPoller.class);

        Bundle bundle = new Bundle();
        LocationPollerParameter parameter = new LocationPollerParameter(bundle);
        parameter.setIntentToBroadcastOnCompletion(new Intent(context, LocationReceiver.class));
        // try GPS and fall back to NETWORK_PROVIDER
        parameter.setProviders(new String[] { LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER });
        parameter.setTimeout(60000);
        i.putExtras(bundle);

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), PERIOD, pi);
    }

    public static class LocationReceiver extends BroadcastReceiver {

        private static final String TAG = "LocationReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            LocationPollerResult locationResult = new LocationPollerResult(b);
            Location loc = locationResult.getLocation();
            String msg;
            if (loc == null) {
                loc = locationResult.getLastKnownLocation();

                if (loc == null) {
                    msg = locationResult.getError();
                } else {
                    msg = "TIMEOUT, lastKnown=" + loc.toString();
                }
            } else {
                msg = loc.toString();
            }

            if (msg == null) {
                msg = "Invalid broadcast received!";
            }
            Log.d(TAG, msg);
        }
    }
}
