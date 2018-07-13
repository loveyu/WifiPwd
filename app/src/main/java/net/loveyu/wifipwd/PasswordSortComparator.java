package net.loveyu.wifipwd;

import java.util.Comparator;
import java.util.Map;

/**
 * 排序类
 */
public class PasswordSortComparator implements Comparator<Map<String, String>> {

    private String currentSSID;

    public PasswordSortComparator(String currentSSID) {
        this.currentSSID = currentSSID;
    }

    @Override
    public int compare(Map<String, String> t1, Map<String, String> t2) {
        String ssid_1 = t1.get("ssid");
        String ssid_2 = t2.get("ssid");

        if (ssid_1.equals(ssid_2)) {
            return 0;
        }

        if (ssid_1.equals(currentSSID)) {
            return -1;
        }
        if (ssid_2.equals(currentSSID)) {
            return 1;
        }
        return ssid_1.compareToIgnoreCase(ssid_2);
    }
}
