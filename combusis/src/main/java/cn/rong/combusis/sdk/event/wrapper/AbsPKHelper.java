package cn.rong.combusis.sdk.event.wrapper;

import androidx.annotation.NonNull;

import com.kit.utils.Logger;
import com.kit.wapper.IResultBack;

import cn.rong.combusis.EventBus;
import cn.rong.combusis.sdk.VoiceRoomApi;
import cn.rongcloud.voiceroom.api.PKState;
import cn.rongcloud.voiceroom.api.RCVoiceRoomEngine;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomCallback;
import cn.rongcloud.voiceroom.model.RCPKInfo;

/**
 * 实现PK相关回调
 */
public abstract class AbsPKHelper extends AbsEvenHelper {
    //pk 邀请者
    protected PKInviter pkInviter;

    public enum Type {
        PK_NONE,//默认状态
        PK_INVITE,//邀请状态
        PK_GOING,//pk 进行中
        PK_FINISH//pk关闭状态
    }

    protected Type current = Type.PK_NONE;

    @Override
    public Type getPKState() {
        return current;
    }

    /**
     * PK开启成功
     *
     * @param rcpkInfo
     */
    @Override
    public void onPKgoing(@NonNull RCPKInfo rcpkInfo) {
        Logger.e(TAG, "onPKgoing");
        //邀请同意 开始PK 释放被邀请信息
        VoiceRoomApi.getApi().releasePKInvitee();
        current = Type.PK_GOING;
        dispatchPKState();
    }

    /**
     * PK结束回调
     */
    @Override
    public void onPKFinish() {
        Logger.e(TAG, "onPKFinish");
        current = Type.PK_FINISH;
        dispatchPKState();
    }

    /**
     * 接收到PK邀请回调
     *
     * @param inviterRoomId 发起邀请人的房间Id
     * @param inviterUserId 发起邀请人的Id
     */
    @Override
    public void onReveivePKInvitation(String inviterRoomId, String inviterUserId) {
        Logger.e(TAG, "onReveivePKInvitation");
        //保存邀请者信息
        pkInviter = new PKInviter();
        pkInviter.inviterRoomId = inviterRoomId;
        pkInviter.inviterId = inviterUserId;
        current = Type.PK_INVITE;
        dispatchPKState();
        onShowTipDialog(inviterRoomId, inviterUserId, TipType.InvitedPK, new IResultBack<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                RCVoiceRoomEngine.getInstance().responsePKInvitation(inviterRoomId, inviterUserId, result ? PKState.accept : PKState.reject, new RCVoiceRoomCallback() {
                    @Override
                    public void onSuccess() {
                        EToast.showToastWithLag(TAG, result ? "同意PK成功" : "拒绝PK成功");
                    }

                    @Override
                    public void onError(int code, String message) {
                        EToast.showToastWithLag(TAG, (result ? "同意PK失败" : "拒绝PK失败") + " code = " + code + " message = " + message);
                    }
                });
            }
        });
    }


    /**
     * PK邀请被取消
     *
     * @param roomId 发起邀请人的房间Id
     * @param userId 发起邀请人的Id
     */
    @Override
    public void onPKInvitationCanceled(String roomId, String userId) {
        Logger.e(TAG, "onPKInvitationCanceled");
        EventDialogHelper.helper().dismissDialog();
        // 释放邀请者信息
        pkInviter = null;
        current = Type.PK_NONE;
        dispatchPKState();
    }

    /**
     * PK邀请被拒绝
     *
     * @param roomId 发起邀请人的房间Id
     * @param userId 发起邀请人的Id
     */
    @Override
    public void onPKInvitationRejected(String roomId, String userId) {
        Logger.e(TAG, "onPKInvitationRejected");
        //邀请被拒绝 释放被邀请信息
        VoiceRoomApi.getApi().releasePKInvitee();
        current = Type.PK_NONE;
        dispatchPKState();
    }

    @Override
    public void onPKInvitationIgnored(String s, String s1) {
        Logger.e(TAG, "onPKInvitationIgnored");
        //邀请被忽略 释放被邀请信息
        VoiceRoomApi.getApi().releasePKInvitee();
        current = Type.PK_NONE;
        dispatchPKState();
    }

    private void dispatchPKState() {
        EventBus.get().emit(EventBus.TAG.PK_STATE, current);
    }
}