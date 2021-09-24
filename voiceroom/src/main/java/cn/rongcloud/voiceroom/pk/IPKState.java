package cn.rongcloud.voiceroom.pk;

import android.app.Activity;

import com.kit.wapper.IResultBack;

public interface IPKState {
    void init(String roomId, VRStateListener listener);

    void unInit();

    /**
     * 发送pk邀请
     *
     * @param activity
     * @param resultBack 邀请成功回调
     */
    void sendPkInvitation(Activity activity, IResultBack<Boolean> resultBack);

    /**
     * 取消邀请
     *
     * @param activity
     * @param resultBack 取消邀请会状态回调
     */
    void cancelPkInvitation(Activity activity, IResultBack<Boolean> resultBack);

    interface VRStateListener {
        void onPkStart();

        void onPkStop();
    }
}
