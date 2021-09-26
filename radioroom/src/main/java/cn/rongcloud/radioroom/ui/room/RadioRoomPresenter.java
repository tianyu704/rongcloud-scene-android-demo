package cn.rongcloud.radioroom.ui.room;

import androidx.lifecycle.Lifecycle;

import com.basis.mvp.BasePresenter;
import io.rong.imlib.model.MessageContent;

/**
 * @author gyn
 * @date 2021/9/24
 */
public class RadioRoomPresenter extends BasePresenter<RadioRoomView> {
    private VoiceRoomBean mVoiceRoomBean;
    public RadioRoomPresenter(RadioRoomView mView, Lifecycle lifecycle) {
        super(mView, lifecycle);
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void joinRoom(VoiceRoomBean voiceRoomBean) {
        this.mVoiceRoomBean = voiceRoomBean;
    }

    public void sendMessage(String msg) {
        RCChatroomBarrage barrage = new RCChatroomBarrage();
        barrage.setContent(msg);
        barrage.setUserId(AccountStore.INSTANCE.getUserId());
        barrage.setUserName(AccountStore.INSTANCE.getUserName());
        sendMessage(barrage);
    }

    private void sendMessage(MessageContent message) {
        RCMessager.getInstance().sendChatRoomMessage(mVoiceRoomBean.getRoomId(), message, new SendMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }



    public void setMessage(MessageContent message) {

    }
}
