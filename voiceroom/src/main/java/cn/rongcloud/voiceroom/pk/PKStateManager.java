package cn.rongcloud.voiceroom.pk;

import android.app.Activity;
import android.content.DialogInterface;
import android.util.Log;

import com.basis.widget.BottomDialog;
import com.kit.utils.KToast;
import com.kit.wapper.IResultBack;

import cn.rong.combusis.EventBus;
import cn.rong.combusis.sdk.event.EventHelper;
import cn.rong.combusis.sdk.event.wrapper.AbsPKHelper;

public class PKStateManager implements IPKState, EventBus.EventCallback, DialogInterface.OnDismissListener {
    private final static String TAG = "VoiceRoomManager";
    private String roomId;
    private final static PKStateManager manager = new PKStateManager();
    private VRStateListener stateListener;

    public PKStateManager() {
    }

    public static PKStateManager get() {
        return manager;
    }


    public void unInit() {
        roomId = null;
        EventHelper.helper().unregeister();
        EventBus.get().off(EventBus.TAG.PK_STATE, this);
    }

    public void init(String roomId, IPKState.VRStateListener listener) {
        this.roomId = roomId;
        this.stateListener = listener;
        // 注册房间事件监听
        EventHelper.helper().regeister(roomId);
        // 注册pk状态监听
        EventBus.get().on(EventBus.TAG.PK_STATE, this);
    }

    private AbsPKHelper.Type pkState = AbsPKHelper.Type.PK_NONE;

    @Override
    public void onEvent(Object... args) {
        if (args.length != 1) return;
        if (args[0] instanceof AbsPKHelper.Type) {
            pkState = (AbsPKHelper.Type) args[0];
            Log.e(TAG, "onEvent:" + pkState);
            if (AbsPKHelper.Type.PK_GOING == pkState) {
                if (null != stateListener) stateListener.onPkStart();
            } else if (AbsPKHelper.Type.PK_FINISH == pkState) {
                if (null != stateListener) stateListener.onPkStop();
            }
        }
    }

    private BottomDialog dialog;


    @Override
    public void sendPkInvitation(Activity activity, IResultBack<Boolean> resultBack) {
        if (pkState == AbsPKHelper.Type.PK_INVITE) {
            KToast.show("您已发出邀请，请耐心等待对方处理");
            return;
        }
        if (dialog != null) dialog.dismiss();
        dialog = new RoomOwerDialog(activity, resultBack).setOnCancelListener(this);
    }

    @Override
    public void cancelPkInvitation(Activity activity, IResultBack<Boolean> resultBack) {
        if (pkState != AbsPKHelper.Type.PK_INVITE) {
            KToast.show("你还未发出PK邀请");
            return;
        }
        if (dialog != null) dialog.dismiss();
        dialog = new CancelPKDialog(activity, resultBack).setOnCancelListener(this);
    }


    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        dialog = null;
    }
}
