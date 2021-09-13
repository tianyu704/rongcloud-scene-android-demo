package cn.rongcloud.voiceroom.event.wrapper;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.kit.wapper.IResultBack;

import cn.rong.combusis.feedback.FeedbackDialog;


public class EventDialogHelper {

    private final static EventDialogHelper helper = new EventDialogHelper();
    private FeedbackDialog dialog;

    public static EventDialogHelper helper() {
        return helper;
    }

    public void dismissDialog() {
        if (null != dialog) {
            dialog.dismiss();
        }
        dialog = null;
    }

    public void showTipDialog(Activity activity, String title, String message, IResultBack<Boolean> resultBack) {
        if (null == dialog || !dialog.enable()) {
            dialog = new FeedbackDialog(activity,
                    new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            EventDialogHelper.this.dialog = null;
                        }
                    });
        }
        TextView textView = new TextView(dialog.getContext());
        textView.setText(message);
        textView.setTextSize(18);
        textView.setTextColor(Color.parseColor("#343434"));
        dialog.replaceContent(title,
                "拒绝",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissDialog();
                        if (null != resultBack) resultBack.onResult(false);
                    }
                },
                "同意",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissDialog();
                        if (null != resultBack) resultBack.onResult(true);
                    }
                },
                textView);
        dialog.show();
    }
}
