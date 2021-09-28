package cn.rongcloud.radioroom.ui.room;

import android.text.TextUtils;

import androidx.lifecycle.LifecycleOwner;

import com.basis.mvp.BasePresenter;
import com.basis.net.oklib.OkApi;
import com.basis.net.oklib.OkParams;
import com.basis.net.oklib.WrapperCallBack;
import com.basis.net.oklib.wrapper.Wrapper;
import com.kit.utils.Logger;
import com.rongcloud.common.utils.AccountStore;

import java.util.HashMap;

import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.message.RCChatroomAdmin;
import cn.rong.combusis.message.RCChatroomBarrage;
import cn.rong.combusis.message.RCChatroomEnter;
import cn.rong.combusis.message.RCChatroomGift;
import cn.rong.combusis.message.RCChatroomGiftAll;
import cn.rong.combusis.message.RCChatroomLocationMessage;
import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.sdk.event.EventHelper;
import cn.rong.combusis.ui.room.fragment.ClickCallback;
import cn.rong.combusis.ui.room.model.MemberCache;
import cn.rongcloud.messager.RCMessager;
import cn.rongcloud.messager.SendMessageCallback;
import cn.rongcloud.radioroom.IRCRadioRoomEngine;
import cn.rongcloud.radioroom.RCRadioRoomEngine;
import cn.rongcloud.radioroom.callback.RCRadioRoomCallback;
import cn.rongcloud.radioroom.rroom.RCRadioEventListener;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

/**
 * @author gyn
 * @date 2021/9/24
 */
public class RadioRoomPresenter extends BasePresenter<RadioRoomView> implements RCRadioEventListener, RadioRoomMemberSettingClickListener {
    private VoiceRoomBean mVoiceRoomBean;
    private String mRoomId = "";
    private LifecycleOwner lifecycleOwner;

    public RadioRoomPresenter(RadioRoomView mView, LifecycleOwner lifecycleOwner) {
        super(mView, lifecycleOwner.getLifecycle());
        this.lifecycleOwner = lifecycleOwner;
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
            // 发送默认消息
            sendDefaultMessage();
            // 获取房间内成员和管理员列表
            MemberCache.getInstance().fetchData(mRoomId);
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
     * 房主上麦
     */
    public void enterSeat() {
        RCRadioRoomEngine.getInstance().enterSeat(new RCRadioRoomCallback() {
            @Override
            public void onSuccess() {
                Logger.e("==============enterSeat onSuccess");
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
        EventHelper.helper().getOnLineUserIds(mVoiceRoomBean.getRoomId(), strings -> {
            mView.setOnlineCount(strings.size());
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
            Logger.e("=================发送了默认消息");
            // 发送进入房间的消息
            RCChatroomEnter enter = new RCChatroomEnter();
            enter.setUserId(AccountStore.INSTANCE.getUserId());
            enter.setUserName(AccountStore.INSTANCE.getUserName());
            sendMessage(enter);
        }
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
        sendMessage(barrage);
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

    @Override
    public void clickKickRoom(User user, ClickCallback<Boolean> callback) {
        if (mVoiceRoomBean == null) {
            return;
        }

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

    }

    @Override
    public void onMessageReceived(Message message) {
        Logger.e("==============onMessageReceived: " + message.toString());
        MessageContent content = message.getContent();
        mView.addToMessageList(content, false);
        if (content instanceof RCChatroomGift || content instanceof RCChatroomGiftAll) {

        } else if (content instanceof RCChatroomAdmin) {
            // 管理变更
            // 显示到弹幕列表
            mView.addToMessageList(content, false);
            // 刷新房间管理列表
            MemberCache.getInstance().refreshAdminData(mRoomId);
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
    }
}
