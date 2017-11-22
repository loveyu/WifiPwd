package net.loveyu.wifipwd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

public class WifiReceiver extends BroadcastReceiver {
    MainActivity activity;

    public WifiReceiver(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (
                action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION) ||
                        action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) ||
                        action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            //wifi state change
            activity.refresh_list(false);
        }
    }
}
