package cn.rongcloud.radioroom.ui.room;

import android.graphics.Color;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.basis.net.LoadTag;
import com.basis.net.oklib.OkApi;
import com.basis.net.oklib.WrapperCallBack;
import com.basis.net.oklib.wrapper.Wrapper;
import com.kit.utils.Logger;
import com.kit.wapper.IResultBack;
import com.rongcloud.common.utils.AccountStore;
import com.rongcloud.common.utils.ImageLoaderUtil;
import com.rongcloud.common.utils.UiUtils;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.message.RCChatroomEnter;
import cn.rong.combusis.message.RCChatroomGift;
import cn.rong.combusis.message.RCChatroomGiftAll;
import cn.rong.combusis.message.RCChatroomLocationMessage;
import cn.rong.combusis.provider.voiceroom.RoomOwnerType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.provider.voiceroom.VoiceRoomProvider;
import cn.rong.combusis.sdk.event.EventHelper;
import cn.rong.combusis.ui.room.AbsRoomFragment;
import cn.rong.combusis.ui.room.RoomMessageAdapter;
import cn.rong.combusis.ui.room.dialog.ExitRoomPopupWindow;
import cn.rong.combusis.ui.room.dialog.RoomNoticeDialog;
import cn.rong.combusis.ui.room.widget.RoomBottomView;
import cn.rong.combusis.ui.room.widget.RoomSeatView;
import cn.rong.combusis.ui.room.widget.RoomTitleBar;
import cn.rongcloud.messager.RCMessager;
import cn.rongcloud.messager.SendMessageCallback;
import cn.rongcloud.radioroom.R;
import cn.rongcloud.radioroom.RCRadioRoomCallback;
import cn.rongcloud.radioroom.RCRadioRoomEngine;
import cn.rongcloud.radioroom.rroom.RCRadioEventListener;
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

/**
 * @author gyn
 * @date 2021/9/17
 */
public class RadioRoomFragment extends AbsRoomFragment<VoiceRoomBean, RadioRoomPresenter> implements RCRadioEventListener, RoomMessageAdapter.OnClickMessageUserListener, RadioRoomView, RoomBottomView.OnBottomOptionClickListener {
    private VoiceRoomBean mVoiceRoomBean;
    private ImageView mBackgroundImageView;
    private RoomTitleBar mRoomTitleBar;
    private TextView mNoticeView;
    private RoomSeatView mRoomSeatView;
    private RoomBottomView mRoomBottomView;
    private RecyclerView mMessageView;
    private RoomMessageAdapter mRoomMessageAdapter;
    private ExitRoomPopupWindow mExitRoomPopupWindow;
    private LoadTag mLoadTag;
    private RoomNoticeDialog mNoticeDialog;

    public static Fragment getInstance() {
        return new RadioRoomFragment();
    }

    @Override
    public RadioRoomPresenter createPresent() {
        return new RadioRoomPresenter(this, getLifecycle());
    }

    @Override
    public int setLayoutId() {
        return R.layout.fragment_radio_room;
    }

    @Override
    public void init() {

        mLoadTag = new LoadTag(getActivity());
        mNoticeDialog = new RoomNoticeDialog(getContext());
        // 头部
        mRoomTitleBar = getView(R.id.room_title_bar);
        mRoomTitleBar.setOnMenuClickListener(v -> {
            clickMenu();
        });
        mNoticeView = getView(R.id.tv_notice);
        mNoticeView.setOnClickListener(v -> {
            mNoticeDialog.show("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n\n\n\n" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n\n\n\n" +
                    "aaaaaaaaaaaaaaaaaaaaaaa\n\n\n\n" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n\n\n\n" +
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", false, notice -> {

            });
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
    public void initListener() {

    }

    @Override
    public void joinRoom(VoiceRoomBean voiceRoomBean) {
        present.joinRoom(voiceRoomBean);
        RCRadioRoomEngine.getInstance().setRadioEventListener(this);
        mVoiceRoomBean = voiceRoomBean;
        setRoomData(mVoiceRoomBean);
        sendSystemMessage();
    }

    @Override
    public void onBackPressed() {
        clickMenu();
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
            sendMessage();
        }
    }

    private void sendMessage() {
        RCChatroomEnter enter = new RCChatroomEnter();
        enter.setUserId(AccountStore.INSTANCE.getUserId());
        enter.setUserName(AccountStore.INSTANCE.getUserName());
        RCMessager.getInstance().sendChatRoomMessage(mVoiceRoomBean.getRoomId(), enter, new SendMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                Logger.e("111111111111111111111");
            }

            @Override
            public void onError(Message message, int code, String reason) {
                Logger.e("111111111111111111111" + code + reason);
            }
        });
    }

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

    /**
     * 点击右上角菜单按钮
     */
    private void clickMenu() {
        mExitRoomPopupWindow = new ExitRoomPopupWindow(getContext(), getRoomOwnerType(), new ExitRoomPopupWindow.OnOptionClick() {
            @Override
            public void clickPackRoom() {

            }

            @Override
            public void clickLeaveRoom() {
                // 观众离开房间
                leaveRoom();
            }

            @Override
            public void clickCloseRoom() {
                mLoadTag.show("正在退出房间");
                // 房主关闭房间
                OkApi.get(VRApi.deleteRoom(mVoiceRoomBean.getRoomId()), null, new WrapperCallBack() {
                    @Override
                    public void onResult(Wrapper result) {
                        if (result.ok()) {
                            leaveRoom();
                        } else {
                            mLoadTag.dismiss();
                        }
                    }

                    @Override
                    public void onError(int code, String msg) {
                        super.onError(code, msg);
                        mLoadTag.dismiss();
                    }
                });
            }
        });
        mExitRoomPopupWindow.setAnimationStyle(R.style.popup_window_anim_style);
        mExitRoomPopupWindow.showAtLocation(mBackgroundImageView, Gravity.TOP, 0, 0);
    }

    private void leaveRoom() {
        RCRadioRoomEngine.getInstance().leaveRoom(new RCRadioRoomCallback() {

            @Override
            public void onSuccess() {
                Logger.e("==============leaveRoom onSuccess");
                mLoadTag.dismiss();
                getActivity().finish();
            }

            @Override
            public void onError(int code, String message) {
                Logger.e("==============leaveRoom onError");
                mLoadTag.dismiss();
                getActivity().finish();
            }
        });
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
        mRoomTitleBar.setDelay(0, false);
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

    @Override
    public void onSpeakingStateChanged(boolean isSpeaking) {
//        Logger.e("==============onSpeakingStateChanged: " + isSpeaking);
        mRoomSeatView.setSpeaking(isSpeaking);
    }

    @Override
    public void onMessageReceived(Message message) {
        Logger.e("==============onMessageReceived: " + message.toString());
        showMessage(message.getContent(), false);
        if (message.getContent() instanceof RCChatroomGift || message.getContent() instanceof RCChatroomGiftAll) {

        }
    }

    @Override
    public void onAudienceEnter(String s) {
        Logger.e("==============onAudienceEnter: " + s);
        EventHelper.helper().getOnLineUserIds(mVoiceRoomBean.getRoomId(), new IResultBack<List<String>>() {
            @Override
            public void onResult(List<String> strings) {
                mRoomTitleBar.setOnlineNum(strings.size());
            }
        });
    }

    @Override
    public void onAudienceLeave(String s) {
        Logger.e("==============onAudienceLeave: " + s);
        EventHelper.helper().getOnLineUserIds(mVoiceRoomBean.getRoomId(), new IResultBack<List<String>>() {
            @Override
            public void onResult(List<String> strings) {
                mRoomTitleBar.setOnlineNum(strings.size());
            }
        });
    }

    @Override
    public void onNetworkStatus(int delayMs) {
//        Logger.e("==============onNetworkStatus: " + delayMs);
//        mRoomTitleBar.setDelay(delayMs);
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

    @Override
    public void clickMessageUser(String userId) {

    }

    @Override
    public void clickSendMessage(String message) {
        present.sendMessage(message);
    }

    @Override
    public void clickPrivateMessage() {

    }

    @Override
    public void clickSeatOrder() {

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
