package cn.rongcloud.voiceroom.room;

import static cn.rongcloud.voiceroom.room.NewVoiceRoomPresenter.STATUS_NOT_ON_SEAT;
import static cn.rongcloud.voiceroom.room.NewVoiceRoomPresenter.STATUS_ON_SEAT;
import static cn.rongcloud.voiceroom.room.NewVoiceRoomPresenter.STATUS_WAIT_FOR_SEAT;

import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

import cn.rong.combusis.manager.RCChatRoomMessageManager;
import cn.rong.combusis.message.RCChatroomEnter;
import cn.rong.combusis.message.RCChatroomLocationMessage;
import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.provider.voiceroom.RoomOwnerType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.provider.voiceroom.VoiceRoomProvider;
import cn.rong.combusis.ui.room.AbsRoomFragment;
import cn.rong.combusis.ui.room.RoomMessageAdapter;
import cn.rong.combusis.ui.room.fragment.MemberSettingFragment;
import cn.rong.combusis.ui.room.widget.RoomBottomView;
import cn.rong.combusis.ui.room.widget.RoomSeatView;
import cn.rong.combusis.ui.room.widget.RoomTitleBar;
import cn.rongcloud.voiceroom.R;
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomCallback;
import cn.rongcloud.voiceroom.model.RCVoiceSeatInfo;
import cn.rongcloud.voiceroom.net.bean.respond.Member;
import cn.rongcloud.voiceroom.room.adapter.NewVoiceRoomSeatsAdapter;
import cn.rongcloud.voiceroom.room.dialogFragment.seatoperation.SeatOperationViewPagerFragment;
import cn.rongcloud.voiceroom.ui.uimodel.UiRoomModel;
import cn.rongcloud.voiceroom.ui.uimodel.UiSeatModel;
import io.rong.imlib.model.MessageContent;

/**
 * @author 李浩
 * @date 2021/9/24
 */
public class NewVoiceRoomFragment extends AbsRoomFragment<VoiceRoomBean, NewVoiceRoomPresenter>
        implements IVoiceRoomFragmentView, RoomMessageAdapter.OnClickMessageUserListener, RoomBottomView.OnBottomOptionClickListener {
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

    public static Fragment getInstance() {
        return new NewVoiceRoomFragment();
    }

    @Override
    public int setLayoutId() {
        return R.layout.fragment_new_voice_room;
    }

    @Override
    public void init() {
        // 头部
        mRoomTitleBar = getView(R.id.room_title_bar);
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

        });
        mNoticeView = getView(R.id.tv_notice);
        mNoticeView.setOnClickListener(v -> {
            // TODO:公告弹框
            Logger.e("click notice");
        });
        // 背景
        mBackgroundImageView = getView(R.id.iv_background);
        // 房主座位
        mRoomSeatView = getView(R.id.room_seat_view);
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
     * 麦位被点击的情况
     *
     * @param seatModel
     * @param position
     */
    private void onClickVoiceRoomSeats(UiSeatModel seatModel, int position) {
        switch (getRoomOwnerType()){
            case VOICE_OWNER://房主
                onClickVoiceRoomSeatsByOwner(seatModel,position);
                break;
            case VOICE_VIEWER://观众
                onClickVoiceRoomSeatsByViewer(seatModel,position);
                break;
        }

    }

    /**
     * 观众点击麦位的时候
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
                mMemberSettingFragment.setSeatPosition(position+1);
                mMemberSettingFragment.setMute(seatModel.isMute());
                mMemberSettingFragment.show(getChildFragmentManager(), user, mVoiceRoomBean.getCreateUserId());
            }
        }
    }

    /**
     * 房主点击麦位的时候
     * @param seatModel
     * @param position
     */
    private void onClickVoiceRoomSeatsByOwner(UiSeatModel seatModel, int position) {
        if (seatModel.getSeatStatus() == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty
            ||seatModel.getSeatStatus() == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusLocking) {
            //点击空座位或者锁定座位的时候，弹出弹窗
            present.enterSeatOwner(seatModel,position);
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
            mMemberSettingFragment.setSeatPosition(position+1);
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

    private void showMessage(MessageContent messageContent, boolean isRefresh) {
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
        // 加载背景
        ImageLoaderUtil.INSTANCE.loadImage(requireContext(), mBackgroundImageView, mVoiceRoomBean.getBackgroundUrl(), R.color.black);
        // 设置title数据
        mRoomTitleBar.setData(mVoiceRoomBean.getRoomName(), mVoiceRoomBean.getId());

        // 设置房主麦位信息
        mRoomSeatView.setData(mVoiceRoomBean.getCreateUserName(), mVoiceRoomBean.getCreateUserPortrait());
        // 设置底部按钮
        mRoomBottomView.setData(getRoomOwnerType(), this);
        // 设置消息列表数据
        mRoomMessageAdapter.setRoomCreateId(mVoiceRoomBean.getCreateUserId());
//        mRoomMessageAdapter.setData();
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
     */
    @Override
    public void prepareJoinRoom() {
        present.initListener();
    }

//    @Override
//    public void onSpeakingStateChanged(boolean isSpeaking) {
//        Logger.e("==============onSpeakingStateChanged: " + isSpeaking);
//        mRoomSeatView.setSpeaking(isSpeaking);
//    }
//
//    @Override
//    public void onMessageReceived(Message message) {
//        Logger.e("==============onMessageReceived: " + message.toString());
//        showMessage(message.getContent(), false);
//        if (message.getContent() instanceof RCChatroomGift || message.getContent() instanceof RCChatroomGiftAll) {
//
//        }
//    }


    @Override
    public void clickMessageUser(String userId) {

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

    }

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
    public void clickSendMessage(String message) {

    }

    @Override
    public void clickPrivateMessage() {

    }

    @Override
    public void clickSeatOrder() {
        //弹窗邀请弹窗 并且将申请的集合和可以被要求的传入
        present.showSeatOperationViewPagerFragment(0);
    }

    @Override
    public void clickSettings() {

    }

    @Override
    public void clickPk(boolean isInPk) {

    }

    @Override
    public void clickRequestSeat() {

    }

    @Override
    public void onSendGift() {

    }
}