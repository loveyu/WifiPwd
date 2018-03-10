package net.loveyu.wifipwd;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.content.ClipboardManager;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
    private ArrayList<Map<String, String>> list;

    private boolean is_root_check = false;

    private WifiAdapter wifiAdapter = null;

    private Action ac;

    private ListView lv;

    private int log = 0;

    public MsgHandle handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale();
        ac = new Action(this);
        handler = new MsgHandle(this, ac);
        if (ac.check_root()) {
            setContentView(R.layout.activity_main);
            registerWifiChange();
            handler.startReadList(false, false);
        } else {
            setContentView(R.layout.activity_no_root);
        }
    }

    public void setList(ArrayList<Map<String, String>> li) {
        list = li;
        TextView tv = (TextView) findViewById(R.id.textViewNotice);
        if (list == null) {
            tv.setText(getString(R.string.read_wifi_list_error));
        } else {
            is_root_check = true;
            if (wifiAdapter == null) {
                wifiAdapter = new WifiAdapter(this, list);
            }
            if (list.size() == 0) {
                tv.setText(getString(R.string.wifi_list_is_empty));
            } else {
                lv = (ListView) findViewById(R.id.listView);
                lv.setAdapter(wifiAdapter);
                registerForContextMenu(lv);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        view.showContextMenu();
                    }
                });
                String text = getString(R.string.wifi_list_number) + list.size();
                tv.setText(text);
            }
        }
    }

    private void setLocale() {
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.locale = Locale.getDefault();
        resources.updateConfiguration(config, dm);
    }

    private void registerWifiChange() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new WifiReceiver(this), filter);
    }

    public void refresh_list(boolean show_notify) {
        if (log++ == 0 || !is_root_check) {
            //first not log
            return;
        }
        if (!handler.startReadList(true, show_notify)) {
            return;
        }
        if (!is_root_check) {
            Toast.makeText(this, getString(R.string.can_not_root_permission), Toast.LENGTH_SHORT).show();
        }
    }

    public void refreshLvList(ArrayList<Map<String, String>> list, boolean show_notify) {
        if (list == null) {
            Toast.makeText(this, getString(R.string.read_wifi_list_error), Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (list.size() == 0) {
                Toast.makeText(this, getString(R.string.wifi_list_is_empty), Toast.LENGTH_SHORT).show();
                return;
            } else {
                wifiAdapter.list = list;
                lv.setAdapter(wifiAdapter);
            }
        }
        lv.deferNotifyDataSetChanged();
        if (show_notify) {
            Toast.makeText(this, getString(R.string.refresh_success), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh_list(false);
    }

    private void open_url(String url) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_VIEW);
        MainActivity.this.startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, R.string.copy_password, 0, getString(R.string.copy_password));
        menu.add(Menu.NONE, R.string.copy_ssid, 0, getString(R.string.copy_ssid));
        menu.add(Menu.NONE, R.string.copy_ssid_and_password, 0, getString(R.string.copy_ssid_and_password));
    }

    //选中菜单Item后触发
    public boolean onContextItemSelected(MenuItem item) {
        //关键代码在这里
        AdapterView.AdapterContextMenuInfo menuInfo;
        menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int indexListView = menuInfo.position;
        if (list == null) return false;
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        switch (item.getItemId()) {
            case R.string.copy_password:
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, list.get(indexListView).get("psk")));
                break;
            case R.string.copy_ssid:
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, list.get(indexListView).get("ssid")));
                break;
            case R.string.copy_ssid_and_password:
                Map<String, String> s = list.get(indexListView);
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null,
                        getString(R.string.wifi_ssid) + s.get("ssid") + "\n" + getString(R.string.password) + s.get("psk")));
                break;
            default:
                return false;
        }
        Toast.makeText(this, getString(R.string.already_copy), Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int aFeatureId, MenuItem aMenuItem) {
        if (aFeatureId== Window.FEATURE_CONTEXT_MENU)
            return onContextItemSelected(aMenuItem);
        else
            return super.onMenuItemSelected(aFeatureId, aMenuItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_exit:
                finish();
                return true;
            case R.id.action_help:
                open_url("https://www.loveyu.org/3356.html?from=android_wifipwd&v=" + getAppVersionName(this));
                return true;
            case R.id.open_source:
                open_url("https://github.com/loveyu/WifiPwd");
                return true;
            case R.id.refresh_list:
                refresh_list(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
        return versionName;
    }
}
