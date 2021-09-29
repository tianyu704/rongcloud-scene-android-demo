package cn.rongcloud.voiceroom.room;

import static cn.rong.combusis.sdk.Api.EVENT_AGREE_MANAGE_PICK;
import static cn.rong.combusis.sdk.Api.EVENT_KICK_OUT_OF_SEAT;
import static cn.rong.combusis.sdk.Api.EVENT_REQUEST_SEAT_AGREE;
import static cn.rong.combusis.sdk.Api.EVENT_REQUEST_SEAT_REFUSE;
import static cn.rong.combusis.sdk.event.wrapper.EToast.showToast;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;

import com.basis.mvp.BasePresenter;
import com.basis.net.oklib.OkApi;
import com.basis.net.oklib.OkParams;
import com.basis.net.oklib.WrapperCallBack;
import com.basis.net.oklib.wrapper.Wrapper;
import com.rongcloud.common.utils.AccountStore;
import com.rongcloud.common.utils.AudioManagerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.manager.RCChatRoomMessageManager;
import cn.rong.combusis.message.RCChatroomAdmin;
import cn.rong.combusis.message.RCChatroomKickOut;
import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.sdk.event.wrapper.EToast;
import cn.rong.combusis.ui.room.fragment.ClickCallback;
import cn.rong.combusis.ui.room.fragment.MemberSettingFragment;
import cn.rong.combusis.ui.room.model.MemberCache;
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomCallback;
import cn.rongcloud.voiceroom.model.RCVoiceSeatInfo;
import cn.rongcloud.voiceroom.room.dialogFragment.NewEmptySeatFragment;
import cn.rongcloud.voiceroom.room.dialogFragment.NewSelfSettingFragment;
import cn.rongcloud.voiceroom.room.dialogFragment.seatoperation.SeatOperationViewPagerFragment;
import cn.rongcloud.voiceroom.ui.uimodel.UiSeatModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableEmitter;
import io.reactivex.rxjava3.core.CompletableOnSubscribe;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

/**
 * 语聊房present
 */
public class NewVoiceRoomPresenter extends BasePresenter<IVoiceRoomFragmentView> implements
        IRongCoreListener.OnReceiveMessageListener, IVoiceRoomPresent, MemberSettingFragment.OnMemberSettingClickListener {

    public static final int STATUS_ON_SEAT = 0;
    public static final int STATUS_NOT_ON_SEAT = 1;
    public static final int STATUS_WAIT_FOR_SEAT = 2;
    /**
     * 语聊房model
     */
    private NewVoiceRoomModel newVoiceRoomModel;

    private List<UiSeatModel> currentSeats = new ArrayList<>();

    private VoiceRoomBean mVoiceRoomBean;

    public void setmVoiceRoomBean(VoiceRoomBean mVoiceRoomBean) {
        this.mVoiceRoomBean = mVoiceRoomBean;
    }

    public int currentStatus = STATUS_NOT_ON_SEAT;

    public NewVoiceRoomPresenter(IVoiceRoomFragmentView mView, Lifecycle lifecycle) {
        super(mView, lifecycle);
        newVoiceRoomModel = new NewVoiceRoomModel(this, lifecycle);
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
        RCVoiceRoomEngine.getInstance().setVoiceRoomEventListener(null);
        RCVoiceRoomEngine.getInstance().removeMessageReceiveListener(this);
        super.onDestroy();
    }

    /**
     * 消息接收
     *
     * @param message
     * @param i
     * @return
     */
    @Override
    public boolean onReceived(Message message, int i) {
        if (mVoiceRoomBean!=null&& TextUtils.isEmpty(mVoiceRoomBean.getRoomId())){
            RCChatRoomMessageManager.INSTANCE.onReceiveMessage(mVoiceRoomBean.getRoomId(), message.getContent());
        }
        return true;
    }

    /**
     * 设置当前的voiceBean
     *
     * @param mVoiceRoomBean
     */
    @Override
    public void setCurrentRoom(VoiceRoomBean mVoiceRoomBean) {
        this.mVoiceRoomBean = mVoiceRoomBean;
        //界面初始化成功的时候，要去请求网络
        newVoiceRoomModel.getRoomInfo(getmVoiceRoomBean().getRoomId());
        MemberCache.getInstance().fetchData(mVoiceRoomBean.getRoomId());
        //监听房间里面的人
        MemberCache.getInstance().getMemberList().observe(((NewVoiceRoomFragment) mView).getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                newVoiceRoomModel.onMemberListener(users);
            }
        });
        setObMessageListener();
    }

    @Override
    public VoiceRoomBean getmVoiceRoomBean() {
        return mVoiceRoomBean;
    }


    @Override
    public void initListener() {
        //设置界面监听
        RCVoiceRoomEngine.getInstance().setVoiceRoomEventListener(newVoiceRoomModel);
        RCVoiceRoomEngine.getInstance().addMessageReceiveListener(this);
        setObSeatListChange();
        setObRoomEventChange();
        setRequestSeatListener();
    }

    private void setRequestSeatListener() {
        newVoiceRoomModel.obRequestSeatListChange()
                .subscribe(new Consumer<List<User>>() {
                    @Override
                    public void accept(List<User> users) throws Throwable {
                        //申请的，通知底部弹窗的刷新
                        mView.showUnReadRequestNumber(users.size());
                    }
                });
    }

    /**
     * 设置接收消息的监听（包括自己发送的，以及外部发送过来的）
     */
    private void setObMessageListener() {
        RCChatRoomMessageManager.INSTANCE.obMessageReceiveByRoomId(mVoiceRoomBean.getRoomId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MessageContent>() {
                    @Override
                    public void accept(MessageContent messageContent) throws Throwable {
                        Log.e("TAG", "accept: " + messageContent);
                    }
                });
    }


    /**
     * 空座位被点击
     *
     * @param position
     */
    @Override
    public void enterSeatViewer(int position) {
        //判断是否在麦位上
        if (userInSeat()) {
            //在麦位上
            RCVoiceRoomEngine.getInstance().switchSeatTo(position, new RCVoiceRoomCallback() {
                @Override
                public void onSuccess() {
                    AudioManagerUtil.INSTANCE.choiceAudioModel();
                }

                @Override
                public void onError(int code, String message) {
                    mView.showToast(message);
                }
            });
        } else {
            //不在麦位上
            //如果当前正在等待并且不可以自有上麦的模式
            if (currentStatus == STATUS_WAIT_FOR_SEAT && !newVoiceRoomModel.currentUIRoomInfo.isFreeEnterSeat()) {
                mView.showRevokeSeatRequest();
                return;
            }
            // 自由上麦模式
            if (newVoiceRoomModel.currentUIRoomInfo.isFreeEnterSeat()) {
                int index = position;
                if (index == -1) {
                    index = getAvailableIndex();
                }
                if (index == -1) {
                    mView.showToast("当前麦位已满");
                    return;
                }
                RCVoiceRoomEngine.getInstance().enterSeat(index, new RCVoiceRoomCallback() {
                    @Override
                    public void onSuccess() {
                        mView.showToast("上麦成功");
                        AudioManagerUtil.INSTANCE.choiceAudioModel();
                    }

                    @Override
                    public void onError(int code, String message) {
                        mView.showToast(message);
                    }
                });
            } else {
                //申请连麦
                RCVoiceRoomEngine.getInstance().requestSeat(new RCVoiceRoomCallback() {
                    @Override
                    public void onSuccess() {
                        currentStatus = STATUS_WAIT_FOR_SEAT;
                        mView.changeStatus(STATUS_WAIT_FOR_SEAT);
                        mView.showToast("已申请连线，等待房主接受");
                    }

                    @Override
                    public void onError(int code, String message) {
                        mView.showToast("请求连麦失败");
                    }
                });
            }
        }
    }

    /**
     * 空座位被点击 房主
     *
     * @param position
     */
    public void enterSeatOwner(UiSeatModel seatModel, int position) {
        if (seatModel.getSeatStatus()==RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty||seatModel.getSeatStatus()==
                RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusLocking){
            //如果当前是空座位或者是上锁的座位
            new NewEmptySeatFragment(seatModel,mVoiceRoomBean.getRoomId(),newVoiceRoomModel).show(((NewVoiceRoomFragment) mView).getChildFragmentManager());
        }else if (seatModel.getSeatStatus()==RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusUsing){

        }
    }

    /**
     * 监听麦位数量
     */
    private void setObSeatListChange() {
        addSubscription(newVoiceRoomModel.obSeatListChange().subscribe(new Consumer<List<UiSeatModel>>() {
            @Override
            public void accept(List<UiSeatModel> uiSeatModels) throws Throwable {
                mView.onSeatListChange(uiSeatModels);
                currentSeats.clear();
                currentSeats.addAll(uiSeatModels);
            }
        }));
    }


    /**
     * 监听房间的改变
     */
    private void setObRoomEventChange() {
        addSubscription(newVoiceRoomModel.obRoomEventChange().subscribe(new Consumer<Pair<String, ArrayList<String>>>() {
            @Override
            public void accept(Pair<String, ArrayList<String>> stringArrayListPair) throws Throwable {
                switch (stringArrayListPair.first) {
                    case EVENT_REQUEST_SEAT_AGREE://请求麦位被允许
                        currentStatus = STATUS_NOT_ON_SEAT;
                        //加入麦位
                        enterSeatIfAvailable();
                        //去更改底部的状态显示按钮
                        mView.changeStatus(currentStatus);
                        break;
                    case EVENT_REQUEST_SEAT_REFUSE://请求麦位被拒绝
                        mView.showToast("您的上麦请求被拒绝");
                        currentStatus = STATUS_NOT_ON_SEAT;
                        //去更改底部的状态显示按钮
                        mView.changeStatus(currentStatus);
                        break;
                    case EVENT_KICK_OUT_OF_SEAT: //被抱下麦
                        mView.showToast("您已被抱下麦位");
                        break;
                }
            }
        }));
    }

    /**
     * 上麦
     */
    public void enterSeatIfAvailable() {
        RCVoiceRoomEngine.getInstance()
                .notifyVoiceRoom(EVENT_AGREE_MANAGE_PICK, AccountStore.INSTANCE.getUserId());
        int availableIndex = getAvailableIndex();
        if (availableIndex > 0) {
            RCVoiceRoomEngine
                    .getInstance()
                    .enterSeat(availableIndex, new RCVoiceRoomCallback() {
                        @Override
                        public void onSuccess() {
                            mView.showToast("上麦成功");
                            AudioManagerUtil.INSTANCE.choiceAudioModel();
                        }

                        @Override
                        public void onError(int code, String message) {
                            mView.showToast(message);
                        }
                    });
        } else {
            mView.showToast("当前没有空余的麦位");
        }
    }

    @Override
    public void onNetworkStatus(int i) {
        mView.onNetworkStatus(i);
    }

    /**
     * 房主
     *
     * @return
     */
    private boolean userInSeat() {
        for (UiSeatModel currentSeat : currentSeats) {
            if (currentSeat.getUserId() != null && currentSeat.getUserId().equals(AccountStore.INSTANCE.getUserId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 位置是否有效
     *
     * @return
     */
    private int getAvailableIndex() {
        for (int i = 0; i < currentSeats.size(); i++) {
            UiSeatModel uiSeatModel = currentSeats.get(i);
            if (uiSeatModel.getSeatStatus() == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty && i != 0) {
                return i;
            }
        }
        return -1;
    }

    public NewSelfSettingFragment showNewSelfSettingFragment(UiSeatModel seatModel, String roomId) {
        NewSelfSettingFragment newSelfSettingFragment = new NewSelfSettingFragment(seatModel, mVoiceRoomBean.getRoomId()
                , newVoiceRoomModel);
        return newSelfSettingFragment;
    }

    public void sendMessage(MessageContent messageContent) {
        RCChatRoomMessageManager.INSTANCE.sendChatMessage(mVoiceRoomBean.getRoomId(), messageContent, true
                , new Function1<Integer, Unit>() {
                    @Override
                    public Unit invoke(Integer integer) {
                        //发送成功，回调给接收的地方，统一去处理，避免多个地方处理
                        return null;
                    }
                }, new Function2<IRongCoreEnum.CoreErrorCode, Integer, Unit>() {
                    @Override
                    public Unit invoke(IRongCoreEnum.CoreErrorCode coreErrorCode, Integer integer) {
                        mView.showToast("发送失败");
                        return null;
                    }
                });
    }

    /**
     * 设置管理员
     *
     * @param user
     * @param callback
     */
    @Override
    public void clickSettingAdmin(User user, ClickCallback<Boolean> callback) {
        if (mVoiceRoomBean == null) {
            return;
        }
        boolean isAdmin = !MemberCache.getInstance().isAdmin(user.getUserId());
        HashMap<String, Object> params = new OkParams()
                .add("roomId", mVoiceRoomBean.getRoomId())
                .add("userId", user.getUserId())
                .add("isManage", isAdmin)
                .build();
        // 先请求 设置/取消 管理员
        OkApi.put(VRApi.ADMIN_MANAGE, params, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (result.ok()) {
                    RCChatroomAdmin admin = new RCChatroomAdmin();
                    admin.setAdmin(isAdmin);
                    admin.setUserId(user.getUserId());
                    admin.setUserName(user.getUserName());
                    // 成功后发送管理变更的消息
                    sendMessage(admin);
                    callback.onResult(true, "");
                } else {
                    showToast(result.getMessage());
                    callback.onResult(true, result.getMessage());
                }
            }
        });
    }

    /**
     * 同意上麦
     */
    public void acceptRequestSeat(String userId,ClickCallback<Boolean> callback) {
        addSubscription(Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter emitter) throws Throwable {
                int availableIndex = getAvailableIndex();
                if (availableIndex < 0) {
                    showToast("房间麦位已满");
                    return;
                }
                RCVoiceRoomEngine.getInstance()
                        .acceptRequestSeat(userId, new RCVoiceRoomCallback() {
                            @Override
                            public void onSuccess() {
                                emitter.onComplete();
                                callback.onResult(true,"");
                            }

                            @Override
                            public void onError(int i, String s) {
                                emitter.onError(new Throwable(s));
                            }
                        });
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
    }

    /**
     * 邀请上麦
     *
     * @param user
     * @param callback
     */
    @Override
    public void clickInviteSeat(User user, ClickCallback<Boolean> callback) {
        addSubscription(Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter emitter) throws Throwable {
                if (getAvailableIndex() < 0) {
                    emitter.onError(new Throwable("麦位已满"));
                    return;
                }
                RCVoiceRoomEngine.getInstance().pickUserToSeat(user.getUserId(), new RCVoiceRoomCallback() {
                    @Override
                    public void onSuccess() {
                        //邀请成功,集合会跟着变化
                        emitter.onComplete();
                    }

                    @Override
                    public void onError(int i, String s) {
                        emitter.onError(new Throwable(s));
                    }
                });
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Throwable {
                        callback.onResult(true, "");
                    }
                }).doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        callback.onResult(false, throwable.getMessage());
                    }
                }).subscribe());
    }

    /**
     * 踢出去房间
     *
     * @param user
     * @param callback
     */
    @Override
    public void clickKickRoom(User user, ClickCallback<Boolean> callback) {
        addSubscription(Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@io.reactivex.rxjava3.annotations.NonNull CompletableEmitter emitter) throws Throwable {
                RCVoiceRoomEngine.getInstance().kickUserFromRoom(user.getUserId(), new RCVoiceRoomCallback() {
                    @Override
                    public void onSuccess() {
                        //踢出房间成功以后，要发送消息给被踢出的人
                        RCChatRoomMessageManager.INSTANCE.sendChatMessage(mVoiceRoomBean.getRoomId(),
                                new RCChatroomKickOut(),
                                true,
                                new Function1<Integer, Unit>() {
                                    @Override
                                    public Unit invoke(Integer integer) {
                                        //成功
                                        emitter.onComplete();
                                        return null;
                                    }
                                }
                                , new Function2<IRongCoreEnum.CoreErrorCode, Integer, Unit>() {
                                    @Override
                                    public Unit invoke(IRongCoreEnum.CoreErrorCode coreErrorCode, Integer integer) {
                                        //失败
                                        emitter.onError(new Throwable(coreErrorCode + ""));
                                        return null;
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(int i, String s) {
                        emitter.onError(new Throwable(s));
                    }
                });
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Throwable {
                        callback.onResult(true, "");
                    }
                }).doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        callback.onResult(false, throwable.getMessage());
                    }
                }).subscribe());
    }

    /**
     * 抱下麦
     *
     * @param user
     * @param callback
     */
    @Override
    public void clickKickSeat(User user, ClickCallback<Boolean> callback) {
        addSubscription(Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@io.reactivex.rxjava3.annotations.NonNull CompletableEmitter emitter) throws Throwable {
                RCVoiceRoomEngine.getInstance().kickUserFromSeat(user.getUserId(), new RCVoiceRoomCallback() {
                    @Override
                    public void onSuccess() {
                        emitter.onComplete();
                    }

                    @Override
                    public void onError(int i, String s) {
                        emitter.onError(new Throwable(s));
                    }
                });
            }
        }).doOnComplete(new Action() {
            @Override
            public void run() throws Throwable {
                callback.onResult(true, "");
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Throwable {
                callback.onResult(false, throwable.getMessage());
            }
        }).subscribe());
    }


    @Override
    public void clickMuteSeat(User user, ClickCallback<Boolean> callback) {
        clickMuteSeatByUser(user,callback);
    }

    /**
     *座位开麦或者闭麦，通过麦位的位置
     */
    public void clickMuteSeatByIndex(int index ,boolean isMute,ClickCallback<Boolean> callback){
        clickMuteSeat(index,isMute,callback);
    }
    /**
     *座位开麦或者闭麦，通过当前麦位的位置的用户
     */
    public void clickMuteSeatByUser(User user,ClickCallback<Boolean> callback){
        UiSeatModel uiSeatModel = newVoiceRoomModel.getSeatInfoByUserId(user.getUserId());
        clickMuteSeat(uiSeatModel.getIndex(),!uiSeatModel.isMute(),callback);
    }

    /**
     * 座位禁麦，根据点击的位置来禁止
     */
    public void clickMuteSeat(int index, boolean isMute,ClickCallback<Boolean> callback) {
        addSubscription(Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter emitter) throws Throwable {
                RCVoiceRoomEngine.getInstance()
                        .muteSeat(index, isMute, new RCVoiceRoomCallback() {
                            @Override
                            public void onSuccess() {
                                //座位禁麦成功
                                emitter.onComplete();
                                if (isMute) {
                                    EToast.showToast("此麦位已闭麦");
                                }else {
                                    EToast.showToast("已取消闭麦");
                                }
                            }

                            @Override
                            public void onError(int i, String s) {
                                //座位禁麦失败
                                emitter.onError(new Throwable(s));
                            }
                        });
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        callback.onResult(false, throwable.getMessage());
                    }
                }).doOnComplete(new Action() {
                    @Override
                    public void run() throws Throwable {
                        callback.onResult(true, "");
                    }
                }).subscribe());
    }

    /**
     * 关闭座位(根据用户的ID去关闭)
     * @param user
     * @param callback
     */
    public void clickCloseSeatByUser(User user,ClickCallback<Boolean> callback){
        UiSeatModel uiSeatModel = newVoiceRoomModel.getSeatInfoByUserId(user.getUserId());
        clickCloseSeatByIndex(uiSeatModel.getIndex(),true,callback);
    }
    /**
     * 根据麦位的位置去关闭座位
     */
    public void clickCloseSeatByIndex(int index, boolean isClose,ClickCallback<Boolean> callback) {
        addSubscription(Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@io.reactivex.rxjava3.annotations.NonNull CompletableEmitter emitter) throws Throwable {
                RCVoiceRoomEngine.getInstance().lockSeat(index, isClose, new RCVoiceRoomCallback() {
                    @Override
                    public void onSuccess() {
                        //锁座位成功
                        emitter.onComplete();
                    }

                    @Override
                    public void onError(int i, String s) {
                        //锁座位失败
                        emitter.onError(new Throwable(s));
                    }
                });
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        callback.onResult(false, throwable.getMessage());
                    }
                }).doOnComplete(new Action() {
                    @Override
                    public void run() throws Throwable {
                        callback.onResult(true, "");
                    }
                }).subscribe());
    }
    /**
     * 关闭座位(根据用户的ID去关闭)
     *
     * @param user
     * @param callback
     */
    @Override
    public void clickCloseSeat(User user, ClickCallback<Boolean> callback) {
        clickCloseSeatByUser(user,callback);
    }

    /**
     * 发送礼物
     *
     * @param user
     */
    @Override
    public void clickSendGift(User user) {

    }

    public void showSeatOperationViewPagerFragment(int index) {
        SeatOperationViewPagerFragment seatOperationViewPagerFragment = new SeatOperationViewPagerFragment(newVoiceRoomModel,index);
        seatOperationViewPagerFragment.show(((NewVoiceRoomFragment) mView).getChildFragmentManager());
    }
}
