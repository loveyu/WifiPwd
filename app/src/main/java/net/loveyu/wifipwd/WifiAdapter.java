package net.loveyu.wifipwd;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class WifiAdapter extends BaseAdapter {
    Context context;
    ArrayList<String[]> list;

    public WifiAdapter(Context context, ArrayList<String[]> list) {
        super();
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        String[] obj = list.get(position);
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = View.inflate(context, R.layout.wifi_item, null);
            holder = new ViewHolder();
            holder.ssid = (TextView) convertView.findViewById(R.id.textView_i_ssid);
            holder.pwd = (TextView) convertView.findViewById(R.id.textView_i_pwd);
            convertView.setTag(holder);
        }
        holder.ssid.setText(obj[0]);
        holder.pwd.setText(obj[1]);
        return convertView;
    }

}

