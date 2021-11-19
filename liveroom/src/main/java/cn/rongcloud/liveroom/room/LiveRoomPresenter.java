package cn.rongcloud.liveroom.room;


import android.text.TextUtils;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.basis.mvp.BasePresenter;
import com.basis.net.oklib.OkApi;
import com.basis.net.oklib.OkParams;
import com.basis.net.oklib.WrapperCallBack;
import com.basis.net.oklib.wrapper.Wrapper;
import com.kit.utils.Logger;
import com.rongcloud.common.utils.AccountStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import cn.rong.combusis.provider.voiceroom.RoomOwnerType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.provider.voiceroom.VoiceRoomProvider;
import cn.rong.combusis.sdk.event.wrapper.EToast;
import cn.rong.combusis.ui.room.dialog.shield.Shield;
import cn.rong.combusis.ui.room.fragment.ClickCallback;
import cn.rong.combusis.ui.room.fragment.MemberSettingFragment;
import cn.rong.combusis.ui.room.fragment.roomsetting.IFun;
import cn.rong.combusis.ui.room.fragment.seatsetting.ICommonDialog;
import cn.rong.combusis.ui.room.fragment.seatsetting.RevokeSeatRequestFragment;
import cn.rong.combusis.ui.room.fragment.seatsetting.SeatOperationViewPagerFragment;
import cn.rong.combusis.ui.room.model.MemberCache;
import cn.rong.combusis.ui.room.widget.RoomTitleBar;
import cn.rongcloud.liveroom.helper.LiveEventHelper;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.rong.imlib.model.MessageContent;
import io.rong.message.TextMessage;

/**
 * 直播房
 */
public class LiveRoomPresenter extends BasePresenter<LiveRoomView> implements
        ILiveRoomPresent, RoomTitleBar.OnFollowClickListener,
        MemberSettingFragment.OnMemberSettingClickListener,
        ICommonDialog {

    private VoiceRoomBean mVoiceRoomBean;//房间信息
    private RoomOwnerType roomOwnerType;//房间用户身份
    private List<Shield> shields = new ArrayList<>();//当前屏蔽词
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
        // TODO 请求数据
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
                    mView.dismissLoading();
                    VoiceRoomBean roomBean = result.get(VoiceRoomBean.class);
                    if (roomBean != null) {
                        mVoiceRoomBean = roomBean;
                        setCurrentRoom(mVoiceRoomBean);
                        if (isInRoom) {
                            //如果已经在房间里面了,那么需要重新设置监听
                            initLiveRoomListener(roomId);
                            mView.changeStatus(LiveEventHelper.getInstance().getCurrentStatus());
//                            voiceRoomModel.currentUIRoomInfo.setMute(EventHelper.helper().getMuteAllRemoteStreams());
//                            voiceRoomModel.onSeatInfoUpdate(EventHelper.helper().getRCVoiceSeatInfoList());
                            mView.dismissLoading();
                        } else {
//                            leaveRoom(roomId, isCreate, true);
                        }

                    }
                } else {
                    mView.dismissLoading();
                    if (result.getCode() == 30001) {
                        //房间不存在了
                        mView.showFinishView();
//                        leaveRoom(roomId, isCreate, false);
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
     * 获取屏蔽词
     */
    private void getShield() {
        OkApi.get(VRApi.getShield(mVoiceRoomBean.getRoomId()), null, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (result.ok()) {
                    List<Shield> list = result.getList(Shield.class);
                    shields.clear();
                    if (list != null) {
                        shields.addAll(list);
                    }
                }
            }
        });
    }


    @Override
    public void requestSeat(int position) {

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
                            mView.setRoomName(name);
                            mVoiceRoomBean.setRoomName(name);
//                            RCVoiceRoomInfo rcRoomInfo = voiceRoomModel.currentUIRoomInfo.getRcRoomInfo();
//                            rcRoomInfo.setRoomName(name);
//                            RCVoiceRoomEngine.getInstance().setRoomInfo(rcRoomInfo, new RCVoiceRoomCallback() {
//                                @Override
//                                public void onSuccess() {
//                                    Log.e(TAG, "onSuccess: ");
//                                }
//
//                                @Override
//                                public void onError(int i, String s) {
//                                    Log.e(TAG, "onError: ");
//                                }
//                            });
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
     */
    @Override
    public void setCurrentRoom(VoiceRoomBean mVoiceRoomBean) {
        roomOwnerType = VoiceRoomProvider.provider().getRoomOwnerType(mVoiceRoomBean);
        if (isInRoom) {
            //恢复一下当前信息就可以了
            List<MessageContent> messageList = LiveEventHelper.getInstance().getMessageList();
            mView.addMessageList(messageList, true);
        } else {
            // 发送默认消息
            sendDefaultMessage();
        }
        mView.setRoomName(mVoiceRoomBean.getRoomName());
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
        //监听房间里面的人
        MemberCache.getInstance().getMemberList()
                .observe(((LiveRoomFragment) mView).getViewLifecycleOwner(), new Observer<List<User>>() {
                    @Override
                    public void onChanged(List<User> users) {
                        //人数
//                mView.setOnlineCount(users.size());
//                voiceRoomModel.onMemberListener(users);
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
    }

    @Override
    public void getGiftCount(String roomId) {
        OkApi.get(VRApi.getGiftList(roomId), null, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (result.ok()) {
                    Map<String, String> map = result.getMap();
                    Logger.e("================" + map.toString());

                }
            }
        });
    }

    @Override
    public void modifyNotice(String notice) {

    }

    /**
     * 送给麦位和房主
     */
    @Override
    public void sendGift() {

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
            for (Shield shield : shields) {
                if (messageContent instanceof RCChatroomBarrage) {
                    if (((RCChatroomBarrage) messageContent).getContent().contains(shield.getName())) {
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
    public void showNewRevokeSeatRequestFragment() {
        RevokeSeatRequestFragment revokeSeatRequestFragment = new RevokeSeatRequestFragment();
        revokeSeatRequestFragment.setSeatActionClickListener(this);
        revokeSeatRequestFragment.show(mView.getLiveFragmentManager());
    }
}
