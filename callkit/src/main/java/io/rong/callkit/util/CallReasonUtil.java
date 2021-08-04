package io.rong.callkit.util;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.rongcloud.common.utils.UIKit;

import io.rong.callkit.R;
import io.rong.calllib.RongCallCommon;

public class CallReasonUtil {

    private static String getString(@NonNull @StringRes int id) {
        return UIKit.getResources().getString(id);
    }

    public static void showToastByReason(RongCallCommon.CallDisconnectedReason reason) {
        String text = null;
        switch (reason) {
            case CANCEL:
                text = getString(R.string.rc_voip_mo_cancel);
                break;
            case REJECT:
                text = getString(R.string.rc_voip_mo_reject);
                break;
            case NO_RESPONSE:
            case BUSY_LINE:
                text = getString(R.string.rc_voip_mo_no_response);
                break;
            case REMOTE_BUSY_LINE:
                text = getString(R.string.rc_voip_mt_busy_toast);
                break;
            case REMOTE_CANCEL:
                text = getString(R.string.rc_voip_mt_cancel);
                break;
            case REMOTE_REJECT:
                text = getString(R.string.rc_voip_mt_reject);
                break;
            case REMOTE_NO_RESPONSE:
                text = getString(R.string.rc_voip_mt_no_response);
                break;
            case NETWORK_ERROR:
                if (!CallKitUtils.isNetworkAvailable(UIKit.getContext())) {
                    text = getString(R.string.rc_voip_call_network_error);
                } else {
                    text = getString(R.string.rc_voip_call_terminalted);
                }
                break;
            case REMOTE_HANGUP:
            case HANGUP:
            case INIT_VIDEO_ERROR:
                text = getString(R.string.rc_voip_call_terminalted);
                break;
            case OTHER_DEVICE_HAD_ACCEPTED:
                text = getString(R.string.rc_voip_call_other);
                break;
        }
        if (text != null) {
            Toast.makeText(UIKit.getContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

}
