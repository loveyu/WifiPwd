package net.loveyu.wifipwd;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 读取配置文件信息
 * Created by loveyu on 2016/1/31.
 */
public class ReadWpaCfg {
    ArrayList<Map<String, String>> list;

    private Process p = null;

    private String path;

    public ReadWpaCfg(String path) {
        list = new ArrayList<Map<String, String>>();
        this.path = path;
    }

    public void read() throws IOException {
        String s = "";
        DataOutputStream os = null;
        BufferedReader in = null;

        try {
            if (p == null) {
                p = Runtime.getRuntime().exec("su");
            }
            os = new DataOutputStream(p.getOutputStream());
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            os.writeBytes("cat " + path + "\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            String line;
            while ((line = in.readLine()) != null) {
                s += line.trim() + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Read", e.getMessage());
        }
        try {
            if (os != null) {
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        parse(s);

    }

    private void parse(String content) {
        Pattern pattern = Pattern.compile("network=\\{\\n([\\s\\S]+?)\\n\\}");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            add(matcher.group());
        }
    }

    private void add(String content) {
        content = content.substring(9, content.length() - 2);
        HashMap<String, String> map = new HashMap<String, String>();
        String[] list = content.split("\\n");
        String k, v;
        for (String info : list) {
            int index = info.indexOf("=");
            if (index > -1) {
                k = info.substring(0, index);
                v = info.substring(index + 1);
            } else {
                continue;
            }
            if ("ssid".equals(k)) {
                if (v.charAt(0) == '"') {
                    v = v.substring(1, v.length() - 1);
                } else {
                    v = convertUTF8ToString(v);
                }
            } else if ("psk".equals(k)) {
                v = v.substring(1, v.length() - 1);
            }
            if (v == null) {
                continue;
            }
            map.put(k, v);
        }
        this.list.add(map);
    }

    /**
     * 读取密码列表
     *
     * @param context 传入上下文数据，用于附加数据及排序
     * @return 返回完整的数据列表
     */
    public ArrayList<Map<String, String>> getPasswordList(Context context) {
        ArrayList<Map<String, String>> rt = new ArrayList<Map<String, String>>();
        for (Map<String, String> map : this.list) {
            if (map.containsKey("psk") && map.containsKey("ssid")) {
                rt.add(map);
            }
        }
        return this.sortPasswordList(rt, context);
    }

    /**
     * 对密码进行排序
     *
     * @param passwordList 已保存的密码列表
     * @param context      传入上下文数据，用于附加数据及排序
     * @return 返回排序后的数据
     */
    private ArrayList<Map<String, String>> sortPasswordList(ArrayList<Map<String, String>> passwordList, Context context) {
        if (passwordList.size() < 1) {
            //数据不足原样返回
            return passwordList;
        }

        Collections.sort(passwordList, new PasswordSortComparator(getCurrentSSID(context)));

        return passwordList;
    }

    private String getCurrentSSID(Context context) {
        try {
            //读取当前链接的Wifi信息
            WifiManager mWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (!mWifi.isWifiEnabled()) {
                //wifi 未启用
                return "";
            }
            WifiInfo wifiInfo = mWifi.getConnectionInfo();
            String ssid = wifiInfo.getSSID();

            if (ssid.charAt(0) == '"') {
                ssid = ssid.substring(1, ssid.length() - 1);
            } else {
                ssid = convertUTF8ToString(ssid);
            }

            return ssid;
        } catch (Exception ex) {
            //出错了就直接返回
            return "";
        }
    }

    private static String convertUTF8ToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        try {
            s = s.toUpperCase();
            int total = s.length() / 2;
            int pos = 0;
            byte[] buffer = new byte[total];
            for (int i = 0; i < total; i++) {
                int start = i * 2;
                buffer[i] = (byte) Integer.parseInt(s.substring(start, start + 2), 16);
                pos++;
            }
            return new String(buffer, 0, pos, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
