package cn.rongcloud.liveroom.helper;

import static cn.rong.combusis.provider.voiceroom.CurrentStatusType.STATUS_NOT_ON_SEAT;

import android.text.TextUtils;
import android.util.Log;

import com.basis.UIStack;
import com.basis.net.oklib.OkApi;
import com.basis.net.oklib.OkParams;
import com.basis.net.oklib.WrapperCallBack;
import com.basis.net.oklib.wrapper.Wrapper;
import com.meihu.beauty.utils.MhDataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.common.ui.dialog.ConfirmDialog;
import cn.rong.combusis.manager.RCChatRoomMessageManager;
import cn.rong.combusis.message.RCChatroomLocationMessage;
import cn.rong.combusis.music.MusicManager;
import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.provider.voiceroom.CurrentStatusType;
import cn.rong.combusis.sdk.event.listener.LeaveRoomCallBack;
import cn.rong.combusis.sdk.event.wrapper.EToast;
import cn.rong.combusis.ui.room.fragment.ClickCallback;
import cn.rongcloud.liveroom.api.RCLiveEngine;
import cn.rongcloud.liveroom.api.RCLiveEventListener;
import cn.rongcloud.liveroom.api.RCLiveMixType;
import cn.rongcloud.liveroom.api.RCLiveSeatInfo;
import cn.rongcloud.liveroom.api.RCRect;
import cn.rongcloud.liveroom.api.callback.RCLiveCallback;
import cn.rongcloud.liveroom.api.error.RCLiveError;
import cn.rongcloud.liveroom.manager.SeatManager;
import cn.rongcloud.rtc.base.RCRTCVideoFrame;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

/**
 * @author lihao
 * @project RongRTCDemo
 * @date 2021/11/16
 * @time 5:30 下午
 * 用来直播房的各种监听事件  发送消息 麦位操作等
 * 维护一定的集合来返回事件
 */
public class LiveEventHelper implements ILiveEventHelper, RCLiveEventListener {

    private String TAG = "LiveEventHelper";

    private List<MessageContent> messageList;
    private String roomId;//直播房的房间ID
    private CurrentStatusType currentStatus = STATUS_NOT_ON_SEAT;
    private List<LiveRoomListener> liveRoomListeners = new ArrayList<>();

    public static LiveEventHelper getInstance() {
        return helper.INSTANCE;
    }

    @Override
    public CurrentStatusType getCurrentStatus() {
        return currentStatus;
    }

    @Override
    public void setCurrentStatus(CurrentStatusType currentStatus) {
        this.currentStatus = currentStatus;
    }

    @Override
    public void leaveRoom(LeaveRoomCallBack callback) {
        RCLiveEngine.getInstance().leaveRoom(new RCLiveCallback() {
            @Override
            public void onSuccess() {
                MusicManager.get().stopPlayMusic();
                unRegister();
                changeUserRoom("");
                if (callback != null)
                    callback.onSuccess();
            }

            @Override
            public void onError(int code, RCLiveError error) {
                if (callback != null)
                    callback.onError(code, error.getMessage());
            }
        });
    }

    @Override
    public void joinRoom(String roomId, ClickCallback<Boolean> callback) {
        register(roomId);
        RCLiveEngine.getInstance().joinCDNRoom(roomId, new RCLiveCallback() {
            @Override
            public void onSuccess() {
                changeUserRoom(roomId);
                if (callback != null)
                    callback.onResult(true, "加入房间成功");
            }

            @Override
            public void onError(int code, RCLiveError error) {
                if (callback != null)
                    callback.onResult(false, error.getMessage());
                EToast.showToast("加入房间失败:" + error.getMessage());
            }
        });
    }

    @Override
    public void pickUserToSeat(String userId, ClickCallback<Boolean> callback) {

    }

    /**
     * 接受上麦请求。如果上麦的麦位已被占用，SDK 会自动查询第一个空麦位
     *
     * @param userId   目标用户id
     * @param callback 结果回调
     */
    @Override
    public void acceptRequestSeat(String userId, ClickCallback<Boolean> callback) {
        RCLiveEngine.getInstance().acceptRequest(userId, new RCLiveCallback() {
            @Override
            public void onSuccess() {
                if (callback != null)
                    callback.onResult(true, "接受请求连麦成功");
            }

            @Override
            public void onError(int code, RCLiveError error) {
                if (callback != null)
                    callback.onResult(false, "接受请求连麦成功:" + error.getMessage());
            }
        });
    }

    @Override
    public void cancelRequestSeat(ClickCallback<Boolean> callback) {
        RCLiveEngine.getInstance().cancelRequest(new RCLiveCallback() {
            @Override
            public void onSuccess() {
                if (callback != null)
                    callback.onResult(true, "取消请求连麦成功");
            }

            @Override
            public void onError(int code, RCLiveError error) {
                if (callback != null)
                    callback.onResult(false, "取消请求连麦失败:" + error.getMessage());
            }
        });
    }

    @Override
    public void lockSeat(int index, boolean isClose, ClickCallback<Boolean> callback) {

    }

    @Override
    public void muteSeat(int index, boolean isMute, ClickCallback<Boolean> callback) {

    }

    @Override
    public void kickUserFromRoom(User user, ClickCallback<Boolean> callback) {

    }

    @Override
    public void kickUserFromSeat(User user, ClickCallback<Boolean> callback) {

    }

    @Override
    public void changeUserRoom(String roomId) {
        HashMap<String, Object> params = new OkParams()
                .add("roomId", roomId)
                .build();
        OkApi.get(VRApi.USER_ROOM_CHANGE, params, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (result.ok()) {
                    Log.e(TAG, "onResult: " + result.getMessage());
                }
            }
        });
    }

    @Override
    public void finisRoom(ClickCallback<Boolean> callback) {
        RCLiveEngine.getInstance().finish(new RCLiveCallback() {
            @Override
            public void onSuccess() {
                changeUserRoom("");
                callback.onResult(true, "关闭成功");
            }

            @Override
            public void onError(int code, RCLiveError error) {
                callback.onResult(false, "关闭失败");
            }
        });
    }

    @Override
    public void begin(String roomId, ClickCallback<Boolean> callback) {
        register(roomId);
        RCLiveEngine.getInstance().begin(roomId, new RCLiveCallback() {
            @Override
            public void onSuccess() {
                //开启直播并且加入房间成功
                Log.e(TAG, "onSuccess: ");
                changeUserRoom(roomId);
                callback.onResult(true, "开启直播成功");
            }

            @Override
            public void onError(int code, RCLiveError error) {
                Log.e("TAG", "onError: " + code);
                callback.onResult(false, "开启直播失败");
            }
        });
    }

    @Override
    public void prepare(ClickCallback<Boolean> callback) {
        RCLiveEngine.getInstance().prepare(new RCLiveCallback() {
            @Override
            public void onSuccess() {
                callback.onResult(true, "准备直播成功");
            }

            @Override
            public void onError(int code, RCLiveError error) {
                EToast.showToast(error.getMessage());
            }
        });
    }

    @Override
    public void requestLiveVideo(int index, ClickCallback<Boolean> callback) {
        //判断当前是否有足够的视频位置
        RCLiveSeatInfo rcLiveSeatInfo = SeatManager.get().getSeatByIndex(index);
        if (rcLiveSeatInfo == null) {
            EToast.showToast("申请麦位索引不正确！");
            return;
        }
        RCLiveEngine.getInstance().requestLiveVideo(index, new RCLiveCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onResult(true, "");
                }
                EToast.showToast("已申请连线，等待房主接受");
            }

            @Override
            public void onError(int code, RCLiveError error) {
                if (callback != null) {
                    callback.onResult(false, error.getMessage());
                }
                EToast.showToast("请求连麦失败");
            }
        });
    }

    public List<MessageContent> getMessageList() {
        return messageList;
    }

    public String getRoomId() {
        return roomId;
    }

    @Override
    public void register(String roomId) {
        this.roomId = roomId;
        this.messageList = new ArrayList<>();
        RCLiveEngine.getInstance().setLiveEventListener(this);
        liveRoomListeners.clear();
    }

    @Override
    public void unRegister() {
        this.roomId = null;
        messageList.clear();
        RCLiveEngine.getInstance().setLiveEventListener(null);
        liveRoomListeners.clear();
    }

    /**
     * 监听直播房的一些事件
     *
     * @param liveRoomListener
     */
    public void addLiveRoomListeners(LiveRoomListener liveRoomListener) {
        liveRoomListeners.add(liveRoomListener);
    }

    /**
     * 清除直播房fragment监听
     */
    public void removeLiveRoomListeners() {
        liveRoomListeners.clear();
    }

    /**
     * 发送消息
     *
     * @param messageContent 消息体
     * @param isShowLocation 是否在本地显示
     */
    @Override
    public void sendMessage(MessageContent messageContent, boolean isShowLocation) {
        if (!TextUtils.isEmpty(roomId))
            if (messageContent instanceof RCChatroomLocationMessage) {
                RCChatRoomMessageManager.INSTANCE.sendLocationMessage(roomId, messageContent);
            } else {
                RCChatRoomMessageManager.INSTANCE.sendChatMessage(roomId, messageContent, isShowLocation
                        , new Function1<Integer, Unit>() {
                            @Override
                            public Unit invoke(Integer integer) {
                                if (isShowLocation) {
                                    messageList.add(messageContent);
                                }
                                return null;
                            }
                        }, new Function2<IRongCoreEnum.CoreErrorCode, Integer, Unit>() {
                            @Override
                            public Unit invoke(IRongCoreEnum.CoreErrorCode coreErrorCode, Integer integer) {
                                EToast.showToast("发送失败");
                                return null;
                            }
                        });
            }

    }

    @Override
    public void onRoomInfoReady() {
        Log.e(TAG, "onRoomInfoReady: ");
    }

    @Override
    public void onRoomInfoUpdate(String key, String value) {
        Log.e(TAG, "onRoomInfoUpdate: ");
    }

    @Override
    public void onUserEnter(String userId) {
        Log.e(TAG, "onUserEnter: " + userId);
    }

    @Override
    public void onUserExit(String userId) {
        Log.e(TAG, "onUserExit: " + userId);
    }

    /**
     * 用户被踢出房间
     *
     * @param userId     被踢用户唯一标识
     * @param operatorId 踢人操作的执行用户的唯一标识
     */
    @Override
    public void onUserKitOut(String userId, String operatorId) {
        Log.e(TAG, "onUserKitOut: ");
    }

    /**
     * 连麦用户集合
     *
     * @param lineMicUserIds 连麦的用户集合
     */
    @Override
    public void onLiveVideoUpdate(List<String> lineMicUserIds) {
        Log.e(TAG, "onLiveVideoUpdate: " + lineMicUserIds);
    }

    @Override
    public void onLiveVideoRequestChanage() {
        Log.e(TAG, "onLiveVideoRequestChanage: ");
    }

    @Override
    public void onLiveVideoRequestAccepted() {
        Log.e(TAG, "onLiveVideoRequestAccepted: ");
    }

    @Override
    public void onLiveVideoRequestRejected() {
        Log.e(TAG, "onLiveVideoRequestRejected: ");
    }

    @Override
    public void onReceiveLiveVideoRequest() {
        Log.e(TAG, "onReceiveLiveVideoRequest: ");
    }

    @Override
    public void onLiveVideoRequestCanceled() {
        Log.e(TAG, "onLiveVideoRequestCanceled: ");
    }

    @Override
    public void onliveVideoInvitationReceived() {
        Log.e(TAG, "onliveVideoInvitationReceived: ");
    }

    @Override
    public void onliveVideoInvitationCanceled() {
        Log.e(TAG, "onliveVideoInvitationCanceled: ");
    }

    @Override
    public void onliveVideoInvitationAccepted(String userId) {
        Log.e(TAG, "onliveVideoInvitationAccepted: ");
    }

    @Override
    public void onliveVideoInvitationRejected(String userId) {
        Log.e(TAG, "onliveVideoInvitationRejected: ");
    }

    @Override
    public void onLiveVideoStarted() {
        Log.e(TAG, "onLiveVideoStarted: ");
    }

    @Override
    public void onLiveVideoStoped() {
        Log.e(TAG, "onLiveVideoStoped: ");
    }

    @Override
    public void onReceiveMessage(Message message) {
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onReceiveMessage(message);
        }
        //统一处理
        if (!TextUtils.isEmpty(roomId) && message.getConversationType() == Conversation.ConversationType.CHATROOM) {
            RCChatRoomMessageManager.INSTANCE.onReceiveMessage(roomId, message.getContent());
        }
        Log.e(TAG, "onReceiveMessage: ");
    }

    @Override
    public void onNetworkStatus(long delayMs) {
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onNetworkStatus(delayMs);
        }
        Log.e(TAG, "onNetworkStatus: " + delayMs);
    }

    /**
     * 处理美颜
     *
     * @param frame 视频流采样数据
     */
    @Override
    public void onOutputSampleBuffer(RCRTCVideoFrame frame) {
        int render = MhDataManager.getInstance().render(frame.getTextureId(), frame.getWidth(), frame.getWidth());
        frame.setTextureId(render);
    }

    @Override
    public void onLiveVideoUserClick(String userId) {
        Log.e(TAG, "onLiveVideoUserClick: " + userId);
    }

    @Override
    public void onLiveUserLayout(Map<String, RCRect> frameInfo) {
        Log.e(TAG, "onLiveUserLayout: " + frameInfo);
    }

    @Override
    public void onRoomDestory() {
        ConfirmDialog confirmDialog = new ConfirmDialog(UIStack.getInstance().getTopActivity(), "当前直播已结束", true
                , "确定", "", null, new Function0<Unit>() {
            @Override
            public Unit invoke() {
                leaveRoom(null);
                return null;
            }
        });
        confirmDialog.show();
        Log.e(TAG, "onRoomDestory: ");
    }

    @Override
    public void onRoomMixTypeChange(RCLiveMixType mixType) {
        Log.e(TAG, "onRoomMixTypeChange: " + mixType);
    }

    private static class helper {
        static final LiveEventHelper INSTANCE = new LiveEventHelper();
    }

}
