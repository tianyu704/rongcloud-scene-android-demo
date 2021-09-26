package cn.rongcloud.voiceroom.pk;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.basis.net.oklib.OkApi;
import com.basis.net.oklib.WrapperCallBack;
import com.basis.net.oklib.wrapper.Wrapper;
import com.basis.widget.BottomDialog;
import com.kit.UIKit;
import com.kit.cache.GsonUtil;
import com.kit.utils.KToast;
import com.kit.utils.Logger;
import com.kit.wapper.IResultBack;

import java.util.HashMap;
import java.util.Map;

import cn.rong.combusis.EventBus;
import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.sdk.VoiceRoomApi;
import cn.rong.combusis.sdk.event.EventHelper;
import cn.rong.combusis.sdk.event.wrapper.IEventHelp;
import cn.rongcloud.voiceroom.R;
import cn.rongcloud.voiceroom.api.PKState;
import cn.rongcloud.voiceroom.model.RCPKInfo;
import cn.rongcloud.voiceroom.pk.widget.IPK;

/**
 * 邀请PK状态管理
 * // PK邀请
 * 1、显示在线房主列表，选则发起PK邀请 进入已邀请状态 该状态不可再次邀请
 * 2、对方拒绝PK或忽略PK邀请，恢复原状 可再次邀请
 * 3、对方同意PK,进入PK状态，上报pk状态：开始
 * // 开始PK
 * 4、PKView开启PK记时
 * 5、PKView记时结束，执行api：退出PK ，进入pk的惩罚阶段，并上报pk状态：惩罚阶段
 * // 惩罚阶段
 * 6、PKView开启惩罚即时
 * 7、pk惩罚即时结束，Pk流程结束，上报pk状态：pk结束
 * //手动结束pk
 * 8、pk开始后，PK双方有一个方手动退出pk， 直接结束本次pk，并上报pk状态：pk结束
 * <p>
 * 被邀请侧pk状态管理
 */
public class PKStateManager implements IPKState, EventBus.EventCallback, DialogInterface.OnDismissListener {
    private final static String TAG = "VoiceRoomManager";
    private String roomId;
    private IPK pkView;
    private final static PKStateManager manager = new PKStateManager();
    private VRStateListener stateListener;
    private RCPKInfo rcpkInfo;

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

    private IEventHelp.Type pkState = IEventHelp.Type.PK_NONE;

    public void init(String roomId, IPK pkView, IPKState.VRStateListener listener) {
        this.roomId = roomId;
        this.pkView = pkView;
        this.stateListener = listener;
        // 注册房间事件监听
        EventHelper.helper().regeister(roomId);
        // 注册pk状态监听
        EventBus.get().on(EventBus.TAG.PK_STATE, this);
        EventBus.get().on(EventBus.TAG.PK_RESPONSE, this);
    }

    private BottomDialog dialog;

    @Override
    public void sendPkInvitation(Activity activity, IResultBack<Boolean> resultBack) {
        if (pkState == IEventHelp.Type.PK_INVITE) {
            KToast.show("您已发出邀请，请耐心等待对方处理");
            return;
        }
        if (dialog != null) dialog.dismiss();
        dialog = new RoomOwerDialog(activity, resultBack).setOnCancelListener(this);
        dialog.show();
    }

    @Override
    public void cancelPkInvitation(Activity activity, IResultBack<Boolean> resultBack) {
        if (pkState != IEventHelp.Type.PK_INVITE) {
            KToast.show("你还未发出PK邀请");
            return;
        }
        if (dialog != null) dialog.dismiss();
        dialog = new CancelPKDialog(activity, resultBack).setOnCancelListener(this);
        dialog.show();
    }

    @Override
    public void refreshPKGiftRank() {
        Map<String, Object> params = new HashMap<>();
        OkApi.post(VRApi.getPKInfo(roomId), params, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (null != result && result.ok()) {
                    // 解析数据
                }
            }
        });
    }

    @Override
    public void onEvent(Object... args) {
        if (args.length < 1) return;
        if (args[0] instanceof IEventHelp.Type) {
            pkState = (IEventHelp.Type) args[0];
            Log.e(TAG, "onEvent:" + pkState);
            // pk邀请成功 对方同意 进入pk开始阶段
            if (IEventHelp.Type.PK_GOING == pkState) {
                if (args.length == 2) {
                    rcpkInfo = (RCPKInfo) args[1];
                }
                handlePKStart();
            } else if (IEventHelp.Type.PK_FINISH == pkState) {
                // 避免手动结束导致问题 在pk记时结束
                // handlePKStop();
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

    /**
     * 处理pk开始阶段
     */
    void handlePKStart() {
        if (null != stateListener) stateListener.onPkStart();
        // 上报pk状态：开始
        reportPKState(0);
        // pk记时
        if (null != pkView) {
            pkView.pkStart(new IPK.OnTimerEndListener() {
                @Override
                public void onTimerEnd() {
                    handlePkPunish();
                }
            });
        }
    }

    /**
     * 处理pk惩罚阶段
     * 1、api quitPK
     * 2、上报pk状态：惩罚阶段
     */
    void handlePkPunish() {
        //调用api 退出pk
        VoiceRoomApi.getApi().quitPK(new IResultBack<Boolean>() {
            @Override
            public void onResult(Boolean aBoolean) {
                //退出成功，上报pk状态进入惩罚阶段
                reportPKState(1);
                //惩罚记时
                if (null != pkView) {
                    pkView.punishStart(new IPK.OnTimerEndListener() {
                        @Override
                        public void onTimerEnd() {
                            handlePKStop();
                        }
                    });
                }
            }
        });
    }

    /**
     * 处理pk结束
     */
    void handlePKStop() {
        if (null != stateListener) stateListener.onPkStop();
        // 上报pk状态：结束
        reportPKState(2);
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

    /**
     * @param status pk状态 0：开始 1：暂停，惩罚阶段（送礼物不计算了） 2：结束
     */
    void reportPKState(int status) {
        if (null != rcpkInfo) {
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("roomId", roomId);
        String toRoomId = TextUtils.equals(rcpkInfo.getInviteeRoomId(), roomId) ? rcpkInfo.getInviterRoomId() : rcpkInfo.getInviteeRoomId();
        params.put("toRoomId", toRoomId);
        params.put("status", status);
        OkApi.post(VRApi.PK_STATE, params, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                Logger.e(TAG, "reportPKState:" + GsonUtil.obj2Json(result));
            }
        });
    }
}
