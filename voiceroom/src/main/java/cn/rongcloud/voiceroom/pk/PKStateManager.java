package cn.rongcloud.voiceroom.pk;

import android.app.Activity;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.basis.widget.BottomDialog;
import com.kit.UIKit;
import com.kit.utils.KToast;
import com.kit.wapper.IResultBack;

import cn.rong.combusis.EventBus;
import cn.rong.combusis.sdk.event.EventHelper;
import cn.rong.combusis.sdk.event.wrapper.AbsPKHelper;
import cn.rongcloud.voiceroom.R;
import cn.rongcloud.voiceroom.api.PKState;

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
        EventBus.get().off(EventBus.TAG.PK_RESPONSE, this);
    }

    public void init(String roomId, IPKState.VRStateListener listener) {
        this.roomId = roomId;
        this.stateListener = listener;
        // 注册房间事件监听
        EventHelper.helper().regeister(roomId);
        // 注册pk状态监听
        EventBus.get().on(EventBus.TAG.PK_STATE, this);
        EventBus.get().on(EventBus.TAG.PK_RESPONSE, this);
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
        } else if (args[0] instanceof PKState) {
            PKState pkState = (PKState) args[0];
            if (pkState == PKState.reject) {
                KToast.show("您的PK邀请被拒绝");
            } else if (pkState == PKState.ignore) {
                KToast.show("您的PK邀请被忽略");
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
        dialog.show();
    }

    @Override
    public void cancelPkInvitation(Activity activity, IResultBack<Boolean> resultBack) {
        if (pkState != AbsPKHelper.Type.PK_INVITE) {
            KToast.show("你还未发出PK邀请");
            return;
        }
        if (dialog != null) dialog.dismiss();
        dialog = new CancelPKDialog(activity, resultBack).setOnCancelListener(this);
        dialog.show();
    }


    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        dialog = null;
    }

    @Override
    public void enterPkWithAnimation(View left, View in, long duration) {
        startAnimation(left, R.anim.anim_left_out, duration, true);
        startAnimation(in, R.anim.anim_right_in, duration, false);
    }

    @Override
    public void quitPkWithAnimation(View left, View in, long duration) {
        startAnimation(left, R.anim.anim_right_out, duration, true);
        startAnimation(in, R.anim.anim_left_in, duration, false);
    }

    /**
     * 开启动画
     *
     * @param view     视图
     * @param animalId anim资源id
     * @param duration 动画时长
     * @param out      是否离屏
     */
    private void startAnimation(View view, int animalId, long duration, boolean out) {
        if (null == view) return;
        Animation animation = AnimationUtils.loadAnimation(UIKit.getContext(), animalId);
        animation.setDuration(duration);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (!out) view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (out) view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(animation);
    }
}
