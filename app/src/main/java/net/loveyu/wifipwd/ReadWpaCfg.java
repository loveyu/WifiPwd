package net.loveyu.wifipwd;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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

    public ReadWpaCfg(String path) throws IOException {
        list = new ArrayList<Map<String, String>>();
        String s = "";
        DataOutputStream os = null;
        BufferedReader in = null;
        try {
            Process p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("cat " + path + "\n");
            os.writeBytes("exit\n");
            os.flush();
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                s += line.trim() + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                os.close();
            }
            if (in != null) {
                in.close();
            }
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

    public ArrayList<Map<String, String>> getPasswordList() {
        ArrayList<Map<String, String>> rt = new ArrayList<Map<String, String>>();
        for (Map<String, String> map : this.list) {
            if (map.containsKey("psk") && map.containsKey("ssid")) {
                rt.add(map);
            }
        }
        return rt;
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
