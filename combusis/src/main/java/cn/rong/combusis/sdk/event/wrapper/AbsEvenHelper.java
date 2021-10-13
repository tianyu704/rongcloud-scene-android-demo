package cn.rong.combusis.sdk.event.wrapper;

import android.text.TextUtils;
import android.util.Log;

import com.kit.cache.GsonUtil;
import com.kit.wapper.IResultBack;

import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.sdk.event.listener.RoomListener;
import cn.rong.combusis.sdk.event.listener.StatusListener;
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomEventListener;
import cn.rongcloud.voiceroom.model.RCVoiceRoomInfo;
import cn.rongcloud.voiceroom.model.RCVoiceSeatInfo;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

public abstract class AbsEvenHelper implements IEventHelp, RCVoiceRoomEventListener {
    protected final String TAG = this.getClass().getSimpleName();
    protected final static Object obj = new Object();
    protected List<RoomListener> listeners;//房间监听
    protected List<StatusListener> statusListeners;//网络状态监听
    protected List<RCVoiceSeatInfo> mSeatInfos;//当前麦序
    protected RCVoiceRoomInfo roomInfo;//房间信息
    protected RCVoiceRoomEventListener rcVoiceRoomEventListener;

    protected abstract void onShowTipDialog(String roomId, String userId, TipType type, IResultBack<Boolean> resultBack);

    protected String roomId;

    protected void init(String roomId, RCVoiceRoomEventListener rcVoiceRoomEventListener) {
        this.roomId = roomId;
        this.rcVoiceRoomEventListener = rcVoiceRoomEventListener;
        RCVoiceRoomEngine.getInstance().setVoiceRoomEventListener(this);
        if (null == mSeatInfos) mSeatInfos = new ArrayList<>();
        if (null == listeners) listeners = new ArrayList<>();
        if (null == statusListeners) statusListeners = new ArrayList<>();
    }

    protected void unInit() {
        RCVoiceRoomEngine.getInstance().setVoiceRoomEventListener(null);
        if (null != mSeatInfos) mSeatInfos.clear();
        if (null != listeners) listeners.clear();
        if (null != statusListeners) statusListeners.clear();
        roomInfo = null;
        roomId = null;
    }

    @Override
    public void onRoomKVReady() {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onRoomKVReady();
        }
        Log.d(TAG, "onRoomKVReady");
    }

    /**
     * 房间信息跟新回调
     *
     * @param room
     */
    @Override
    public void onRoomInfoUpdate(RCVoiceRoomInfo room) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onRoomInfoUpdate(room);
        }
        this.roomInfo = room;
        Log.d(TAG, "onRoomInfoUpdate:" + GsonUtil.obj2Json(roomInfo));
        if (null != listeners) {
            for (RoomListener l : listeners) {
                l.onRoomInfo(roomInfo);
            }
        }
    }

    /**
     * 麦位列表跟新回调
     *
     * @param list
     */
    @Override
    public void onSeatInfoUpdate(List<RCVoiceSeatInfo> list) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onSeatInfoUpdate(list);
        }
        int count = list.size();
        Log.d(TAG, "onSeatInfoUpdate: count = " + count);
        for (int i = 0; i < count; i++) {
            RCVoiceSeatInfo info = list.get(i);
            Log.d(TAG, "index = " + i + "  " + GsonUtil.obj2Json(info));
        }
        synchronized (obj) {
            mSeatInfos.clear();
            mSeatInfos.addAll(list);
        }
        refreshSeatInfos();
    }

    private void refreshSeatInfos() {
        if (null != listeners) {
            for (RoomListener l : listeners) {
                l.onSeatList(mSeatInfos);
            }
        }
    }

    //同步回调 onSeatInfoUpdate 此处无特殊需求可不处理
    @Override
    public void onUserEnterSeat(int index, String userId) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onUserEnterSeat(index, userId);
        }
        Log.d(TAG, "onUserEnterSeat: index = " + index + " userId = " + userId);
        RCVoiceSeatInfo info = getSeatInfo(index);
        if (null != info) {
            info.setUserId(userId);
            info.setStatus(RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusUsing);
        }
        refreshSeatInfos();
    }

    //同步回调 onSeatInfoUpdate 此处无特殊需求可不处理
    @Override
    public void onUserLeaveSeat(int index, String userId) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onUserLeaveSeat(index, userId);
        }
        Log.d(TAG, "onUserLeaveSeat: index = " + index + " userId = " + userId);
        RCVoiceSeatInfo info = getSeatInfo(index);
        if (null != info) {
            info.setUserId(null);
            // 可能是锁定导致的
            // info.setStatus(RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty);
        }
        refreshSeatInfos();
    }

    //同步回调 onSeatInfoUpdate 此处无特殊需求可不处理
    @Override
    public void onSeatMute(int index, boolean mute) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onSeatMute(index, mute);
        }
        Log.d(TAG, "onSeatMute: index = " + index + " mute = " + mute);
    }

    //同步回调 onSeatInfoUpdate 此处无特殊需求可不处理
    @Override
    public void onSeatLock(int index, boolean locked) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onSeatLock(index, locked);
        }
        Log.d(TAG, "onSeatLock: index = " + index + " locked = " + locked);
    }

    /**
     * 观众进入
     *
     * @param userId
     */
    @Override
    public void onAudienceEnter(String userId) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onAudienceEnter(userId);
        }
        Log.d(TAG, "onAudienceEnter: userId = " + userId);
        if (null != listeners) {
            getOnLineUserIds(roomId, new IResultBack<List<String>>() {
                @Override
                public void onResult(List<String> strings) {
                    for (RoomListener l : listeners) {
                        l.onOnLineUserIds(strings);
                    }
                }
            });
        }
    }

    /**
     * 观众离开房间
     *
     * @param userId
     */
    @Override
    public void onAudienceExit(String userId) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onAudienceExit(userId);
        }
        Log.d(TAG, "onAudienceExit: userId = " + userId);
        if (null != listeners) {
            getOnLineUserIds(roomId, new IResultBack<List<String>>() {
                @Override
                public void onResult(List<String> strings) {
                    for (RoomListener l : listeners) {
                        l.onOnLineUserIds(strings);
                    }
                }
            });
        }
    }

    /**
     * 说话状态回调 比较频繁
     *
     * @param index    麦位索引
     * @param speaking 是否正在语音
     */
    @Override
    public void onSpeakingStateChanged(int index, boolean speaking) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onSpeakingStateChanged(index, speaking);
        }
//        Log.v(TAG, "onSpeakingStateChanged: index = " + index + " speaking = " + speaking);
        if (null != statusListeners) {
            for (StatusListener l : statusListeners) {
                l.onSpeaking(index, speaking);
            }
        }
    }

    @Override
    public void onMessageReceived(Message message) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onMessageReceived(message);
        }
        if (message.getConversationType() == Conversation.ConversationType.PRIVATE) {
            if (null != statusListeners) {
                if (!TextUtils.isEmpty(roomId)) {
                    getUnReadMegCount(roomId, new IResultBack<Integer>() {
                        @Override
                        public void onResult(Integer integer) {
                            for (StatusListener l : statusListeners) {
                                l.onReceive(integer);
                            }
                        }
                    });
                }
            }
        }
        Log.v(TAG, "onMessageReceived: " + GsonUtil.obj2Json(message.getContent()));
    }

    /**
     * 房间通知回调
     *
     * @param name
     * @param content
     */
    @Override
    public void onRoomNotificationReceived(String name, String content) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onRoomNotificationReceived(name, content);
        }
        Log.v(TAG, "onRoomNotificationReceived: name = " + name + " content = " + content);
        if (null != listeners) {
            for (RoomListener l : listeners) {
                l.onNotify(name, content);
            }
        }
    }

    /**
     * 当前用户被抱上麦回调
     *
     * @param userId 发起邀请的用户id
     */
    @Override
    public void onPickSeatReceivedFrom(String userId) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onPickSeatReceivedFrom(userId);
        }
        Log.d(TAG, "onPickSeatReceivedFrom: userId = " + userId);
//        onShowTipDialog("", userId, TipType.InvitedSeat, new IResultBack<Boolean>() {//邀请上麦
//            @Override
//            public void onResult(Boolean result) {
//                if (result) {
//                    //同意
//                    VoiceRoomApi.getApi().notifyRoom(Api.EVENT_AGREE_MANAGE_PICK, AccountStore.INSTANCE.getUserId());
//                    //获取可用麦位索引
//                    int availableIndex = getAvailableSeatIndex();
//                    if (availableIndex > -1) {
//                        VoiceRoomApi.getApi().enterSeat(availableIndex, null);
//                    } else {
//                        EToast.showToast("当前没有空余的麦位");
//                    }
//                } else {//拒绝
//                    VoiceRoomApi.getApi().notifyRoom(Api.EVENT_REJECT_MANAGE_PICK, AccountStore.INSTANCE.getUserId());
//                }
//            }
//        });
    }

    /**
     * 被踢下麦回调
     *
     * @param index 麦位索引
     */
    @Override
    public void onKickSeatReceived(int index) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onKickSeatReceived(index);
        }
        Log.d(TAG, "onPickSeatReceivedFrom: index = " + index);
    }

    /**
     * 房主或管理员同意当前用户的排麦申请的回调
     * 1. enterSeat
     */
    @Override
    public void onRequestSeatAccepted() {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onRequestSeatAccepted();
        }
        Log.d(TAG, "onRequestSeatAccepted: ");
//        VoiceRoomApi.getApi().notifyRoom(Api.EVENT_AGREE_MANAGE_PICK, AccountStore.INSTANCE.getUserId());
//        //获取可用麦位索引
//        int availableIndex = getAvailableSeatIndex();
//        if (availableIndex > -1) {
//            VoiceRoomApi.getApi().enterSeat(availableIndex, null);
//        } else {
//            EToast.showToast("当前没有空余的麦位");
//        }
    }

    /**
     * 发送的排麦请求被房主或管理员拒绝
     */
    @Override
    public void onRequestSeatRejected() {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onRequestSeatRejected();
        }
        Log.d(TAG, "onRequestSeatRejected: ");
        EToast.showToast("您的上麦申请被拒绝啦");
    }

    /**
     * 排麦列表发生变化
     * 1、获取申请排麦id列表
     * 2、过滤已经在房间的用户
     * 3、弹框提 同意、拒绝
     */
    @Override
    public void onRequestSeatListChanged() {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onRequestSeatListChanged();
        }
        Log.d(TAG, "onRequestSeatListChanged: ");
//        RCVoiceRoomEngine.getInstance().getRequestSeatUserIds(new RCVoiceRoomResultCallback<List<String>>() {
//            @Override
//            public void onSuccess(List<String> strings) {
//                Log.e(TAG, "getRequestSeatUserIds: ids = " + GsonUtil.obj2Json(strings));
//                List<String> requestIds = new ArrayList<>();
//                for (String id : strings) {
//                    if (null == getSeatInfo(id)) {//过滤 不再麦位上
//                        requestIds.add(id);
//                    }
//                }
//                if (!requestIds.isEmpty()) {
//                    String userId = requestIds.get(0);
//                    onShowTipDialog("", userId, TipType.RequestSeat, new IResultBack<Boolean>() {//申请上麦
//                        @Override
//                        public void onResult(Boolean result) {
//                            if (result) {
//                                //同意
//                                int index = getAvailableSeatIndex();
//                                if (index > -1) {
//                                    RCVoiceRoomEngine.getInstance().acceptRequestSeat(userId, null);
//                                } else {
//                                    EToast.showToast("当前没有空余的麦位");
//                                }
//                            } else {//拒绝
//                                RCVoiceRoomEngine.getInstance().rejectRequestSeat(userId, null);
//                            }
//                        }
//                    });
//                } else {//申请被取消
//                    EventDialogHelper.helper().dismissDialog();
//                }
//            }
//
//            @Override
//            public void onError(int i, String s) {
//                Log.e(TAG, "onError: code:" + i + " ,message = " + s);
//            }
//        });

    }

    /**
     * 收到邀请
     *
     * @param invitationId 邀请标识 Id
     * @param userId       发送邀请用户的标识
     * @param content      邀请内容 （用户可以自定义）
     */
    @Override
    public void onInvitationReceived(String invitationId, String userId, String content) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onInvitationReceived(invitationId, userId, content);
        }
        Log.d(TAG, "onInvitationReceived: invitationId = " + invitationId + " userId = " + userId + " content = " + content);
    }

    /**
     * 邀请被接受回调
     *
     * @param invitationId 邀请标识 Id
     */
    @Override
    public void onInvitationAccepted(String invitationId) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onInvitationAccepted(invitationId);
        }
        Log.d(TAG, "onInvitationAccepted: invitationId = " + invitationId);
    }

    /**
     * 邀请被拒绝回调
     *
     * @param invitationId 邀请标识 Id
     */
    @Override
    public void onInvitationRejected(String invitationId) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onInvitationRejected(invitationId);
        }
        Log.d(TAG, "onInvitationRejected: invitationId = " + invitationId);
    }

    /**
     * 邀请被取消回调
     *
     * @param invitationId 邀请标识 Id
     */
    @Override
    public void onInvitationCancelled(String invitationId) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onInvitationCancelled(invitationId);
        }
        Log.d(TAG, "onInvitationCancelled: invitationId = " + invitationId);
    }

    /**
     * 被踢出房间回调
     *
     * @param targetId 被踢用户的标识
     * @param userId   发起踢人用户的标识
     */
    @Override
    public void onUserReceiveKickOutRoom(String targetId, String userId) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onUserReceiveKickOutRoom(targetId, userId);
        }
        Log.d(TAG, "onUserReceiveKickOutRoom: targetId = " + targetId);
    }

    /**
     * 网络状态
     *
     * @param i 网络延迟 ms
     */
    @Override
    public void onNetworkStatus(int i) {
        if (rcVoiceRoomEventListener != null) {
            rcVoiceRoomEventListener.onNetworkStatus(i);
        }
//        Log.d(TAG, "onNetworkStatus: rtt = " + i);
        if (null != statusListeners) {
            for (StatusListener l : statusListeners) {
                l.onStatus(i);
            }
        }
    }


//    /**
//     * PK开启成功
//     *
//     * @param rcpkInfo
//     */
//    @Override
//    public void onPKgoing(@NonNull RCPKInfo rcpkInfo) {
//        Logger.e(TAG, "onPKgoing");
//        //邀请同意 开始PK 释放被邀请信息
//        VoiceRoomApi.getApi().releasePKInvitee();
//    }
//
//    /**
//     * PK结束回调
//     */
//    @Override
//    public void onPKFinish() {
//        Logger.e(TAG, "onPKFinish");
//    }
//
//    /**
//     * 接收到PK邀请回调
//     *
//     * @param inviterRoomId 发起邀请人的房间Id
//     * @param inviterUserId 发起邀请人的Id
//     */
//    @Override
//    public void onReveivePKInvitation(String inviterRoomId, String inviterUserId) {
//        Logger.e(TAG, "onReveivePKInvitation");
//        //保存邀请者信息
//        pkInviter = new PKInviter();
//        pkInviter.inviterRoomId = inviterRoomId;
//        pkInviter.inviterId = inviterUserId;
//
//        onShowTipDialog(inviterRoomId, inviterUserId, TipType.InvitedPK, new IResultBack<Boolean>() {
//            @Override
//            public void onResult(Boolean result) {
//                RCVoiceRoomEngine.getInstance().responsePKInvitation(inviterRoomId, inviterUserId, result ? PKState.accept : PKState.reject, new RCVoiceRoomCallback() {
//                    @Override
//                    public void onSuccess() {
//                        EToast.showToastWithLag(TAG, result ? "同意PK成功" : "拒绝PK成功");
//                    }
//
//                    @Override
//                    public void onError(int code, String message) {
//                        EToast.showToastWithLag(TAG, (result ? "同意PK失败" : "拒绝PK失败") + " code = " + code + " message = " + message);
//                    }
//                });
//            }
//        });
//
//    }
//
//
//    /**
//     * PK邀请被取消
//     *
//     * @param roomId 发起邀请人的房间Id
//     * @param userId 发起邀请人的Id
//     */
//    @Override
//    public void onPKInvitationCanceled(String roomId, String userId) {
//        Logger.e(TAG, "onPKInvitationCanceled");
//        EventDialogHelper.helper().dismissDialog();
//        // 释放邀请者信息
//        pkInviter = null;
//    }
//
//    /**
//     * PK邀请被拒绝
//     *
//     * @param roomId 发起邀请人的房间Id
//     * @param userId 发起邀请人的Id
//     */
//    @Override
//    public void onPKInvitationRejected(String roomId, String userId) {
//        Logger.e(TAG, "onPKInvitationRejected");
//        //邀请被拒绝 释放被邀请信息
//        VoiceRoomApi.getApi().releasePKInvitee();
//    }
//
//    @Override
//    public void onPKInvitationIgnored(String s, String s1) {
//        Logger.e(TAG, "onPKInvitationIgnored");
//        //邀请被忽略 释放被邀请信息
//        VoiceRoomApi.getApi().releasePKInvitee();
//    }
}