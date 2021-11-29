package cn.rongcloud.liveroom.room;

import android.content.Context;
import android.view.View;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

import com.basis.mvp.IBaseView;

import java.util.List;

import cn.rong.combusis.provider.voiceroom.CurrentStatusType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.ui.room.fragment.roomsetting.IFun;
import cn.rong.combusis.ui.room.model.Member;
import io.rong.imlib.model.MessageContent;

/**
 * 直播房
 */
public interface LiveRoomView extends IBaseView {
    /**
     * 当前直播已结束
     */
    void showFinishView();

    /**
     * 设置房间数据
     */
    void setRoomData(VoiceRoomBean voiceRoomBean);

    /**
     * 设置布局的顶部关注按钮的文字状态
     */
    void setTitleFollow(boolean isFollow);

    /**
     * 添加单条公屏消息
     */
    void addMessageContent(MessageContent messageContent, boolean isReset);

    /**
     * 添加多条公屏消息
     */
    void addMessageList(List<MessageContent> messageContentList, boolean isReset);

    /**
     * 清空消息输入框
     */
    void clearInput();

    /**
     * 隐藏软键盘和底部输入框
     */
    void hideSoftKeyboardAndIntput();

    /**
     * 设置喜欢的动画
     */
    void showLikeAnimation();

    /**
     * 当前用户的麦位状态
     *
     */
    void changeStatus();

    /**
     * 获取fragment管理器
     *
     * @return
     */
    FragmentManager getLiveFragmentManager();

    /**
     * 关闭当前页面
     */
    void finish();

    /**
     * 显示直播view
     */
    void showRCLiveVideoView(View videoView);

    /**
     * 显示消息延迟
     *
     * @param delayMs
     */
    void showNetWorkStatus(long delayMs);

    /**
     * 显示在线人数
     *
     * @param onLineCount
     */
    void setOnlineCount(int onLineCount);

    /**
     * 显示房主的礼物数量
     */
    void setCreateUserGift(String giftCount);

    /**
     * 显示发送礼物弹窗
     *
     * @param voiceRoomBean
     * @param selectUserId
     * @param members
     */
    void showSendGiftDialog(VoiceRoomBean voiceRoomBean, String selectUserId, List<Member> members);

    /**
     * 显示音乐弹窗
     */
    void showMusicDialog();

    /**
     * 底部设置弹窗
     *
     * @param funList
     */
    void showRoomSettingFragment(List<MutableLiveData<IFun.BaseFun>> funList);

    /**
     * 获取上下文
     *
     * @return
     */
    Context getLiveActivity();

    /**
     * 设置公告的内容
     *
     * @param notice
     */
    void setNotice(String notice);

    /**
     * 展示申请人数
     */
    void showUnReadRequestNumber(int requestNumber);

}
