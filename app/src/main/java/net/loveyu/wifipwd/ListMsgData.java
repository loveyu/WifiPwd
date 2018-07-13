package net.loveyu.wifipwd;

import java.util.ArrayList;
import java.util.Map;

public class ListMsgData {
    public boolean is_refresh;
    public boolean show_notify;
    public ArrayList<Map<String, String>>  list;
    ListMsgData(boolean is_refresh, ArrayList<Map<String, String>> list, boolean show_notify) {
        this.is_refresh = is_refresh;
        this.show_notify = show_notify;
        this.list = list;
    }
}
