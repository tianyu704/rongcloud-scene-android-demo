package cn.rongcloud.voiceroom.room;

import static cn.rongcloud.voiceroom.room.NewVoiceRoomPresenter.STATUS_NOT_ON_SEAT;
import static cn.rongcloud.voiceroom.room.NewVoiceRoomPresenter.STATUS_ON_SEAT;
import static cn.rongcloud.voiceroom.room.NewVoiceRoomPresenter.STATUS_WAIT_FOR_SEAT;

import android.graphics.Color;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.kit.utils.Logger;
import com.rongcloud.common.utils.AccountStore;
import com.rongcloud.common.utils.ImageLoaderUtil;
import com.rongcloud.common.utils.UiUtils;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.common.ui.dialog.EditDialog;
import cn.rong.combusis.common.ui.dialog.InputPasswordDialog;
import cn.rong.combusis.manager.RCChatRoomMessageManager;
import cn.rong.combusis.message.RCChatroomBarrage;
import cn.rong.combusis.message.RCChatroomEnter;
import cn.rong.combusis.message.RCChatroomLocationMessage;
import cn.rong.combusis.message.RCChatroomVoice;
import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.provider.user.UserProvider;
import cn.rong.combusis.provider.voiceroom.RoomOwnerType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.provider.voiceroom.VoiceRoomProvider;
import cn.rong.combusis.ui.room.AbsRoomFragment;
import cn.rong.combusis.ui.room.RoomMessageAdapter;
import cn.rong.combusis.ui.room.dialog.ExitRoomPopupWindow;
import cn.rong.combusis.ui.room.dialog.RoomNoticeDialog;
import cn.rong.combusis.ui.room.dialog.shield.ShieldDialog;
import cn.rong.combusis.ui.room.fragment.BackgroundSettingFragment;
import cn.rong.combusis.ui.room.fragment.MemberListFragment;
import cn.rong.combusis.ui.room.fragment.MemberSettingFragment;
import cn.rong.combusis.ui.room.fragment.roomsetting.IFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomSettingFragment;
import cn.rong.combusis.ui.room.model.MemberCache;
import cn.rong.combusis.ui.room.widget.RoomBottomView;
import cn.rong.combusis.ui.room.widget.RoomSeatView;
import cn.rong.combusis.ui.room.widget.RoomTitleBar;
import cn.rong.combusis.widget.VoiceRoomMiniManager;
import cn.rongcloud.voiceroom.R;
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomCallback;
import cn.rongcloud.voiceroom.model.RCVoiceSeatInfo;
import cn.rongcloud.voiceroom.room.adapter.NewVoiceRoomSeatsAdapter;
import cn.rongcloud.voiceroom.ui.uimodel.UiRoomModel;
import cn.rongcloud.voiceroom.ui.uimodel.UiSeatModel;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MessageContent;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

/**
 * @author 李浩
 * @date 2021/9/24
 */
public class NewVoiceRoomFragment extends AbsRoomFragment<VoiceRoomBean, NewVoiceRoomPresenter>
        implements IVoiceRoomFragmentView, RoomMessageAdapter.OnClickMessageUserListener,
        RoomBottomView.OnBottomOptionClickListener, MemberListFragment.OnClickUserListener {
    private VoiceRoomBean mVoiceRoomBean;
    private ImageView mBackgroundImageView;
    private RoomTitleBar mRoomTitleBar;
    private TextView mNoticeView;
    private RoomSeatView mRoomSeatView;
    private RoomBottomView mRoomBottomView;
    private RecyclerView mMessageView;
    private RoomMessageAdapter mRoomMessageAdapter;
    private RecyclerView rv_seat_list;
    private NewVoiceRoomSeatsAdapter voiceRoomSeatsAdapter;
    private MemberSettingFragment mMemberSettingFragment;
    private ExitRoomPopupWindow mExitRoomPopupWindow;
    private RoomNoticeDialog mNoticeDialog;
    private MemberListFragment mMemberListFragment;
    private RoomSettingFragment mRoomSettingFragment;
    private InputPasswordDialog mInputPasswordDialog;
    private EditDialog mEditDialog;
    private ShieldDialog mShieldDialog;
    private BackgroundSettingFragment mBackgroundSettingFragment;

    public static Fragment getInstance() {
        return new NewVoiceRoomFragment();
    }

    @Override
    public int setLayoutId() {
        return R.layout.fragment_new_voice_room;
    }

    @Override
    public void init() {
        mNoticeDialog = new RoomNoticeDialog(getContext());
        mRoomSettingFragment = new RoomSettingFragment(present);
        // 头部
        mRoomTitleBar = getView(R.id.room_title_bar);
        mRoomTitleBar.setOnMemberClickListener(v -> {
            mMemberListFragment = new MemberListFragment(mVoiceRoomBean.getRoomId(), this);
            mMemberListFragment.show(getChildFragmentManager());
        });

        //麦位
        rv_seat_list = getView(R.id.rv_seat_list);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 4);
        rv_seat_list.setLayoutManager(gridLayoutManager);
        voiceRoomSeatsAdapter = new NewVoiceRoomSeatsAdapter(getActivity(), new NewVoiceRoomSeatsAdapter.OnClickVoiceRoomSeatsListener() {
            @Override
            public void clickVoiceRoomSeats(UiSeatModel uiSeatModel, int position) {
                onClickVoiceRoomSeats(uiSeatModel, position);
            }
        });
        voiceRoomSeatsAdapter.setHasStableIds(true);
        rv_seat_list.setAdapter(voiceRoomSeatsAdapter);

        mRoomTitleBar.setOnMenuClickListener(v -> {
            clickMenu();
        });

        mNoticeView = getView(R.id.tv_notice);
        mNoticeView.setOnClickListener(v -> {
            showNoticeDialog();
        });
        // 背景
        mBackgroundImageView = getView(R.id.iv_background);
        // 房主座位
        mRoomSeatView = getView(R.id.room_seat_view);
        //房主点击头像的时候
        mRoomSeatView.setRoomOwnerHeadOnclickListener(new RoomSeatView.RoomOwnerHeadOnclickListener() {
            @Override
            public void onClick() {
                //弹出
                if (getRoomOwnerType() == RoomOwnerType.VOICE_OWNER) {
                    //如果是房间所有者 ,如果在麦位上,那么弹出下麦和关闭麦克风弹窗，如果不在麦位上，那么直接上麦
                    present.onClickRoomOwnerView(getChildFragmentManager());
                }
            }
        });
        // 底部操作按钮和双击送礼物
        mRoomBottomView = getView(R.id.room_bottom_view);
        // 弹幕消息列表
        mMessageView = getView(R.id.rv_message);
        mMessageView.setLayoutManager(new LinearLayoutManager(getContext()));
        mMessageView.addItemDecoration(new DefaultItemDecoration(Color.TRANSPARENT, 0, UiUtils.INSTANCE.dp2Px(getContext(), 5)));
        mRoomMessageAdapter = new RoomMessageAdapter(getContext(), this);
        mMessageView.setAdapter(mRoomMessageAdapter);
    }

    /**
     * 显示公告弹窗
     */
    @Override
    public void showNoticeDialog() {
        boolean isEdit = false;
        if (getRoomOwnerType() == RoomOwnerType.VOICE_OWNER) {
            isEdit = true;
        } else {
            isEdit = false;
        }
        mNoticeDialog.show("", isEdit, new RoomNoticeDialog.OnSaveNoticeListener() {
            @Override
            public void saveNotice(String notice) {
                //修改公告信息
                present.modifyNotice(notice);
            }
        });
    }

    /**
     * 点击右上角菜单按钮
     */
    private void clickMenu() {
        mExitRoomPopupWindow = new ExitRoomPopupWindow(getContext(), getRoomOwnerType(), new ExitRoomPopupWindow.OnOptionClick() {
            @Override
            public void clickPackRoom() {
                //最小化窗口
                //先做悬浮窗
                requireActivity().moveTaskToBack(true);
                //缩放动画,并且显示悬浮窗，在这里要做悬浮窗判断
                requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                //设置一下当前的封面图
                VoiceRoomMiniManager.getInstance().init(requireActivity(), requireActivity().getIntent());
                VoiceRoomMiniManager.getInstance().refreshRoomOwner(mVoiceRoomBean.getRoomId());
                VoiceRoomMiniManager.getInstance()
                        .setBackgroudPic(mVoiceRoomBean.getThemePictureUrl(), activity);
                VoiceRoomMiniManager.getInstance().showMiniWindows();
            }

            @Override
            public void clickLeaveRoom() {
                // 观众离开房间
                present.leaveRoom();
            }

            @Override
            public void clickCloseRoom() {
                // 房主关闭房间
                present.closeRoom();
            }
        });
        mExitRoomPopupWindow.setAnimationStyle(R.style.popup_window_anim_style);
        mExitRoomPopupWindow.showAtLocation(mBackgroundImageView, Gravity.TOP, 0, 0);
    }


    /**
     * 麦位被点击的情况
     *
     * @param seatModel
     * @param position
     */
    private void onClickVoiceRoomSeats(UiSeatModel seatModel, int position) {
        switch (getRoomOwnerType()) {
            case VOICE_OWNER://房主
                onClickVoiceRoomSeatsByOwner(seatModel, position);
                break;
            case VOICE_VIEWER://观众
                onClickVoiceRoomSeatsByViewer(seatModel, position);
                break;
        }

    }

    /**
     * 观众点击麦位的时候
     *
     * @param seatModel
     * @param position
     */
    private void onClickVoiceRoomSeatsByViewer(UiSeatModel seatModel, int position) {
        if (seatModel.getSeatStatus() == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusLocking) {
            // 点击锁定座位
            showToast("该座位已锁定");
        } else if (seatModel.getSeatStatus() == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty) {
            //点击空座位 的时候
            present.enterSeatViewer(position);
        } else if (seatModel.getSeatStatus() == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusUsing) {
            if (seatModel.getUserId().equals(AccountStore.INSTANCE.getUserId())) {
                // 点击自己头像
                present.showNewSelfSettingFragment(seatModel, mVoiceRoomBean.getRoomId()).show(getChildFragmentManager());
            } else {
                // 点击别人头像
                if (mMemberSettingFragment == null) {
                    mMemberSettingFragment = new MemberSettingFragment(getRoomOwnerType(), present);
                }
                User user = new User();
                user.setUserId(seatModel.getUserId());
                user.setUserName(seatModel.getUserName());
                user.setPortrait(seatModel.getPortrait());
                mMemberSettingFragment.setMemberIsOnSeat(true);
                mMemberSettingFragment.setSeatPosition(position + 1);
                mMemberSettingFragment.setMute(seatModel.isMute());
                mMemberSettingFragment.show(getChildFragmentManager(), user, mVoiceRoomBean.getCreateUserId());
            }
        }
    }

    /**
     * 房主点击麦位的时候
     *
     * @param seatModel
     * @param position
     */
    private void onClickVoiceRoomSeatsByOwner(UiSeatModel seatModel, int position) {
        if (seatModel.getSeatStatus() == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty
                || seatModel.getSeatStatus() == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusLocking) {
            //点击空座位或者锁定座位的时候，弹出弹窗
            present.enterSeatOwner(seatModel, position);
        } else if (seatModel.getSeatStatus() == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusUsing) {
            //弹窗设置弹窗
            // 点击别人头像
            if (mMemberSettingFragment == null) {
                mMemberSettingFragment = new MemberSettingFragment(getRoomOwnerType(), present);
            }
            User user = new User();
            user.setUserId(seatModel.getUserId());
            user.setUserName(seatModel.getUserName());
            user.setPortrait(seatModel.getPortrait());
            mMemberSettingFragment.setMemberIsOnSeat(true);
            mMemberSettingFragment.setSeatPosition(position + 1);
            mMemberSettingFragment.setMute(seatModel.isMute());
            mMemberSettingFragment.show(getChildFragmentManager(), user, mVoiceRoomBean.getCreateUserId());
        }
    }


    @Override
    public void initListener() {

    }

    @Override
    public void joinRoom(VoiceRoomBean voiceRoomBean) {
        mVoiceRoomBean = voiceRoomBean;
        setRoomData(mVoiceRoomBean);
        present.setCurrentRoom(mVoiceRoomBean);
        sendSystemMessage();
    }

    @Override
    public void leaveRoom() {
        //离开房间的时候
        present.leaveCurrentRoom();
    }

    @Override
    public void onBackPressed() {
        RCVoiceRoomEngine.getInstance().leaveRoom(new RCVoiceRoomCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: ");
                getActivity().finish();
            }

            @Override
            public void onError(int i, String s) {
                Log.d(TAG, "onError: ");
            }
        });
    }


    @Override
    public NewVoiceRoomPresenter createPresent() {
        return new NewVoiceRoomPresenter(this, getLifecycle());
    }

    /**
     * 进入房间后发送默认的消息
     */
    private void sendSystemMessage() {
        if (mVoiceRoomBean != null) {
            showMessage(null, true);
            // 默认消息
            RCChatroomLocationMessage welcome = new RCChatroomLocationMessage();
            welcome.setContent(String.format("欢迎来到 %s", mVoiceRoomBean.getRoomName()));
            showMessage(welcome, false);
            RCChatroomLocationMessage tips = new RCChatroomLocationMessage();
            tips.setContent("感谢使用融云 RTC 语音房，请遵守相关法规，不要传播低俗、暴力等不良信息。欢迎您把使用过程中的感受反馈给我们。");
            showMessage(tips, false);
            Logger.e("=================发送了默认消息");
            // 广播消息
            RCChatroomEnter enter = new RCChatroomEnter();
            enter.setUserId(AccountStore.INSTANCE.getUserId());
            enter.setUserName(AccountStore.INSTANCE.getUserName());
            RCChatRoomMessageManager.INSTANCE.sendChatMessage(mVoiceRoomBean.getRoomId(), enter, false,
                    integer -> null, (coreErrorCode, integer) -> null);
        }
    }

    /**
     * 显示消息
     *
     * @param messageContent
     * @param isRefresh
     */
    @Override
    public void showMessage(MessageContent messageContent, boolean isRefresh) {
        List<MessageContent> list = new ArrayList<>(1);
        if (messageContent != null) {
            list.add(messageContent);
        }
        mRoomMessageAdapter.setData(list, isRefresh);
        int count = mRoomMessageAdapter.getItemCount();
        if (count > 0) {
            mMessageView.smoothScrollToPosition(count - 1);
        }
    }

    @Override
    public void showSettingDialog(List<MutableLiveData<IFun.BaseFun>> funList) {
        mRoomSettingFragment.show(getChildFragmentManager(), funList);
    }

    /**
     * 设置房间数据
     *
     * @param voiceRoomBean
     */
    private void setRoomData(VoiceRoomBean voiceRoomBean) {
        // 设置房间类型
        RoomOwnerType roomOwnerType = VoiceRoomProvider.provider().getRoomOwnerType(voiceRoomBean);
        setRoomOwnerType(roomOwnerType);
        if (roomOwnerType == RoomOwnerType.RADIO_OWNER) {
            RCVoiceRoomEngine.getInstance().enterSeat(0, new RCVoiceRoomCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(int i, String s) {

                }
            });
        }
        // 房主上麦
        if (roomOwnerType == RoomOwnerType.VOICE_OWNER) {
            present.roomOwnerEnterSeat();
        }
        // 加载背景
        ImageLoaderUtil.INSTANCE.loadImage(requireContext(), mBackgroundImageView, mVoiceRoomBean.getBackgroundUrl(), R.color.black);
        // 设置title数据
        mRoomTitleBar.setData(mVoiceRoomBean.getRoomName(), mVoiceRoomBean.getId());

        // 设置房主麦位信息
        mRoomSeatView.setData(mVoiceRoomBean.getCreateUserName(), mVoiceRoomBean.getCreateUserPortrait());
        // 设置底部按钮
        mRoomBottomView.setData(getRoomOwnerType(), this, voiceRoomBean.getRoomId());
        // 设置消息列表数据
        mRoomMessageAdapter.setRoomCreateId(mVoiceRoomBean.getCreateUserId());

        loadMemberData();
    }

    /**
     * 下载房间成员列表数据
     */
    private void loadMemberData() {

    }

    @Override
    public void destroyRoom() {

    }

    /**
     * 加入房间之前的准备工作
     *
     * @param roomId
     */
    @Override
    public void prepareJoinRoom(String roomId) {
        present.initListener(roomId);
    }

    @Override
    public void onSpeakingStateChanged(boolean isSpeaking) {
        mRoomSeatView.setSpeaking(isSpeaking);
    }

    /**
     * 刷新当前房主信息Ui
     *
     * @param uiSeatModel
     */
    @Override
    public void refreshRoomOwner(UiSeatModel uiSeatModel) {
        if (uiSeatModel == null) {
            return;
        }
        if (TextUtils.isEmpty(uiSeatModel.getUserId())) {
            mRoomSeatView.setData("", null);
            mRoomSeatView.setSpeaking(false);
            mRoomSeatView.setRoomOwnerMute(false);
            mRoomSeatView.setGiftCount(0);
        } else {
            User member = MemberCache.getInstance().getMember(uiSeatModel.getUserId());
            mRoomSeatView.setData(member.getUserName(), member.getPortrait());
            mRoomSeatView.setSpeaking(uiSeatModel.isSpeaking());
            mRoomSeatView.setRoomOwnerMute(uiSeatModel.isMute());
            mRoomSeatView.setGiftCount(uiSeatModel.getGiftCount());
        }
    }

    /**
     * 刷新当前麦位信息UI
     *
     * @param index
     * @param uiSeatModel
     */
    @Override
    public void refreshSeatIndex(int index, UiSeatModel uiSeatModel) {
        mRoomSeatView.post(new Runnable() {
            @Override
            public void run() {
                voiceRoomSeatsAdapter.refreshIndex(index, uiSeatModel);
            }
        });
    }

    @Override
    public void showNotice(String notice, boolean isModify) {
        mNoticeDialog.setNotice(notice);
    }


    @Override
    public void clickMessageUser(String userId) {
        UserProvider.provider().getAsyn(userId, userInfo -> {
            User user = new User();
            user.setUserId(userId);
            user.setUserName(userInfo.getName());
            user.setPortrait(userInfo.getPortraitUri().toString());
            clickUser(user);
        });
    }

    @Override
    public void onJoinRoomSuccess() {

    }

    @Override
    public void initRoleView(@NonNull UiRoomModel roomInfo) {

    }

    @Override
    public void leaveRoomSuccess() {

    }

    @Override
    public void onJoinNextRoom(boolean start) {

    }

    @Override
    public void enterSeatSuccess() {
        showToast("上麦成功");
    }

    /**
     * 最小化
     */
    @Override
    public void packupRoom() {

    }

    @Override
    public void refreshOnlineUsersNumber(int onlineUsersNumber) {

    }

    @Override
    public void refreshRoomInfo(@NonNull UiRoomModel roomInfo) {

    }

    @Override
    public void onSeatInfoChange(int index, @NonNull UiSeatModel uiSeatModel) {

    }

    /**
     * 座位发生了改变
     *
     * @param uiSeatModelList
     */
    @Override
    public void onSeatListChange(@NonNull List<UiSeatModel> uiSeatModelList) {
        List<UiSeatModel> uiSeatModels = uiSeatModelList.subList(1, uiSeatModelList.size());
        voiceRoomSeatsAdapter.refreshData(uiSeatModels);
    }

    @Override
    public void sendTextMessageSuccess(@NonNull String message) {

    }

    @Override
    public void showChatRoomMessage(@NonNull MessageContent messageContent) {

    }

    @Override
    public void showPickReceived(boolean isCreateReceive, @NonNull String userId) {

    }

    @Override
    public void switchToAdminRole(boolean isAdmin, @NonNull UiRoomModel roomInfo) {

    }

    @Override
    public void changeStatus(int status) {
        //修改底部的状态
//        switch (status) {
//            case STATUS_NOT_ON_SEAT
//                iv_request_enter_seat.setImageResource(R.drawable.ic_request_enter_seat):break;
//            case iv_request_enter_seat
//                iv_request_enter_seat.setImageResource(R.drawable.ic_wait_enter_seat):break;
//            case iv_request_enter_seat
//                iv_request_enter_seat.setImageResource(R.drawable.ic_on_seat):break;
//        }

    }

    @Override
    public void showUnReadRequestNumber(int number) {
        mRoomBottomView.setmSeatOrderNumber(number);
    }

    @Override
    public void showUnreadMessage(int count) {

    }

    @Override
    public void showFov(@Nullable Point from) {

    }

    /**
     * 显示撤销麦位申请的弹窗
     */
    @Override
    public void showRevokeSeatRequest() {
        switch (present.currentStatus) {
            case STATUS_ON_SEAT:
                break;
            case STATUS_WAIT_FOR_SEAT:
//                RevokeSeatRequestFragment(this).show(
//                        this @VoiceRoomFragment.childFragmentManager
//                )
                present.showNewRevokeSeatRequestFragment();
                break;
            case STATUS_NOT_ON_SEAT:
                present.enterSeatViewer(-1);
                break;
        }
    }

    @Override
    public void showRoomClose() {

    }

    @Override
    public void onMemberInfoChange() {

    }

    @Override
    public void onNetworkStatus(int i) {
        mRoomTitleBar.post(new Runnable() {
            @Override
            public void run() {
                mRoomTitleBar.setDelay(i);
            }
        });
    }

    @Override
    public void finish() {
        requireActivity().finish();
    }

    @Override
    public void clickSendMessage(String message) {
        //发送文字消息
        RCChatroomBarrage barrage = new RCChatroomBarrage();
        barrage.setContent(message);
        barrage.setUserId(AccountStore.INSTANCE.getUserId());
        barrage.setUserName(AccountStore.INSTANCE.getUserName());
        present.sendMessage(barrage);
    }

    @Override
    public void clearInput() {
        mRoomBottomView.clearInput();
    }


    /**
     * 发送私信
     */
    @Override
    public void clickPrivateMessage() {
        RouteUtils.routeToSubConversationListActivity(
                requireActivity(),
                Conversation.ConversationType.PRIVATE,
                "消息"
        );
    }

    @Override
    public void clickSeatOrder() {
        //弹窗邀请弹窗 并且将申请的集合和可以被要求的传入
        present.showSeatOperationViewPagerFragment(0);
    }

    /**
     * 设置按钮
     */
    @Override
    public void clickSettings() {
        present.showSettingDialog();
    }

    /**
     * PK
     *
     * @param isInPk
     */
    @Override
    public void clickPk(boolean isInPk) {

    }

    /**
     * 请求上麦按钮
     */
    @Override
    public void clickRequestSeat() {

    }

    /**
     * 发送礼物
     */
    @Override
    public void onSendGift() {

    }

    @Override
    public void onSendVoiceMessage(RCChatroomVoice rcChatroomVoice) {
        RCChatRoomMessageManager.INSTANCE.sendChatMessage(mVoiceRoomBean.getRoomId(), rcChatroomVoice, true, new Function1<Integer, Unit>() {
            @Override
            public Unit invoke(Integer integer) {
                //成功
                return null;
            }
        }, new Function2<IRongCoreEnum.CoreErrorCode, Integer, Unit>() {
            @Override
            public Unit invoke(IRongCoreEnum.CoreErrorCode coreErrorCode, Integer integer) {
                //失败
                return null;
            }
        });
    }

    @Override
    public void clickUser(User user) {
        //如果点击的是本人的名称，那么无效
        if (TextUtils.equals(user.getUserId(), AccountStore.INSTANCE.getUserId())) {
            return;
        }
        if (mMemberSettingFragment == null) {
            mMemberSettingFragment = new MemberSettingFragment(getRoomOwnerType(), present);
        }
        mMemberSettingFragment.show(getChildFragmentManager(), user, mVoiceRoomBean.getCreateUserId());
    }

    @Override
    public void showSetPasswordDialog(MutableLiveData<IFun.BaseFun> item) {
        mInputPasswordDialog = new InputPasswordDialog(getContext(), false, () -> null, s -> {
            if (TextUtils.isEmpty(s)) {
                return null;
            }
            if (s.length() < 4) {
                showToast(getString(R.string.text_please_input_four_number));
                return null;
            }
            mInputPasswordDialog.dismiss();
            present.setRoomPassword(true, s, item);
            return null;
        });
        mInputPasswordDialog.show();
    }

    @Override
    public void showSetRoomNameDialog(String name) {
        mEditDialog = new EditDialog(
                requireActivity(),
                "修改房间标题",
                "请输入房间名",
                name,
                10,
                () -> null,
                s -> {
                    present.setRoomName(s);
                    mEditDialog.dismiss();
                    return null;
                }
        );
        mEditDialog.show();
    }

    /**
     * 屏蔽词弹窗
     * @param roomId
     */
    @Override
    public void showShieldDialog(String roomId) {
        mShieldDialog = new ShieldDialog(requireActivity(), roomId, 10);
        mShieldDialog.show();
    }

    /**
     * 房间背景弹窗
     * @param url
     */
    @Override
    public void showSelectBackgroundDialog(String url) {
        mBackgroundSettingFragment = new BackgroundSettingFragment(url, present);
        mBackgroundSettingFragment.show(getChildFragmentManager());
    }


    @Override
    public void setVoiceName(String name) {
        mRoomTitleBar.setRoomName(name);
    }

    @Override
    public void setRoomBackground(String url) {
        ImageLoaderUtil.INSTANCE.loadImage(requireContext(), mBackgroundImageView, url, R.color.black);
    }
}
