package cn.rongcloud.liveroom.room;


import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.meihu.beautylibrary.bean.MHConfigConstants;
import com.rongcloud.common.utils.AccountStore;
import com.rongcloud.common.utils.UiUtils;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.rong.combusis.common.ui.dialog.EditDialog;
import cn.rong.combusis.common.ui.dialog.InputPasswordDialog;
import cn.rong.combusis.intent.IntentWrap;
import cn.rong.combusis.message.RCAllBroadcastMessage;
import cn.rong.combusis.message.RCChatroomBarrage;
import cn.rong.combusis.message.RCChatroomVoice;
import cn.rong.combusis.music.MusicDialog;
import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.provider.voiceroom.CurrentStatusType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.sdk.event.wrapper.EToast;
import cn.rong.combusis.ui.OnItemClickListener;
import cn.rong.combusis.ui.beauty.BeautyDialogFragment;
import cn.rong.combusis.ui.room.AbsRoomActivity;
import cn.rong.combusis.ui.room.AbsRoomFragment;
import cn.rong.combusis.ui.room.RoomMessageAdapter;
import cn.rong.combusis.ui.room.dialog.RoomNoticeDialog;
import cn.rong.combusis.ui.room.dialog.shield.ShieldDialog;
import cn.rong.combusis.ui.room.fragment.MemberListFragment;
import cn.rong.combusis.ui.room.fragment.roomsetting.IFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomBeautyFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomBeautyMakeUpFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomLockFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomMusicFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomNameFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomNoticeFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomOverTurnFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomSeatModeFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomSettingFragment;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomShieldFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomSpecialEffectsFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomTagsFun;
import cn.rong.combusis.ui.room.fragment.roomsetting.RoomVideoSetFun;
import cn.rong.combusis.ui.room.widget.AllBroadcastView;
import cn.rong.combusis.ui.room.widget.GiftAnimationView;
import cn.rong.combusis.ui.room.widget.RecyclerViewAtVP2;
import cn.rong.combusis.ui.room.widget.RoomBottomView;
import cn.rong.combusis.ui.room.widget.RoomTitleBar;
import cn.rongcloud.liveroom.R;
import cn.rongcloud.liveroom.helper.LiveEventHelper;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imkit.utils.StatusBarUtil;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MessageContent;

/**
 * 直播房界面
 */
public class LiveRoomFragment extends AbsRoomFragment<LiveRoomPresenter>
        implements LiveRoomView, CreateLiveRoomFragment.CreateRoomCallBack,
        RoomBottomView.OnBottomOptionClickListener, MemberListFragment.OnClickUserListener
        , View.OnClickListener, AllBroadcastView.OnClickBroadcast,
        OnItemClickListener<MutableLiveData<IFun.BaseFun>>, RoomMessageAdapter.OnClickMessageUserListener {


    private FrameLayout flLiveView;
    private GiftAnimationView giftView;
    private ConstraintLayout clLiveRoomView;
    private RoomTitleBar roomTitleBar;
    private TextView tvNotice;
    private RecyclerViewAtVP2 rvMessage;
    private AllBroadcastView viewAllBroadcast;
    private RoomBottomView roomBottomView;
    private RelativeLayout rlRoomFinishedId;
    private Button btnGoBackList;
    private FrameLayout flCreateLiveRoom;
    private boolean isCreate;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private CreateLiveRoomFragment createLiveRoomFragment;
    private RoomSettingFragment mRoomSettingFragment;
    private RoomNoticeDialog mNoticeDialog;
    private InputPasswordDialog mInputPasswordDialog;
    private EditDialog setRoomNameDialog;
    private String mRoomId;
    private ShieldDialog mShieldDialog;
    private MusicDialog mMusicDialog;
    private RoomMessageAdapter mRoomMessageAdapter;
    private String roomName;//当前房间名称
    private BeautyDialogFragment tiezhiDilog;
    private BeautyDialogFragment meiyanDialog;
    private BeautyDialogFragment meizhuangDialog;
    private BeautyDialogFragment texiaoDialog;

    public static Fragment getInstance(String roomId, boolean isCreate) {
        Bundle bundle = new Bundle();
        bundle.putString(ROOM_ID, roomId);
        bundle.putBoolean(IntentWrap.KEY_IS_CREATE, isCreate);
        LiveRoomFragment fragment = new LiveRoomFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public LiveRoomPresenter createPresent() {
        return new LiveRoomPresenter(this, this.getLifecycle());
    }

    @Override
    public int setLayoutId() {
        return R.layout.fragment_live_room;
    }

    @Override
    public void init() {
        initView();
        initData();
        initDialog();
        fragmentManager = getActivity().getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if (isCreate) {
            clLiveRoomView.setVisibility(View.INVISIBLE);
            createLiveRoomFragment = CreateLiveRoomFragment.getInstance();
            createLiveRoomFragment.setCreateRoomCallBack(this);
            fragmentTransaction.replace(R.id.fl_create_live_room, createLiveRoomFragment);
            fragmentTransaction.commit();
        }
    }

    private void initData() {
        isCreate = getArguments().getBoolean(IntentWrap.KEY_IS_CREATE);
        mRoomId = getArguments().getString(ROOM_ID);
    }

    /**
     * 构建部分dialog
     */
    private void initDialog() {
        if (null == mNoticeDialog) {
            mNoticeDialog = new RoomNoticeDialog(activity);
        }
    }

    @Override
    public void onSoftKeyboardChange(int height) {
        roomBottomView.setPadding(0, 0, 0, height);
    }

    /**
     * 加入房间，初次进入和切换房间的时候
     */
    @Override
    public void joinRoom() {
        //如果当前不是创建房间，就继续执行，如果是创建房间,那么就创建成功了以后去执行
        if (!isCreate) {
            present.init(mRoomId, isCreate);
            viewAllBroadcast.setBroadcastListener();
        }
    }

    @Override
    public void onBackPressed() {
        requireActivity().finish();
    }

    @Override
    public void addSwitchRoomListener() {
        ((AbsRoomActivity) requireActivity()).addSwitchRoomListener(mRoomId, this);
    }

    @Override
    public void removeSwitchRoomListener() {
        ((AbsRoomActivity) requireActivity()).removeSwitchRoomListener(mRoomId);
    }

    @Override
    public void preJoinRoom() {
        super.preJoinRoom();
    }

    @Override
    public void destroyRoom() {
        super.destroyRoom();
        present.unInitLiveRoomListener();
    }

    @Override
    public void initListener() {
        super.initListener();
        btnGoBackList.setOnClickListener(this::onClick);
    }

    private void initView() {
        flLiveView = (FrameLayout) getView().findViewById(R.id.fl_live_view);
        giftView = (GiftAnimationView) getView().findViewById(R.id.gift_view);
        clLiveRoomView = (ConstraintLayout) getView().findViewById(R.id.cl_live_room_view);
        roomTitleBar = (RoomTitleBar) getView().findViewById(R.id.room_title_bar);
        tvNotice = (TextView) getView().findViewById(R.id.tv_notice);

        viewAllBroadcast = (AllBroadcastView) getView().findViewById(R.id.view_all_broadcast);
        roomBottomView = (RoomBottomView) getView().findViewById(R.id.room_bottom_view);
        rlRoomFinishedId = (RelativeLayout) getView().findViewById(R.id.rl_room_finished_id);
        btnGoBackList = (Button) getView().findViewById(R.id.btn_go_back_list);
        flCreateLiveRoom = (FrameLayout) getView().findViewById(R.id.fl_create_live_room);
        clLiveRoomView.setPadding(0, StatusBarUtil.getStatusBarHeight(requireContext()), 0, 0);

        //弹幕消息列表
        rvMessage = (RecyclerViewAtVP2) getView().findViewById(R.id.rv_message);
        rvMessage.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMessage.addItemDecoration(new DefaultItemDecoration(Color.TRANSPARENT, 0, UiUtils.INSTANCE.dp2Px(getContext(), 5)));
        mRoomMessageAdapter = new RoomMessageAdapter(getContext(), this);
        rvMessage.setAdapter(mRoomMessageAdapter);
    }

    /**
     * 创建房间成功
     *
     * @param voiceRoomBean
     */
    @Override
    public void onCreateSuccess(VoiceRoomBean voiceRoomBean) {
        if (createLiveRoomFragment != null) {
            fragmentTransaction.remove(createLiveRoomFragment);
        }
        mRoomId = voiceRoomBean.getRoomId();
        clLiveRoomView.setVisibility(View.VISIBLE);
        present.init(mRoomId, isCreate);
        viewAllBroadcast.setBroadcastListener();
    }

    /**
     * 房间已经存在
     *
     * @param voiceRoomBean
     */
    @Override
    public void onCreateExist(VoiceRoomBean voiceRoomBean) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_go_back_list) {
            requireActivity().finish();
        }
    }

    /**
     * 点击用户
     *
     * @param user
     */
    @Override
    public void clickUser(User user) {

    }

    /**
     * 发送全服广播
     *
     * @param message
     */
    @Override
    public void clickBroadcast(RCAllBroadcastMessage message) {

    }

    /**
     * 发送消息
     *
     * @param message
     */
    @Override
    public void clickSendMessage(String message) {
        //发送文字消息
        RCChatroomBarrage barrage = new RCChatroomBarrage();
        barrage.setContent(message);
        barrage.setUserId(AccountStore.INSTANCE.getUserId());
        barrage.setUserName(AccountStore.INSTANCE.getUserName());
        present.sendMessage(barrage);
    }

    /**
     * 点击私信
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
        showSeatOperationViewPagerFragment(0);
    }

    /**
     * 显示申请列表弹窗
     *
     * @param i
     */
    private void showSeatOperationViewPagerFragment(int i) {
//        SeatOperationViewPagerFragment seatOperationViewPagerFragment = new SeatOperationViewPagerFragment(voiceRoomModel, index);
//        seatOperationViewPagerFragment.show(((VoiceRoomFragment) mView).getChildFragmentManager());
    }

    /**
     * 点击设置
     */
    @Override
    public void clickSettings() {
        List<MutableLiveData<IFun.BaseFun>> funList = Arrays.asList(
                new MutableLiveData<>(new RoomLockFun(true ? 1 : 0)),
                new MutableLiveData<>(new RoomNameFun(0)),
                new MutableLiveData<>(new RoomNoticeFun(0)),
                new MutableLiveData<>(new RoomShieldFun(0)),
                new MutableLiveData<>(new RoomOverTurnFun(0)),
                new MutableLiveData<>(new RoomTagsFun(0)),
                new MutableLiveData<>(new RoomBeautyFun(0)),
                new MutableLiveData<>(new RoomBeautyMakeUpFun(0)),
                new MutableLiveData<>(new RoomSeatModeFun(0)),
                new MutableLiveData<>(new RoomSpecialEffectsFun(0)),
                new MutableLiveData<>(new RoomMusicFun(0)),
                new MutableLiveData<>(new RoomVideoSetFun(0))
        );
        if (mRoomSettingFragment == null) {
            mRoomSettingFragment = new RoomSettingFragment(this);
        }
        mRoomSettingFragment.show(getChildFragmentManager(), funList);
    }

    /**
     * PK
     */
    @Override
    public void clickPk() {

    }

    /**
     * 点击申请连麦
     */
    @Override
    public void clickRequestSeat() {
        present.requestSeat(-1);
    }

    /**
     * 发送礼物
     */
    @Override
    public void onSendGift() {
        present.sendGift();
    }

    /**
     * 发送语音消息
     *
     * @param rcChatroomVoice
     */
    @Override
    public void onSendVoiceMessage(RCChatroomVoice rcChatroomVoice) {
        present.sendMessage(rcChatroomVoice);
    }

    @Override
    public void clickItem(MutableLiveData<IFun.BaseFun> item, int position) {
        IFun.BaseFun fun = item.getValue();
        if (fun instanceof RoomNoticeFun) {
            showNoticeDialog(true);
        } else if (fun instanceof RoomLockFun) {
            if (fun.getStatus() == 1) {
                present.setRoomPassword(false, "", item, mRoomId);
            } else {
                showSetPasswordDialog(item);
            }
        } else if (fun instanceof RoomNameFun) {
            showSetRoomNameDialog();
        } else if (fun instanceof RoomShieldFun) {
            showShieldDialog();
        } else if (fun instanceof RoomSeatModeFun) {
            if (fun.getStatus() == 1) {
                //申请上麦
                present.setSeatMode(false);
            } else {
                //自由上麦
                present.setSeatMode(true);
            }
        } else if (fun instanceof RoomMusicFun) {
            //音乐 判断房主是否在麦位上
//            UiSeatModel seatInfoByUserId = voiceRoomModel.getSeatInfoByUserId(AccountStore.INSTANCE.getUserId());
//            if (seatInfoByUserId != null && seatInfoByUserId.getSeatStatus() == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusUsing) {
//                //在座位上，可以播放音乐
//                showMusicDialog();
//            } else {
//                EToast.showToast("请先上麦之后再播放音乐");
//            }
        } else if (fun instanceof RoomOverTurnFun) {

        } else if (fun instanceof RoomBeautyFun) {
            if (meiyanDialog == null)
                meiyanDialog = new BeautyDialogFragment(requireActivity(), MHConfigConstants.MEI_YAN);
            meiyanDialog.show();
        } else if (fun instanceof RoomBeautyMakeUpFun) {
            if (meizhuangDialog == null)
                meizhuangDialog = new BeautyDialogFragment(requireActivity(), MHConfigConstants.MEI_ZHUANG);
            meizhuangDialog.show();
        } else if (fun instanceof RoomTagsFun) {
            if (tiezhiDilog == null)
                tiezhiDilog = new BeautyDialogFragment(requireActivity(), MHConfigConstants.TIE_ZHI);
            tiezhiDilog.show();
        } else if (fun instanceof RoomSpecialEffectsFun) {
            if (texiaoDialog == null)
                texiaoDialog = new BeautyDialogFragment(requireActivity(), MHConfigConstants.TE_XIAO);
            texiaoDialog.show();
        } else if (fun instanceof RoomVideoSetFun) {

        }
    }

    /**
     * 显示公告弹窗
     *
     * @param isEdit
     */
    public void showNoticeDialog(boolean isEdit) {
        mNoticeDialog.show("", isEdit, new RoomNoticeDialog.OnSaveNoticeListener() {
            @Override
            public void saveNotice(String notice) {
                //修改公告信息
                present.modifyNotice(notice);
            }
        });
    }

    /**
     * 显示密码设置弹窗
     *
     * @param item
     */
    public void showSetPasswordDialog(MutableLiveData<IFun.BaseFun> item) {
        if (mInputPasswordDialog == null)
            mInputPasswordDialog = new InputPasswordDialog(getContext(), false, () -> null, s -> {
                if (TextUtils.isEmpty(s)) {
                    return null;
                }
                if (s.length() < 4) {
                    showToast(getString(R.string.text_please_input_four_number));
                    return null;
                }
                mInputPasswordDialog.dismiss();
                present.setRoomPassword(true, s, item, mRoomId);
                return null;
            });
        mInputPasswordDialog.show();
    }

    /**
     * 弹出房间名称弹窗
     */
    public void showSetRoomNameDialog() {
        setRoomNameDialog = new EditDialog(
                requireActivity(),
                "修改房间标题",
                "请输入房间名",
                roomName,
                10,
                false,
                () -> null,
                newName -> {
                    if (TextUtils.isEmpty(newName)) {
                        EToast.showToast("房间名称不能为空");
                        return null;
                    }
                    present.setRoomName(newName, mRoomId);
                    setRoomNameDialog.dismiss();
                    return null;
                }
        );
        setRoomNameDialog.show();
    }

    /**
     * 显示屏蔽词弹窗
     */
    public void showShieldDialog() {
        if (mShieldDialog == null)
            mShieldDialog = new ShieldDialog(requireActivity(), mRoomId, 10);
        mShieldDialog.show();
    }

    /**
     * 显示音乐弹窗
     */
    public void showMusicDialog() {
        if (mShieldDialog == null)
            mMusicDialog = new MusicDialog(mRoomId);
        mMusicDialog.show(getChildFragmentManager());
    }

    /**
     * 当前房间已结束
     */
    @Override
    public void showFinishView() {
        clLiveRoomView.setVisibility(View.INVISIBLE);
        rlRoomFinishedId.setVisibility(View.VISIBLE);
    }

    /**
     * 设置房间的数据
     *
     * @param voiceRoomBean
     */
    @Override
    public void setRoomData(VoiceRoomBean voiceRoomBean) {
        clLiveRoomView.setVisibility(View.VISIBLE);
        rlRoomFinishedId.setVisibility(View.GONE);

        // 设置title数据
        roomTitleBar.setData(voiceRoomBean.getRoomName(), voiceRoomBean.getId(), voiceRoomBean.getUserId(), present);

        // 设置底部按钮
        roomBottomView.setData(present.getRoomOwnerType(), this, voiceRoomBean.getRoomId());
        // 设置消息列表数据
        mRoomMessageAdapter.setRoomCreateId(voiceRoomBean.getCreateUserId());
    }


    @Override
    public void setTitleFollow(boolean isFollow) {
        roomTitleBar.setFollow(isFollow);
    }


    /**
     * 添加消息到公屏上
     *
     * @param messageContent 消息
     * @param isReset        是否重置消息 false 代表不重置，仅仅添加 true 代表清空之前消息，重置消息
     */
    @Override
    public void addMessageContent(MessageContent messageContent, boolean isReset) {
        List<MessageContent> list = new ArrayList<>(1);
        if (messageContent != null) {
            list.add(messageContent);
        }
        mRoomMessageAdapter.setData(list, isReset);
        int count = mRoomMessageAdapter.getItemCount();
        if (count > 0) {
            rvMessage.smoothScrollToPosition(count - 1);
        }
    }

    @Override
    public void addMessageList(List<MessageContent> messageContentList, boolean isReset) {
        mRoomMessageAdapter.setData(messageContentList, isReset);
        int count = mRoomMessageAdapter.getItemCount();
        if (count > 0) {
            rvMessage.smoothScrollToPosition(count - 1);
        }
    }

    @Override
    public void clearInput() {
        roomBottomView.clearInput();
    }

    @Override
    public void hideSoftKeyboardAndIntput() {
        roomBottomView.hideSoftKeyboardAndInput();
    }

    @Override
    public void showLikeAnimation() {
        giftView.showFov(roomBottomView.getGiftViewPoint());
    }

    @Override
    public void changeStatus(CurrentStatusType status) {
        LiveEventHelper.getInstance().setCurrentStatus(status);
        //修改底部的状态
        switch (status) {
            case STATUS_NOT_ON_SEAT:
                //申请中
                roomBottomView.setRequestSeatImage(R.drawable.ic_request_enter_seat);
                ((AbsRoomActivity) requireActivity()).setCanSwitch(true);
                break;
            case STATUS_WAIT_FOR_SEAT:
                //等待中
                roomBottomView.setRequestSeatImage(R.drawable.ic_wait_enter_seat);
                break;
            case STATUS_ON_SEAT:
                //已经在麦上
                roomBottomView.setRequestSeatImage(R.drawable.ic_on_seat);
                ((AbsRoomActivity) requireActivity()).setCanSwitch(false);
                break;
        }
    }

    @Override
    public void setRoomName(String roomName) {
        this.roomName = roomName;
        roomTitleBar.setRoomName(roomName);
    }

    @Override
    public void clickMessageUser(String userId) {

    }
}
