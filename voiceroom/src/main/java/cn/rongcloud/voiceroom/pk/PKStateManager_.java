package cn.rongcloud.voiceroom.pk;

import android.app.Activity;
import android.content.DialogInterface;
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
import com.rongcloud.common.utils.AccountStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rong.combusis.EventBus;
import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.sdk.VoiceRoomApi;
import cn.rong.combusis.sdk.event.EventHelper;
import cn.rong.combusis.sdk.event.wrapper.IEventHelp;
import cn.rongcloud.voiceroom.R;
import cn.rongcloud.voiceroom.model.PKResponse;
import cn.rongcloud.voiceroom.model.RCPKInfo;
import cn.rongcloud.voiceroom.pk.domain.PKInfo;
import cn.rongcloud.voiceroom.pk.widget.IPK;

/**
 * 邀请PK状态管理
 * // PK邀请
 * 1、显示在线房主列表，选则发起PK邀请 进入已邀请状态 该状态不可再次邀请
 * 2、对方拒绝PK或忽略PK邀请，恢复原状 可再次邀请
 * 3、对方同意PK,进入PK状态，上报pk状态：开始
 * // 开始PK
 * 4、PKView开启PK记时
 * 5、PKView记时结束， ，进入pk的惩罚阶段，并上报pk状态：惩罚阶段
 * // 惩罚阶段
 * 6、PKView开启惩罚即时
 * 7、pk惩罚即时结束，Pk流程结束，上报pk状态：pk结束 执行api：退出PK
 * //手动结束pk
 * 8、pk开始后，PK双方有一个方手动退出pk， 直接结束本次pk，并上报pk状态：pk结束
 * <p>
 * 被邀请侧pk状态管理
 * 1、接收到pk邀请 显示弹框
 * 2、同意或拒绝，60s后不出里自动忽略
 * 3、结束pkGoning回调 进入pk阶段
 * 4、pk记时结束 进入惩罚阶段
 * 5、惩罚记时结束 暂不做处理，约定有邀请方quitPK
 * 6、等待pkFinish回调 结束pk流程
 */
public class PKStateManager_ implements IPKState, EventBus.EventCallback, DialogInterface.OnDismissListener {
    private final static String TAG = "PKStateManager";
    private final static PKStateManager_ manager = new PKStateManager_();
    private String roomId, pkRoomId;
    private IPK pkView;
    private VRStateListener stateListener;
    // 标是否是邀请者 pk记时结束 约定邀请者调用quitPk
//    private boolean isInviter;
    private RCPKInfo rcpkInfo;
    private IEventHelp.Type pkState = IEventHelp.Type.PK_NONE;
    private BottomDialog dialog;
    private PKInfo lastLeftInfo;
    private PKInfo lastRightInfo;

    public PKStateManager_() {
    }

    public static PKStateManager_ get() {
        return manager;
    }

    public void unInit() {
        roomId = null;
        EventHelper.helper().unregeister();
        EventBus.get().off(EventBus.TAG.PK_STATE, this);
        EventBus.get().off(EventBus.TAG.PK_RESPONSE, this);
    }

    public void init(String roomId, IPK pkView, VRStateListener listener) {
        this.roomId = roomId;
        this.pkView = pkView;
        this.stateListener = listener;
        // 注册房间事件监听
        EventHelper.helper().regeister(roomId, null);
        // 注册pk状态监听
        EventBus.get().on(EventBus.TAG.PK_STATE, this);
        EventBus.get().on(EventBus.TAG.PK_RESPONSE, this);
    }

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
    public void quitPK(Activity activity) {
        VoiceRoomApi.getApi().quitPK(new IResultBack<Boolean>() {
            @Override
            public void onResult(Boolean aBoolean) {
                //手动退出成功，上报pk状态：pk结束
                reportPKState(2);
                //惩罚记时
                if (null != pkView) pkView.pkStop();
                // 回调状态
                if (null != stateListener) stateListener.onPkStop();
            }
        });
    }

    @Override
    public void refreshPKGiftRank() {
        // load left pk info
        OkApi.get(VRApi.getPKInfo(roomId), null, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                Logger.e(TAG, "result:" + GsonUtil.obj2Json(result));
                if (null != result && result.ok()) {
                    PKInfo pkInfo = result.get(PKInfo.class);
                    Logger.e(TAG, "pkInfo:" + GsonUtil.obj2Json(pkInfo));
                    refreshPKInfo(pkInfo, null);
                }
            }
        });
        // load right pk info
        OkApi.get(VRApi.getPKInfo(pkRoomId), null, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (null != result && result.ok()) {
                    if (null != result && result.ok()) {
                        refreshPKInfo(null, result.get(PKInfo.class));
                    }
                }
            }
        });
    }

    void refreshPKInfo(PKInfo left, PKInfo right) {
        if (null != left) {
            lastLeftInfo = left;
        }
        if (null != right) {
            lastRightInfo = right;
        }
        // set score
        pkView.setPKScore(null == lastLeftInfo ? 0 : lastLeftInfo.getScore(),
                null == lastRightInfo ? 0 : lastRightInfo.getScore());

        List<String> lefts = new ArrayList<>();
        if (null != lastLeftInfo) {
            List<User> users = lastLeftInfo.getUserInfoList();
            int ls = null == users ? 0 : users.size();
            for (int i = 0; i < ls; i++) {
                lefts.add(users.get(i).getPortraitUrl());
            }
        }

        List<String> rights = new ArrayList<>();
        if (null != lastRightInfo) {
            List<User> users = lastRightInfo.getUserInfoList();
            int ls = null == users ? 0 : users.size();
            for (int i = 0; i < ls; i++) {
                rights.add(users.get(i).getPortraitUrl());
            }
        }
        // set sender rank
        pkView.setGiftSenderRank(lefts, rights);
    }

    @Override
    public void onEvent(String tag, Object... args) {
        if (args.length < 1) return;
        if (args[0] instanceof IEventHelp.Type) {
            pkState = (IEventHelp.Type) args[0];
            Log.e(TAG, "onEvent:" + pkState);
            // pk邀请成功 对方同意 进入pk开始阶段
            if (IEventHelp.Type.PK_GOING == pkState) {
                if (args.length == 2) {
                    rcpkInfo = (RCPKInfo) args[1];
//                    RCPKInfo rcpkInfo = (RCPKInfo) args[1];
//                    pkRoomId = TextUtils.equals(rcpkInfo.getInviteeRoomId(), roomId) ? rcpkInfo.getInviterRoomId() : rcpkInfo.getInviteeRoomId();
//                    isInviter = AccountStore.INSTANCE.getUserId().equals(rcpkInfo.getInviterId());
//                    Logger.e(TAG, "isInviter = " + isInviter);
                }
                handlePKStart();
            } else if (IEventHelp.Type.PK_FINISH == pkState) {
                // 对方手动结束pk
                handlePKStop();
            }
        } else if (args[0] instanceof PKResponse) {
            PKResponse pkState = (PKResponse) args[0];
            if (pkState == PKResponse.reject) {
                KToast.show("您的PK邀请被拒绝");
            } else if (pkState == PKResponse.ignore) {
                KToast.show("您的PK邀请被忽略");
            }
        }
    }

    /**
     * 处理pk开始阶段
     */
    void handlePKStart() {
        Logger.e(TAG, "PK Start");
        if (null != stateListener) stateListener.onPkStart();
        // 上报pk状态：开始
        reportPKState(0);
        // pk记时
        if (null != pkView && null != rcpkInfo) {
            String local = AccountStore.INSTANCE.getUserId();
            String otherId = local.equals(rcpkInfo.getInviterId()) ? rcpkInfo.getInviteeId() : rcpkInfo.getInviterId();
            pkView.setPKUserInfo(local, otherId);
            pkView.pkStart(-1, new IPK.OnTimerEndListener() {
                @Override
                public void onTimerEnd() {
                    handlePkPunish();
                }
            });
        }
        // 首次刷新pk信息
        refreshPKGiftRank();
    }

    /**
     * 处理pk惩罚阶段
     * 1、上报pk状态：惩罚阶段
     * 2、惩罚记时结束：邀请者 quitPK,接收者：记录标识
     */
    void handlePkPunish() {
        Logger.e(TAG, "PK Punish");
        //调用api 退出pk
        reportPKState(1);
        //惩罚记时
        if (null != pkView) {
            pkView.pkPunish(-1, new IPK.OnTimerEndListener() {
                @Override
                public void onTimerEnd() {
                    if (AccountStore.INSTANCE.getUserId().equals(rcpkInfo.getInviterId())) {
                        //约定邀请者 quitpk
                        VoiceRoomApi.getApi().quitPK(new IResultBack<Boolean>() {
                            @Override
                            public void onResult(Boolean aBoolean) {
                                if (aBoolean) {
                                    handlePKStop();
                                } else {
                                    KToast.show("PK结束失败");
                                }
                            }
                        });
                    } else {
                        // 受邀者 修改惩罚结束标识 等待pkFinish回调
                    }
                }
            });
        }
    }

    /**
     * 处理pk结束
     */
    void handlePKStop() {
        Logger.e(TAG, "PK Stop");
        if (null != stateListener) stateListener.onPkStop();
        if (null != pkView) pkView.pkStop();
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

    void reportPKState(int status) {
        Map<String, Object> params = new HashMap<>();
        params.put("roomId", roomId);
        params.put("toRoomId", pkRoomId);
        params.put("status", status);
        OkApi.post(VRApi.PK_STATE, params, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                Logger.e(TAG, "reportPKState:" + GsonUtil.obj2Json(result));
            }

            @Override
            public void onError(int code, String msg) {
                super.onError(code, msg);
            }
        });
    }

}
