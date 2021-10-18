package cn.rongcloud.voiceroom.room;

import android.graphics.Point;

import androidx.lifecycle.MutableLiveData;

import com.basis.mvp.IBaseView;

import java.util.List;

import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.ui.room.fragment.roomsetting.IFun;
import cn.rong.combusis.ui.room.model.Member;
import cn.rongcloud.voiceroom.ui.uimodel.UiRoomModel;
import cn.rongcloud.voiceroom.ui.uimodel.UiSeatModel;
import io.rong.imlib.model.MessageContent;

public interface IVoiceRoomFragmentView extends IBaseView {

    void setRoomData(VoiceRoomBean voiceRoomBean);

    void onJoinRoomSuccess();

    void initRoleView(UiRoomModel roomInfo);


    void leaveRoomSuccess();

    void onJoinNextRoom(boolean start);

    void enterSeatSuccess();


    void refreshOnlineUsersNumber(int onlineUsersNumber);

    /**
     * 通知指定坐席信息发生了改变，刷新之
     */
    void onSeatInfoChange(int index,UiSeatModel uiSeatModel);

    void onSeatListChange(List<UiSeatModel> uiSeatModelList);

    void sendTextMessageSuccess(String message);

    void showChatRoomMessage(MessageContent messageContent);

    void showPickReceived(boolean isCreateReceive,String userId);

    void switchToAdminRole(boolean isAdmin,UiRoomModel roomInfo);

    void changeStatus(int status);

    void showUnReadRequestNumber(int number);

    void showUnreadMessage(int count);

    void showFov(Point from);

    void showRevokeSeatRequest();

    void showRoomClose();

    void onMemberInfoChange();

    void onNetworkStatus(int i);

    void finish();

    void onSpeakingStateChanged(boolean speaking);

    void refreshRoomOwner(UiSeatModel uiSeatModel);

    void refreshSeatIndex(int index,UiSeatModel uiSeatModel);

    void showNotice(String notice, boolean isModify);

    void clearInput();

    void hideSoftKeyboardAndIntput();

    void showMessage(MessageContent messageContent, boolean isRefresh);

    void showSettingDialog(List<MutableLiveData<IFun.BaseFun>> funList);

    void showSetPasswordDialog(MutableLiveData<IFun.BaseFun> item);

    void showSetRoomNameDialog(String name);

    void setVoiceName(String name);

    void showShieldDialog(String roomId);

    void showSelectBackgroundDialog(String url);

    void showNoticeDialog();

    void setRoomBackground(String url);

    void refreshSeat();

    void showSendGiftDialog(String roomId, String createUserId, String selectUserId, List<Member> members);

    void showUserSetting(Member member);

    void showMusicDialog();
}
