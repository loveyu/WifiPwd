package net.loveyu.wifipwd;

import android.os.Handler;
import android.os.Message;

public class MsgHandle extends Handler {

    private MainActivity mainActivity;
    public Action ac;

    public final static int WpListUpdate = 1;

    private boolean read_list_is_finish = true;

    MsgHandle(MainActivity mainActivity, Action ac) {
        super();
        this.mainActivity = mainActivity;
        this.ac = ac;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case WpListUpdate:
                ListMsgData data = (ListMsgData) msg.obj;
                if (data.is_refresh) {
                    mainActivity.refreshLvList(data.list, data.show_notify);
                } else {
                    mainActivity.setList(data.list);
                }
                read_list_is_finish = true;
                break;
        }
    }

    public boolean startReadList(boolean is_refresh, boolean show_notify) {
        if (!read_list_is_finish) {
            return false;
        }
        read_list_is_finish = false;
        new Thread(new readRunnable(this, is_refresh, show_notify)).start();
        return true;
    }
}
