package cn.rongcloud.radioroom.ui.room;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.kit.utils.Logger;
import com.rongcloud.common.utils.ImageLoaderUtil;

import cn.rong.combusis.message.RCChatroomGift;
import cn.rong.combusis.message.RCChatroomGiftAll;
import cn.rong.combusis.provider.voiceroom.RoomOwnerType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.provider.voiceroom.VoiceRoomProvider;
import cn.rong.combusis.ui.room.AbsRoomFragment;
import cn.rong.combusis.ui.room.widget.RoomBottomView;
import cn.rong.combusis.ui.room.widget.RoomSeatView;
import cn.rong.combusis.ui.room.widget.RoomTitleBar;
import cn.rongcloud.radioroom.R;
import cn.rongcloud.radioroom.RCRadioRoomCallback;
import cn.rongcloud.radioroom.RCRadioRoomEngine;
import cn.rongcloud.radioroom.rroom.RCRadioEventListener;
import io.rong.imlib.model.Message;

/**
 * @author gyn
 * @date 2021/9/17
 */
public class RadioRoomFragment extends AbsRoomFragment<VoiceRoomBean> implements RCRadioEventListener {
    private VoiceRoomBean mVoiceRoomBean;
    private ImageView mBackgroundImageView;
    private RoomTitleBar mRoomTitleBar;
    private TextView mNoticeView;
    private RoomSeatView mRoomSeatView;
    private RoomBottomView mRoomBottomView;

    public static Fragment getInstance() {
        return new RadioRoomFragment();
    }

    @Override
    public int setLayoutId() {
        return R.layout.fragment_radio_room;
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
    }

    @Override
    public void joinRoom(VoiceRoomBean voiceRoomBean) {
        RCRadioRoomEngine.getInstance().setRadioEventListener(this);
        mVoiceRoomBean = voiceRoomBean;
        setRoomData(mVoiceRoomBean);
    }

    @Override
    public void destroyRoom() {

    }

    @Override
    public void onSpeakingStateChanged(boolean isSpeaking) {
        Logger.e("==============onSpeakingStateChanged: " + isSpeaking);
        mRoomSeatView.setSpeaking(isSpeaking);
    }

    @Override
    public void onMessageReceived(Message message) {
        Logger.e("==============onMessageReceived: " + message.toString());
        if (message.getContent() instanceof RCChatroomGift || message.getContent() instanceof RCChatroomGiftAll) {
            // TODO

        }
    }

    @Override
    public void onNetworkStatus(int delayMs) {
        Logger.e("==============onNetworkStatus: " + delayMs);
        mRoomTitleBar.setDelay(delayMs);
    }

    @Override
    public void onRadioPause() {
        Logger.e("==============onRadioPause");
    }

    @Override
    public void onRadioResume() {
        Logger.e("==============onRadioResume");
    }

    @Override
    public void onRadioName(String name) {
        Logger.e("==============onRadioName: " + name);
    }
}
