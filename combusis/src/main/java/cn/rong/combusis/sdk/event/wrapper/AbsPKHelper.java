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

    protected Type current = Type.PK_NONE;

    @Override
    protected void init(String roomId) {
        super.init(roomId);
        current = Type.PK_NONE;
        //由于手动调quitPK 不会回调onPkFinish，因此需要在调用api成功后修改current状态
        EventBus.get().on(EventBus.TAG.PK_QUIT, new EventBus.EventCallback() {
            @Override
            public void onEvent(Object... args) {
                current = Type.PK_NONE;
            }
        });
    }

    @Override
    protected void unInit() {
        super.unInit();
        current = Type.PK_NONE;
        EventBus.get().off(EventBus.TAG.PK_QUIT, null);
    }

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
        dispatchPKState(rcpkInfo);
    }

    /**
     * PK结束回调
     */
    @Override
    public void onPKFinish() {
        Logger.e(TAG, "onPKFinish");
        current = Type.PK_FINISH;
        dispatchPKState(null);
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
        dispatchPKState(null);
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
        dispatchPKState(null);
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
        IEventHelp.PKInvitee invitee = VoiceRoomApi.getApi().getPKInvitee();
        //判断是否是当前正在邀请的信息
        if (invitee.inviteeRoomId.equals(roomId) && invitee.inviteeId.equals(userId)) {
            dispatchPKResponse(PKState.reject);
            //邀请被忽略 该邀请流程结束 释放被邀请信息
            VoiceRoomApi.getApi().releasePKInvitee();
        }
        current = Type.PK_NONE;
        dispatchPKState(null);
    }

    @Override
    public void onPKInvitationIgnored(String roomId, String userId) {
        Logger.e(TAG, "onPKInvitationIgnored");
        IEventHelp.PKInvitee invitee = VoiceRoomApi.getApi().getPKInvitee();
        //判断是否是当前正在邀请的信息
        if (invitee.inviteeRoomId.equals(roomId) && invitee.inviteeId.equals(userId)) {
            dispatchPKResponse(PKState.ignore);
            //邀请被忽略 该邀请流程结束 释放被邀请信息
            VoiceRoomApi.getApi().releasePKInvitee();
        }
        current = Type.PK_NONE;
        dispatchPKState(null);
    }

    private void dispatchPKState(RCPKInfo rcpkInfo) {
        EventBus.get().emit(EventBus.TAG.PK_STATE, current, rcpkInfo);
    }

    private void dispatchPKResponse(PKState pkState) {
        EventBus.get().emit(EventBus.TAG.PK_RESPONSE, pkState);
    }
}