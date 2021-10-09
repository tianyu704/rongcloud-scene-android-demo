package cn.rongcloud.radioroom.ui.room;

import androidx.lifecycle.MutableLiveData;

import com.basis.mvp.IBaseView;

import java.util.List;

import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.ui.room.fragment.roomsetting.IFun;
import cn.rong.combusis.ui.room.widget.RoomSeatView.SeatState;
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

    void showNotice(String notice, boolean isModify);

    void setSeatState(SeatState seatState);

    void showSettingDialog(List<MutableLiveData<IFun.BaseFun>> funList);

    void showSetPasswordDialog(MutableLiveData<IFun.BaseFun> item);

    void showSetRoomNameDialog(String name);

    void showSelectBackgroundDialog(String url);

    void setRoomBackground(String url);

    void showShieldDialog(String roomId);
}
