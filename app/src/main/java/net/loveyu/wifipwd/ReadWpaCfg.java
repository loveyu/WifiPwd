package net.loveyu.wifipwd;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * 读取配置文件信息
 * Created by loveyu on 2016/1/31.
 */
public class ReadWpaCfg {
    ArrayList<Map<String, String>> list;
    private String wpa_config_path;
    private String WifiConfigStore_path;
    private boolean need_read_wap_config = false;

    ReadWpaCfg(String wpa_supplicant_path, String WifiConfigStorePath) {
        list = new ArrayList<Map<String, String>>();
        this.wpa_config_path = wpa_supplicant_path;
        this.WifiConfigStore_path = WifiConfigStorePath;
    }

    /**
     * @param force_refresh 是否强制刷新
     */
    public void read(boolean force_refresh) {
        if (!force_refresh) {
            if (list.size() > 0) {
                return;
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            need_read_wap_config = false;
            read_xml_config();
        } else {
            need_read_wap_config = true;
        }
        if (need_read_wap_config) {
            read_wpa_config();
        }
    }

    private Process get_su_process() throws IOException {
        return Runtime.getRuntime().exec("su");
    }

    private void read_xml_config() {
        StringBuilder s = new StringBuilder("");
        DataOutputStream os = null;
        BufferedReader in = null;
        try {
            Process process = get_su_process();
            os = new DataOutputStream(process.getOutputStream());
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            os.writeBytes("cat " + WifiConfigStore_path + "\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            String line;
            while ((line = in.readLine()) != null) {
                s.append(line.trim()).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ReadEX", e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (os != null) os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        close_io_stream(in, os);
        String new_str = s.toString();
        if ("".equals(new_str)) {
            need_read_wap_config = true;
            return;
        }
        parse_xml(new_str);
    }

    private void parse_xml(String xml) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        ByteArrayInputStream is;
        try {
            is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(is);
            Element root = document.getDocumentElement();
            NodeList items = root.getElementsByTagName("NetworkList");
            if (items.getLength() > 0) {
                NodeList network_list = ((Element) items.item(0)).getElementsByTagName("Network");
                for (int i = 0; i < network_list.getLength(); i++) {
                    NodeList item = ((Element) network_list.item(i)).getElementsByTagName("WifiConfiguration");
                    if (item.getLength() < 1) {
                        continue;
                    }
                    Element elem = (Element) (item.item(0));
                    NodeList wp_node_list = elem.getElementsByTagName("string");
                    if (wp_node_list.getLength() < 2) {
                        continue;
                    }
                    String ssid = "";
                    String psk = "";

                    for (int j = 0; j < wp_node_list.getLength(); j++) {
                        Element e = (Element) wp_node_list.item(j);
                        String name = e.getAttribute("name");
                        String value = e.getFirstChild().getNodeValue();
                        if ("SSID".equals(name)) {
                            ssid = value;
                        } else if ("PreSharedKey".equals(name)) {
                            psk = value;
                        }
                    }
                    add_kv(ssid, psk);
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    private void read_wpa_config() {
        StringBuilder s = new StringBuilder("");
        DataOutputStream os = null;
        BufferedReader in = null;
        try {
            Process process = get_su_process();
            os = new DataOutputStream(process.getOutputStream());
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            os.writeBytes("cat " + wpa_config_path + "\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            String line;
            while ((line = in.readLine()) != null) {
                s.append(line.trim()).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Read", e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (os != null) os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        parse(s.toString());
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
        String[] list = content.split("\\n");
        HashMap<String, String> map = new HashMap<String, String>();
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

    private void add_kv(String ssid, String psk) {
        if (ssid == null || psk == null || "".equals(ssid) || psk.equals("")) {
            return;
        }
        HashMap<String, String> map = new HashMap<String, String>();

        if (ssid.charAt(0) == '"') {
            ssid = ssid.substring(1, ssid.length() - 1);
        } else {
            ssid = convertUTF8ToString(ssid);
        }
        if ("".equals(ssid)) {
            return;
        }
        psk = psk.substring(1, psk.length() - 1);
        if ("".equals(psk)) {
            return;
        }
        map.put("ssid", ssid);
        map.put("psk", psk);
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
