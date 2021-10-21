package cn.rongcloud.voiceroom.pk;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.basis.widget.BottomDialog;
import com.kit.UIKit;
import com.kit.utils.KToast;
import com.kit.utils.Logger;
import com.kit.wapper.IResultBack;
import com.rongcloud.common.utils.AccountStore;

import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.EventBus;
import cn.rong.combusis.VRCenterDialog;
import cn.rong.combusis.message.RCChatroomPK;
import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.sdk.VoiceRoomApi;
import cn.rong.combusis.sdk.event.wrapper.IEventHelp;
import cn.rongcloud.voiceroom.R;
import cn.rongcloud.voiceroom.model.PKResponse;
import cn.rongcloud.voiceroom.model.RCPKInfo;
import cn.rongcloud.voiceroom.pk.domain.PKInfo;
import cn.rongcloud.voiceroom.pk.domain.PKResult;
import cn.rongcloud.voiceroom.pk.widget.IPK;

/**
 * PK主播端
 * // PK邀请
 * 1、显示在线房主列表，选则发起PK邀请 进入已邀请状态 该状态不可再次邀请
 * 2、对方拒绝PK或忽略PK邀请，恢复原状 可再次邀请
 * 3、对方同意PK,SDK进入PK状态，接收到SDK PKGong状态暂不处理，暂约定pk邀请者上报pk状态：开始，pk双方等待服务端分发pk开始状态消息
 * // 开始PK
 * 1、pk双发接收到服务端分发的pk消息，解析pk状态：0，pk开始
 * 2、PKView开启PK记时
 * 3、PKView记时结束，暂停，等待服务端分发pk惩罚状态消息。
 * // 惩罚
 * 1、pk双发接收到服务端分发的pk惩罚状态消息，解析pk状态：1，惩罚开始，
 * 2、PKView开启惩罚记时
 * 3、PKView记时结束。暂停，等待服务端分发pk结束状态消息。
 * // pk结束
 * 1、pk双发接收到服务端分发的pk消息，解析pk状态：2，pk结束
 * 2、暂约定pk邀请者执行sdk的quitPk,结束pk
 * // 主动结束pk
 * 1、pk双方均可主动结束pk流程
 * 2、pk结束方，主动调sdk的quitPK,结束pk，并上报pk状态结束 等待服务端分发消息
 * 3、pk双方接收到服务端分发的pk消息，结束pk流程
 * 观众端：
 * 1、检查当前房间的状态，若果在pk中 需返回pk双方房间和房主信息，-1：不在pk 0：pk 阶段  2：惩罚阶段
 * 2、根据pk的状态，ui调转到不同的PK阶段
 */
public class PKStateManager implements IPKState, EventBus.EventCallback, DialogInterface.OnDismissListener {
    private final static String TAG = "PKStateManager";
    private String roomId, pkRoomId;
    private IPK pkView;
    private final static PKStateManager manager = new PKStateManager();
    private VRStateListener stateListener;
    // 标是否是邀请者 pk记时结束 约定邀请者调用quitPk
    private RCPKInfo rcpkInfo;

    public PKStateManager() {
    }

    public static PKStateManager get() {
        return manager;
    }

    public void unInit() {
        roomId = null;
//        EventHelper.helper().unregeister();
        EventBus.get().off(EventBus.TAG.PK_STATE, this);
        EventBus.get().off(EventBus.TAG.PK_RESPONSE, this);
        EventBus.get().off(EventBus.TAG.PK_GIFT, this);
    }

    private IEventHelp.Type pkState = IEventHelp.Type.PK_NONE;

    public void init(String roomId, IPK pkView, IPKState.VRStateListener listener) {
        this.roomId = roomId;
        this.pkView = pkView;
        this.stateListener = listener;
        // 注册房间事件监听
//        if (!EventHelper.helper().isInitlaized()) {
//            EventHelper.helper().regeister(roomId, null);
//        }
        // 注册pk状态监听
        EventBus.get().on(EventBus.TAG.PK_STATE, this);
        EventBus.get().on(EventBus.TAG.PK_RESPONSE, this);
        EventBus.get().on(EventBus.TAG.PK_GIFT, this);
        // 观众端 检查pk状态
        PKApi.getPKInfo(roomId, new IResultBack<PKResult>() {
            @Override
            public void onResult(PKResult pkResult) {
                if (null == pkResult || pkResult.getStatusMsg() == -1 || pkResult.getStatusMsg() == 2) {
                    Logger.e(TAG, "init: Not In PK");
                    return;
                }
                handleAudienceJoinPk(pkResult);
            }
        });
    }

    /**
     * 格式化pk信息
     *
     * @return PKInfo[2]
     * left：index = 0
     * right：index = 1
     */
    private PKInfo[] formatPKInfo(PKResult pkResult) {
        if (null == pkResult) {
            return null;
        }
        List<PKInfo> pkInfos = pkResult.getRoomScores();
        if (null == pkInfos || 2 != pkInfos.size()) {
            return null;
        }
        PKInfo[] result = new PKInfo[2];
        PKInfo first = pkInfos.get(0);
        if (roomId.equals(first.getRoomId())) {
            result[0] = first;
            result[1] = pkInfos.get(1);
        } else {
            result[0] = pkInfos.get(1);
            result[1] = first;
        }
        return result;
    }

    /**
     * 处理观众根据当前pk状态 进入不同pk阶段
     * 根据pk状态jump 不同阶段
     */
    void handleAudienceJoinPk(PKResult pkResult) {
        Logger.e(TAG, "init JumpTo PK");
        if (null != stateListener) stateListener.onPkStart();
        // 观众端需要获取pk双方房间和房主信息
        int state = pkResult.getStatusMsg();
        if (null != pkView) {
            refreshPKInfo(pkResult);
            long timeDiff = null == pkResult ? -1 : pkResult.getTimeDiff();
            if (0 == state) {// pk 阶段
                pkView.pkStart(timeDiff, new IPK.OnTimerEndListener() {
                    @Override
                    public void onTimerEnd() {
                        // 等待服务端分发pk惩罚消息
                    }
                });
            } else if (1 == state) {// pk 惩罚阶段
                pkView.pkPunish(timeDiff, new IPK.OnTimerEndListener() {
                    @Override
                    public void onTimerEnd() {
                        // 等待服务端分发pk结束消息
                    }
                });
            }
        }
    }

    private BottomDialog dialog;

    @Override
    public void sendPkInvitation(Activity activity, IResultBack<Boolean> resultBack) {
        if (StateUtil.enableInvite()) {
            if (dialog != null) dialog.dismiss();
            dialog = new RoomOwerDialog(activity, resultBack).setOnCancelListener(this);
            dialog.show();
        }
    }

    @Override
    public void cancelPkInvitation(Activity activity, IResultBack<Boolean> resultBack) {
        if (StateUtil.enableCancelInvite()) {
            if (dialog != null) dialog.dismiss();
            dialog = new CancelPKDialog(activity, resultBack).setOnCancelListener(this);
            dialog.show();
        }
    }

    /**
     * 退出pk 执行上报pk状态结束
     */
    @Override
    public void quitPK(Activity activity) {
        if (!StateUtil.isPking()) {
            KToast.show("请先发起PK");
            return;
        }
        VRCenterDialog dialog = new VRCenterDialog(activity, null);
        TextView textView = new TextView(dialog.getContext());
        textView.setTextSize(16);
        textView.setTextColor(Color.parseColor("#ffffff"));
        textView.setText(R.string.quit_pk_dialog_tip);
        dialog.replaceContent(UIKit.getResources().getString(R.string.dialog_tip),
                UIKit.getResources().getString(R.string.dialog_cancle),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                },
                UIKit.getResources().getString(R.string.dialog_agree),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        PKApi.reportPKEnd(roomId, pkRoomId, new IResultBack<Boolean>() {
                            @Override
                            public void onResult(Boolean s) {
                                // 等待服务端分发pk结束消息
                            }
                        });
                    }
                },
                textView);
        dialog.show();
    }

    @Override
    public void refreshPKGiftRank() {
        PKApi.getPKInfo(roomId, new IResultBack<PKResult>() {
            @Override
            public void onResult(PKResult pkResult) {
                refreshPKInfo(pkResult);
            }
        });
    }

    void refreshPKInfo(PKResult pkResult) {
        PKInfo[] pkInfos = formatPKInfo(pkResult);
        if (null == pkInfos) return;
        pkView.setPKUserInfo(pkInfos[0].getUserId(), pkInfos[1].getUserId());
        // set score
        pkView.setPKScore(pkInfos[0].getScore(), pkInfos[1].getScore());
        // left
        List<String> lefts = new ArrayList<>();
        List<User> lusers = pkInfos[0].getUserInfoList();
        int ls = null == lusers ? 0 : lusers.size();
        for (int i = 0; i < ls; i++) {
            lefts.add(lusers.get(i).getPortrait());
        }
        // right
        List<String> rights = new ArrayList<>();
        List<User> rusers = pkInfos[1].getUserInfoList();
        int rs = null == rusers ? 0 : rusers.size();
        for (int i = 0; i < rs; i++) {
            rights.add(rusers.get(i).getPortrait());
        }
        pkView.setGiftSenderRank(lefts, rights);
    }

    /**
     * 判断是否是邀请者
     *
     * @return 是否是邀请者
     */
    boolean isInviter() {
        return null != rcpkInfo && AccountStore.INSTANCE.getUserId().equals(rcpkInfo.getInviterId());
    }

    @Override
    public void onEvent(String tag, Object... args) {
        if (EventBus.TAG.PK_STATE.equals(tag) && args[0] instanceof IEventHelp.Type) {
            pkState = (IEventHelp.Type) args[0];
            Log.e(TAG, "onEvent:" + pkState);
            // pk邀请成功 对方同意 进入pk开始阶段
            if (IEventHelp.Type.PK_GOING == pkState) {
                if (args.length == 2) {
                    rcpkInfo = (RCPKInfo) args[1];
                    pkRoomId = TextUtils.equals(rcpkInfo.getInviteeRoomId(), roomId) ? rcpkInfo.getInviterRoomId() : rcpkInfo.getInviteeRoomId();
                }
                // 1、约定 邀请者开启pk 上报pk开始状态
                // 2、被邀请者 暂不处理，等待服务端pk开启消息
                if (isInviter()) {
                    PKApi.reportPKStart(roomId, pkRoomId, new IResultBack<Boolean>() {
                        @Override
                        public void onResult(Boolean success) {
                            // 等待 PK_START
                        }
                    });
                }
            } else if (IEventHelp.Type.PK_FINISH == pkState) {// pk finish
                // 改由服务端分发消息 暂定有邀请者调用api结束
            } else if (IEventHelp.Type.PK_START == pkState
                    || IEventHelp.Type.PK_PUNISH == pkState
                    || IEventHelp.Type.PK_STOP == pkState) {
                RCChatroomPK chatroomPK = null;
                if (args.length == 2) {
                    chatroomPK = (RCChatroomPK) args[1];
                }
                if (IEventHelp.Type.PK_START == pkState) {// pk 开始
                    handlePKStart();
                } else if (IEventHelp.Type.PK_PUNISH == pkState) {// pk 惩罚
                    handlePkPunish();
                } else if (IEventHelp.Type.PK_STOP == pkState) {// pk 结束
                    handlePKStop();
                    if (null != chatroomPK) {
                        // pk结束者房间id
                        String stopId = chatroomPK.getStopPkRoomId();
                        if (TextUtils.isEmpty(stopId)) {
                            KToast.show("本轮PK结束");
                        } else {
                            if (TextUtils.equals(stopId, roomId)) {// 当前房主结束
                                KToast.show("我方挂端，本轮PK结束");
                            } else {
                                KToast.show("对方挂端，本轮PK结束");
                            }
                        }
                    }
                }
            }
            if (null != stateListener) stateListener.onPkState();
        } else if (EventBus.TAG.PK_RESPONSE.equals(tag) && args[0] instanceof PKResponse) {
            PKResponse pkState = (PKResponse) args[0];
            if (pkState == PKResponse.reject) {
                KToast.show("您的PK邀请被拒绝");
            } else if (pkState == PKResponse.ignore) {
                KToast.show("您的PK邀请被忽略");
            }
        } else if (EventBus.TAG.PK_GIFT.equals(tag)) {
            Logger.e(TAG, "礼物消息");
            refreshPKGiftRank();
        }
    }

    /**
     * 接收到服务下发的pk开启消息 执行
     * 处理pk开始阶段
     * 注意：pkResult 不为空说明是init check inPK 传过来的 不需要再获取pkinfo
     */
    void handlePKStart() {
        Logger.e(TAG, "PK Start");
        if (null != stateListener) stateListener.onPkStart();
        // 首次刷新pk信息
        PKApi.getPKInfo(roomId, new IResultBack<PKResult>() {
            @Override
            public void onResult(PKResult pkResult) {
                refreshPKInfo(pkResult);
                long timeDiff = null == pkResult ? -1 : pkResult.getTimeDiff();
                pkView.pkStart(timeDiff, new IPK.OnTimerEndListener() {
                    @Override
                    public void onTimerEnd() {
                        // 等待服务端分发pk惩罚消息
                    }
                });
            }
        });
    }

    /**
     * 处理pk惩罚阶段
     * 2、惩罚记时结束：邀请者 quitPK,接收者：记录标识
     */
    void handlePkPunish() {
        Logger.e(TAG, "PK Punish");
        //惩罚记时
        if (null != pkView) {
            pkView.pkPunish(-1, new IPK.OnTimerEndListener() {
                @Override
                public void onTimerEnd() {
                    // 等待服务端分发pk结束消息
                }
            });
        }
    }

    /**
     * 处理pk结束
     * 接收到服务下发的pk结束消息 执行
     * 1、邀请者调用SDK quitPk 结束pk
     * 2、被邀请者 暂不处理
     */
    void handlePKStop() {
        Logger.e(TAG, "PK Stop");
        if (null != stateListener) stateListener.onPkStop();
        if (null != pkView) pkView.pkStop();
        if (isInviter()) {
            //约定邀请者 quitpk
            VoiceRoomApi.getApi().quitPK(new IResultBack<Boolean>() {
                @Override
                public void onResult(Boolean aBoolean) {
                    if (!aBoolean) KToast.show("PK结束失败");
                }
            });
        } else {
            // 受邀者 修改惩罚结束标识 等待pkFinish回调
        }
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
