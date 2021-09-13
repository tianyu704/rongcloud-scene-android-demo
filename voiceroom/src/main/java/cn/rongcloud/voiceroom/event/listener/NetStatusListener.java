package cn.rongcloud.voiceroom.event.listener;

public interface NetStatusListener {
    /**
     * 网络延迟监听
     *
     * @param delay
     */
    void onStatus(int delay);
}