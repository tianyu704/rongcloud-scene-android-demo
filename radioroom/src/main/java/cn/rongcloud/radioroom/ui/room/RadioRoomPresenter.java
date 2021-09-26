package cn.rongcloud.radioroom.ui.room;

import androidx.lifecycle.Lifecycle;

import com.basis.mvp.BasePresenter;
import io.rong.imlib.model.MessageContent;

/**
 * @author gyn
 * @date 2021/9/24
 */
public class RadioRoomPresenter extends BasePresenter<RadioRoomView> {

    public RadioRoomPresenter(RadioRoomView mView, Lifecycle lifecycle) {
        super(mView, lifecycle);
    }

    public void setMessage(MessageContent message) {

    }
}
