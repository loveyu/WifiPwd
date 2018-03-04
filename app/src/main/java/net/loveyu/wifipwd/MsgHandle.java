package net.loveyu.wifipwd;

import android.os.Handler;
import android.os.Message;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

public class MsgHandle extends Handler {

    public MainActivity mainActivity;
    public Action ac;

    private final static int WpListUpdate = 1;

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
                mainActivity.setList(data.list);
                if (data.is_refresh) {
                    mainActivity.refreshLvList();
                }

                read_list_is_finish = true;
                break;
        }
    }

    public boolean startReadList(final boolean is_refresh) {
        if (!read_list_is_finish) {
            return false;
        }
        read_list_is_finish = false;
        final MsgHandle self = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = self.obtainMessage(WpListUpdate);
                ArrayList<Map<String, String>> list = self.ac.get_list();
                msg.obj = new ListMsgData(is_refresh, list);
                self.sendMessage(msg);
            }
        }).start();
        return true;
    }
}
