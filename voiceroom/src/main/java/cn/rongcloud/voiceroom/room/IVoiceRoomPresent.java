package cn.rongcloud.voiceroom.room;



import com.basis.mvp.IBasePresent;

import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;

interface IVoiceRoomPresent extends IBasePresent {

    void onNetworkStatus(int i);

    void setCurrentRoom(VoiceRoomBean mVoiceRoomBean);

    VoiceRoomBean getmVoiceRoomBean();


}
