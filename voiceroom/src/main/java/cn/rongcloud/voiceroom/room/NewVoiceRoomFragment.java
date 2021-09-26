package cn.rongcloud.voiceroom.room;

import android.graphics.Color;
import android.graphics.Point;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import cn.rong.combusis.provider.voiceroom.RoomOwnerType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.provider.voiceroom.VoiceRoomProvider;
import cn.rong.combusis.ui.room.AbsRoomFragment;
import cn.rong.combusis.ui.room.RoomMessageAdapter;
import cn.rong.combusis.ui.room.widget.RoomBottomView;
import cn.rong.combusis.ui.room.widget.RoomSeatView;
import cn.rong.combusis.ui.room.widget.RoomTitleBar;
import cn.rongcloud.radioroom.RCRadioRoomCallback;
import cn.rongcloud.radioroom.RCRadioRoomEngine;
import cn.rongcloud.voiceroom.R;
import cn.rongcloud.voiceroom.ui.uimodel.UiRoomModel;
import cn.rongcloud.voiceroom.ui.uimodel.UiSeatModel;
import io.rong.imlib.model.MessageContent;

/**
 * @author 李浩
 * @date 2021/9/24
 */
public class NewVoiceRoomFragment extends AbsRoomFragment<VoiceRoomBean, NewVoiceRoomPresenter> implements IVoiceRoomFragmentView,
        RoomMessageAdapter.OnClickMessageUserListener {
    private VoiceRoomBean mVoiceRoomBean;
    private ImageView mBackgroundImageView;
    private RoomTitleBar mRoomTitleBar;
    private TextView mNoticeView;
    private RoomSeatView mRoomSeatView;
    private RoomBottomView mRoomBottomView;
    private RecyclerView mMessageView;
    private RoomMessageAdapter mRoomMessageAdapter;

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

    @Override
    public void joinRoom(VoiceRoomBean voiceRoomBean) {
//        RCRadioRoomEngine.getInstance().setRadioEventListener(this);
        mVoiceRoomBean = voiceRoomBean;
        setRoomData(mVoiceRoomBean);
        sendSystemMessage();
    }

    @Override
    public void onBackPressed() {

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
            RCRadioRoomEngine.getInstance().enterSeat(new RCRadioRoomCallback() {
                @Override
                public void onSuccess() {
                    Logger.e("==============enterSeat onSuccess");
                }

                @Override
                public void onError(int code, String message) {
                    Logger.e("==============enterSeat onError, code:" + code + ",message:" + message);
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
        mRoomBottomView.setData(getRoomOwnerType(), null);
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

    @Override
    public void onSeatListChange(@NonNull List<UiSeatModel> uiSeatModelList) {

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

    }

    @Override
    public void showUnReadRequestNumber(int number) {

    }

    @Override
    public void showUnreadMessage(int count) {

    }

    @Override
    public void showFov(@Nullable Point from) {

    }

    @Override
    public void showRevokeSeatRequest() {

    }

    @Override
    public void showRoomClose() {

    }

    @Override
    public void onMemberInfoChange() {

    }

    @Override
    public void showSingalInfo(int i) {

    }
}
