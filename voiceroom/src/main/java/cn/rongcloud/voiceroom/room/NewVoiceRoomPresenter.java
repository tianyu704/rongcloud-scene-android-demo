package cn.rongcloud.voiceroom.room;

import androidx.lifecycle.Lifecycle;

import com.basis.mvp.BasePresenter;

import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.model.Message;

/**
 * 语聊房present
 */
public class NewVoiceRoomPresenter extends BasePresenter<IVoiceRoomFragmentView> implements
        IRongCoreListener.OnReceiveMessageListener, IVoiceRoomPresent {

    /**
     * 语聊房model
     */
    private NewVoiceRoomModel newVoiceRoomModel;

    private VoiceRoomBean mVoiceRoomBean;

    public NewVoiceRoomPresenter(IVoiceRoomFragmentView mView, Lifecycle lifecycle) {
        super(mView, lifecycle);
        newVoiceRoomModel = new NewVoiceRoomModel(this, lifecycle);

        //界面初始化成功的时候，要去请求网络
//        newVoiceRoomModel.getRoomInfo(getmVoiceRoomBean().getRoomId());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        RCVoiceRoomEngine.getInstance().setVoiceRoomEventListener(null);
        RCVoiceRoomEngine.getInstance().removeMessageReceiveListener(this);
        super.onDestroy();
    }

    /**
     * 消息接收
     *
     * @param message
     * @param i
     * @return
     */
    @Override
    public boolean onReceived(Message message, int i) {
        return false;
    }

    /**
     * 设置当前的voiceBean
     *
     * @param mVoiceRoomBean
     */
    @Override
    public void setCurrentRoom(VoiceRoomBean mVoiceRoomBean) {
        this.mVoiceRoomBean = mVoiceRoomBean;
        //设置界面监听
        RCVoiceRoomEngine.getInstance().setVoiceRoomEventListener(newVoiceRoomModel);
        RCVoiceRoomEngine.getInstance().addMessageReceiveListener(this);
    }

    @Override
    public VoiceRoomBean getmVoiceRoomBean() {
        return mVoiceRoomBean;
    }

    @Override
    public void onNetworkStatus(int i) {
        mView.onNetworkStatus(i);
    }
}
