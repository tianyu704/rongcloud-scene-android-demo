package cn.rongcloud.radioroom.ui.room;

import com.basis.mvp.IBaseView;

import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import io.rong.imlib.model.MessageContent;

/**
 * @author gyn
 * @date 2021/9/24
 */
public interface RadioRoomView extends IBaseView {

    void setRoomData(VoiceRoomBean voiceRoomBean);

    void setOnlineCount(int num);

    void addToMessageList(MessageContent messageContent, boolean isRefresh);

    void clearInput();

    void finish();

    void setSpeaking(boolean speaking);

    void setRadioName(String name);
}
