package cn.rongcloud.voiceroom.room;

import static cn.rong.combusis.sdk.Api.EVENT_KICK_OUT_OF_SEAT;
import static cn.rong.combusis.sdk.Api.EVENT_REQUEST_SEAT_AGREE;
import static cn.rong.combusis.sdk.Api.EVENT_REQUEST_SEAT_CANCEL;
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
import com.kit.utils.Logger;
import com.rongcloud.common.utils.AccountStore;
import com.rongcloud.common.utils.AudioManagerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.common.ui.dialog.ConfirmDialog;
import cn.rong.combusis.manager.RCChatRoomMessageManager;
import cn.rong.combusis.message.RCChatroomAdmin;
import cn.rong.combusis.message.RCChatroomBarrage;
import cn.rong.combusis.message.RCChatroomEnter;
import cn.rong.combusis.message.RCChatroomGift;
import cn.rong.combusis.message.RCChatroomGiftAll;
import cn.rong.combusis.message.RCChatroomKickOut;
import cn.rong.combusis.message.RCChatroomLocationMessage;
import cn.rong.combusis.message.RCChatroomSeats;
import cn.rong.combusis.message.RCChatroomVoice;
import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.ui.room.dialog.shield.Shield;
import cn.rong.combusis.ui.room.fragment.ClickCallback;
import cn.rong.combusis.ui.room.fragment.MemberSettingFragment;
import cn.rong.combusis.ui.room.model.MemberCache;
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomCallback;
import cn.rongcloud.voiceroom.model.RCVoiceSeatInfo;
import cn.rongcloud.voiceroom.room.dialogFragment.NewCreatorSettingFragment;
import cn.rongcloud.voiceroom.room.dialogFragment.NewEmptySeatFragment;
import cn.rongcloud.voiceroom.room.dialogFragment.NewSelfSettingFragment;
import cn.rongcloud.voiceroom.room.dialogFragment.seatoperation.NewRevokeSeatRequestFragment;
import cn.rongcloud.voiceroom.room.dialogFragment.seatoperation.SeatOperationViewPagerFragment;
import cn.rongcloud.voiceroom.ui.uimodel.UiRoomModel;
import cn.rongcloud.voiceroom.ui.uimodel.UiSeatModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
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
    /**
     * 房间信息
     */
    private VoiceRoomBean mVoiceRoomBean;

    private ConfirmDialog confirmDialog;

    public int currentStatus = STATUS_NOT_ON_SEAT;
    private List<Shield> shields;

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
        if (mVoiceRoomBean != null && !TextUtils.isEmpty(mVoiceRoomBean.getRoomId())) {
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
        newVoiceRoomModel.getRoomInfo(getmVoiceRoomBean().getRoomId()).subscribe();
        MemberCache.getInstance().fetchData(mVoiceRoomBean.getRoomId());
        //监听房间里面的人
        MemberCache.getInstance().getMemberList().observe(((NewVoiceRoomFragment) mView).getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                newVoiceRoomModel.onMemberListener(users);
            }
        });
        //获取屏蔽词
        getShield();
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
        setObSeatInfoChange();
        setObRoomInfoChange();
    }


    /**
     * 监听房间的信息
     */
    private void setObRoomInfoChange() {
        addSubscription(newVoiceRoomModel.obRoomInfoChange()
                .subscribe(new Consumer<UiRoomModel>() {
                    @Override
                    public void accept(UiRoomModel uiRoomModel) throws Throwable {
                        String extra = "";
                        if (uiRoomModel.getRcRoomInfo() != null) {
                            extra = uiRoomModel.getRcRoomInfo().getExtra();
                        }
                        String notice = TextUtils.isEmpty(extra) ? String.format("欢迎来到 %s", mVoiceRoomBean.getRoomName()) : extra;
                        mView.showNotice(notice, false);
                    }
                }));
    }

    /**
     * 麦位信息改变监听
     */
    private void setObSeatInfoChange() {
        addSubscription(newVoiceRoomModel.obSeatInfoChange().subscribe(new Consumer<UiSeatModel>() {
            @Override
            public void accept(UiSeatModel uiSeatModel) throws Throwable {
                //根据位置去刷新波纹
                int index = uiSeatModel.getIndex();
                if (index == 0) {
                    mView.refreshRoomOwner(uiSeatModel);
                } else {
                    mView.refreshSeatIndex(index - 1, uiSeatModel);
                }
            }
        }));
    }

    /**
     * 设置请求上麦监听
     */
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
                        //将消息显示到列表上
                        Class<? extends MessageContent> aClass = messageContent.getClass();
                        if (RCChatroomVoice.class.equals(aClass) || RCChatroomLocationMessage.class.equals(aClass)
                                || RCChatroomVoice.class.equals(aClass) || RCChatroomBarrage.class.equals(aClass)
                                || RCChatroomEnter.class.equals(aClass) || RCChatroomKickOut.class.equals(aClass)
                                || RCChatroomGift.class.equals(aClass) || RCChatroomAdmin.class.equals(aClass)
                                || RCChatroomSeats.class.equals(aClass) || RCChatroomGiftAll.class.equals(aClass)) {
                            mView.showMessage(messageContent, false);
                        }
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
        if (newVoiceRoomModel.userInSeat()) {
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
                    index = newVoiceRoomModel.getAvailableIndex();
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
        if (seatModel.getSeatStatus() == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty || seatModel.getSeatStatus() ==
                RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusLocking) {
            //如果当前是空座位或者是上锁的座位
            new NewEmptySeatFragment(seatModel, mVoiceRoomBean.getRoomId(), newVoiceRoomModel).show(((NewVoiceRoomFragment) mView).getChildFragmentManager());
        } else if (seatModel.getSeatStatus() == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusUsing) {
            //如果座位正在使用中

        }
    }

    /**
     * 监听麦位改变
     */
    private void setObSeatListChange() {
        addSubscription(newVoiceRoomModel.obSeatListChange().subscribe(new Consumer<List<UiSeatModel>>() {
            @Override
            public void accept(List<UiSeatModel> uiSeatModels) throws Throwable {
                mView.onSeatListChange(uiSeatModels);
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
                        newVoiceRoomModel.enterSeatIfAvailable();
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
                    case EVENT_REQUEST_SEAT_CANCEL://撤销麦位申请
                        currentStatus = STATUS_NOT_ON_SEAT;
                        mView.changeStatus(currentStatus);
                        break;
                }
            }
        }));
    }


    @Override
    public void onNetworkStatus(int i) {
        mView.onNetworkStatus(i);
    }


    /**
     * 麦位上点击自己的头像
     *
     * @param seatModel
     * @param roomId
     * @return
     */
    public NewSelfSettingFragment showNewSelfSettingFragment(UiSeatModel seatModel, String roomId) {
        NewSelfSettingFragment newSelfSettingFragment = new NewSelfSettingFragment(seatModel, mVoiceRoomBean.getRoomId()
                , newVoiceRoomModel);
        return newSelfSettingFragment;
    }

    /**
     * 房间所有者点击自己的头像
     */
    public void onClickRoomOwnerView(FragmentManager fragmentManager) {
        UiSeatModel uiSeatModel = newVoiceRoomModel.getUiSeatModels().get(0);
        if (uiSeatModel != null) {
            if (!TextUtils.isEmpty(uiSeatModel.getUserId()) && uiSeatModel.getUserId().equals(AccountStore.INSTANCE.getUserId())) {
                //如果在麦位上
                NewCreatorSettingFragment newCreatorSettingFragment = new NewCreatorSettingFragment(newVoiceRoomModel, uiSeatModel);
                newCreatorSettingFragment.show(fragmentManager);
            } else {
                //如果不在麦位上，直接上麦
                roomOwnerEnterSeat();
            }
        }

    }

    /**
     * 发送消息
     *
     * @param messageContent
     */
    public void sendMessage(MessageContent messageContent) {
        //先判断是否包含了屏蔽词
        boolean isContains = false;
        if (shields != null) {
            for (Shield shield : shields) {
                if (messageContent.toString().contains(shield.getName())) {
                    isContains = true;
                    break;
                }
            }
            if (isContains) {
                //如果是包含了敏感词'
                mView.showMessage(messageContent, false);
                mView.clearInput();
                return;
            }
        }
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
     * 设置屏蔽词
     *
     * @param shields
     */
    public void setShield(List<String> shields) {
        Logger.e(shields.toString());
    }

    /**
     * 获取屏蔽词
     */
    private void getShield() {
        OkApi.get(VRApi.getShield(mVoiceRoomBean.getRoomId()), null, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (result.ok()) {
                    shields = result.getList(Shield.class);
                }
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

    @Override
    public void clickInviteSeat(User user, ClickCallback<Boolean> callback) {
        newVoiceRoomModel.clickInviteSeat(user.getUserId(), callback);
    }

    @Override
    public void clickKickRoom(User user, ClickCallback<Boolean> callback) {
        newVoiceRoomModel.clickKickRoom(user, callback);
    }

    @Override
    public void clickKickSeat(User user, ClickCallback<Boolean> callback) {
        newVoiceRoomModel.clickKickSeat(user, callback);
    }


    @Override
    public void clickMuteSeat(User user, ClickCallback<Boolean> callback) {
        clickMuteSeatByUser(user, callback);
    }

    /**
     * 座位开麦或者闭麦，通过麦位的位置
     */
    public void clickMuteSeatByIndex(int index, boolean isMute, ClickCallback<Boolean> callback) {
        newVoiceRoomModel.clickMuteSeat(index, isMute, callback);
    }

    /**
     * 座位开麦或者闭麦，通过当前麦位的位置的用户
     */
    public void clickMuteSeatByUser(User user, ClickCallback<Boolean> callback) {
        UiSeatModel uiSeatModel = newVoiceRoomModel.getSeatInfoByUserId(user.getUserId());
        newVoiceRoomModel.clickMuteSeat(uiSeatModel.getIndex(), !uiSeatModel.isMute(), callback);
    }


    /**
     * 关闭座位(根据用户的ID去关闭)
     *
     * @param user
     * @param callback
     */
    public void clickCloseSeatByUser(User user, ClickCallback<Boolean> callback) {
        UiSeatModel uiSeatModel = newVoiceRoomModel.getSeatInfoByUserId(user.getUserId());
        newVoiceRoomModel.clickCloseSeatByIndex(uiSeatModel.getIndex(), true, callback);
    }

    /**
     * 关闭座位(根据用户的ID去关闭)
     *
     * @param user
     * @param callback
     */
    @Override
    public void clickCloseSeat(User user, ClickCallback<Boolean> callback) {
        clickCloseSeatByUser(user, callback);
    }

    /**
     * 发送礼物
     *
     * @param user
     */
    @Override
    public void clickSendGift(User user) {

    }

    /**
     * 展示邀请和接受邀请fragment
     *
     * @param index
     */
    public void showSeatOperationViewPagerFragment(int index) {
        SeatOperationViewPagerFragment seatOperationViewPagerFragment = new SeatOperationViewPagerFragment(newVoiceRoomModel, index);
        seatOperationViewPagerFragment.show(((NewVoiceRoomFragment) mView).getChildFragmentManager());
    }

    /**
     * 展示撤销麦位申请
     */
    public void showNewRevokeSeatRequestFragment() {
        NewRevokeSeatRequestFragment newRevokeSeatRequestFragment = new NewRevokeSeatRequestFragment(newVoiceRoomModel);
        newRevokeSeatRequestFragment.show(((NewVoiceRoomFragment) mView).getChildFragmentManager());
    }

    /**
     * 弹窗收到上麦邀请弹窗
     *
     * @param isCreate 是否是房主
     * @param userId   邀请人的ID
     */
    public void showPickReceivedDialog(boolean isCreate, String userId) {
        String pickName = isCreate ? "房主" : "管理员";
        confirmDialog = new ConfirmDialog(((NewVoiceRoomFragment) mView).getActivity(),
                "您被" + pickName + "邀请上麦，是否同意?", true,
                "同意", "拒绝", new Function0<Unit>() {
            @Override
            public Unit invoke() {
                //拒绝
                confirmDialog.dismiss();
                newVoiceRoomModel.refuseInvite(userId);
                return null;
            }
        }, new Function0<Unit>() {
            @Override
            public Unit invoke() {
                //同意
                newVoiceRoomModel.enterSeatIfAvailable();
                confirmDialog.dismiss();
                return null;
            }
        }
        );
        confirmDialog.show();
    }


    /**
     * 调用离开房间
     */
    public void leaveRoom() {
        RCVoiceRoomEngine.getInstance().leaveRoom(new RCVoiceRoomCallback() {

            @Override
            public void onSuccess() {
                Logger.e("==============leaveRoom onSuccess");
                mView.dismissLoading();
                mView.finish();
            }

            @Override
            public void onError(int code, String message) {
                Logger.e("==============leaveRoom onError");
                mView.dismissLoading();
                mView.showToast(message);
            }
        });
    }

    /**
     * 房主关闭房间
     */
    public void closeRoom() {
        mView.showLoading("正在关闭房间");
        // 房主关闭房间，调用删除房间接口
        OkApi.get(VRApi.deleteRoom(mVoiceRoomBean.getRoomId()), null, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (result.ok()) {
                    leaveRoom();
                } else {
                    mView.dismissLoading();
                }
            }

            @Override
            public void onError(int code, String msg) {
                super.onError(code, msg);
                mView.dismissLoading();
                mView.showToast(msg);
            }
        });
    }

    /**
     * 房主上麦
     */
    public void roomOwnerEnterSeat() {
        RCVoiceRoomEngine.getInstance().enterSeat(0, new RCVoiceRoomCallback() {
            @Override
            public void onSuccess() {
                AudioManagerUtil.INSTANCE.choiceAudioModel();
                mView.enterSeatSuccess();
            }

            @Override
            public void onError(int i, String message) {
                mView.showToast(message);
            }
        });
    }


    /**
     * 修改房间公告
     *
     * @param notice
     */
    public void modifyNotice(String notice) {
//        RCVoiceRoomEngine.getInstance().notifyVoiceRoom(IRCVoiceRoomEngine.);

    }

//    /**
//     * 发送公告更新的
//     */
//    private void sendNoticeModifyMessage() {
//        RCChatroomLocationMessage tips = new RCChatroomLocationMessage();
//        tips.setContent("房间公告已更新！");
////        mView.addToMessageList(tips, false);
//    }
}
