package cn.rongcloud.voiceroom.pk.widget;

import java.util.List;

/**
 * PK两种状态 pk阶段、惩罚阶段
 * pk阶段
 * 1、开启pk记时
 * 2、记时结束 ->进入惩罚阶段
 * 惩罚阶段
 * 1、惩罚记时
 * 2、记时结束 -> 结束pk流程
 */
public interface IPK {
    /**
     * pk 开始
     *
     * @param localId  当前人的id
     * @param pkId     pk对象的UserId
     * @param listener 定时结束回调
     */
    void pkStart(String localId, String pkId, OnTimerEndListener listener);

    /**
     * 惩罚 开始
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
    void setPKScore(int left, int right);

    /**
     * 设置pk 双方礼物赠送者排行榜
     *
     * @param lefts  左侧排行榜
     * @param rights 右侧排行榜
     */
    void setGiftRank(List<String> lefts, List<String> rights);

    interface OnTimerEndListener {
        void onTimerEnd();
    }
}