package net.loveyu.wifipwd;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class DeviceUuidFactory {
    private static final String PREFS_FILE = "device_id.xml";
    private static final String PREFS_DEVICE_ID = "device_id";
    private static volatile UUID uuid;

    public DeviceUuidFactory(Context context) {
        if (uuid == null) {
            synchronized (DeviceUuidFactory.class) {
                if (uuid == null) {
                    final SharedPreferences prefs = context
                            .getSharedPreferences(PREFS_FILE, 0);
                    final String id = prefs.getString(PREFS_DEVICE_ID, null);
                    if (id != null) {
                        // Use the ids previously computed and stored in the
                        // prefs file
                        uuid = UUID.fromString(id);
                    } else {
                        uuid = UUID.randomUUID();
                        // Write the value out to the prefs file
                        prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString()).apply();
                    }
                }
            }
        }
    }

    public UUID getDeviceUuid() {
        return uuid;
    }
}
