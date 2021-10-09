package cn.rongcloud.voiceroom.room;

import android.graphics.Point;

import java.util.List;

import com.basis.mvp.IBaseView;
import cn.rongcloud.voiceroom.ui.uimodel.UiRoomModel;
import cn.rongcloud.voiceroom.ui.uimodel.UiSeatModel;
import io.rong.imlib.model.MessageContent;

public interface IVoiceRoomFragmentView extends IBaseView {

    void onJoinRoomSuccess();

    void initRoleView(UiRoomModel roomInfo);


    void leaveRoomSuccess();

    void onJoinNextRoom(boolean start);

    void enterSeatSuccess();

    void packupRoom();

    void refreshOnlineUsersNumber(int onlineUsersNumber);

    /**
     * 依据 RoomInfo 刷新 UI
     */
    void refreshRoomInfo(UiRoomModel roomInfo);

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

    void showMessage(MessageContent messageContent, boolean isRefresh);
}
