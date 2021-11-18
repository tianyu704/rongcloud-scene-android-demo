package cn.rongcloud.liveroom.helper;

import io.rong.imlib.model.MessageContent;

/**
 * @author lihao
 * @project RongRTCDemo
 * @date 2021/11/16
 * @time 5:26 下午
 */
public interface ILiveEventHelper {

    /**
     * 注册
     *
     * @param roomId
     */
    void register(String roomId);

    /**
     * 反注册
     */
    void unRegister();

    /**
     * 发送消息
     *
     * @param messageContent
     */
    void sendMessage(MessageContent messageContent, boolean isShowLocation);
}
