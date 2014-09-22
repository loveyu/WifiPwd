package net.loveyu.wifipwd;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Action {
    private Context context;

    public Action(Context context) {
        this.context = context;
    }

    public ArrayList<String[]> get_list() {
        ArrayList<String[]> list = new ArrayList<String[]>();
        try {
            String path = context.getCacheDir().getPath();
            if (run_cmd("cp /data/misc/wifi/wpa_supplicant.conf " + path)) {
                path += "/wpa_supplicant.conf";
                run_cmd("chmod 777 " + path);
                BufferedReader fr = new BufferedReader(new FileReader(path));
                String s, ssid = null;
                while ((s = fr.readLine()) != null) {
                    String[] arr = s.split("=");
                    if (arr.length != 2) continue;
                    arr[0] = arr[0].trim();
                    arr[1] = arr[1].trim();
                    if ("ssid".equals(arr[0])) {
                        ssid = arr[1].substring(1, arr[1].length() - 1);
                    } else if ("psk".equals(arr[0])) {
                        if (ssid != null) {
                            String[] tmp = new String[2];
                            tmp[0] = ssid;
                            tmp[1] = arr[1].substring(1, arr[1].length() - 1);
                            list.add(tmp);
                            ssid = null;
                        }
                    }
                }
                fr.close();
                File f = new File(path);
                if (!f.delete()) {
                    Log.e("Delete", "cache file delete error.");
                }
            } else {
                Log.e("CP", "copy file error.");
                Toast.makeText(context, context.getString(R.string.can_no_read_file), Toast.LENGTH_LONG).show();
                return null;
            }
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("Read", e.getMessage());
            return null;
        }
        return list;
    }

    public boolean run_cmd(String command) {
        Process process = null;
        DataOutputStream os = null;
        int rt = 1;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            rt = process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
        return rt == 0;
    }

    public boolean check_root() {
        try {
            try {
                return run_cmd("system/bin/mount -o rw,remount -t rootfs /data");
            } catch (Exception e) {
                Toast.makeText(context, context.getString(R.string.can_not_root_permission) + ":" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
