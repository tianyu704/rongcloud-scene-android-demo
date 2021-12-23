package cn.rongcloud.voice.room;


import com.basis.mvp.IBasePresent;

import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rongcloud.voice.ui.uimodel.UiSeatModel;

interface IVoiceRoomPresent extends IBasePresent {

    void onNetworkStatus(int i);

    void setCurrentRoom(VoiceRoomBean mVoiceRoomBean);

    VoiceRoomBean getmVoiceRoomBean();

    /**
     * 监听时间
     * @param roomId
     */
    void initListener(String roomId);

    /**
     * 空座位被点击 观众
     */
    void enterSeatViewer(int position);

    /**
     * 空座位被点击 房主
     *
     * @param seatStatus
     * @param
     */
    void enterSeatOwner(UiSeatModel seatStatus);
}
