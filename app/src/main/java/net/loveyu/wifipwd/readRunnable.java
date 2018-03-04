package net.loveyu.wifipwd;

import android.os.Message;

import java.util.ArrayList;
import java.util.Map;

public class readRunnable implements Runnable {
    private MsgHandle handle;
    private boolean is_refresh;
    private boolean show_notify;

    public readRunnable(MsgHandle handle, boolean is_refresh, boolean show_notify) {
        this.handle = handle;
        this.is_refresh = is_refresh;
        this.show_notify = show_notify;
    }

    @Override
    public void run() {
        Message msg = handle.obtainMessage(handle.WpListUpdate);
        ArrayList<Map<String, String>> list = handle.ac.get_list();
        msg.obj = new ListMsgData(is_refresh, list);
        handle.sendMessage(msg);
    }
}
