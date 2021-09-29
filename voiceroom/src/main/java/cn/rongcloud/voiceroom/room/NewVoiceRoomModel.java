package cn.rongcloud.voiceroom.room;


import static cn.rong.combusis.sdk.Api.EVENT_KICK_OUT_OF_SEAT;
import static cn.rong.combusis.sdk.Api.EVENT_REQUEST_SEAT_AGREE;
import static cn.rong.combusis.sdk.Api.EVENT_REQUEST_SEAT_REFUSE;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import com.basis.mvp.BaseModel;
import com.rongcloud.common.utils.AccountStore;
import com.rongcloud.common.utils.AudioManagerUtil;


import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.ui.room.model.MemberCache;
import cn.rongcloud.rtc.api.RCRTCAudioMixer;
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomCallback;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomEventListener;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomResultCallback;
import cn.rongcloud.voiceroom.model.RCPKInfo;
import cn.rongcloud.voiceroom.model.RCVoiceRoomInfo;
import cn.rongcloud.voiceroom.model.RCVoiceSeatInfo;
import cn.rongcloud.voiceroom.net.VoiceRoomNetManager;
import cn.rongcloud.voiceroom.net.bean.respond.VoiceRoomInfoBean;
import cn.rongcloud.voiceroom.ui.uimodel.UiMemberModel;
import cn.rongcloud.voiceroom.ui.uimodel.UiRoomModel;
import cn.rongcloud.voiceroom.ui.uimodel.UiSeatModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableEmitter;
import io.reactivex.rxjava3.core.CompletableOnSubscribe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.rong.imlib.model.Message;

/**
 * 语聊房的逻辑处理
 */
public class NewVoiceRoomModel extends BaseModel<NewVoiceRoomPresenter> implements RCVoiceRoomEventListener {


    //线程调度器
    Scheduler dataModifyScheduler = Schedulers.computation();

    //麦位信息变化监听器
    private BehaviorSubject<UiSeatModel> seatInfoChangeSubject = BehaviorSubject.create();

    //座位数量订阅器,为了让所有订阅的地方都能回调回去
    private BehaviorSubject<List<UiSeatModel>> seatListChangeSubject = BehaviorSubject.create();

    //房间信息发生改变的额订阅
    private BehaviorSubject<UiRoomModel> roomInfoSubject = BehaviorSubject.create();

    //房间事件监听（麦位 进入 踢出等等）
    private BehaviorSubject<Pair<String, ArrayList<String>>> roomEventSubject = BehaviorSubject.create();
    /**
     * 用户信息监听
     */
    private BehaviorSubject<UiMemberModel> memberInfoChangeSubject = BehaviorSubject.create();

    /**
     * 申请和撤销上麦下麦的监听
     */
    private BehaviorSubject<List<User>> obRequestSeatListChangeSuject=BehaviorSubject.create();
    /**
     * 可以被邀请的人员监听
     */
    private BehaviorSubject<List<User>> obInviteSeatListChangeSuject=BehaviorSubject.create();

    public UiRoomModel currentUIRoomInfo = new UiRoomModel(roomInfoSubject);

    //本地麦克风的状态，默认是开启的
    private boolean recordingStatus = true;
    //在麦位
    private ArrayList<UiSeatModel> uiSeatModels= new ArrayList<>();
    //申请连麦的集合
    private ArrayList<User> requestSeats= new ArrayList<>();;
    //可以被邀请的集合
    private ArrayList<User> inviteSeats=new ArrayList<>();

    public ArrayList<User> getInviteSeats() {
        return inviteSeats;
    }

    public ArrayList<User> getRequestSeats() {
        return requestSeats;
    }

    public boolean isRecordingStatus() {
        return recordingStatus;
    }

    public void setRecordingStatus(boolean recordingStatus) {
        this.recordingStatus = recordingStatus;
    }

    /**
     * 监听麦位数量变化
     */
    public Observable<List<UiSeatModel>> obSeatListChange() {
        return seatListChangeSubject.subscribeOn(dataModifyScheduler)
                .observeOn(AndroidSchedulers.mainThread());
    }
    /**
     * 监控指定位置的麦位的信息变化
     */
    public Observable<UiSeatModel> obSeatInfoByIndex(int index){
        return seatListChangeSubject.map(new Function<List<UiSeatModel>, UiSeatModel>() {
            @Override
            public UiSeatModel apply(List<UiSeatModel> uiSeatModels) throws Throwable {
                return uiSeatModels.get(index);
            }
        }).subscribeOn(dataModifyScheduler)
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 监听房间的事件
     */
    public Observable<Pair<String, ArrayList<String>>> obRoomEventChange() {
        return roomEventSubject.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(dataModifyScheduler);
    }

    /**
     * 监听申请上麦和撤销申请的监听
     *
     */
    public Observable<List<User>> obRequestSeatListChange(){
        return obRequestSeatListChangeSuject.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(dataModifyScheduler);
    }

    /**
     * 监听可以被邀请的人员
     */
    public Observable<List<User>> obInviteSeatListChange(){
        return obInviteSeatListChangeSuject.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(dataModifyScheduler);
    }


    public NewVoiceRoomModel(NewVoiceRoomPresenter present, Lifecycle lifecycle) {
        super(present, lifecycle);
    }

    @Override
    public void onRoomKVReady() {

    }

    @Override
    public void onRoomInfoUpdate(RCVoiceRoomInfo rcVoiceRoomInfo) {

    }

    /**
     * 麦位信息发生了变化
     *
     * @param list
     */
    @Override
    public void onSeatInfoUpdate(List<RCVoiceSeatInfo> list) {
        uiSeatModels.clear();
        for (int i = 0; i < list.size(); i++) {
            //构建一个集合返回去
            UiSeatModel uiSeatModel = new UiSeatModel(i, list.get(i), seatInfoChangeSubject);
            uiSeatModels.add(uiSeatModel);
        }
        seatListChangeSubject.onNext(uiSeatModels);
    }

    /**
     * 用户加入麦位
     * @param i
     * @param s
     */
    @Override
    public void onUserEnterSeat(int i, String s) {

    }

    /**
     * 用户离开麦位
     * @param i
     * @param s
     */
    @Override
    public void onUserLeaveSeat(int i, String s) {

    }

    @Override
    public void onSeatMute(int i, boolean b) {

    }

    /**
     * 锁住当前座位
     *
     * @param i
     * @param b
     */
    @Override
    public void onSeatLock(int i, boolean b) {
        //锁住的位置，和状态
    }

    @Override
    public void onAudienceEnter(String s) {

    }

    @Override
    public void onAudienceExit(String s) {

    }

    @Override
    public void onSpeakingStateChanged(int i, boolean b) {
        Log.e("TAG", "onSpeakingStateChanged: ");
    }

    @Override
    public void onMessageReceived(Message message) {

    }

    @Override
    public void onRoomNotificationReceived(String s, String s1) {

    }

    /**
     *
     * @param s
     */
    @Override
    public void onPickSeatReceivedFrom(String s) {
        Log.e("TAG", "onPickSeatReceivedFrom: ");
    }

    /**
     * 被抱下麦
     *
     * @param i
     */
    @Override
    public void onKickSeatReceived(int i) {
        roomEventSubject.onNext(new Pair(EVENT_KICK_OUT_OF_SEAT, new ArrayList<>()));
        AudioManagerUtil.INSTANCE.choiceAudioModel();
    }

    /**
     * 请求加入麦位被允许
     */
    @Override
    public void onRequestSeatAccepted() {
        roomEventSubject.onNext(new Pair(EVENT_REQUEST_SEAT_AGREE, new ArrayList<>()));
    }

    /**
     * 请求加入麦位被拒绝
     */
    @Override
    public void onRequestSeatRejected() {
        roomEventSubject.onNext(new Pair(EVENT_REQUEST_SEAT_REFUSE, new ArrayList<>()));
    }

    /**
     * 接收请求麦位
     */
    @Override
    public void onRequestSeatListChanged() {
        getRequestSeatUserIds();
    }

    /**
     * 获取到正在申请麦位的用户的信息
     */
    public void getRequestSeatUserIds(){
        RCVoiceRoomEngine.getInstance().getRequestSeatUserIds(new RCVoiceRoomResultCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> strings) {
                //获取到当前房间所有用户
                List<User> users = MemberCache.getInstance().getMemberList().getValue();
                requestSeats.clear();
                for (User user : users) {
                    //判断申请的用户是否在麦位里面,如果在，说明是撤销，如果不是，那么
                    //如果当前用户不在麦位上,申请连麦的
                    for (String userid : strings) {
                        if (userid.equals(user.getUserId())){
                            requestSeats.add(user);
                        }
                    }
                }
                obRequestSeatListChangeSuject.onNext(requestSeats);
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    /**
     * 收到邀请
     * @param s
     * @param s1
     * @param s2
     */
    @Override
    public void onInvitationReceived(String s, String s1, String s2) {
        Log.e("TAG", "onInvitationReceived: " );
    }

    /**
     * 同意邀请
     * @param s
     */
    @Override
    public void onInvitationAccepted(String s) {
        Log.e("TAG", "onInvitationAccepted: " );
    }

    /**
     * 拒绝邀请
     * @param s
     */
    @Override
    public void onInvitationRejected(String s) {
        Log.e("TAG", "onInvitationRejected: " );
    }

    /**
     * 取消邀请
     * @param s
     */
    @Override
    public void onInvitationCancelled(String s) {
        Log.e("TAG", "onInvitationCancelled: " );
    }

    /**
     * 用户收到被踢出房间
     * @param s
     * @param s1
     */
    @Override
    public void onUserReceiveKickOutRoom(String s, String s1) {

    }

    /**
     * 网络信号监听
     *
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
    public Single<VoiceRoomBean> getRoomInfo(String roomId) {

        return Single.create(new SingleOnSubscribe<VoiceRoomBean>() {
            @Override
            public void subscribe(@io.reactivex.rxjava3.annotations.NonNull SingleEmitter<VoiceRoomBean> emitter) throws Throwable {
                VoiceRoomBean voiceRoomBean = present.getmVoiceRoomBean();
                if (present.getmVoiceRoomBean() != null) {
                    emitter.onSuccess(voiceRoomBean);
                } else {
                    //通过网络去获取
                    queryRoomInfoFromServer(roomId);
                }
            }
        });
    }

    /**
     * 通过网络去获取最新的房间信息
     *
     * @param roomId
     * @return
     */
    public Single<VoiceRoomInfoBean> queryRoomInfoFromServer(String roomId) {
        return VoiceRoomNetManager.INSTANCE.getARoomApi()
                .getVoiceRoomInfo(roomId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(new Consumer<VoiceRoomInfoBean>() {
                    @Override
                    public void accept(VoiceRoomInfoBean voiceRoomInfoBean) throws Throwable {
                       //房间信息

                    }
                });
    }

    private RCRTCAudioMixer.MixingState currentMusicState = RCRTCAudioMixer.MixingState.STOPPED;

    /**
     * 麦位断开链接
     *
     * @param userId
     * @return
     */
    public Completable leaveSeat(String userId) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@io.reactivex.rxjava3.annotations.NonNull CompletableEmitter emitter) throws Throwable {
                UiSeatModel uiSeatModel = getSeatInfoByUserId(userId);
                RCVoiceRoomEngine.getInstance().leaveSeat(new RCVoiceRoomCallback() {
                    @Override
                    public void onSuccess() {
                        if (currentMusicState == RCRTCAudioMixer.MixingState.PLAY
                                || currentMusicState == RCRTCAudioMixer.MixingState.PAUSED
                        ) {
                            stopPlayMusic();
                        }
                        AudioManagerUtil.INSTANCE.choiceAudioModel();
                        emitter.onComplete();
                    }

                    @Override
                    public void onError(int i, String s) {

                    }
                });
            }
        }).subscribeOn(dataModifyScheduler);
    }

    /**
     * 停止音乐播放，临时放这里
     */
    public void stopPlayMusic() {
//        try {
//            musicStopFlag = true;
//            if (playNextMusicJob?.isActive == true) {
//                playNextMusicJob?.cancel()
//            }
//            RCRTCAudioMixer.getInstance().stop()
//        } catch (e: Exception) {
//            Log.e(TAG, "stopPlayMusic: ", e)
//        }
    }

    /**
     * 根据ID获取当前的麦位信息
     *
     * @param userId
     * @return
     */
    public UiSeatModel getSeatInfoByUserId(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        for (UiSeatModel uiSeatModel : uiSeatModels) {
            if (!TextUtils.isEmpty(uiSeatModel.getUserId())&&uiSeatModel.getUserId().equals(userId)) {
                return uiSeatModel;
            }
        }
        return null;
    }

    /**
     * 当房间人员变化的时候监听
     * @param users
     */
    public void onMemberListener(List<User> users) {
        //只要不在麦位的人都可以被邀请
        inviteSeats.clear();
        for (User user : users) {
            boolean isInSeat=false;
            for (UiSeatModel uiSeatModel : uiSeatModels) {
                if ((!TextUtils.isEmpty(uiSeatModel.getUserId())&&uiSeatModel.getUserId().equals(user.getUserId()))
                        ||user.getUserId().equals(AccountStore.INSTANCE.getUserId())){
                    isInSeat=true;
                    break;
                }
            }
            if (!isInSeat){
                inviteSeats.add(user);
            }
        }
        obInviteSeatListChangeSuject.onNext(inviteSeats);
    }
}
