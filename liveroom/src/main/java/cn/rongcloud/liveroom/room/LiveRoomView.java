package cn.rongcloud.liveroom.room;

import androidx.fragment.app.FragmentManager;

import com.basis.mvp.IBaseView;

import java.util.List;

import cn.rong.combusis.provider.voiceroom.CurrentStatusType;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
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
     * @param status
     */
    void changeStatus(CurrentStatusType status);

    /**
     * 设置房间名称
     *
     * @param name
     */
    void setRoomName(String name);

    /**
     * 获取fragment管理器
     *
     * @return
     */
    FragmentManager getLiveFragmentManager();
}
