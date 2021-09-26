package cn.rongcloud.voiceroom.room;



import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import com.basis.mvp.BaseModel;


import java.util.List;

import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomEventListener;
import cn.rongcloud.voiceroom.model.RCPKInfo;
import cn.rongcloud.voiceroom.model.RCVoiceRoomInfo;
import cn.rongcloud.voiceroom.model.RCVoiceSeatInfo;
import cn.rongcloud.voiceroom.net.VoiceRoomNetManager;
import cn.rongcloud.voiceroom.net.bean.respond.VoiceRoomInfoBean;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.rong.imlib.model.Message;

/**
 * 语聊房的逻辑处理
 */
public class NewVoiceRoomModel extends BaseModel<NewVoiceRoomPresenter> implements RCVoiceRoomEventListener {


    public NewVoiceRoomModel(NewVoiceRoomPresenter present, Lifecycle lifecycle) {
        super(present, lifecycle);
    }

    @Override
    public void onRoomKVReady() {

    }

    @Override
    public void onRoomInfoUpdate(RCVoiceRoomInfo rcVoiceRoomInfo) {

    }

    @Override
    public void onSeatInfoUpdate(List<RCVoiceSeatInfo> list) {

    }

    @Override
    public void onUserEnterSeat(int i, String s) {

    }

    @Override
    public void onUserLeaveSeat(int i, String s) {

    }

    @Override
    public void onSeatMute(int i, boolean b) {

    }

    @Override
    public void onSeatLock(int i, boolean b) {

    }

    @Override
    public void onAudienceEnter(String s) {

    }

    @Override
    public void onAudienceExit(String s) {

    }

    @Override
    public void onSpeakingStateChanged(int i, boolean b) {

    }

    @Override
    public void onMessageReceived(Message message) {

    }

    @Override
    public void onRoomNotificationReceived(String s, String s1) {

    }

    @Override
    public void onPickSeatReceivedFrom(String s) {

    }

    @Override
    public void onKickSeatReceived(int i) {

    }

    @Override
    public void onRequestSeatAccepted() {

    }

    @Override
    public void onRequestSeatRejected() {

    }

    @Override
    public void onRequestSeatListChanged() {

    }

    @Override
    public void onInvitationReceived(String s, String s1, String s2) {

    }

    @Override
    public void onInvitationAccepted(String s) {

    }

    @Override
    public void onInvitationRejected(String s) {

    }

    @Override
    public void onInvitationCancelled(String s) {

    }

    @Override
    public void onUserReceiveKickOutRoom(String s, String s1) {

    }

    /**
     * 网络信号监听
     * @param i
     */
    @Override
    public void onNetworkStatus(int i) {
        present.onNetworkStatus(i);
        Log.d("TAG", "onNetworkStatus: ");
    }

    @Override
    public void onPKgoing(@NonNull RCPKInfo rcpkInfo) {

    }

    @Override
    public void onPKFinish() {

    }

    @Override
    public void onReveivePKInvitation(String s, String s1) {

    }

    @Override
    public void onPKInvitationCanceled(String s, String s1) {

    }

    @Override
    public void onPKInvitationRejected(String s, String s1) {

    }

    @Override
    public void onPKInvitationIgnored(String s, String s1) {

    }

    /**
     * 获取房间信息
     */
    public Single<VoiceRoomBean> getRoomInfo(String roomId){

        return Single.create(new SingleOnSubscribe<VoiceRoomBean>() {
            @Override
            public void subscribe(@io.reactivex.rxjava3.annotations.NonNull SingleEmitter<VoiceRoomBean> emitter) throws Throwable {
                VoiceRoomBean voiceRoomBean = present.getmVoiceRoomBean();
                if (present.getmVoiceRoomBean()!=null) {
                    emitter.onSuccess(voiceRoomBean);
                }else {
                    //通过网络去获取
                    queryRoomInfoFromServer(roomId);
                }
            }
        });
    }

    /**
     * 通过网络去获取最新的房间信息
     * @param roomId
     * @return
     */
    public Single<VoiceRoomInfoBean> queryRoomInfoFromServer(String roomId){
       return VoiceRoomNetManager.INSTANCE.getARoomApi()
                .getVoiceRoomInfo(roomId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(new Consumer<VoiceRoomInfoBean>() {
                    @Override
                    public void accept(VoiceRoomInfoBean voiceRoomInfoBean) throws Throwable {

                    }
                });
    }
}
