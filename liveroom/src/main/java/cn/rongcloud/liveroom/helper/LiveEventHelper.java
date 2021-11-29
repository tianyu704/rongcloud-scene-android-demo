package cn.rongcloud.liveroom.helper;

import static cn.rong.combusis.provider.voiceroom.CurrentStatusType.STATUS_NOT_ON_SEAT;
import static cn.rong.combusis.provider.voiceroom.CurrentStatusType.STATUS_ON_SEAT;
import static cn.rong.combusis.provider.voiceroom.CurrentStatusType.STATUS_WAIT_FOR_SEAT;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;

import com.basis.UIStack;
import com.basis.net.oklib.OkApi;
import com.basis.net.oklib.OkParams;
import com.basis.net.oklib.WrapperCallBack;
import com.basis.net.oklib.wrapper.Wrapper;
import com.meihu.beauty.utils.MhDataManager;
import com.rongcloud.common.utils.AccountStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import cn.rongcloud.liveroom.api.callback.RCLiveResultCallback;
import cn.rongcloud.liveroom.api.error.RCLiveError;
import cn.rongcloud.liveroom.manager.SeatManager;
import cn.rongcloud.liveroom.room.LiveRoomKvKey;
import cn.rongcloud.rtc.base.RCRTCVideoFrame;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
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
    private String createUserId;//房间创建人ID
    private CurrentStatusType currentStatus = STATUS_NOT_ON_SEAT;
    private List<LiveRoomListener> liveRoomListeners = new ArrayList<>();
    private ConfirmDialog pickReceivedDialog;

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
    public void register(String roomId) {
        this.roomId = roomId;
        this.messageList = new ArrayList<>();
        RCLiveEngine.getInstance().setLiveEventListener(this);
        liveRoomListeners.clear();
    }

    @Override
    public void unRegister() {
        this.roomId = null;
        this.createUserId = null;
        setCurrentStatus(STATUS_NOT_ON_SEAT);
        messageList.clear();
        RCLiveEngine.getInstance().setLiveEventListener(null);
        liveRoomListeners.clear();
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
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

    /**
     * 如果麦位为-1，会自动查询第一个麦位
     *
     * @param userId
     * @param index
     * @param callback
     */
    @Override
    public void pickUserToSeat(String userId, int index, ClickCallback<Boolean> callback) {
        RCLiveEngine.getInstance().invitateLiveVideo(userId, index, new RCLiveCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) callback.onResult(true, "邀请用户已成功");
            }

            @Override
            public void onError(int code, RCLiveError error) {
                if (callback != null) callback.onResult(false, "邀请用户已失败");
            }
        });
    }

    /**
     * 撤销麦位邀请
     */
    @Override
    public void cancelInvitation(String userId, ClickCallback<Boolean> callback) {
        RCLiveEngine.getInstance().cancelInvitation(userId, new RCLiveCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) callback.onResult(true, "撤销麦位邀请成功");
            }

            @Override
            public void onError(int code, RCLiveError error) {
                if (callback != null) callback.onResult(true, "撤销麦位邀请失败");
            }
        });
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
                    callback.onResult(false, "接受请求连麦失败:" + error.getMessage());
            }
        });
    }

    /**
     * 拒绝用户的上麦申请
     *
     * @param userId
     * @param callback
     */
    @Override
    public void rejectRequestSeat(String userId, ClickCallback<Boolean> callback) {
        RCLiveEngine.getInstance().rejectRequest(userId, new RCLiveCallback() {
            @Override
            public void onSuccess() {
                if (callback != null)
                    callback.onResult(true, "拒绝请求连麦成功");
            }

            @Override
            public void onError(int code, RCLiveError error) {
                if (callback != null)
                    callback.onResult(false, "拒绝请求连麦申请失败:" + error.getMessage());
            }
        });
    }

    @Override
    public void cancelRequestSeat(ClickCallback<Boolean> callback) {
        RCLiveEngine.getInstance().cancelRequest(new RCLiveCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    setCurrentStatus(STATUS_NOT_ON_SEAT);
                    callback.onResult(true, "取消请求连麦成功");
                }

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
        RCLiveEngine.getInstance().kictOutRoom(user.getUserId(), new RCLiveCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) callback.onResult(true, "踢出成功");
            }

            @Override
            public void onError(int code, RCLiveError error) {
                if (callback != null) callback.onResult(false, error.getMessage());
            }
        });
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
    public void finishRoom(ClickCallback<Boolean> callback) {
        RCLiveEngine.getInstance().finish(new RCLiveCallback() {
            @Override
            public void onSuccess() {
                unRegister();
                changeUserRoom("");
                if (callback != null)
                    callback.onResult(true, "关闭成功");
            }

            @Override
            public void onError(int code, RCLiveError error) {
                if (callback != null)
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
                if (callback != null)
                    callback.onResult(true, "开启直播成功");
            }

            @Override
            public void onError(int code, RCLiveError error) {
                Log.e("TAG", "onError: " + code);
                if (callback != null)
                    callback.onResult(false, "开启直播失败");
            }
        });
    }

    @Override
    public void prepare(ClickCallback<Boolean> callback) {
        RCLiveEngine.getInstance().prepare(new RCLiveCallback() {
            @Override
            public void onSuccess() {
                if (callback != null)
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
        RCLiveEngine.getInstance().requestLiveVideo(index, new RCLiveCallback() {
            @Override
            public void onSuccess() {
                setCurrentStatus(STATUS_WAIT_FOR_SEAT);
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

    @Override
    public void enterSeat(int index, ClickCallback<Boolean> callback) {
        //判断当前是否有足够的视频位置
        RCLiveSeatInfo rcLiveSeatInfo = SeatManager.get().getSeatByIndex(index);
        if (rcLiveSeatInfo == null) {
            EToast.showToast("麦位已满！");
            return;
        }
        //上麦成功的话
        setCurrentStatus(STATUS_ON_SEAT);
    }

    @Override
    public void updateRoomInfoKv(String key, String vaule, ClickCallback<Boolean> callback) {
        Map<String, String> kv = new HashMap<>();
        kv.put(key, vaule);
        RCLiveEngine.getInstance().setRoomInfo(kv, new RCLiveCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onResult(true, "更新" + key + "成功");
                }
            }

            @Override
            public void onError(int code, RCLiveError error) {
                if (callback != null) {
                    callback.onResult(false, "更新" + key + "失败:" + error.getMessage());
                }
            }
        });
    }

    /**
     * 获取KV消息
     *
     * @param key
     * @param callback
     */
    @Override
    public void getRoomInfoByKey(String key, ClickCallback<Boolean> callback) {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Throwable {
                RCLiveEngine.getInstance().getRoomInfo(key, new RCLiveResultCallback<String>() {
                    @Override
                    public void onResult(String vaule) {
                        if (TextUtils.equals(LiveRoomKvKey.LIVE_ROOM_ENTER_SEAT_MODE, key)) {
                            //如果是查询上麦模式
                            if (TextUtils.isEmpty(vaule)) {
                                //默认为申请上麦
                                emitter.onNext("0");
                                return;
                            }
                        }
                        emitter.onNext(vaule);
                    }

                    @Override
                    public void onError(int code, RCLiveError error) {
                        emitter.onError(new Throwable(error.getMessage()));
                    }
                });
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String vaule) throws Throwable {
                        if (callback != null) {
                            callback.onResult(true, vaule);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        if (callback != null) {
                            callback.onResult(false, throwable.getMessage());
                        }
                    }
                });
    }

    /**
     * 获取正在申请的人
     *
     * @param callback
     */
    @Override
    public void getRequestLiveVideoIds(ClickCallback<List<String>> callback) {
        RCLiveEngine.getInstance().getRequestLiveVideoIds(new RCLiveResultCallback<List<String>>() {
            @Override
            public void onResult(List<String> result) {
                for (LiveRoomListener liveRoomListener : liveRoomListeners) {
                    liveRoomListener.onRequestLiveVideoIds(result);
                }
                if (callback != null) {
                    callback.onResult(result, "");
                }
            }

            @Override
            public void onError(int code, RCLiveError error) {

            }
        });
    }

    /**
     * 获取邀请上麦的人数
     *
     * @param callback
     */
    @Override
    public void getInvitateLiveVideoIds(ClickCallback<List<String>> callback) {
        RCLiveEngine.getInstance().getInvitateLiveVideoIds(new RCLiveResultCallback<List<String>>() {
            @Override
            public void onResult(List<String> result) {
                if (callback != null) callback.onResult(result, "");
            }

            @Override
            public void onError(int code, RCLiveError error) {
                Log.e(TAG, "getInvitateLiveVideoIds: " + "获取邀请上麦的人数失败：" + error);
            }
        });
    }

    public List<MessageContent> getMessageList() {
        return messageList;
    }

    public String getRoomId() {
        return roomId;
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
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onRoomInfoReady();
        }
        Log.e(TAG, "onRoomInfoReady: ");
    }

    /**
     * @param key   直播间信息的存kv的key
     * @param value 直播间信息的存kv的value
     */
    @Override
    public void onRoomInfoUpdate(String key, String value) {
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onRoomInfoUpdate(key, value);
        }
        Log.e(TAG, "onRoomInfoUpdate: ");
    }


    @Override
    public void onUserEnter(String userId) {
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onUserEnter(userId);
        }
        Log.e(TAG, "onUserEnter: " + userId);
    }

    @Override
    public void onUserExit(String userId) {
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onUserExit(userId);
        }
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
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onUserKitOut(userId, operatorId);
        }
        //被踢出房间，调用离开房间接口和反注册
        if (TextUtils.equals(userId, AccountStore.INSTANCE.getUserId())) {
            EToast.showToast("你已被踢出房间");
            leaveRoom(null);
        }
        Log.e(TAG, "onUserKitOut: ");
    }

    /**
     * 连麦用户集合
     *
     * @param lineMicUserIds 连麦的用户集合
     */
    @Override
    public void onLiveVideoUpdate(List<String> lineMicUserIds) {
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onLiveVideoUpdate(lineMicUserIds);
        }
        Log.e(TAG, "onLiveVideoUpdate: " + lineMicUserIds);
    }

    /**
     * 申请麦位列表发生了变化
     */
    @Override
    public void onLiveVideoRequestChanage() {
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onLiveVideoRequestChanage();
        }
        Log.e(TAG, "onLiveVideoRequestChanage: ");
    }

    /**
     * 申请上麦被同意：只有申请者收到回调
     */
    @Override
    public void onLiveVideoRequestAccepted() {
        setCurrentStatus(STATUS_ON_SEAT);
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onLiveVideoRequestAccepted();
        }
        Log.e(TAG, "onLiveVideoRequestAccepted: ");
    }

    /**
     * 申请上麦被拒绝：只有申请者收到回调
     */
    @Override
    public void onLiveVideoRequestRejected() {
        setCurrentStatus(STATUS_NOT_ON_SEAT);
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onLiveVideoRequestRejected();
        }
        Log.e(TAG, "onLiveVideoRequestRejected: ");
    }

    /**
     * 接收到上麦申请：只有被申请者收到回调
     */
    @Override
    public void onReceiveLiveVideoRequest() {
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onReceiveLiveVideoRequest();
        }
        Log.e(TAG, "onReceiveLiveVideoRequest: ");
    }

    /**
     * 申请上麦已被取消：只有被申请者收到回调
     */
    @Override
    public void onLiveVideoRequestCanceled() {
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onLiveVideoRequestCanceled();
        }
        Log.e(TAG, "onLiveVideoRequestCanceled: ");
    }

    /**
     * 收到连线邀请
     */
    @Override
    public void onliveVideoInvitationReceived() {
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onliveVideoInvitationReceived();
        }
        getInvitateLiveVideoIds(new ClickCallback<List<String>>() {
            @Override
            public void onResult(List<String> result, String msg) {
                for (String s : result) {
                    if (TextUtils.equals(s, AccountStore.INSTANCE.getUserId())) {
                        showPickReceivedDialog();
                        break;
                    }
                }
            }
        });
        Log.e(TAG, "onliveVideoInvitationReceived: ");
    }

    /**
     * 弹窗收到上麦邀请弹窗
     */
    public void showPickReceivedDialog() {
        pickReceivedDialog = new ConfirmDialog((UIStack.getInstance().getTopActivity()),
                "主播邀请您连线，是否同意? 10S", true,
                "同意", "拒绝", new Function0<Unit>() {
            @Override
            public Unit invoke() {
                //拒绝邀请
                RCLiveEngine.getInstance().rejectInvitation(null);
                return null;
            }
        }, new Function0<Unit>() {
            @Override
            public Unit invoke() {
                //同意邀请
                RCLiveEngine.getInstance().acceptInvitation(null);
                if (currentStatus == STATUS_WAIT_FOR_SEAT) {
                    //被邀请上麦了，并且同意了，如果该用户已经申请了上麦，那么主动撤销掉申请
                    cancelRequestSeat(null);
                }
                return null;
            }
        }
        );
        Disposable subscribe = Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(11)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Throwable {
                        pickReceivedDialog.updateMessage("主播邀请您连线，是否同意? " + (10 - aLong) + "s");
                        if (10 == aLong) {
                            //超时自动拒绝
                            RCLiveEngine.getInstance().rejectInvitation(null);
                            pickReceivedDialog.dismiss();
                        }
                    }
                });
        pickReceivedDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (subscribe != null) {
                    subscribe.dispose();
                }
            }
        });
        pickReceivedDialog.show();
    }

    /**
     * 收到取消上麦邀请：只有被邀请者收到
     */
    @Override
    public void onliveVideoInvitationCanceled() {
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onliveVideoInvitationCanceled();
        }
        if (pickReceivedDialog != null) pickReceivedDialog.dismiss();
        Log.e(TAG, "onliveVideoInvitationCanceled: ");
    }

    @Override
    public void onliveVideoInvitationAccepted(String userId) {
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onliveVideoInvitationAccepted(userId);
        }
        if (TextUtils.equals(userId, AccountStore.INSTANCE.getUserId())) {
            EToast.showToast("用户连线成功");
        }
        Log.e(TAG, "onliveVideoInvitationAccepted: ");
    }

    @Override
    public void onliveVideoInvitationRejected(String userId) {
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onliveVideoInvitationRejected(userId);
        }
        if (TextUtils.equals(userId, AccountStore.INSTANCE.getUserId())) {
            EToast.showToast("用户拒绝邀请");
        }
        Log.e(TAG, "onliveVideoInvitationRejected: ");
    }

    @Override
    public void onLiveVideoStarted() {
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onLiveVideoStarted();
        }
        Log.e(TAG, "onLiveVideoStarted: ");
    }

    @Override
    public void onLiveVideoStoped() {
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onLiveVideoStoped();
        }
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
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onLiveVideoUserClick(userId);
        }
        Log.e(TAG, "onLiveVideoUserClick: " + userId);
    }

    @Override
    public void onLiveUserLayout(Map<String, RCRect> frameInfo) {
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onLiveUserLayout(frameInfo);
        }
        Log.e(TAG, "onLiveUserLayout: " + frameInfo);
    }

    @Override
    public void onRoomDestory() {
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onRoomDestory();
        }
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
        for (LiveRoomListener liveRoomListener : liveRoomListeners) {
            liveRoomListener.onRoomMixTypeChange(mixType);
        }
        Log.e(TAG, "onRoomMixTypeChange: " + mixType);
    }

    private static class helper {
        static final LiveEventHelper INSTANCE = new LiveEventHelper();
    }

}
