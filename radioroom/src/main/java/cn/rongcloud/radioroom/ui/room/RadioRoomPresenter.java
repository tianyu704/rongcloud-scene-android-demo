package cn.rongcloud.radioroom.ui.room;

import android.text.TextUtils;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.basis.mvp.BasePresenter;
import com.basis.net.oklib.OkApi;
import com.basis.net.oklib.OkParams;
import com.basis.net.oklib.WrapperCallBack;
import com.basis.net.oklib.wrapper.Wrapper;
import com.kit.utils.Logger;
import com.rongcloud.common.utils.AccountStore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.message.RCChatroomAdmin;
import cn.rong.combusis.message.RCChatroomBarrage;
import cn.rong.combusis.message.RCChatroomEnter;
import cn.rong.combusis.message.RCChatroomGift;
import cn.rong.combusis.message.RCChatroomGiftAll;
import cn.rong.combusis.message.RCChatroomKickOut;
import cn.rong.combusis.message.RCChatroomLocationMessage;
import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.ui.OnItemClickListener;
import cn.rong.combusis.ui.room.dialog.shield.Shield;
import cn.rong.combusis.ui.room.fragment.BackgroundSettingFragment;
import cn.rong.combusis.ui.room.fragment.ClickCallback;
import cn.rong.combusis.ui.room.fragment.roomsetting.IFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomBackgroundFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomLockFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomMusicFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomNameFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomNoticeFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomPauseFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomShieldFun;
import cn.rong.combusis.ui.room.model.MemberCache;
import cn.rong.combusis.ui.room.widget.RoomSeatView;
import cn.rongcloud.messager.RCMessager;
import cn.rongcloud.messager.SendMessageCallback;
import cn.rongcloud.radioroom.IRCRadioRoomEngine;
import cn.rongcloud.radioroom.RCRadioRoomEngine;
import cn.rongcloud.radioroom.callback.RCRadioRoomCallback;
import cn.rongcloud.radioroom.callback.RCRadioRoomResultCallback;
import cn.rongcloud.radioroom.rroom.RCRadioEventListener;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.ChatRoomInfo;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

/**
 * @author gyn
 * @date 2021/9/24
 */
public class RadioRoomPresenter extends BasePresenter<RadioRoomView> implements RCRadioEventListener,
        RadioRoomMemberSettingClickListener, OnItemClickListener<MutableLiveData<IFun.BaseFun>>, BackgroundSettingFragment.OnSelectBackgroundListener {
    private VoiceRoomBean mVoiceRoomBean;
    private String mRoomId = "";

    public RadioRoomPresenter(RadioRoomView mView, LifecycleOwner lifecycleOwner) {
        super(mView, lifecycleOwner.getLifecycle());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void joinRoom(VoiceRoomBean voiceRoomBean) {
        // 注册房间事件监听
        RCRadioRoomEngine.getInstance().setRadioEventListener(this);
        if (voiceRoomBean != null) {
            this.mVoiceRoomBean = voiceRoomBean;
            mRoomId = voiceRoomBean.getRoomId();
            mView.setRoomData(voiceRoomBean);
            // 发送默认消息
            sendDefaultMessage();
            // 获取房间内成员和管理员列表
            MemberCache.getInstance().fetchData(mRoomId);
            refreshRoomMemberCount();
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

    /**
     * 刷新房间信息
     */
    public void refreshRoomInfo() {
        OkApi.get(VRApi.getRoomInfo(mRoomId), null, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (result.ok()) {
                    VoiceRoomBean roomBean = result.get(VoiceRoomBean.class);
                    if (roomBean != null) {
                        mVoiceRoomBean = roomBean;
                        mView.setRoomData(mVoiceRoomBean);
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
     * 房主上麦
     */
    public void enterSeat() {
        RCRadioRoomEngine.getInstance().enterSeat(new RCRadioRoomCallback() {
            @Override
            public void onSuccess() {
                Logger.e("==============enterSeat onSuccess");
                mView.setSeatState(RoomSeatView.SeatState.NORMAL);
                RCRadioRoomEngine.getInstance().notifyRadioRoom(IRCRadioRoomEngine.NotifyType.RC_SUSPEND, "0", null);
            }

            @Override
            public void onError(int code, String message) {
                Logger.e("==============enterSeat onError, code:" + code + ",message:" + message);
            }
        });
    }

    /**
     * 刷新房间人数
     */
    public void refreshRoomMemberCount() {
        RongIMClient.getInstance()
                .getChatRoomInfo(mRoomId, 0, ChatRoomInfo.ChatRoomMemberOrder.RC_CHAT_ROOM_MEMBER_ASC, new RongIMClient.ResultCallback<ChatRoomInfo>() {
                    @Override
                    public void onSuccess(ChatRoomInfo chatRoomInfo) {
                        if (chatRoomInfo != null) {
                            mView.setOnlineCount(chatRoomInfo.getTotalMemberCount());
                        }
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                    }
                });
    }

    /**
     * 获取房间公告
     */
    public void getNotice(boolean isModify) {
        RCRadioRoomEngine.getInstance().getNotice(new RCRadioRoomResultCallback<String>() {
            @Override
            public void onSuccess(String s) {
                mView.showNotice(s, isModify);
            }

            @Override
            public void onError(int i, String s) {
                mView.showNotice(String.format("欢迎来到%s。", mVoiceRoomBean.getRoomName()), isModify);
            }
        });
    }

    /**
     * 修改房间公告
     *
     * @param notice
     */
    public void modifyNotice(String notice) {
        RCRadioRoomEngine.getInstance().notifyRadioRoom(IRCRadioRoomEngine.NotifyType.RC_NOTICE, notice, new RCRadioRoomCallback() {
            @Override
            public void onSuccess() {
                sendNoticeModifyMessage();
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    /**
     * 发送默认消息
     */
    private void sendDefaultMessage() {
        if (mVoiceRoomBean != null) {
            // 清空所有消息
            mView.addToMessageList(null, true);
            // 默认消息
            RCChatroomLocationMessage welcome = new RCChatroomLocationMessage();
            welcome.setContent(String.format("欢迎来到 %s", mVoiceRoomBean.getRoomName()));
            mView.addToMessageList(welcome, false);
            RCChatroomLocationMessage tips = new RCChatroomLocationMessage();
            tips.setContent("感谢使用融云 RTC 语音房，请遵守相关法规，不要传播低俗、暴力等不良信息。欢迎您把使用过程中的感受反馈给我们。");
            mView.addToMessageList(tips, false);
            // 发送进入房间的消息
            RCChatroomEnter enter = new RCChatroomEnter();
            enter.setUserId(AccountStore.INSTANCE.getUserId());
            enter.setUserName(AccountStore.INSTANCE.getUserName());
            sendMessage(enter);
        }
    }

    /**
     * 发送公告更新的
     */
    private void sendNoticeModifyMessage() {
        RCChatroomLocationMessage tips = new RCChatroomLocationMessage();
        tips.setContent("房间公告已更新！");
        mView.addToMessageList(tips, false);
    }

    /**
     * 发送文字消息
     *
     * @param msg 消息内容
     */
    public void sendMessage(String msg) {
        RCChatroomBarrage barrage = new RCChatroomBarrage();
        barrage.setContent(msg);
        barrage.setUserId(AccountStore.INSTANCE.getUserId());
        barrage.setUserName(AccountStore.INSTANCE.getUserName());
        getShield(new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (result.ok()) {
                    List<Shield> shields = result.getList(Shield.class);
                    boolean isContains = false;
                    if (shields != null) {
                        for (Shield shield : shields) {
                            if (msg.contains(shield.getName())) {
                                isContains = true;
                                break;
                            }
                        }
                    }
                    if (isContains) {
                        mView.addToMessageList(barrage, false);
                        mView.clearInput();
                    } else {
                        sendMessage(barrage);
                    }
                } else {
                    sendMessage(barrage);
                }
            }

            @Override
            public void onError(int code, String msg) {
                super.onError(code, msg);
                sendMessage(barrage);
            }
        });

    }

    /**
     * 设置弹框数据
     */
    public void showSettingDialog() {
        List<MutableLiveData<IFun.BaseFun>> funList = Arrays.asList(
                new MutableLiveData<>(new RoomLockFun(mVoiceRoomBean.isPrivate() ? 1 : 0)),
                new MutableLiveData<>(new RoomNameFun(0)),
                new MutableLiveData<>(new RoomNoticeFun(0)),
                new MutableLiveData<>(new RoomBackgroundFun(0)),
                new MutableLiveData<>(new RoomShieldFun(0)),
                new MutableLiveData<>(new RoomMusicFun(0)),
                new MutableLiveData<>(new RoomPauseFun(0))
        );
        mView.showSettingDialog(funList);
    }

    /**
     * 发送消息
     *
     * @param messageContent
     */
    private void sendMessage(MessageContent messageContent) {
        RCMessager.getInstance().sendChatRoomMessage(mVoiceRoomBean.getRoomId(), messageContent, new SendMessageCallback() {
            @Override
            public void onAttached(Message message) {
            }

            @Override
            public void onSuccess(Message message) {
                // 自己进入的消息不显示到弹幕列表
                if (messageContent instanceof RCChatroomEnter && isSelf(((RCChatroomEnter) messageContent).getUserId())) {
                    return;
                }
                // 自己发消息成功后清除输入框内容
                if (messageContent instanceof RCChatroomBarrage && isSelf(((RCChatroomBarrage) messageContent).getUserId())) {
                    mView.clearInput();
                }
                mView.addToMessageList(messageContent, false);
            }

            @Override
            public void onError(Message message, int code, String reason) {
                mView.showToast("发送失败");
            }
        });
    }

    private boolean isSelf(String userId) {
        return TextUtils.equals(userId, AccountStore.INSTANCE.getUserId());
    }

    /**
     * 房主关闭房间
     */
    public void closeRoom() {
        mView.showLoading("正在关闭房间");
        // 房主关闭房间，调用删除房间接口
        OkApi.get(VRApi.deleteRoom(mVoiceRoomBean.getRoomId()), null, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (result.ok()) {
                    leaveRoom();
                } else {
                    mView.dismissLoading();
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

    /**
     * 设置屏蔽词
     *
     * @param shields
     */
    public void setShield(List<String> shields) {
        Logger.e(shields.toString());
    }

    /**
     * 获取屏蔽词
     */
    private void getShield(WrapperCallBack wrapperCallBack) {
        OkApi.get(VRApi.getShield(mRoomId), null, wrapperCallBack);
    }

    /**
     * 暂停直播
     */
    public void pauseRadioLive() {
        RCRadioRoomEngine.getInstance().leaveSeat(new RCRadioRoomCallback() {
            @Override
            public void onSuccess() {
                mView.setSeatState(RoomSeatView.SeatState.OWNER_PAUSE);
                // 发暂停通知
                RCRadioRoomEngine.getInstance().notifyRadioRoom(IRCRadioRoomEngine.NotifyType.RC_SUSPEND, "1", null);
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    /**
     * 调用离开房间
     */
    public void leaveRoom() {
        RCRadioRoomEngine.getInstance().leaveRoom(new RCRadioRoomCallback() {

            @Override
            public void onSuccess() {
                Logger.e("==============leaveRoom onSuccess");
                mView.dismissLoading();
                mView.finish();
            }

            @Override
            public void onError(int code, String message) {
                Logger.e("==============leaveRoom onError");
                mView.dismissLoading();
                mView.showToast(message);
            }
        });
    }

    /**
     * 设置房间密码
     *
     * @param isPrivate
     * @param password
     * @param item
     */
    public void setRoomPassword(boolean isPrivate, String password, MutableLiveData<IFun.BaseFun> item) {
        int p = isPrivate ? 1 : 0;
        OkApi.put(VRApi.ROOM_PASSWORD,
                new OkParams()
                        .add("roomId", mRoomId)
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

    /**
     * 修改房间名称
     *
     * @param name
     */
    public void setRoomName(String name) {
        OkApi.put(VRApi.ROOM_NAME,
                new OkParams()
                        .add("roomId", mRoomId)
                        .add("name", name)
                        .build(),
                new WrapperCallBack() {
                    @Override
                    public void onResult(Wrapper result) {
                        if (result.ok()) {
                            mView.showToast("修改成功");
                            mView.setRadioName(name);
                            mVoiceRoomBean.setRoomName(name);
                            RCRadioRoomEngine.getInstance().notifyRadioRoom(IRCRadioRoomEngine.NotifyType.RC_ROOM_NAME, name, null);
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

    @Override
    public void clickKickRoom(User user, ClickCallback<Boolean> callback) {
        if (mVoiceRoomBean == null) {
            return;
        }
        RCRadioRoomEngine.getInstance().kickUserFromRoom(user.getUserId(), new RCRadioRoomCallback() {
            @Override
            public void onSuccess() {
                MemberCache.getInstance().removeMember(user);
                RCChatroomKickOut kickOut = new RCChatroomKickOut();
                kickOut.setUserId(AccountStore.INSTANCE.getUserId());
                kickOut.setUserName(AccountStore.INSTANCE.getUserName());
                kickOut.setTargetId(user.getUserId());
                kickOut.setTargetName(user.getUserName());
                sendMessage(kickOut);
                callback.onResult(true, "");
            }

            @Override
            public void onError(int i, String s) {
                mView.showToast(s);
            }
        });
    }

    @Override
    public void clickSettingAdmin(User user, ClickCallback<Boolean> callback) {
        if (mVoiceRoomBean == null) {
            return;
        }
        boolean isAdmin = !MemberCache.getInstance().isAdmin(user.getUserId());
        HashMap<String, Object> params = new OkParams()
                .add("roomId", mRoomId)
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
                    mView.showToast(result.getMessage());
                    callback.onResult(true, result.getMessage());
                }
            }
        });
    }

    @Override
    public void clickSendGift(User user) {

    }

    @Override
    public void onSpeakingStateChanged(boolean b) {
        mView.setSpeaking(b);
        Logger.e("==============onSpeakingStateChanged: " + b);
    }

    @Override
    public void onMessageReceived(Message message) {
        MessageContent content = message.getContent();
        Logger.e("==============onMessageReceived: " + content.toString());
        // 显示到弹幕列表
        mView.addToMessageList(content, false);
        if (content instanceof RCChatroomGift || content instanceof RCChatroomGiftAll) {

        } else if (content instanceof RCChatroomAdmin) {
            // 刷新房间管理列表
            MemberCache.getInstance().refreshAdminData(mRoomId);
        } else if (content instanceof RCChatroomKickOut) {
            // 如果踢出的是自己，就离开房间
            String targetId = ((RCChatroomKickOut) content).getTargetId();
            if (TextUtils.equals(targetId, AccountStore.INSTANCE.getUserId())) {
                leaveRoom();
                mView.showToast("你已被踢出房间");
            }
        }
    }

    @Override
    public void onAudienceEnter(String s) {
        Logger.e("==============onAudienceEnter: " + s);
        refreshRoomMemberCount();
    }

    @Override
    public void onAudienceLeave(String s) {
        Logger.e("==============onAudienceLeave: " + s);
        refreshRoomMemberCount();
    }

    @Override
    public void onNetworkStatus(int i) {

    }

    @Override
    public void onRadioPause() {
        Logger.e("==============onRadioPause");
    }

    @Override
    public void onRadioResume() {
        Logger.e("==============onRadioResume");
    }

    @Override
    public void onRadioName(String s) {
        mView.setRadioName(s);
    }

    @Override
    public void onRadioRoomNotify(IRCRadioRoomEngine.NotifyType notifyType, String s) {
        Logger.e("===============" + notifyType.getValue() + "=====" + s);
        switch (notifyType) {
            case RC_NOTICE:
                sendNoticeModifyMessage();
                break;
            case RC_SPEAKING:
                boolean isSpeaking = TextUtils.equals(s, "1");
                onSpeakingStateChanged(isSpeaking);
                break;
            case RC_SUSPEND:
                boolean isPause = TextUtils.equals(s, "1");
                if (isPause) {
                    mView.setSeatState(RoomSeatView.SeatState.VIEWER_PAUSE);
                } else {
                    mView.setSeatState(RoomSeatView.SeatState.NORMAL);
                }
                break;
            case RC_BGNAME:
                mView.setRoomBackground(s);
                break;
        }
    }

    /**
     * 房主踢人成功的回调
     *
     * @param s
     * @param s1
     */
    @Override
    public void onUserReceiveKickOutRoom(String s, String s1) {
        Logger.e("==============onUserReceiveKickOutRoom" + s + " " + s1);

    }

    /**
     * 设置里面的设置选项
     *
     * @param item
     * @param position
     */
    @Override
    public void clickItem(MutableLiveData<IFun.BaseFun> item, int position) {
        IFun.BaseFun fun = item.getValue();
        if (fun instanceof RoomPauseFun) {
            pauseRadioLive();
        } else if (fun instanceof RoomNoticeFun) {
            getNotice(true);
        } else if (fun instanceof RoomLockFun) {
            if (fun.getStatus() == 1) {
                setRoomPassword(false, "", item);
            } else {
                mView.showSetPasswordDialog(item);
            }
        } else if (fun instanceof RoomNameFun) {
            mView.showSetRoomNameDialog(mVoiceRoomBean.getRoomName());
        } else if (fun instanceof RoomBackgroundFun) {
            mView.showSelectBackgroundDialog(mVoiceRoomBean.getBackgroundUrl());
        } else if (fun instanceof RoomShieldFun) {
            mView.showShieldDialog(mRoomId);
        }
    }

    @Override
    public void selectBackground(String url) {
        OkApi.put(VRApi.ROOM_BACKGROUND, new OkParams()
                .add("roomId", mRoomId)
                .add("backgroundUrl", url)
                .build(), new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (result.ok()) {
                    mVoiceRoomBean.setBackgroundUrl(url);
                    mView.setRoomBackground(url);
                    RCRadioRoomEngine.getInstance().notifyRadioRoom(IRCRadioRoomEngine.NotifyType.RC_BGNAME, url, null);
                    mView.showToast("设置成功");
                } else {
                    mView.showToast("设置失败");
                }
            }

            @Override
            public void onError(int code, String msg) {
                super.onError(code, msg);
                mView.showToast("设置失败");
            }
        });
    }
}
