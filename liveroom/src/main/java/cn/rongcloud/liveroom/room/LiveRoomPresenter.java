package cn.rongcloud.liveroom.room;


import static cn.rong.combusis.EventBus.TAG.UPDATE_SHIELD;
import static cn.rong.combusis.sdk.event.wrapper.EToast.showToast;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.basis.mvp.BasePresenter;
import com.basis.net.oklib.OkApi;
import com.basis.net.oklib.OkParams;
import com.basis.net.oklib.WrapperCallBack;
import com.basis.net.oklib.wrapper.Wrapper;
import com.google.gson.JsonArray;
import com.kit.UIKit;
import com.kit.utils.Logger;
import com.rongcloud.common.utils.AccountStore;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rong.combusis.EventBus;
import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.manager.AllBroadcastManager;
import cn.rong.combusis.manager.RCChatRoomMessageManager;
import cn.rong.combusis.message.RCAllBroadcastMessage;
import cn.rong.combusis.message.RCChatroomAdmin;
import cn.rong.combusis.message.RCChatroomBarrage;
import cn.rong.combusis.message.RCChatroomEnter;
import cn.rong.combusis.message.RCChatroomGift;
import cn.rong.combusis.message.RCChatroomGiftAll;
import cn.rong.combusis.message.RCChatroomKickOut;
import cn.rong.combusis.message.RCChatroomLike;
import cn.rong.combusis.message.RCChatroomLocationMessage;
import cn.rong.combusis.message.RCChatroomSeats;
import cn.rong.combusis.message.RCChatroomVoice;
import cn.rong.combusis.message.RCFollowMsg;
import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.provider.voiceroom.CurrentStatusType;
import cn.rong.combusis.provider.voiceroom.RoomOwnerType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.provider.voiceroom.VoiceRoomProvider;
import cn.rong.combusis.sdk.event.listener.LeaveRoomCallBack;
import cn.rong.combusis.sdk.event.wrapper.EToast;
import cn.rong.combusis.ui.room.dialog.shield.Shield;
import cn.rong.combusis.ui.room.fragment.ClickCallback;
import cn.rong.combusis.ui.room.fragment.MemberSettingFragment;
import cn.rong.combusis.ui.room.fragment.gift.GiftFragment;
import cn.rong.combusis.ui.room.fragment.roomsetting.IFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomBeautyFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomBeautyMakeUpFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomLockFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomMusicFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomNameFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomNoticeFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomOverTurnFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomSeatModeFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomShieldFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomSpecialEffectsFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomTagsFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomVideoSetFun;
import cn.rong.combusis.ui.room.fragment.seatsetting.ICommonDialog;
import cn.rong.combusis.ui.room.fragment.seatsetting.RevokeSeatRequestFragment;
import cn.rong.combusis.ui.room.fragment.seatsetting.SeatOperationViewPagerFragment;
import cn.rong.combusis.ui.room.model.Member;
import cn.rong.combusis.ui.room.model.MemberCache;
import cn.rong.combusis.ui.room.widget.RoomBottomView;
import cn.rong.combusis.ui.room.widget.RoomTitleBar;
import cn.rongcloud.liveroom.api.RCLiveEngine;
import cn.rongcloud.liveroom.api.RCLiveMixType;
import cn.rongcloud.liveroom.api.RCRect;
import cn.rongcloud.liveroom.helper.LiveEventHelper;
import cn.rongcloud.liveroom.helper.LiveRoomListener;
import cn.rongcloud.rtc.base.RCRTCVideoFrame;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.TextMessage;

/**
 * 直播房
 */
public class LiveRoomPresenter extends BasePresenter<LiveRoomView> implements
        ILiveRoomPresent, RoomTitleBar.OnFollowClickListener,
        MemberSettingFragment.OnMemberSettingClickListener,
        ICommonDialog, LiveRoomListener, GiftFragment.OnSendGiftListener
        , RoomBottomView.OnBottomOptionClickListener {

    private String TAG = "LiveRoomPresenter";

    private VoiceRoomBean mVoiceRoomBean;//房间信息
    private RoomOwnerType roomOwnerType;//房间用户身份
    private List<String> shields = new ArrayList<>();//当前屏蔽词
    private List<Disposable> disposablesManager = new ArrayList<>();//监听管理器
    private ArrayList<User> requestSeats = new ArrayList<>();//申请连麦的集合
    private ArrayList<User> inviteSeats = new ArrayList<>();//可以被邀请的集合
    private boolean isInRoom;

    public LiveRoomPresenter(LiveRoomView mView, Lifecycle lifecycle) {
        super(mView, lifecycle);
    }


    /**
     * 初始化
     *
     * @param roomId
     * @param isCreate
     */
    public void init(String roomId, boolean isCreate) {
        isInRoom = TextUtils.equals(LiveEventHelper.getInstance().getRoomId(), roomId);
        getRoomInfo(roomId, isCreate);
    }

    /**
     * 获取房间数据
     *
     * @param roomId
     * @param isCreate
     */
    public void getRoomInfo(String roomId, boolean isCreate) {
        mView.showLoading("");
        OkApi.get(VRApi.getRoomInfo(roomId), null, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (result.ok()) {
                    VoiceRoomBean roomBean = result.get(VoiceRoomBean.class);
                    if (roomBean != null) {
                        mVoiceRoomBean = roomBean;
                        if (isInRoom) {
                            //如果已经在房间里面了,那么需要重新设置监听
                            mView.changeStatus(LiveEventHelper.getInstance().getCurrentStatus());
                            setCurrentRoom(mVoiceRoomBean, isCreate);
//                            voiceRoomModel.currentUIRoomInfo.setMute(EventHelper.helper().getMuteAllRemoteStreams());
//                            voiceRoomModel.onSeatInfoUpdate(EventHelper.helper().getRCVoiceSeatInfoList());
                            mView.dismissLoading();
                        } else {
                            leaveRoom(roomId, isCreate, true);
                        }

                    }
                } else {
                    mView.dismissLoading();
                    if (result.getCode() == 30001) {
                        //房间不存在了
                        mView.showFinishView();
                        leaveRoom(roomId, isCreate, false);
                    }
                }
            }

            @Override
            public void onError(int code, String msg) {
                super.onError(code, msg);
            }
        });
    }

    /**
     * 先退出上次房间，再加入房间
     *
     * @param roomId
     * @param isCreate
     * @param isExit
     */
    private void leaveRoom(String roomId, boolean isCreate, boolean isExit) {
        LiveEventHelper.getInstance().leaveRoom(new LeaveRoomCallBack() {
            @Override
            public void onSuccess() {
                if (isExit) {
                    UIKit.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            joinRoom(roomId, isCreate);
                        }
                    }, 1000);
                }
            }

            @Override
            public void onError(int code, String message) {
                if (isExit) {
                    joinRoom(roomId, isCreate);
                }
            }
        });
    }

    /**
     * 加入房间
     *
     * @param roomId
     * @param isCreate
     */
    private void joinRoom(String roomId, boolean isCreate) {
        if (mVoiceRoomBean.getCreateUserId().equals(AccountStore.INSTANCE.getUserId())) {
            prepare(roomId, isCreate);
        } else {
            //如果是观众就直接加入房间
            LiveEventHelper.getInstance().joinRoom(roomId, new ClickCallback<Boolean>() {
                @Override
                public void onResult(Boolean result, String msg) {
                    mView.dismissLoading();
                    if (result) {
                        setCurrentRoom(mVoiceRoomBean, isCreate);
                    }
                }
            });
        }
    }

    /**
     * 获取屏蔽词
     */
    private void getShield() {
        LiveEventHelper.getInstance().getRoomInfoByKey(LiveRoomKvKey.LIVE_ROOM_SHIELDS, new ClickCallback<Boolean>() {
            @Override
            public void onResult(Boolean result, String msg) {
                if (result) {
                    shields.clear();
                    try {
                        JSONArray jsonArray = new JSONArray(msg);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String shile = (String) jsonArray.get(i);
                            shields.add(shile);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    @Override
    public void requestSeat(int position) {
        CurrentStatusType currentStatus = LiveEventHelper.getInstance().getCurrentStatus();
        if (currentStatus == CurrentStatusType.STATUS_ON_SEAT) {
            return;
        }
        //如果当前正在等待并且不可以自有上麦的模式
//        if (currentStatus == CurrentStatusType.STATUS_WAIT_FOR_SEAT && !voiceRoomModel.currentUIRoomInfo.isFreeEnterSeat()) {
//            showRevokeSeatRequestFragment();
//            return;
//        }
        //如果是自由上麦模式
//        if (voiceRoomModel.currentUIRoomInfo.isFreeEnterSeat()) {
//            int index = position;
//            if (index == -1) {
//                index = voiceRoomModel.getAvailableIndex();
//            }
//            if (index == -1) {
//                mView.showToast("当前麦位已满");
//                return;
//            }
//            RCVoiceRoomEngine.getInstance().enterSeat(index, new RCVoiceRoomCallback() {
//                @Override
//                public void onSuccess() {
//                    mView.showToast("上麦成功");
//                    AudioManagerUtil.INSTANCE.choiceAudioModel();
//                }
//
//                @Override
//                public void onError(int code, String message) {
//                    mView.showToast(message);
//                }
//            });
//        } else {
//            //申请视频直播
//            LiveEventHelper.getInstance().requestLiveVideo(position, new ClickCallback<Boolean>() {
//                @Override
//                public void onResult(Boolean result, String msg) {
//                    if (result) {
//                        mView.changeStatus(CurrentStatusType.STATUS_WAIT_FOR_SEAT);
//                    }
//                }
//            });
//        }
    }

    @Override
    public void setRoomPassword(boolean isPrivate, String password, MutableLiveData<IFun.BaseFun> item, String roomId) {
        int p = isPrivate ? 1 : 0;
        OkApi.put(VRApi.ROOM_PASSWORD,
                new OkParams()
                        .add("roomId", roomId)
                        .add("isPrivate", p)
                        .add("password", password).build(),
                new WrapperCallBack() {
                    @Override
                    public void onResult(Wrapper result) {
                        if (result.ok()) {
                            mView.showToast(isPrivate ? "设置成功" : "取消成功");
                            mVoiceRoomBean.setIsPrivate(isPrivate ? 1 : 0);
                            mVoiceRoomBean.setPassword(password);
                            IFun.BaseFun fun = item.getValue();
                            fun.setStatus(p);
                            item.setValue(fun);
                        } else {
                            mView.showToast(isPrivate ? "设置失败" : "取消失败");
                        }
                    }
                });
    }


    @Override
    public void setRoomName(String name, String roomId) {
        OkApi.put(VRApi.ROOM_NAME,
                new OkParams()
                        .add("roomId", roomId)
                        .add("name", name)
                        .build(),
                new WrapperCallBack() {
                    @Override
                    public void onResult(Wrapper result) {
                        if (result.ok()) {
                            EToast.showToast("修改成功");
                            mVoiceRoomBean.setRoomName(name);
                            LiveEventHelper.getInstance().updateRoomInfoKv(LiveRoomKvKey.LIVE_ROOM_NAME, name, null);
                        } else {
                            mView.showToast("修改失败");
                        }
                    }

                    @Override
                    public void onError(int code, String msg) {
                        super.onError(code, msg);
                        mView.showToast("修改失败");
                    }
                });
    }

    /**
     * 设置上麦的模式
     * true 自由上麦
     * false 申请上麦
     *
     * @param isFreeEnterSeat
     */
    @Override
    public void setSeatMode(boolean isFreeEnterSeat) {

    }

    @Override
    public RoomOwnerType getRoomOwnerType() {
        return roomOwnerType;
    }

    /**
     * 设置当前房间
     *
     * @param mVoiceRoomBean
     * @param isCreate
     */
    @Override
    public void setCurrentRoom(VoiceRoomBean mVoiceRoomBean, boolean isCreate) {
        initLiveRoomListener(mVoiceRoomBean.getRoomId());
        roomOwnerType = VoiceRoomProvider.provider().getRoomOwnerType(mVoiceRoomBean);
        if (isInRoom) {
            //恢复一下当前信息就可以了
            List<MessageContent> messageList = LiveEventHelper.getInstance().getMessageList();
            mView.addMessageList(messageList, true);
        } else {
            // 发送默认消息
            sendDefaultMessage();
        }
        //显示直播布局
        mView.showRCLiveVideoView(RCLiveEngine.getInstance().preview());
        getShield();
        getGiftCount(mVoiceRoomBean.getRoomId());
        mView.setRoomData(mVoiceRoomBean);
    }

    /**
     * 设置直播房的各种监听
     *
     * @param roomId
     */
    @Override
    public void initLiveRoomListener(String roomId) {
        setObMessageListener(roomId);
        setObShieldListener();
        LiveEventHelper.getInstance().addLiveRoomListeners(this);
        //监听房间里面的人
        MemberCache.getInstance().getMemberList()
                .observe(((LiveRoomFragment) mView).getViewLifecycleOwner(), new Observer<List<User>>() {
                    @Override
                    public void onChanged(List<User> users) {
                        mView.setOnlineCount(users.size());
                    }
                });
        MemberCache.getInstance().getAdminList()
                .observe(((LiveRoomFragment) mView).getViewLifecycleOwner(), new Observer<List<String>>() {
                    @Override
                    public void onChanged(List<String> strings) {
//                mView.refreshSeat();
                    }
                });
    }

    /**
     * 取消房间的各种监听
     */
    @Override
    public void unInitLiveRoomListener() {
        for (Disposable disposable : disposablesManager) {
            disposable.dispose();
        }
        disposablesManager.clear();
        LiveEventHelper.getInstance().removeLiveRoomListeners();
        EventBus.get().off(UPDATE_SHIELD, null);
    }

    @Override
    public void getGiftCount(String roomId) {
        OkApi.get(VRApi.getGiftList(roomId), null, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (result.ok()) {
                    Map<String, String> map = result.getMap();
                    Logger.e("================" + map.toString());
                    for (String userId : map.keySet()) {
                        if (TextUtils.equals(userId, mVoiceRoomBean.getCreateUserId())) {
                            //创建者礼物数量
                            mView.setCreateUserGift(map.get(userId));
                        }
                    }
                }
            }
        });
    }

    @Override
    public void modifyNotice(String notice) {
        LiveEventHelper.getInstance().updateRoomInfoKv(LiveRoomKvKey.LIVE_ROOM_NOTICE, notice, new ClickCallback<Boolean>() {
            @Override
            public void onResult(Boolean result, String msg) {
                if (result) {
                    TextMessage noticeMsg = TextMessage.obtain("房间公告已更新!");
                    sendMessage(noticeMsg);
                } else {
                    EToast.showToast(msg);
                }
            }
        });
    }

    /**
     * 送给麦位和房主
     */
    @Override
    public void sendGift() {

    }

    /**
     * 准备直播
     *
     * @param roomId
     * @param isCreate
     */
    @Override
    public void prepare(String roomId, boolean isCreate) {
        if (isCreate) {
            //如果是创建房间
            begin(roomId, isCreate);
        } else {
            //如果不是创建房间，而是再次进入房间
            LiveEventHelper.getInstance().prepare(new ClickCallback<Boolean>() {
                @Override
                public void onResult(Boolean result, String msg) {
                    if (result) {
                        begin(roomId, isCreate);
                    }
                }
            });
        }
    }

    /**
     * 开始直播并且加入房间
     *
     * @param roomId
     * @param isCreate
     */
    @Override
    public void begin(String roomId, boolean isCreate) {
        LiveEventHelper.getInstance().begin(roomId, new ClickCallback<Boolean>() {
            @Override
            public void onResult(Boolean result, String msg) {
                mView.dismissLoading();
                if (result) {
                    setCurrentRoom(mVoiceRoomBean, isCreate);
                }
            }
        });
    }

    @Override
    public void finishLiveRoom() {
        mView.showLoading("正在关闭房间");
        LiveEventHelper.getInstance().finisRoom(new ClickCallback<Boolean>() {
            @Override
            public void onResult(Boolean result, String msg) {
                //房主关闭房间，调用删除房间接口
                OkApi.get(VRApi.deleteRoom(mVoiceRoomBean.getRoomId()), null, new WrapperCallBack() {
                    @Override
                    public void onResult(Wrapper result) {
                        mView.dismissLoading();
                        if (result.ok()) {
                            mView.finish();
                        }
                    }

                    @Override
                    public void onError(int code, String msg) {
                        super.onError(code, msg);
                        mView.dismissLoading();
                        mView.showToast(msg);
                    }
                });
            }
        });
    }

    @Override
    public void leaveLiveRoom(ClickCallback callBack) {
        mView.showLoading("正在离开当前房间");
        LiveEventHelper.getInstance().leaveRoom(new LeaveRoomCallBack() {
            @Override
            public void onSuccess() {
                mView.dismissLoading();
                mView.finish();
                if (callBack != null)
                    callBack.onResult(true, "成功");
            }

            @Override
            public void onError(int code, String message) {
                mView.dismissLoading();
                EToast.showToast(message);
            }
        });
    }

    /**
     * 监听自己删除或者添加屏蔽词
     */
    private void setObShieldListener() {
        EventBus.get().on(UPDATE_SHIELD, new EventBus.EventCallback() {
            @SuppressLint("NewApi")
            @Override
            public void onEvent(String tag, Object... args) {
                shields.clear();
                ArrayList<Shield> shieldArrayList = (ArrayList<Shield>) args[0];
                for (Shield shield : shieldArrayList) {
                    if (!shield.isDefault()) {
                        //说明是正常的屏蔽词
                        shields.add(shield.getName());
                    }
                }
                JsonArray jsonElements = new JsonArray();
                for (String shield : shields) {
                    jsonElements.add(shield);
                }
                //发送KV消息
                LiveEventHelper.getInstance().updateRoomInfoKv(LiveRoomKvKey.LIVE_ROOM_SHIELDS, jsonElements.toString(), null);
            }
        });
    }

    /**
     * 在这里处理接收所有的消息
     */
    private void setObMessageListener(String roomId) {
        disposablesManager.add(RCChatRoomMessageManager.INSTANCE.
                obMessageReceiveByRoomId(roomId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MessageContent>() {
                    @Override
                    public void accept(MessageContent messageContent) throws Throwable {
                        //发送成功以后需要清除输入框和隐藏软键盘
                        if (null != mView) {
                            mView.clearInput();
                            mView.hideSoftKeyboardAndIntput();
                        }
                        //将消息显示到列表上
                        Class<? extends MessageContent> aClass = messageContent.getClass();
                        if (RCChatroomLocationMessage.class.equals(aClass) || RCChatroomVoice.class.equals(aClass)
                                || RCChatroomBarrage.class.equals(aClass) || RCChatroomEnter.class.equals(aClass)
                                || RCChatroomKickOut.class.equals(aClass) || RCChatroomGift.class.equals(aClass)
                                || RCChatroomAdmin.class.equals(aClass) || RCChatroomSeats.class.equals(aClass)
                                || RCChatroomGiftAll.class.equals(aClass) || RCFollowMsg.class.equals(aClass)
                                || TextMessage.class.equals(aClass)) {
                            if (null != mView) mView.addMessageContent(messageContent, false);
                        }
                        if (RCChatroomGift.class.equals(aClass) || RCChatroomGiftAll.class.equals(aClass)) {
                            getGiftCount(roomId);
                        } else if (aClass.equals(RCChatroomLike.class)) {
                            if (null != mView) mView.showLikeAnimation();
                        } else if (aClass.equals(RCAllBroadcastMessage.class)) {
                            AllBroadcastManager.getInstance().addMessage((RCAllBroadcastMessage) messageContent);
                        } else if (aClass.equals(RCChatroomSeats.class)) {
//                            refreshRoomMember();
                        } else if (aClass.equals(RCChatroomLocationMessage.class)) {

                        } else if (aClass.equals(RCChatroomAdmin.class)) {
                            MemberCache.getInstance().refreshAdminData(mVoiceRoomBean.getRoomId());
                        }
                    }
                }));
    }

    /**
     * 发送消息
     * 默认显示在本地
     *
     * @param messageContent
     */
    @Override
    public void sendMessage(MessageContent messageContent) {
        sendMessage(messageContent, true);
    }

    /**
     * 发送消息
     *
     * @param messageContent 消息体
     * @param isShowLocation 是否显示在本地
     */
    @Override
    public void sendMessage(MessageContent messageContent, boolean isShowLocation) {
        if (!isContainsShield(messageContent))
            LiveEventHelper.getInstance().sendMessage(messageContent, isShowLocation);
    }

    /**
     * 是否包含屏蔽词
     *
     * @return
     */
    private boolean isContainsShield(MessageContent messageContent) {
        boolean isContains = false;
        if (shields != null) {
            for (String shield : shields) {
                if (messageContent instanceof RCChatroomBarrage) {
                    if (((RCChatroomBarrage) messageContent).getContent().contains(shield)) {
                        isContains = true;
                        break;
                    }
                }
            }
            if (isContains) {
                //如果是包含了敏感词'
                mView.addMessageContent(messageContent, false);
                mView.clearInput();
                mView.hideSoftKeyboardAndIntput();
                return true;
            }
        }
        return false;
    }


    /**
     * 界面销毁，取消房间的各种监听,但是不代表取消房间的事件监听
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        unInitLiveRoomListener();
    }

    /**
     * 设置管理员
     *
     * @param user
     * @param callback
     */
    @Override
    public void clickSettingAdmin(User user, ClickCallback<Boolean> callback) {
        if (mVoiceRoomBean == null) {
            return;
        }
        boolean isAdmin = !MemberCache.getInstance().isAdmin(user.getUserId());
        HashMap<String, Object> params = new OkParams()
                .add("roomId", mVoiceRoomBean.getRoomId())
                .add("userId", user.getUserId())
                .add("isManage", isAdmin)
                .build();
        // 先请求 设置/取消 管理员
        OkApi.put(VRApi.ADMIN_MANAGE, params, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (result.ok()) {
                    RCChatroomAdmin admin = new RCChatroomAdmin();
                    admin.setAdmin(isAdmin);
                    admin.setUserId(user.getUserId());
                    admin.setUserName(user.getUserName());
                    // 成功后发送管理变更的消息
                    sendMessage(admin);
                    callback.onResult(true, "");
                } else {
                    showToast(result.getMessage());
                    callback.onResult(true, result.getMessage());
                }
            }
        });
    }

    /**
     * 踢出房间
     *
     * @param user
     * @param callback
     */
    @Override
    public void clickKickRoom(User user, ClickCallback<Boolean> callback) {

    }

    /**
     * 点击发送礼物
     *
     * @param user
     */
    @Override
    public void clickSendGift(User user) {
        mView.showSendGiftDialog(mVoiceRoomBean, user.getUserId(), Arrays.asList(new Member().toMember(user)));
    }

    /**
     * 关注
     *
     * @param isFollow
     * @param followMsg
     */
    @Override
    public void clickFollow(boolean isFollow, RCFollowMsg followMsg) {
        if (isFollow) {
            sendMessage(followMsg);
        }
        mView.setTitleFollow(isFollow);
    }

    /**
     * 进入房间后发送默认的消息
     */
    private void sendDefaultMessage() {
        if (mVoiceRoomBean != null) {
            mView.addMessageContent(null, true);
            // 默认消息
            RCChatroomLocationMessage welcome = new RCChatroomLocationMessage();
            welcome.setContent(String.format("欢迎来到 %s", mVoiceRoomBean.getRoomName()));
            sendMessage(welcome);
            RCChatroomLocationMessage tips = new RCChatroomLocationMessage();
            tips.setContent("感谢使用融云 RTC 语音房，请遵守相关法规，不要传播低俗、暴力等不良信息。欢迎您把使用过程中的感受反馈给我们。");
            sendMessage(tips);
            // 广播消息
            RCChatroomEnter enter = new RCChatroomEnter();
            enter.setUserId(AccountStore.INSTANCE.getUserId());
            enter.setUserName(AccountStore.INSTANCE.getUserName());
            sendMessage(enter, false);
        }
    }

    /**
     * 邀请上麦
     *
     * @param user
     * @param callback
     */
    @Override
    public void clickInviteSeat(User user, ClickCallback<Boolean> callback) {

    }

    /**
     * 同意上麦
     *
     * @param userId
     * @param callback
     */
    @Override
    public void acceptRequestSeat(String userId, ClickCallback<Boolean> callback) {

    }

    /**
     * 撤销申请
     *
     * @param callback
     */
    @Override
    public void cancelRequestSeat(ClickCallback<Boolean> callback) {

    }

    /**
     * 踢下麦
     *
     * @param user
     * @param callback
     */
    @Override
    public void clickKickSeat(User user, ClickCallback<Boolean> callback) {

    }

    /**
     * 开麦或禁麦
     *
     * @param seatIndex
     * @param isMute
     * @param callback
     */
    @Override
    public void clickMuteSeat(int seatIndex, boolean isMute, ClickCallback<Boolean> callback) {

    }

    /**
     * 关闭座位或者打开座位
     *
     * @param seatIndex
     * @param isLock
     * @param callback
     */
    @Override
    public void clickCloseSeat(int seatIndex, boolean isLock, ClickCallback<Boolean> callback) {

    }

    /**
     * 邀请弹窗
     *
     * @param index
     */
    @Override
    public void showSeatOperationViewPagerFragment(int index) {
        SeatOperationViewPagerFragment seatOperationViewPagerFragment
                = new SeatOperationViewPagerFragment(requestSeats, inviteSeats);
        seatOperationViewPagerFragment.setIndex(index);
//        seatOperationViewPagerFragment.setObInviteSeatListChangeSuject(voiceRoomModel.obInviteSeatListChange());
//        seatOperationViewPagerFragment.setObRequestSeatListChangeSuject(voiceRoomModel.obRequestSeatListChange());
        seatOperationViewPagerFragment.setSeatActionClickListener(this);
        seatOperationViewPagerFragment.show(mView.getLiveFragmentManager());
    }

    /**
     * 撤销弹窗
     */
    @Override
    public void showRevokeSeatRequestFragment() {
        RevokeSeatRequestFragment revokeSeatRequestFragment = new RevokeSeatRequestFragment();
        revokeSeatRequestFragment.setSeatActionClickListener(this);
        revokeSeatRequestFragment.show(mView.getLiveFragmentManager());
    }

    @Override
    public void onRoomInfoReady() {

    }

    @Override
    public void onRoomInfoUpdate(String key, String value) {
        switch (key) {
            case LiveRoomKvKey.LIVE_ROOM_NAME://房间名
                mVoiceRoomBean.setRoomName(value);
                break;
            case LiveRoomKvKey.LIVE_ROOM_NOTICE://房间公告
                mView.setNotice(value);
                break;
            case LiveRoomKvKey.LIVE_ROOM_SHIELDS://房间屏蔽词
                getShield();
                break;
        }
    }

    @Override
    public void onUserEnter(String userId) {

    }

    @Override
    public void onUserExit(String userId) {

    }

    @Override
    public void onUserKitOut(String userId, String operatorId) {

    }

    @Override
    public void onLiveVideoUpdate(List<String> lineMicUserIds) {

    }

    @Override
    public void onLiveVideoRequestChanage() {

    }

    @Override
    public void onLiveVideoRequestAccepted() {

    }

    @Override
    public void onLiveVideoRequestRejected() {

    }

    @Override
    public void onReceiveLiveVideoRequest() {

    }

    @Override
    public void onLiveVideoRequestCanceled() {

    }

    @Override
    public void onliveVideoInvitationReceived() {

    }

    @Override
    public void onliveVideoInvitationCanceled() {

    }

    @Override
    public void onliveVideoInvitationAccepted(String userId) {

    }

    @Override
    public void onliveVideoInvitationRejected(String userId) {

    }

    @Override
    public void onLiveVideoStarted() {

    }

    @Override
    public void onLiveVideoStoped() {

    }

    @Override
    public void onReceiveMessage(Message message) {

    }

    @Override
    public void onNetworkStatus(long delayMs) {
        mView.showNetWorkStatus(delayMs);
    }

    @Override
    public void onOutputSampleBuffer(RCRTCVideoFrame frame) {

    }

    @Override
    public void onLiveVideoUserClick(String userId) {

    }

    @Override
    public void onLiveUserLayout(Map<String, RCRect> frameInfo) {

    }

    @Override
    public void onRoomDestory() {

    }

    @Override
    public void onRoomMixTypeChange(RCLiveMixType mixType) {

    }

    @Override
    public void onSendGiftSuccess(List<MessageContent> messages) {
        if (messages != null && !messages.isEmpty()) {
            for (MessageContent message : messages) {
                sendMessage(message);
            }
            getGiftCount(mVoiceRoomBean.getRoomId());
        }
    }

    public String getRoomId() {
        if (mVoiceRoomBean != null) {
            return mVoiceRoomBean.getRoomId();
        }
        return "";
    }

    public String getCreateUserId() {
        if (mVoiceRoomBean != null) {
            return mVoiceRoomBean.getCreateUserId();
        }
        return "";
    }

    public String getRoomName() {
        if (mVoiceRoomBean != null) {
            return mVoiceRoomBean.getRoomName();
        }
        return "";
    }

    @Override
    public void clickSendMessage(String message) {
        //发送文字消息
        RCChatroomBarrage barrage = new RCChatroomBarrage();
        barrage.setContent(message);
        barrage.setUserId(AccountStore.INSTANCE.getUserId());
        barrage.setUserName(AccountStore.INSTANCE.getUserName());
        sendMessage(barrage);
    }

    @Override
    public void clickPrivateMessage() {
        RouteUtils.routeToSubConversationListActivity(
                mView.getLiveActivity(),
                Conversation.ConversationType.PRIVATE,
                "消息"
        );
    }

    @Override
    public void clickSeatOrder() {
        showSeatOperationViewPagerFragment(0);
    }

    @Override
    public void clickSettings() {
        List<MutableLiveData<IFun.BaseFun>> funList = Arrays.asList(
                new MutableLiveData<>(new RoomLockFun(mVoiceRoomBean.isPrivate() ? 1 : 0)),
                new MutableLiveData<>(new RoomNameFun(0)),
                new MutableLiveData<>(new RoomNoticeFun(0)),
                new MutableLiveData<>(new RoomShieldFun(0)),
                new MutableLiveData<>(new RoomOverTurnFun(0)),
                new MutableLiveData<>(new RoomTagsFun(0)),
                new MutableLiveData<>(new RoomBeautyFun(0)),
                new MutableLiveData<>(new RoomBeautyMakeUpFun(0)),
                new MutableLiveData<>(new RoomSeatModeFun(0)),
                new MutableLiveData<>(new RoomSpecialEffectsFun(0)),
                new MutableLiveData<>(new RoomMusicFun(0)),
                new MutableLiveData<>(new RoomVideoSetFun(0))
        );
        mView.showRoomSettingFragment(funList);
    }

    @Override
    public void clickPk() {

    }

    @Override
    public void clickRequestSeat() {
        requestSeat(-1);
    }

    @Override
    public void onSendGift() {
        sendGift();
    }

    @Override
    public void onSendVoiceMessage(RCChatroomVoice rcChatroomVoice) {
        sendMessage(rcChatroomVoice);
    }

}
