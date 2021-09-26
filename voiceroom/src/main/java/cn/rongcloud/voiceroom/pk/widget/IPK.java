package cn.rongcloud.voiceroom.pk.widget;

import java.util.List;

public interface IPK {
    /**
     * pk 开始
     *
     * @param listener 定时结束回调
     */
    void pkStart(OnTimerEndListener listener);

    /**
     * 惩罚阶段开始
     *
     * @param listener 定时结束回调
     */
    void punishStart(OnTimerEndListener listener);

    /**
     * pk 流程结束
     */
    void pkStop();

    /**
     * 设置pk 双方总价值
     *
     * @param left  左侧价值
     * @param right 右侧价值
     */
    void setPKValues(int left, int right);

    /**
     * 设置pk 双方礼物赠送者排行榜
     *
     * @param lefts  左侧排行榜
     * @param rights 右侧排行榜
     */
    void setGiftSenders(List<String> lefts, List<String> rights);

    public interface OnTimerEndListener {
        void onTimerEnd();
    }
}
