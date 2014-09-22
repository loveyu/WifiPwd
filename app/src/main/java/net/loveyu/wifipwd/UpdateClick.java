package net.loveyu.wifipwd;

import android.app.Dialog;
import android.view.View;

public class UpdateClick implements View.OnClickListener {
    Dialog dialog;
    String url;
    MainActivity activity;

    public UpdateClick(MainActivity activity,Dialog dialog, String url) {
        this.dialog = dialog;
        this.url = url;
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        dialog.cancel();
        activity.NotifyOpenUri(url);
    }
}
