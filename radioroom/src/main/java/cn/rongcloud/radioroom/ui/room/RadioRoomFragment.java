package cn.rongcloud.radioroom.ui.room;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rongcloud.common.utils.AccountStore;
import com.rongcloud.common.utils.ImageLoaderUtil;
import com.rongcloud.common.utils.UiUtils;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.common.ui.dialog.EditDialog;
import cn.rong.combusis.common.ui.dialog.InputPasswordDialog;
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
import cn.rong.combusis.ui.room.widget.RoomBottomView;
import cn.rong.combusis.ui.room.widget.RoomSeatView;
import cn.rong.combusis.ui.room.widget.RoomTitleBar;
import cn.rongcloud.radioroom.R;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MessageContent;

/**
 * @author gyn
 * @date 2021/9/17
 */
public class RadioRoomFragment extends AbsRoomFragment<VoiceRoomBean, RadioRoomPresenter> implements
        RoomMessageAdapter.OnClickMessageUserListener, RadioRoomView, RoomBottomView.OnBottomOptionClickListener,
        MemberListFragment.OnClickUserListener {
    private ImageView mBackgroundImageView;
    private RoomTitleBar mRoomTitleBar;
    private TextView mNoticeView;
    private RoomSeatView mRoomSeatView;
    private RoomBottomView mRoomBottomView;
    private RecyclerView mMessageView;
    private View mCoverView;

    private RoomMessageAdapter mRoomMessageAdapter;
    private ExitRoomPopupWindow mExitRoomPopupWindow;
    private RoomNoticeDialog mNoticeDialog;
    private MemberListFragment mMemberListFragment;
    private MemberSettingFragment mMemberSettingFragment;
    private RoomSettingFragment mRoomSettingFragment;
    private InputPasswordDialog mInputPasswordDialog;
    private EditDialog mEditDialog;
    private BackgroundSettingFragment mBackgroundSettingFragment;
    private ShieldDialog mShieldDialog;

    public static Fragment getInstance() {
        return new RadioRoomFragment();
    }

    @Override
    public RadioRoomPresenter createPresent() {
        return new RadioRoomPresenter(this, getViewLifecycleOwner());
    }

    @Override
    public int setLayoutId() {
        return R.layout.fragment_radio_room;
    }

    @Override
    public void init() {
        mNoticeDialog = new RoomNoticeDialog(getContext());
        mRoomSettingFragment = new RoomSettingFragment(present);

        // 头部
        mRoomTitleBar = getView(R.id.room_title_bar);
        mRoomTitleBar.setOnMenuClickListener(v -> {
            clickMenu();
        });
        mRoomTitleBar.setOnMemberClickListener(v -> {
            mMemberListFragment = new MemberListFragment(present.getRoomId(), this);
            mMemberListFragment.show(getChildFragmentManager());
        });
        mNoticeView = getView(R.id.tv_notice);
        mNoticeView.setOnClickListener(v -> {
            present.getNotice(false);
        });
        // 背景
        mBackgroundImageView = getView(R.id.iv_background);
        // 房主座位
        mRoomSeatView = getView(R.id.room_seat_view);
        mRoomSeatView.setResumeLiveClickListener(v -> {
            present.enterSeat();
        });
        // 底部操作按钮和双击送礼物
        mRoomBottomView = getView(R.id.room_bottom_view);
        // 弹幕消息列表
        mMessageView = getView(R.id.rv_message);
        mMessageView.setLayoutManager(new LinearLayoutManager(getContext()));
        mMessageView.addItemDecoration(new DefaultItemDecoration(Color.TRANSPARENT, 0, UiUtils.INSTANCE.dp2Px(getContext(), 5)));
        mRoomMessageAdapter = new RoomMessageAdapter(getContext(), this);
        mMessageView.setAdapter(mRoomMessageAdapter);

        mCoverView = getView(R.id.view_cover);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void joinRoom(VoiceRoomBean voiceRoomBean) {
        present.joinRoom(voiceRoomBean);
    }

    /**
     * 设置房间数据
     *
     * @param voiceRoomBean
     */
    @Override
    public void setRoomData(VoiceRoomBean voiceRoomBean) {
        // 设置房间类型
        RoomOwnerType roomOwnerType = VoiceRoomProvider.provider().getRoomOwnerType(voiceRoomBean);
        setRoomOwnerType(roomOwnerType);
        // 房主上麦
        if (roomOwnerType == RoomOwnerType.RADIO_OWNER) {
            present.enterSeat();
        }
        // 加载背景
        setRoomBackground(voiceRoomBean.getBackgroundUrl());
        // 设置title数据
        mRoomTitleBar.setData(voiceRoomBean.getRoomName(), voiceRoomBean.getId());
        mRoomTitleBar.setDelay(0, false);
        // 设置房主麦位信息
        mRoomSeatView.setData(voiceRoomBean.getCreateUserName(), voiceRoomBean.getCreateUserPortrait());
        // 设置底部按钮
        mRoomBottomView.setData(getRoomOwnerType(), this);
        // 设置消息列表数据
        mRoomMessageAdapter.setRoomCreateId(voiceRoomBean.getCreateUserId());
    }

    @Override
    public void onBackPressed() {
        clickMenu();
    }

    @Override
    public void addToMessageList(MessageContent messageContent, boolean isRefresh) {
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
    public void clearInput() {
        mRoomBottomView.clearInput();
    }

    @Override
    public void finish() {
        requireActivity().finish();
    }

    @Override
    public void setSpeaking(boolean speaking) {
        mRoomSeatView.setSpeaking(speaking);
    }

    @Override
    public void setRadioName(String name) {
        mRoomTitleBar.setRoomName(name);
    }

    @Override
    public void showNotice(String notice, boolean isModify) {
        mNoticeDialog.show(notice, isModify, newNotice -> {
            present.modifyNotice(newNotice);
        });
    }

    @Override
    public void setSeatState(RoomSeatView.SeatState seatState) {
        mRoomSeatView.refreshSeatState(seatState);
        if (seatState == RoomSeatView.SeatState.OWNER_PAUSE) {
            mCoverView.setVisibility(View.VISIBLE);
        } else {
            mCoverView.setVisibility(View.GONE);
        }
    }

    @Override
    public void showSettingDialog(List<MutableLiveData<IFun.BaseFun>> funList) {
        mRoomSettingFragment.show(getChildFragmentManager(), funList);
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

    @Override
    public void showSelectBackgroundDialog(String url) {
        mBackgroundSettingFragment = new BackgroundSettingFragment(url, present);
        mBackgroundSettingFragment.show(getChildFragmentManager());
    }

    @Override
    public void setRoomBackground(String url) {
        ImageLoaderUtil.INSTANCE.loadImage(requireContext(), mBackgroundImageView, url, R.color.black);
    }

    @Override
    public void showShieldDialog(String roomId) {
        mShieldDialog = new ShieldDialog(requireActivity(), roomId, 10);
        mShieldDialog.show();
    }

    /**
     * 点击右上角菜单按钮
     */
    private void clickMenu() {
        mExitRoomPopupWindow = new ExitRoomPopupWindow(getContext(), getRoomOwnerType(), new ExitRoomPopupWindow.OnOptionClick() {
            @Override
            public void clickPackRoom() {
                finish();
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

    @Override
    public void setOnlineCount(int num) {
        mRoomTitleBar.setOnlineNum(num);
    }


    @Override
    public void destroyRoom() {

    }

    @Override
    public void prepareJoinRoom() {

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
    public void clickSendMessage(String message) {
        present.sendMessage(message);
    }

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

    }

    @Override
    public void clickSettings() {
        present.showSettingDialog();
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

    @Override
    public void clickUser(User user) {
        if (TextUtils.equals(user.getUserId(), AccountStore.INSTANCE.getUserId())) {
            return;
        }
        if (mMemberSettingFragment == null) {
            mMemberSettingFragment = new MemberSettingFragment(getRoomOwnerType(), present);
        }
        mMemberSettingFragment.show(getChildFragmentManager(), user, present.getCreateUserId());
    }
}
