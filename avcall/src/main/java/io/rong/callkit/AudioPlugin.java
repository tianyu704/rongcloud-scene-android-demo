/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package io.rong.callkit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import io.rong.callkit.util.CallKitUtils;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.common.RLog;
import io.rong.imkit.conversation.extension.RongExtension;
import io.rong.imkit.conversation.extension.component.plugin.IPluginModule;
import io.rong.imkit.conversation.extension.component.plugin.IPluginRequestPermissionResultCallback;
import io.rong.imkit.utils.PermissionCheckUtil;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.discussion.base.RongDiscussionClient;
import io.rong.imlib.discussion.model.Discussion;
import io.rong.imlib.model.Conversation;
import java.util.ArrayList;

/** Created by weiqinxiao on 16/8/16. */
public class AudioPlugin implements IPluginModule, IPluginRequestPermissionResultCallback {
    private static final String TAG = "AudioPlugin";
    private ArrayList<String> allMembers;
    private Context context;
    private static final int REQEUST_CODE_RECORD_AUDIO_PERMISSION = 101;

    private Conversation.ConversationType conversationType;
    private String targetId;

    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.rc_ic_phone_selector);
    }

    @Override
    public String obtainTitle(Context context) {
        return context.getString(R.string.rc_voip_audio);
    }

    @Override
    public void onClick(Fragment currentFragment, RongExtension extension, int index) {
        context = currentFragment.getActivity().getApplicationContext();
        conversationType = extension.getConversationType();
        targetId = extension.getTargetId();
        Log.i(TAG, "---- targetId==" + targetId);
        String[] permissions = CallKitUtils.getCallpermissions();
        if (PermissionCheckUtil.checkPermissions(currentFragment.getActivity(), permissions)) {
            Log.i(TAG, "---- startAudioActivity ----");
            startAudioActivity(currentFragment, extension);
        } else {
            Log.i(TAG, "---- requestPermissionForPluginResult ----");
            extension.requestPermissionForPluginResult(
                    permissions, REQEUST_CODE_RECORD_AUDIO_PERMISSION, this);
        }
    }

    private void startAudioActivity(Fragment currentFragment, final RongExtension extension) {
        if (context == null) {
            return;
        }
        RongCallSession profile = RongCallClient.getInstance().getCallSession();
        if (profile != null && profile.getStartTime() > 0) {
            Toast.makeText(
                            context,
                            profile.getMediaType() == RongCallCommon.CallMediaType.AUDIO
                                    ? currentFragment.getString(
                                            R.string.rc_voip_call_audio_start_fail)
                                    : currentFragment.getString(
                                            R.string.rc_voip_call_video_start_fail),
                            Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        if (!CallKitUtils.isNetworkAvailable(context)) {
            Toast.makeText(
                            context,
                            currentFragment.getString(R.string.rc_voip_call_network_error),
                            Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (conversationType.equals(Conversation.ConversationType.PRIVATE)) {
            Intent intent = new Intent(RongVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEAUDIO);
            intent.putExtra("conversationType", conversationType.getName().toLowerCase());
            Log.i(
                    TAG,
                    "---- conversationType.getName().toLowerCase() =-"
                            + conversationType.getName().toLowerCase());
            intent.putExtra("targetId", targetId);
            intent.putExtra("callAction", RongCallAction.ACTION_OUTGOING_CALL.getName());
            Log.i(TAG, "---- callAction=" + RongCallAction.ACTION_OUTGOING_CALL.getName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.i(TAG, "getPackageName===" + context.getPackageName());
            intent.setPackage(context.getPackageName());
            context.startActivity(intent);
        } else if (conversationType.equals(Conversation.ConversationType.DISCUSSION)) {
            RongDiscussionClient.getInstance()
                    .getDiscussion(
                            targetId,
                            new IRongCoreCallback.ResultCallback<Discussion>() {
                                @Override
                                public void onSuccess(Discussion discussion) {
                                    Intent intent =
                                            new Intent(context, CallSelectMemberActivity.class);
                                    allMembers = (ArrayList<String>) discussion.getMemberIdList();
                                    intent.putStringArrayListExtra("allMembers", allMembers);
                                    intent.putExtra(
                                            "conversationType", conversationType.getValue());
                                    String myId = RongIMClient.getInstance().getCurrentUserId();
                                    ArrayList<String> invited = new ArrayList<>();
                                    invited.add(myId);
                                    intent.putStringArrayListExtra("invitedMembers", invited);
                                    intent.putExtra(
                                            "mediaType",
                                            RongCallCommon.CallMediaType.AUDIO.getValue());
                                    extension.startActivityForPluginResult(
                                            intent, 110, AudioPlugin.this);
                                }

                                @Override
                                public void onError(IRongCoreEnum.CoreErrorCode e) {
                                    RLog.d(TAG, "get discussion errorCode = " + e.getValue());
                                }
                            });
        } else if (conversationType.equals(Conversation.ConversationType.GROUP)) {
            Intent intent = new Intent(context, CallSelectMemberActivity.class);
            String myId = RongIMClient.getInstance().getCurrentUserId();
            ArrayList<String> invited = new ArrayList<>();
            invited.add(myId);
            intent.putStringArrayListExtra("invitedMembers", invited);
            intent.putExtra("conversationType", conversationType.getValue());
            intent.putExtra("groupId", targetId);
            intent.putExtra("mediaType", RongCallCommon.CallMediaType.AUDIO.getValue());
            extension.startActivityForPluginResult(intent, 110, this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        Intent intent = new Intent(RongVoIPIntent.RONG_INTENT_ACTION_VOIP_MULTIAUDIO);
        ArrayList<String> userIds = data.getStringArrayListExtra("invited");
        ArrayList<String> observers = data.getStringArrayListExtra("observers");
        userIds.add(RongIMClient.getInstance().getCurrentUserId());
        intent.putExtra("conversationType", conversationType.getName().toLowerCase());
        intent.putExtra("targetId", targetId);
        intent.putExtra("callAction", RongCallAction.ACTION_OUTGOING_CALL.getName());
        intent.putStringArrayListExtra("invitedUsers", userIds);
        intent.putStringArrayListExtra("observers", observers);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(context.getPackageName());
        context.startActivity(intent);
    }

    @Override
    public boolean onRequestPermissionResult(
            Fragment fragment,
            RongExtension extension,
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        Context context = fragment.getContext();
        if (PermissionCheckUtil.checkPermissions(context, permissions)) {
            startAudioActivity(fragment, extension);
        } else {
            PermissionCheckUtil.showRequestPermissionFailedAlter(context, permissions, grantResults);
        }
        return true;
    }
}
