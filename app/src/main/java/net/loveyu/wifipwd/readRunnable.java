package net.loveyu.wifipwd;

import android.os.Message;

import java.util.ArrayList;
import java.util.Map;

public class readRunnable implements Runnable {
    private MsgHandle handle;
    private boolean is_refresh;
    private boolean show_notify;

    readRunnable(MsgHandle handle, boolean is_refresh, boolean show_notify) {
        this.handle = handle;
        this.is_refresh = is_refresh;
        this.show_notify = show_notify;
    }

    @Override
    public void run() {
        Message msg = handle.obtainMessage(MsgHandle.WpListUpdate);
        //if need notify, force refresh
        ArrayList<Map<String, String>> list = handle.ac.get_list(show_notify);
        msg.obj = new ListMsgData(is_refresh, list, show_notify);
        handle.sendMessage(msg);
    }
}
