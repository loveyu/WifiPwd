package net.loveyu.wifipwd;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.DisplayMetrics;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;

import static java.net.Proxy.Type.HTTP;

public class Report implements Runnable {
    private MainActivity context;
    private String check_update_url;
    private boolean hasRoot;

    Report(MainActivity context, String check_update_url, boolean hasRoot) {
        this.context = context;
        this.check_update_url = check_update_url;
        this.hasRoot = hasRoot;
    }

    @Override
    public void run() {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            String update_url = check_update_url + "?version=" + pi.versionName;

            URL url = new URL(update_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);

            DisplayMetrics dm = context.getResources().getDisplayMetrics();

            String data = "uid=" + URLEncoder.encode(getUid().toString(), "UTF-8") +
                    "&version=" + URLEncoder.encode(pi.versionName, "UTF-8") +
                    "&version_code=" + pi.versionCode +
                    "&phone=" + URLEncoder.encode(android.os.Build.BRAND, "UTF-8") +
                    "&phone_model=" + URLEncoder.encode(android.os.Build.MODEL, "UTF-8") +
                    "&width=" + dm.widthPixels +
                    "&height=" + dm.heightPixels +
                    "&android=" + android.os.Build.VERSION.RELEASE +
                    "&android_sdk=" + Build.VERSION.SDK_INT +
                    "&has_root=" + (hasRoot ? 1 : 0);

            //获取输出流
            OutputStream out = conn.getOutputStream();
            out.write(data.getBytes());
            out.flush();

            conn.getResponseCode();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取机器唯一ID
     *
     * @return 唯一UUID
     */
    private UUID getUid() {
        DeviceUuidFactory unid = new DeviceUuidFactory(this.context);
        return unid.getDeviceUuid();
    }
}