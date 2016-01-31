package net.loveyu.wifipwd;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Report implements Runnable {
    private MainActivity context;
    private int now_version = 0;
    private String NewVersion = "";
    private String DownloadUrl = "";
    private ArrayList<String> Message = null;
    private String check_update_url;
    public Report(MainActivity context,String check_update_url) {
        this.context = context;
        this.check_update_url = check_update_url;
    }

    /**
     * 打开更新提示框，必须通过UI主线程进行调用
     */
    public void open_dialog(Context context) {
        if (!"".equals(NewVersion) && !"".equals(DownloadUrl)) {
            String new_s = context.getResources().getString(R.string.found_version_update);
            View dv = View.inflate(context, R.layout.dialog_update, null);
            String html = "<p>" + new_s + " : <strong>" + NewVersion + "</strong></p>";
            if (Message != null && Message.size() > 0) {
                html += "<p>";
                int i = 0;
                for (String msg : Message) {
                    html += ((i++ > 0 ? "<br>" : "") + msg);
                }
                html += "</p>";
            }
            TextView v = (TextView) dv.findViewById(R.id.textView);
            v.setMovementMethod(ScrollingMovementMethod.getInstance());
            v.setAutoLinkMask(Linkify.ALL);
            v.setText(Html.fromHtml(html));
            Dialog ad = new Dialog(context);
            ad.setTitle(new_s);
            ad.setContentView(dv);
            dv.findViewById(R.id.button).setOnClickListener(new UpdateClick(this.context, ad, DownloadUrl));
            ad.show();
            NewVersion = "";
            DownloadUrl = "";
            Message = null;
        }
    }

    @Override
    public void run() {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            String update_url = check_update_url+"?version=" + pi.versionName;
            HttpPost httpRequest = new HttpPost(update_url);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("uid", getUid()));
            params.add(new BasicNameValuePair("version", pi.versionName));
            params.add(new BasicNameValuePair("version_code", "" + pi.versionCode));
            now_version = pi.versionCode;
            params.add(new BasicNameValuePair("phone", android.os.Build.BRAND));
            params.add(new BasicNameValuePair("phone_model", android.os.Build.MODEL));
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            params.add(new BasicNameValuePair("width", "" + dm.widthPixels));
            params.add(new BasicNameValuePair("height", "" + dm.heightPixels));
            params.add(new BasicNameValuePair("android", android.os.Build.VERSION.RELEASE));
            params.add(new BasicNameValuePair("android_sdk", "" + Build.VERSION.SDK_INT));
            //发出HTTP request
            httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            //取得HTTP response
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
            //若状态码为200 ok
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                //取出回应字串
                String strResult = EntityUtils.toString(httpResponse.getEntity());
//                Log.d("Json", strResult);
                parseJson(strResult);
            }
        } catch (Exception e) {
            Log.e("Update exception", e.getMessage());
        }
    }

    private void parseJson(String content) {
        try {
            JSONObject json = new JSONObject(content);
            int version_code = json.getInt("version_code");
            if (version_code > now_version) {
                //发现新版本
                NewVersion = json.getString("version");
                DownloadUrl = json.getString("url");
                JSONArray ja = json.getJSONArray("msg");
                if (Message == null) {
                    Message = new ArrayList<String>();
                }
                for (int i = 0; i < ja.length(); i++) {
                    Message.add(ja.getString(i));
                }
                context.NotifyVersionUpdate(this);
            }
        } catch (JSONException e) {
            Log.e("JsonError", "Parse json error.");
            Log.e("JsonError", e.getMessage());
        }
    }

    /**
     * 获取机器唯一ID
     *
     * @return 唯一UUID
     */
    public String getUid() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String tmDevice, androidId;
        tmDevice = tm.getDeviceId();
        androidId = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), (long) tmDevice.hashCode());
        return deviceUuid.toString();
    }
}
