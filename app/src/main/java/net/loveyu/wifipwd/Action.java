package net.loveyu.wifipwd;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * 操作类
 */
public class Action {

    private Context context;

    public Action(Context context) {
        this.context = context;
    }

    /**
     * 获取热点和密码列表
     *
     * @return 异常时返回NULL
     */
    public ArrayList<Map<String, String>> get_list() {
        try {
            ReadWpaCfg cfg = new ReadWpaCfg("/data/misc/wifi/wpa_supplicant.conf");
            return cfg.getPasswordList();
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("Read", e.getMessage());
            return null;
        }
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
