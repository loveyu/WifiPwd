package net.loveyu.wifipwd;

import java.util.ArrayList;
import java.util.Map;

public class ListMsgData {
    public boolean is_refresh;
    public ArrayList<Map<String, String>>  list;
    public ListMsgData(boolean is_refresh, ArrayList<Map<String, String>>  list) {
        this.is_refresh = is_refresh;
        this.list = list;
    }
}
