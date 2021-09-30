package cn.rong.combusis.ui.room.fragment;

import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.FragmentManager;

import com.rongcloud.common.utils.AccountStore;
import com.rongcloud.common.utils.ImageLoaderUtil;

import cn.rong.combusis.R;
import cn.rong.combusis.common.base.BaseBottomSheetDialogFragment;
import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.provider.voiceroom.RoomOwnerType;
import cn.rong.combusis.sdk.event.wrapper.EToast;
import cn.rong.combusis.ui.room.model.MemberCache;
import de.hdodenhof.circleimageview.CircleImageView;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.model.Conversation;

/**
 * @author gyn
 * @date 2021/9/27
 */
public class MemberSettingFragment extends BaseBottomSheetDialogFragment {

    private Guideline mGlBg;
    private CircleImageView mIvMemberPortrait;
    private AppCompatTextView mTvMemberName;
    private AppCompatTextView mTvSeatPosition;
    private ConstraintLayout mClButtons;
    private AppCompatButton mBtnSendGift;
    private AppCompatButton mBtnSendMessage;
    private ConstraintLayout mClMemberSetting;
    private LinearLayout mLlInvitedSeat;
    private LinearLayout mLlKickSeat;
    private LinearLayout mLlCloseSeat;
    private LinearLayout mLlMuteSeat;
    private AppCompatImageView mIvMuteSeat;
    private AppCompatTextView mTvMuteSeat;
    private LinearLayout mLlKickRoom;
    private RelativeLayout mRlSettingAdmin;
    private AppCompatTextView mTvSettingAdmin;
    private AppCompatButton mBtnFollow;

    private RoomOwnerType mRoomOwnerType;
    private User member;
    private String roomUserId;
    private OnMemberSettingClickListener mOnMemberSettingClickListener;

    // 操作的用户是否在麦位上 TODO：@lihao 需要知道当前用户是否在麦位上
    boolean memberIsOnSeat = false;
    //麦位的位置
    int seatPosition = 1;
    //麦位的状态
    boolean isMute=true;

    public void setMute(boolean mute) {
        isMute = mute;
    }

    public void setSeatPosition(int seatPosition) {
        this.seatPosition = seatPosition;
    }


    public void setMemberIsOnSeat(boolean memberIsOnSeat) {
        this.memberIsOnSeat = memberIsOnSeat;
    }

    public MemberSettingFragment(RoomOwnerType roomOwnerType, OnMemberSettingClickListener onMemberSettingClickListener) {
        super(R.layout.fragment_member_setting);
        this.mRoomOwnerType = roomOwnerType;
        this.mOnMemberSettingClickListener = onMemberSettingClickListener;
    }

    @Override
    public void initView() {
        mGlBg = (Guideline) getView().findViewById(R.id.gl_bg);
        mIvMemberPortrait = (CircleImageView) getView().findViewById(R.id.iv_member_portrait);
        mTvMemberName = (AppCompatTextView) getView().findViewById(R.id.tv_member_name);
        mTvSeatPosition = (AppCompatTextView) getView().findViewById(R.id.tv_seat_position);
        mClButtons = (ConstraintLayout) getView().findViewById(R.id.cl_buttons);
        mBtnSendGift = (AppCompatButton) getView().findViewById(R.id.btn_send_gift);
        mBtnSendMessage = (AppCompatButton) getView().findViewById(R.id.btn_send_message);
        mClMemberSetting = (ConstraintLayout) getView().findViewById(R.id.cl_member_setting);
        mLlInvitedSeat = (LinearLayout) getView().findViewById(R.id.ll_invited_seat);
        mLlKickSeat = (LinearLayout) getView().findViewById(R.id.ll_kick_seat);
        mLlCloseSeat = (LinearLayout) getView().findViewById(R.id.ll_close_seat);
        mLlMuteSeat = (LinearLayout) getView().findViewById(R.id.ll_mute_seat);
        mIvMuteSeat = (AppCompatImageView) getView().findViewById(R.id.iv_mute_seat);
        mTvMuteSeat = (AppCompatTextView) getView().findViewById(R.id.tv_mute_seat);
        mLlKickRoom = (LinearLayout) getView().findViewById(R.id.ll_kick_room);
        mRlSettingAdmin = (RelativeLayout) getView().findViewById(R.id.rl_setting_admin);
        mTvSettingAdmin = (AppCompatTextView) getView().findViewById(R.id.tv_setting_admin);
        mBtnFollow = (AppCompatButton) getView().findViewById(R.id.btn_follow);

        refreshView();
    }

    @Override
    public void initListener() {
        super.initListener();
        if (mOnMemberSettingClickListener == null) {
            return;
        }
        mRlSettingAdmin.setOnClickListener(v -> {
            mOnMemberSettingClickListener.clickSettingAdmin(member, (result, msg) -> {
                if (result) {
                    dismiss();
                } else {
                    EToast.showToast("设置失败");
                }
            });
        });
        mLlInvitedSeat.setOnClickListener(v -> {
            mOnMemberSettingClickListener.clickInviteSeat(member, (result, msg) -> {
                if (result) {
                    dismiss();
                    EToast.showToast("发送上麦通知成功");
                } else {
                    EToast.showToast(msg);
                }
            });
        });
        mLlKickRoom.setOnClickListener(v -> {
            mOnMemberSettingClickListener.clickKickRoom(member, (result, msg) -> {
                if (result) {
                    dismiss();
                } else {
                    EToast.showToast(msg);
                }
            });
        });
        mLlKickSeat.setOnClickListener(v -> {
            mOnMemberSettingClickListener.clickKickSeat(member, (result, msg) -> {
                if (result) {
                    dismiss();
                    EToast.showToast("发送下麦通知成功");
                } else {
                    EToast.showToast(msg);
                }
            });
        });
        mLlMuteSeat.setOnClickListener(v -> {
            mOnMemberSettingClickListener.clickMuteSeat(member, (result, msg) -> {
                if (result) {
                    dismiss();
                } else {
                    EToast.showToast(msg);
                }
            });
        });
        mLlCloseSeat.setOnClickListener(v -> {
            mOnMemberSettingClickListener.clickCloseSeat(member, (result, msg) -> {
                if (result) {
                    dismiss();
                    EToast.showToast("座位已关闭");
                } else {
                    EToast.showToast(msg);
                }
            });
        });
        mBtnSendGift.setOnClickListener(v -> {
            mOnMemberSettingClickListener.clickSendGift(member);
            dismiss();
        });
        mBtnSendMessage.setOnClickListener(v -> {
            dismiss();
            RouteUtils.routeToConversationActivity(
                    requireContext(),
                    Conversation.ConversationType.PRIVATE,
                    member.getUserId()
            );
        });
        mBtnFollow.setOnClickListener(v -> {

        });
    }

    public void show(FragmentManager fragmentManager, User member, String roomUserId) {
        this.member = member;
        this.roomUserId = roomUserId;
        show(fragmentManager);
    }

    private void refreshView() {

        // 自己是否是管理员
        boolean selfIsAdmin = MemberCache.getInstance().isAdmin(AccountStore.INSTANCE.getUserId());
        // 操作的用户是否是管理员
        boolean memberIsAdmin = MemberCache.getInstance().isAdmin(member.getUserId());
        // 操作的用户是否是房主
        boolean memberIsOwner = TextUtils.equals(roomUserId, member.getUserId());

        // 头像和昵称
        ImageLoaderUtil.INSTANCE.loadImage(getContext(), mIvMemberPortrait, member.getPortrait(), R.drawable.default_portrait);
        mTvMemberName.setText(member.getUserName());
        // 麦位信息显示
        setSeatPosition(memberIsOnSeat);
        // 设置管理员按钮
        refreshSettingAdmin(memberIsAdmin);
        // 设置底部操作按钮view展示
        refreshBottomView(selfIsAdmin, memberIsAdmin, memberIsOnSeat, memberIsOwner);
    }

    private void refreshBottomView(boolean selfIsAdmin, boolean memberIsAdmin, boolean memberIsOnSeat, boolean memberIsOwner) {
        switch (mRoomOwnerType) {
            case VOICE_OWNER:
                mClMemberSetting.setVisibility(View.VISIBLE);
                // 上下麦
                if (memberIsOnSeat) {
                    mLlKickSeat.setVisibility(View.VISIBLE);
                    mLlInvitedSeat.setVisibility(View.GONE);
                    mLlMuteSeat.setVisibility(View.VISIBLE);
                    mLlCloseSeat.setVisibility(View.VISIBLE);
                    //根据麦位状态
                    if (isMute){
                        mIvMuteSeat.setImageResource(R.drawable.ic_room_setting_unmute_all);
                        mTvMuteSeat.setText("取消禁麦");
                    }else {
                        mIvMuteSeat.setImageResource(R.drawable.ic_member_setting_mute_seat);
                        mTvMuteSeat.setText("座位禁麦");
                    }
                } else {
                    mLlKickSeat.setVisibility(View.GONE);
                    mLlInvitedSeat.setVisibility(View.VISIBLE);
                    mLlMuteSeat.setVisibility(View.GONE);
                    mLlCloseSeat.setVisibility(View.GONE);
                }
                // 可以踢人
                mLlKickRoom.setVisibility(View.VISIBLE);
                break;
            case VOICE_VIEWER:
                // 自己和对方都是管理,或对方是房主,不显示底部操作
                if (selfIsAdmin && memberIsAdmin || memberIsOwner) {
                    mClMemberSetting.setVisibility(View.GONE);
                } else if (selfIsAdmin) { // 自己是管理，对方是普通用户
                    mClMemberSetting.setVisibility(View.VISIBLE);
                    // 不能禁麦和关座位
                    mLlMuteSeat.setVisibility(View.GONE);
                    mLlCloseSeat.setVisibility(View.GONE);
                    // 可以踢人
                    mLlKickRoom.setVisibility(View.VISIBLE);
                    // 可以上下麦
                    if (memberIsOnSeat) {
                        mLlKickSeat.setVisibility(View.VISIBLE);
                        mLlInvitedSeat.setVisibility(View.GONE);
                    } else {
                        mLlKickSeat.setVisibility(View.GONE);
                        mLlInvitedSeat.setVisibility(View.VISIBLE);
                    }
                } else {
                    // 自己是普通用户不能操作
                    mClMemberSetting.setVisibility(View.GONE);
                }
                break;
            case RADIO_OWNER:
                mClMemberSetting.setVisibility(View.VISIBLE);
                // 电台不能上下麦
                mLlKickSeat.setVisibility(View.GONE);
                mLlInvitedSeat.setVisibility(View.GONE);
                mLlMuteSeat.setVisibility(View.GONE);
                mLlCloseSeat.setVisibility(View.GONE);
                // 可以踢人
                mLlKickRoom.setVisibility(View.VISIBLE);
                break;
            case RADIO_VIEWER:
                // 自己和对方都是管理,或对方是房主,不显示底部操作
                if (selfIsAdmin && memberIsAdmin || memberIsOwner) {
                    mClMemberSetting.setVisibility(View.GONE);
                } else if (selfIsAdmin) { // 自己是管理，对方是普通用户
                    mClMemberSetting.setVisibility(View.VISIBLE);
                    // 不能禁麦和关座位
                    mLlMuteSeat.setVisibility(View.GONE);
                    mLlCloseSeat.setVisibility(View.GONE);
                    // 可以踢人
                    mLlKickRoom.setVisibility(View.VISIBLE);
                    // 语音房没有上下麦
                    mLlKickSeat.setVisibility(View.GONE);
                    mLlInvitedSeat.setVisibility(View.GONE);
                } else {
                    // 自己是普通用户不能操作
                    mClMemberSetting.setVisibility(View.GONE);
                }
                break;
        }
    }

    private void refreshSettingAdmin(boolean memberIsAdmin) {
        switch (mRoomOwnerType) {
            case VOICE_OWNER:
            case RADIO_OWNER:
                mRlSettingAdmin.setVisibility(View.VISIBLE);
                if (memberIsAdmin) {
                    mTvSettingAdmin.setText("撤回管理");
                    mTvSettingAdmin.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_member_setting_is_admin,
                            0,
                            0,
                            0
                    );
                } else {
                    mTvSettingAdmin.setText("设为管理");
                    mTvSettingAdmin.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_member_setting_not_admin,
                            0,
                            0,
                            0
                    );
                }
                break;
            default:
                mRlSettingAdmin.setVisibility(View.GONE);
                break;
        }
    }


    private void setSeatPosition(boolean memberIsOnSeat) {
        if (memberIsOnSeat) {
            mTvSeatPosition.setVisibility(View.VISIBLE);
            // TODO：这里需要麦位位置

            mTvSeatPosition.setText(String.format("%s 号麦位", seatPosition));
        } else {
            mTvSeatPosition.setVisibility(View.GONE);
        }
    }

    public interface OnMemberSettingClickListener {
        /**
         * 设置管理员
         *
         * @param user
         * @param callback
         */
        void clickSettingAdmin(User user, ClickCallback<Boolean> callback);

        /**
         * 邀请上麦
         *
         * @param user
         * @param callback
         */
        void clickInviteSeat(User user, ClickCallback<Boolean> callback);

        /**
         * 踢出房间
         *
         * @param user
         * @param callback
         */
        void clickKickRoom(User user, ClickCallback<Boolean> callback);

        /**
         * 下麦
         *
         * @param user
         * @param callback
         */
        void clickKickSeat(User user, ClickCallback<Boolean> callback);

        /**
         * 静麦
         *
         * @param user
         * @param callback
         */
        void clickMuteSeat(User user, ClickCallback<Boolean> callback);

        /**
         * 关闭座位
         *
         * @param user
         * @param callback
         */
        void clickCloseSeat(User user, ClickCallback<Boolean> callback);

        /**
         * 发送礼物
         *
         * @param user
         */
        void clickSendGift(User user);
    }
}
