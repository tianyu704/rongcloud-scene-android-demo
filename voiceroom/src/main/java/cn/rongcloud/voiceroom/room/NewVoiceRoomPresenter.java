package cn.rongcloud.voiceroom.room;

import androidx.lifecycle.Lifecycle;

import com.basis.mvp.BasePresenter;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.model.Message;

public class NewVoiceRoomPresenter extends BasePresenter<IVoiceRoomFragmentView> implements IRongCoreListener.OnReceiveMessageListener{


    public NewVoiceRoomPresenter(IVoiceRoomFragmentView mView, Lifecycle lifecycle) {
        super(mView, lifecycle);
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
        super.onDestroy();
    }

    /**
     * 消息接收
     * @param message
     * @param i
     * @return
     */
    @Override
    public boolean onReceived(Message message, int i) {
        return false;
    }
}
