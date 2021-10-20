/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.radioroom.rroom;

import android.app.Application;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rongcloud.messager.ConnectCallback;
import cn.rongcloud.messager.IResultBack;
import cn.rongcloud.messager.RCMessager;
import cn.rongcloud.messager.SendMessageCallback;
import cn.rongcloud.messager.VRKVStatusListener;
import cn.rongcloud.messager.utils.MLog;
import cn.rongcloud.radioroom.IRCRadioRoomEngine;
import cn.rongcloud.radioroom.RCRadioRoomEngine;
import cn.rongcloud.radioroom.callback.RCRadioRoomBaseCallback;
import cn.rongcloud.radioroom.callback.RCRadioRoomCallback;
import cn.rongcloud.radioroom.callback.RCRadioRoomResultCallback;
import cn.rongcloud.radioroom.utils.JsonUtils;
import cn.rongcloud.radioroom.utils.VMLog;
import cn.rongcloud.rtc.api.RCRTCConfig;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.RCRTCRoomConfig;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCResultDataCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCRoomEventsListener;
import cn.rongcloud.rtc.api.callback.IRCRTCStatusReportListener;
import cn.rongcloud.rtc.api.report.StatusBean;
import cn.rongcloud.rtc.api.report.StatusReport;
import cn.rongcloud.rtc.api.stream.RCRTCCDNInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCLiveInfo;
import cn.rongcloud.rtc.base.RCRTCLiveRole;
import cn.rongcloud.rtc.base.RCRTCMediaType;
import cn.rongcloud.rtc.base.RCRTCRoomType;
import cn.rongcloud.rtc.base.RTCErrorCode;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.chatroom.base.RongChatRoomClient;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

/**
 * 电台房的控制
 */
public class RCRadioRoomEngineImpl extends RCRadioRoomEngine implements VRKVStatusListener,
        IRongCoreListener.OnReceiveMessageListener {
    private static final String TAG = "RCRadioRoomEngineImpl";
    private static final IRCRadioRoomEngine instance = new RCRadioRoomEngineImpl();
    private final static Handler main = new Handler(Looper.getMainLooper());
    /**
     * 是否在说话
     */
    String isSpeaking = "0";
    private RCRTCRoom mRcrtcRoom;
    private RCRadioRoomInfo mRadioRoom;
    private RCRadioEventListener listener;

    private RCRadioRoomEngineImpl() {
        RCMessager.getInstance().addOnReceiveMessageListener(this);
        RCMessager.getInstance().addMessageTypes(RCChatRoomEnter.class, RCChatRoomLeave.class);
        RCMessager.getInstance().addKVStatusListener(this);
    }

    public static IRCRadioRoomEngine getInstance() {
        return instance;
    }

    private void onErrorWithCheck(final RCRadioRoomBaseCallback callback, final int code, final String message) {
        if (null != callback) main.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(code, message);
                VMLog.d(TAG, "onErrorWithCheck:[" + code + "],[" + message + "]");
            }
        });
    }

    private void onSuccessWithCheck(final RCRadioRoomCallback callback) {
        if (checkCallback(callback))
            main.post(new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess();
                }
            });
    }

    private <T> void onSuccessWithCheck(final RCRadioRoomResultCallback<T> callback, final T data) {
        if (checkCallback(callback)) {
            main.post(new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess(data);
                }
            });
        }
    }

    private boolean checkCallback(RCRadioRoomBaseCallback callback) {
        return callback != null;
    }

    @Override
    public void initWithAppKey(Application context, String appKey) {
        RCMessager.getInstance().initWithAppKey(context, appKey);
    }

    @Override
    public void connectWithToken(String token, ConnectCallback callback) {
        RCMessager.getInstance().connectWithToken(token, callback);
    }

    @Override
    public void setRadioEventListener(RCRadioEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void joinRoom(RCRadioRoomInfo roomInfo, final RCRadioRoomCallback callback) {
        this.mRadioRoom = roomInfo;
        if (null == mRadioRoom || !mRadioRoom.check()) {
            onErrorWithCheck(callback, -1, "RoomInfo is Check Null");
            return;
        }
        if (!RCMessager.getInstance().isConnected()) {
            onErrorWithCheck(callback, -2, "Not Connected ");
            return;
        }
        VMLog.v(TAG, "joinRoom:role = " + mRadioRoom.getRole());
        RongChatRoomClient.getInstance().joinChatRoom(mRadioRoom.getRoomId(), -1, new IRongCoreCallback.OperationCallback() {
            @Override
            public void onSuccess() {
                notifyEnterOrLeave(true, null);
                if (RCRTCLiveRole.BROADCASTER == mRadioRoom.getRole()) {
                    updateRadioRoomKV(UpdateKey.RC_ROOM_NAME, mRadioRoom.getRoomName(), new RCRadioRoomCallback() {
                        @Override
                        public void onSuccess() {
                            joinRTCRoom(mRadioRoom.getRoomId(), mRadioRoom.getRole(), callback);
                        }

                        @Override
                        public void onError(int code, String message) {
                            onErrorWithCheck(callback, code, message);
                        }
                    });
                } else {
                    joinRTCRoom(mRadioRoom.getRoomId(), mRadioRoom.getRole(), callback);
                }
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode err) {
                VMLog.e(TAG, "joinRoom#joinChatRoom", err);
                onErrorWithCheck(callback, err.code, err.msg);
            }
        });
    }

    @Override
    public void leaveRoom(final RCRadioRoomCallback callback) {
        if (null == mRadioRoom || !mRadioRoom.check() || null == mRcrtcRoom) {
            onErrorWithCheck(callback, 0, "Not Join RadioRoom");
            return;
        }
        VMLog.d(TAG, "leaveRoom#roomId:" + mRadioRoom.getRoomId());
        leaveSeat(null);
        notifyEnterOrLeave(false, new RCRadioRoomCallback() {
            @Override
            public void onSuccess() {
                quitRoom(callback);
            }

            @Override
            public void onError(int code, String message) {
                quitRoom(callback);
            }
        });
    }

    private void quitRoom(final RCRadioRoomCallback callback) {
        //离开聊天室 无论成功与否 都执行leaveRTCRoom
        RongChatRoomClient.getInstance().quitChatRoom(mRadioRoom.getRoomId(), new IRongCoreCallback.OperationCallback() {
            @Override
            public void onSuccess() {
                VMLog.d(TAG, "quitRoom:onSuccess: ");
                leaveRTCRoom(callback);
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode code) {
                VMLog.e(TAG, "quitRoom:onError", code);
                leaveRTCRoom(null);
                onErrorWithCheck(callback, code.code, code.msg);
            }
        });
    }

    @Override
    public void enterSeat(final RCRadioRoomCallback callback) {
        if (null == mRcrtcRoom || null == mRadioRoom || !mRadioRoom.check()) {
            onErrorWithCheck(callback, -1, "Check RTCRoom Or RadioRoom is Null ");
            return;
        }
        if (mRadioRoom.isInSeat()) {
            onErrorWithCheck(callback, -1, "Is In Seat ");
            return;
        }
        // 取消静音
        muteSelf(false);
        mRcrtcRoom.getLocalUser().publishDefaultLiveStreams(new IRCRTCResultDataCallback<RCRTCLiveInfo>() {
            @Override
            public void onSuccess(RCRTCLiveInfo rcrtcLiveInfo) {
                VMLog.e(TAG, "enterSeat#publishDefaultLiveStreams#onSuccess:");
                RCRTCEngine.getInstance().registerStatusReportListener(new StateListener());
                // 跟新KV
                updateRadioRoomKV(UpdateKey.RC_SEATING, "1", new RCRadioRoomCallback() {
                    @Override
                    public void onSuccess() {
                        mRadioRoom.setInSeat(true);
                        onSuccessWithCheck(callback);
                    }

                    @Override
                    public void onError(int code, String message) {
                        onErrorWithCheck(callback, code, message);
                    }
                });

            }

            @Override
            public void onFailed(RTCErrorCode error) {
                VMLog.e(TAG, "enterSeat#publishDefaultLiveStreams#onFailed", error);
                onErrorWithCheck(callback, error.getValue(), error.getReason());
            }
        });
    }

    @Override
    public void leaveSeat(final RCRadioRoomCallback callback) {
        if (null == mRcrtcRoom || null == mRadioRoom || !mRadioRoom.check()) {
            onErrorWithCheck(callback, -1, "Check RTCRoom Or RadioRoom is Null ");
            return;
        }
        if (!mRadioRoom.isInSeat()) {
            onErrorWithCheck(callback, -1, "Is Not In Seat ");
            return;
        }
        // 静音
        muteSelf(true);
        // 更新KV
        updateRadioRoomKV(UpdateKey.RC_SEATING, "0", new RCRadioRoomCallback() {
            @Override
            public void onSuccess() {
                mRadioRoom.setInSeat(false);
                onSuccessWithCheck(callback);
            }

            @Override
            public void onError(int code, String message) {
                onErrorWithCheck(callback, code, message);
            }
        });
    }

    @Override
    public void muteSelf(boolean isMute) {
        RCRTCEngine.getInstance().getDefaultAudioStream().setMicrophoneDisable(isMute);
    }

    @Override
    public void updateRadioRoomKV(final UpdateKey type, final String value, final RCRadioRoomCallback callback) {
        RCMessager.getInstance().updateKV(mRadioRoom.getRoomId(), type.getValue(), value, false, false,
                new IResultBack<String>() {
                    @Override
                    public void onResult(int code, String data) {
                        VMLog.v(TAG, "notifyRadioRoom#updateKV#onResult:[" + code + "] data = " + data);
                        if (code == IResultBack.ok) {
                            onSuccessWithCheck(callback);
                            if (listener != null) {
                                main.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.onRadioRoomKVUpdate(type, value);
                                    }
                                });
                            }
                        } else {
                            onErrorWithCheck(callback, code, data);
                        }
                    }
                });
    }

    @Override
    public void getRadioRoomValue(UpdateKey key, final RCRadioRoomResultCallback<String> callback) {
        if (mRadioRoom == null) {
            onErrorWithCheck(callback, -1, "radioRoom is null");
            return;
        }
        final String noticeType = key.getValue();
        RongChatRoomClient.getInstance().getChatRoomEntry(mRadioRoom.getRoomId(), noticeType, new IRongCoreCallback.ResultCallback<Map<String, String>>() {
            @Override
            public void onSuccess(Map<String, String> stringStringMap) {
                String value = stringStringMap.get(noticeType);
                if (!TextUtils.isEmpty(value)) {
                    onSuccessWithCheck(callback, value);
                }
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                onErrorWithCheck(callback, coreErrorCode.code, coreErrorCode.msg);
            }
        });
    }

    private void listenRadio(final RCRadioRoomCallback callback) {
        VMLog.d(TAG, "onPublishCDNStream:listenRadio");
        if (null == mRcrtcRoom || null == mRadioRoom || !mRadioRoom.check()) {
            onErrorWithCheck(callback, -1, "Check RTCRoom Or RadioRoom is Null ");
            return;
        }
        RCRTCCDNInputStream stream = mRcrtcRoom.getCDNStream();
        if (null == stream) {
            onErrorWithCheck(callback, -2, "Not Find CDN Stream");
            return;
        }
        mRcrtcRoom.getLocalUser().subscribeStreams(Arrays.asList(stream), new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                onSuccessWithCheck(callback);
            }

            @Override
            public void onFailed(RTCErrorCode code) {
                VMLog.d(TAG, "onPublishCDNStream:listenRadio:" + code.getValue() + "  " + code.getReason());
                onErrorWithCheck(callback, code.getValue(), code.getReason());
            }
        });
    }

    private void release() {
        mRadioRoom = null;
        RCRTCEngine.getInstance().unregisterStatusReportListener();
        if (null != mRcrtcRoom) {
            mRcrtcRoom.unregisterRoomListener();
            mRcrtcRoom = null;
        }
        listener = null;
    }

    private void leaveRTCRoom(final RCRadioRoomCallback callback) {
        RCRTCEngine.getInstance().leaveRoom(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                release();
                RCRTCEngine.getInstance().unInit();
                onSuccessWithCheck(callback);
            }

            @Override
            public void onFailed(RTCErrorCode code) {
                VMLog.e(TAG, "leaveRTCRoom#leaveRoom", code);
                onErrorWithCheck(callback, code.getValue(), code.getReason());
            }
        });
    }

    private void joinRTCRoom(String roomId, final RCRTCLiveRole role, final RCRadioRoomCallback callback) {
        initRCRTCEngine();
        RCRTCRoomConfig config = RCRTCRoomConfig
                .Builder
                .create()
                .setRoomType(RCRTCRoomType.LIVE_AUDIO)
                .setLiveRole(role)
                .build();
        RCRTCEngine.getInstance().joinRoom(roomId, config, new IRCRTCResultDataCallback<RCRTCRoom>() {
            @Override
            public void onSuccess(RCRTCRoom rcrtcRoom) {
                mRcrtcRoom = rcrtcRoom;
                mRcrtcRoom.registerRoomListener(new RoomEventsListener());
                if (role == RCRTCLiveRole.AUDIENCE) {
                    listenRadio(null);
                }
                VMLog.d(TAG, "joinRTCRoom#joinRoom#onSuccess: role = " + role);
                onSuccessWithCheck(callback);
            }

            @Override
            public void onFailed(RTCErrorCode error) {
                VMLog.e(TAG, "joinRTCRoom#joinRoom#onFailed", error);
                onErrorWithCheck(callback, error.getValue(), error.getReason());
            }
        });
    }

    private void initRCRTCEngine() {
        RCRTCConfig.Builder builder = RCRTCConfig
                .Builder
                .create()
                .enableHardwareDecoder(true)
                .enableHardwareEncoder(true);
        String manufacturer = Build.MANUFACTURER.trim();
        if (manufacturer.contains("vivo")) {
            builder.setAudioSource(MediaRecorder.AudioSource.MIC);
        } else {
            builder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                    .enableLowLatencyRecording(true);
        }
        RCRTCConfig config = builder.build();
        RCRTCEngine.getInstance().init(RCMessager.getInstance().getApplication(), config);
    }

    private void notifyEnterOrLeave(boolean enter, final RCRadioRoomCallback callback) {
        MessageContent content;
        if (enter) {
            RCChatRoomEnter en = new RCChatRoomEnter();
            en.setUserId(RongCoreClient.getInstance().getCurrentUserId());
            en.setUserName("");
            content = en;
        } else {
            RCChatRoomLeave leave = new RCChatRoomLeave();
            leave.setUserId(RongCoreClient.getInstance().getCurrentUserId());
            leave.setUserName("");
            content = leave;
        }
        MLog.d(TAG, "notifyEnterOrLeave#roomId:" + mRadioRoom.getRoomId());
        RCMessager.getInstance().sendChatRoomMessage(mRadioRoom.getRoomId(), content, new SendMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                onSuccessWithCheck(callback);
                VMLog.d(TAG, "notifyEnterOrLeave#onSuccess");
            }

            @Override
            public void onError(Message message, int code, String reason) {
                onErrorWithCheck(callback, code, reason);
            }
        });
    }

    @Override
    public void onChatRoomKVSync(String roomId) {
        VMLog.d(TAG, "onChatRoomKVSync: " + roomId);
    }

    @Override
    public void onChatRoomKVUpdate(String roomId, final Map<String, String> chatRoomKvMap) {
        VMLog.d(TAG, "onChatRoomKVUpdate : " + "roomId = " + roomId + " size = " + chatRoomKvMap.size() + " chatRoomKvMap = " + JsonUtils.toJson(chatRoomKvMap));
        if (null == chatRoomKvMap && chatRoomKvMap.isEmpty()) {
            VMLog.d(TAG, "onChatRoomKVUpdate: KV is Empty");
            return;
        }
        if (mRadioRoom == null) {
            VMLog.d(TAG, "onChatRoomKVUpdate: mRadioRoom is null");
            return;
        }
        if (!TextUtils.equals(roomId, mRcrtcRoom.getRoomId())) {
            VMLog.d(TAG, "onChatRoomKVUpdate: roomId not equal");
            return;
        }
        if (null == listener) return;
        final Map<String, String> map = new HashMap<>();
        map.putAll(chatRoomKvMap);
        main.post(new Runnable() {
            @Override
            public void run() {
                UpdateKey[] values = UpdateKey.values();
                String value;
                for (int i = 0; i < values.length; i++) {
                    value = map.get(values[i].getValue());
                    if (value != null) {
                        listener.onRadioRoomKVUpdate(values[i], value);
                    }
                }
            }
        });
    }

    @Override
    public void onChatRoomKVRemove(String s, Map<String, String> map) {
        VMLog.d(TAG, "onChatRoomKVRemove:roomId = " + s);
    }

    @Override
    public boolean onReceived(final Message message, int i) {
        if (null != listener) main.post(new Runnable() {
            @Override
            public void run() {
                if (message.getContent() instanceof RCChatRoomEnter) {
                    RCChatRoomEnter enter = (RCChatRoomEnter) message.getContent();
                    listener.onAudienceEnter(enter.getUserId());
                } else if (message.getContent() instanceof RCChatRoomLeave) {
                    RCChatRoomLeave leave = (RCChatRoomLeave) message.getContent();
                    listener.onAudienceLeave(leave.getUserId());
                } else {
                    listener.onMessageReceived(message);
                }
            }
        });
        return false;
    }

    @Override
    public String getRoomId() {
        return null == mRadioRoom ? "" : mRadioRoom.getRoomId();
    }

    public class StateListener extends IRCRTCStatusReportListener {

        @Override
        public void onConnectionStats(final StatusReport statusReport) {
            String speaking = "0";
            for (StatusBean statusBean : statusReport.statusAudioSends.values()) {
                if (statusBean.isSend && TextUtils.equals(statusBean.mediaType, RCRTCMediaType.AUDIO.getDescription())) {
                    if (statusBean.audioLevel > 0) {
                        speaking = "1";
                    } else {
                        speaking = "0";
                    }
                }
            }
            if (listener != null) {
                if (TextUtils.equals(isSpeaking, speaking)) {
                    return;
                }
                isSpeaking = speaking;
                updateRadioRoomKV(UpdateKey.RC_SPEAKING, isSpeaking, null);
            }
        }
    }

    public class RoomEventsListener extends IRCRTCRoomEventsListener {
        @Override
        public void onPublishCDNStream(RCRTCCDNInputStream stream) {
            VMLog.d(TAG, "onPublishCDNStream:");
            if (RCRTCLiveRole.BROADCASTER == mRadioRoom.getRole()) {
                return;
            }
            listenRadio(new RCRadioRoomCallback() {
                @Override
                public void onSuccess() {
                    VMLog.d(TAG, "onPublishCDNStream:onSuccess");
                }

                @Override
                public void onError(int code, String message) {

                }
            });
        }

        @Override
        public void onRemoteUserPublishResource(RCRTCRemoteUser rcrtcRemoteUser, List<RCRTCInputStream> list) {
        }

        @Override
        public void onRemoteUserMuteAudio(RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, boolean b) {
        }

        @Override
        public void onRemoteUserMuteVideo(RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, boolean b) {
        }

        @Override
        public void onRemoteUserUnpublishResource(RCRTCRemoteUser rcrtcRemoteUser, List<RCRTCInputStream> list) {
        }

        @Override
        public void onUserJoined(RCRTCRemoteUser rcrtcRemoteUser) {
        }

        @Override
        public void onUserLeft(RCRTCRemoteUser rcrtcRemoteUser) {
        }

        @Override
        public void onUserOffline(RCRTCRemoteUser rcrtcRemoteUser) {
        }

        @Override
        public void onPublishLiveStreams(List<RCRTCInputStream> list) {
        }

        @Override
        public void onUnpublishLiveStreams(List<RCRTCInputStream> list) {
        }

        @Override
        public void onLeaveRoom(int i) {
        }
    }
}
