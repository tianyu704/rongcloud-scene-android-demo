/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.api;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

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
import cn.rongcloud.rtc.api.stream.RCRTCInputStream;
import cn.rongcloud.rtc.base.RCRTCLiveRole;
import cn.rongcloud.rtc.base.RCRTCMediaType;
import cn.rongcloud.rtc.base.RCRTCParamsType;
import cn.rongcloud.rtc.base.RCRTCRoomType;
import cn.rongcloud.rtc.base.RTCErrorCode;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomBaseCallback;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomCallback;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomEventListener;
import cn.rongcloud.voiceroom.api.callback.RCVoiceRoomResultCallback;
import cn.rongcloud.voiceroom.model.RCVoiceRoomInfo;
import cn.rongcloud.voiceroom.model.RCVoiceSeatInfo;
import cn.rongcloud.voiceroom.model.VoiceRoomErrorCode;
import cn.rongcloud.voiceroom.model.messagemodel.RCVoiceRoomInviteMessage;
import cn.rongcloud.voiceroom.model.messagemodel.RCVoiceRoomRefreshMessage;
import cn.rongcloud.voiceroom.utils.JsonUtils;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.chatroom.base.RongChatRoomClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

/**
 * 语聊房的控制
 *
 * @author gusd
 * @Date 2021/06/01
 */
class RCVoiceRoomEngineImpl extends RCVoiceRoomEngine implements IRongCoreListener.OnReceiveMessageListener, RongChatRoomClient.KVStatusListener {

    private static final String TAG = "RCVoiceRoomEngineImpl";


    private static final int MAX_MIC_QUEUE_NUMBER = 20;

    private static volatile RCVoiceRoomEngine instance;
    private WeakReference<RCVoiceRoomEventListener> mRoomEventListener;
    private final List<IRongCoreListener.OnReceiveMessageListener> mMessageReceiveListenerList;

    /**
     * 当前的用户 ID
     */
    private String mCurrentUserId;

    private RCRTCRoom mRoom;

    private RCRTCLiveRole mCurrentRole;

    private String mRoomId;

    private RCVoiceRoomInfo mRoomInfo;

    private IRCRTCVoiceRoomEventsListener mVREventListener;

    private VoiceStatusReportListener mVoiceStatusReportListener = new VoiceStatusReportListener();

    private Map<String, String> currentKvMap = new HashMap<>();

    /**
     * 采用 map 和 list 双集合保存，map 用于记录在座位上的人，用于快速查询，list 用于记录位置信息
     */
    private final Map<String, RCVoiceSeatInfo> mUserOnSeatMap;
    private final List<RCVoiceSeatInfo> mRCVoiceSeatInfoList;

    private RCVoiceRoomEngineImpl() {
        mMessageReceiveListenerList = new CopyOnWriteArrayList<>();
        mUserOnSeatMap = new ConcurrentHashMap<>();
        mRCVoiceSeatInfoList = new CopyOnWriteArrayList<>();
        mVREventListener = new IRCRTCVoiceRoomEventsListener();
    }

    public static RCVoiceRoomEngine getInstance() {
        if (instance == null) {
            synchronized (RCVoiceRoomEngine.class) {
                if (instance == null) {
                    instance = new RCVoiceRoomEngineImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public void setVoiceRoomEventListener(RCVoiceRoomEventListener listener) {
        mRoomEventListener = new WeakReference<>(listener);
    }

    @Override
    public void addMessageReceiveListener(IRongCoreListener.OnReceiveMessageListener listener) {
        if (!mMessageReceiveListenerList.contains(listener)) {
            mMessageReceiveListenerList.add(listener);
        }
    }

    @Override
    public void removeMessageReceiveListener(IRongCoreListener.OnReceiveMessageListener listener) {
        mMessageReceiveListenerList.remove(listener);
    }

    @Override
    public void initWithAppKey(Application context, String appKey) {
        RongCoreClient.setServerInfo("http://navqa.cn.ronghub.com", "upload.qiniup.com");
        RongCoreClient.init(context, appKey);
        RongCoreClient.registerMessageType(Arrays.asList(RCVoiceRoomInviteMessage.class, RCVoiceRoomRefreshMessage.class));
        RongCoreClient.setOnReceiveMessageListener(this);
        RongChatRoomClient.getInstance().setKVStatusListener(this);
    }

    private void initRCRTCEngine(Application context) {
        RCRTCConfig build = RCRTCConfig
                .Builder
                .create()
                .enableHardwareDecoder(true)
                .enableHardwareEncoder(true)
                .build();
        RCRTCEngine.getInstance().init(context, build);
    }

    @Override
    public void connectWithToken(final Application context, String appToken, final RCVoiceRoomCallback callback) {
        RongCoreClient.connect(appToken, new IRongCoreCallback.ConnectCallback() {
            @Override
            public void onSuccess(String userId) {
                Log.d(TAG, "onSuccess: userId = " + userId);
                mCurrentUserId = userId;
                initRCRTCEngine(context);
                onSuccessWithCheck(callback);
            }

            @Override
            public void onError(IRongCoreEnum.ConnectionErrorCode e) {
                Log.d(TAG, "onError: error code = " + e.getValue());
                onErrorWithCheck(callback, e.getValue(), "Init token failed");
            }

            @Override
            public void onDatabaseOpened(IRongCoreEnum.DatabaseOpenStatus code) {
                Log.d(TAG, "onDatabaseOpened: ");
            }
        });

    }

    @Override
    public void joinRoom(final String roomId, final RCVoiceRoomCallback callback) {
        mCurrentRole = RCRTCLiveRole.AUDIENCE;
        this.mRoomId = roomId;
        RongChatRoomClient.getInstance().joinChatRoom(roomId, -1, new IRongCoreCallback.OperationCallback() {
            @Override
            public void onSuccess() {
                notifyVoiceRoom(RC_AUDIENCE_JOIN_ROOM, mCurrentUserId);
                joinRTCRoom(roomId, mCurrentRole, new RCVoiceRoomCallback() {
                    @Override
                    public void onSuccess() {
                        changeUserRoleIfNeeded();
                        onSuccessWithCheck(callback);
                    }

                    @Override
                    public void onError(int code, String message) {
                        onErrorWithCheck(callback, code, message);
                    }
                });
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                onErrorWithCheck(callback, coreErrorCode.code, coreErrorCode.msg);
            }
        });
    }

    private void changeUserRoleIfNeeded() {
        int index = seatIndexWithUserSit(mCurrentUserId);
        if (index > 0 && mCurrentRole != RCRTCLiveRole.BROADCASTER) {
            switchRole(RCRTCLiveRole.BROADCASTER, new RCVoiceRoomCallback() {
                @Override
                public void onSuccess() {
                    // TODO: 2021/6/4  
                }

                @Override
                public void onError(int code, String message) {
                    // TODO: 2021/6/4  
                }
            });
        }
        RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
        if (listener != null) {
            listener.onRoomKVReady();
        }
    }

    @Override
    public void leaveRoom(final RCVoiceRoomCallback callback) {
        int index = seatIndexWithUserSit(mCurrentUserId);
        if (index >= 0) {
            leaveSeat(new RCVoiceRoomCallback() {
                @Override
                public void onSuccess() {
                    afterLeaveSeat(callback);
                }

                @Override
                public void onError(int code, String message) {
                    afterLeaveSeat(callback);
                }
            });
        } else {
            afterLeaveSeat(callback);
        }
    }

    private void afterLeaveSeat(final RCVoiceRoomCallback callback) {
        // FIXME: 2021/6/3 该方式可能导致回调走两次
        notifyVoiceRoom(RC_AUDIENCE_LEAVE_ROOM, mCurrentUserId);
        RongChatRoomClient.getInstance().quitChatRoom(mRoomId, new IRongCoreCallback.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: ");
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                Log.d(TAG, "onError : " + "coreErrorCode = " + coreErrorCode.code);
                onErrorWithCheck(callback, coreErrorCode.getValue(), coreErrorCode.getMessage());
            }
        });

        RCRTCEngine.getInstance().leaveRoom(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                clearAll();
                onSuccessWithCheck(callback);
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                onErrorWithCheck(callback, errorCode.getValue(), errorCode.getReason());
            }
        });
    }

    @Override
    public void enterSeat(final int seatIndex, final RCVoiceRoomCallback callback) {
        final RCVoiceSeatInfo seatInfo = getSeatInfoByIndex(seatIndex);
        if (seatInfo == null) {
            onErrorWithCheck(callback, VoiceRoomErrorCode.SEAT_NOT_EXIST);
            return;
        }
        if (TextUtils.equals(seatInfo.getUserId(), mCurrentUserId) && mCurrentRole == RCRTCLiveRole.AUDIENCE) {
            switchRole(RCRTCLiveRole.BROADCASTER, callback);
            return;
        }
        if (seatInfo.getStatus() != RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty) {
            onErrorWithCheck(callback, VoiceRoomErrorCode.SEAT_IS_NOT_IDLE);
            return;
        }

        if (isUserOnSeat(mCurrentUserId)) {
            onErrorWithCheck(callback, VoiceRoomErrorCode.USER_IS_ON_SEAT_NOW);
            return;
        }
        RCVoiceSeatInfo seatInfoClone = seatInfo.clone();
        seatInfoClone.setUserId(mCurrentUserId);
        seatInfoClone.setStatus(RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusUsed);
        updateKvSeatInfo(seatInfoClone, seatIndex, new RCVoiceRoomCallback() {
            @Override
            public void onSuccess() {
                userEnterSeat(seatInfo, mCurrentUserId);
                RCVoiceRoomEventListener currentRoomEventListener = getCurrentRoomEventListener();
                if (currentRoomEventListener != null) {
                    currentRoomEventListener.onUserEnterSeat(seatIndex, mCurrentUserId);
                    currentRoomEventListener.onSeatInfoUpdate(mRCVoiceSeatInfoList);
                }
                switchRole(RCRTCLiveRole.BROADCASTER, callback);
            }

            @Override
            public void onError(int code, String message) {
                onErrorWithCheck(callback, code, message);
            }
        });

    }

    @Override
    public void leaveSeat(final RCVoiceRoomCallback callback) {
        final int seatIndex = seatIndexWithUserSit(mCurrentUserId);
        if (seatIndex < 0) {
            callback.onError(-1, "current user not in seat");
            return;
        }
        final RCVoiceSeatInfo info = mRCVoiceSeatInfoList.get(seatIndex);
        if (info != null) {
            RCVoiceSeatInfo seatClone = info.clone();
            seatClone.setUserId(null);
            seatClone.setStatus(RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty);
            updateKvSeatInfo(seatClone, seatIndex, new RCVoiceRoomCallback() {
                @Override
                public void onSuccess() {
                    userLeaveSeat(info);
                    RCVoiceRoomEventListener currentRoomEventListener = getCurrentRoomEventListener();
                    if (currentRoomEventListener != null) {
                        currentRoomEventListener.onUserLeaveSeat(seatIndex, mCurrentUserId);
                        currentRoomEventListener.onSeatInfoUpdate(mRCVoiceSeatInfoList);
                    }
                    switchRole(RCRTCLiveRole.AUDIENCE, callback);
                }

                @Override
                public void onError(int code, String message) {
                    Log.d(TAG, "onError : " + "code = " + code + "," + "message = " + message);
                    onErrorWithCheck(callback, code, message);
                }
            });
        }
    }

    @Override
    public void switchSeatTo(final int seatIndex, final RCVoiceRoomCallback callback) {
        // FIXME: 2021/6/3 可能多次回调
        final RCVoiceSeatInfo preSeatInfo = getSeatInfoByUserId(mCurrentUserId);
        int preIndex = getIndexBySeatInfo(preSeatInfo);
        if (preSeatInfo == null) {
            onErrorWithCheck(callback, VoiceRoomErrorCode.USER_NOT_ON_SEAT_NOW);
            return;
        }
        if (!containSeatIndex(seatIndex)) {
            onErrorWithCheck(callback, VoiceRoomErrorCode.TARGET_SEAT_NO_IN_RANGE);
            return;
        }
        final RCVoiceSeatInfo targetSeatInfo = getSeatInfoByIndex(seatIndex);
        if (targetSeatInfo == null) {
            onErrorWithCheck(callback, VoiceRoomErrorCode.TARGET_SEAT_NO_IN_RANGE);
            return;
        }

        if (targetSeatInfo.getStatus() != RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty) {
            onErrorWithCheck(callback, VoiceRoomErrorCode.SEAT_IS_NOT_IDLE);
            return;
        }
        RCVoiceSeatInfo preSeatClone = preSeatInfo.clone();
        preSeatClone.setUserId(null);
        preSeatClone.setStatus(RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty);

        updateKvSeatInfo(preSeatClone, preIndex, new RCVoiceRoomCallback() {
            @Override
            public void onSuccess() {
                userLeaveSeat(preSeatInfo);
                RCVoiceRoomEventListener currentRoomEventListener = getCurrentRoomEventListener();
                if (currentRoomEventListener != null) {
                    currentRoomEventListener.onUserLeaveSeat(seatIndex, mCurrentUserId);
                    currentRoomEventListener.onSeatInfoUpdate(mRCVoiceSeatInfoList);
                }

            }

            @Override
            public void onError(int code, String message) {
                onErrorWithCheck(callback, code, message);
            }
        });

        RCVoiceSeatInfo targetInfoClone = targetSeatInfo.clone();
        targetInfoClone.setUserId(mCurrentUserId);
        targetInfoClone.setStatus(RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusUsed);

        updateKvSeatInfo(targetInfoClone, seatIndex, new RCVoiceRoomCallback() {
            @Override
            public void onSuccess() {
                userEnterSeat(targetSeatInfo, mCurrentUserId);
                RCVoiceRoomEventListener currentRoomEventListener = getCurrentRoomEventListener();
                if (currentRoomEventListener != null) {
                    currentRoomEventListener.onUserEnterSeat(seatIndex, mCurrentUserId);
                    currentRoomEventListener.onSeatInfoUpdate(mRCVoiceSeatInfoList);
                }
                muteSelfIfNeed();
                onSuccessWithCheck(callback);
            }

            @Override
            public void onError(int code, String message) {
                onErrorWithCheck(callback, code, message);
            }
        });

        muteSelfIfNeed();
    }

    private void muteSelfIfNeed() {
        RCVoiceSeatInfo seatInfo = getSeatInfoByUserId(mCurrentUserId);
        if (seatInfo != null) {
            disableAudioRecording(seatInfo.isMute());
        }
    }

    @Override
    public void pickUserToSeat(String userId, final RCVoiceRoomCallback callback) {
        if (isUserOnSeat(userId)) {
            onErrorWithCheck(callback, VoiceRoomErrorCode.USER_IS_ON_SEAT_NOW);
            return;
        }
        if (!TextUtils.isEmpty(mCurrentUserId) && TextUtils.equals(mCurrentUserId, userId)) {
            onErrorWithCheck(callback, VoiceRoomErrorCode.USER_CAN_NOT_PICK_SELF_ON_SEAT);
            return;
        }
        String uuid = UUID.randomUUID().toString();
        RCVoiceRoomInviteMessage inviteMessage = new RCVoiceRoomInviteMessage();
        inviteMessage.setSendUserId(mCurrentUserId);
        inviteMessage.setType(RCVoiceRoomInviteMessage.RCInviteCmdType.RCInviteCmdTypeRequest);
        inviteMessage.setContent(RC_PICKER_USER_SEAT_CONTENT);
        inviteMessage.setInvitationId(uuid);
        inviteMessage.setTargetId(userId);
        RongCoreClient.getInstance().sendMessage(Conversation.ConversationType.CHATROOM, mRoomId, inviteMessage, null, null, new IRongCoreCallback.ISendMessageCallback() {

            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                onSuccessWithCheck(callback);
            }

            @Override
            public void onError(Message message, IRongCoreEnum.CoreErrorCode coreErrorCode) {
                onErrorWithCheck(callback, coreErrorCode.getValue(), coreErrorCode.getMessage());
            }
        });
    }

    @Override
    public void kickSeatFromSeat(final String userId, final RCVoiceRoomCallback callback) {
        final RCVoiceSeatInfo seatInfo = getSeatInfoByUserId(userId);
        if (seatInfo == null) {
            onErrorWithCheck(callback, VoiceRoomErrorCode.USER_NOT_ON_SEAT_NOW);
            return;
        }
        if (!TextUtils.isEmpty(mCurrentUserId) && TextUtils.equals(mCurrentUserId, userId)) {
            onErrorWithCheck(callback, VoiceRoomErrorCode.USER_CAN_NOT_KICK_SELF);
            return;
        }
        final int index = seatIndexWithUserSit(seatInfo.getUserId());
        if (index >= 0) {
            RCVoiceSeatInfo seatInfoClone = seatInfo.clone();
            seatInfoClone.setUserId(null);
            seatInfoClone.setStatus(RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty);
            updateKvSeatInfo(seatInfoClone, index, new RCVoiceRoomCallback() {
                @Override
                public void onSuccess() {
                    userLeaveSeat(seatInfo);
                    RCVoiceRoomEventListener roomEventListener = getCurrentRoomEventListener();
                    if (roomEventListener != null) {
                        roomEventListener.onUserLeaveSeat(index, userId);
                        roomEventListener.onSeatInfoUpdate(mRCVoiceSeatInfoList);
                    }
                    onSuccessWithCheck(callback);
                }

                @Override
                public void onError(int code, String message) {
                    onErrorWithCheck(callback, code, message);
                }
            });
        }
    }

    @Override
    public void kickUserFromRoom(final String userId, final RCVoiceRoomCallback callback) {
        String uuid = UUID.randomUUID().toString();
        RCVoiceRoomInviteMessage message = new RCVoiceRoomInviteMessage();
        message.setSendUserId(mCurrentUserId);
        message.setType(RCVoiceRoomInviteMessage.RCInviteCmdType.RCInviteCmdTypeRequest);
        message.setInvitationId(uuid);
        message.setTargetId(userId);
        message.setContent(RC_KICK_USER_OUT_ROOM_CONTENT);
        RongCoreClient.getInstance().sendMessage(Conversation.ConversationType.CHATROOM, mRoomId, message, null, null, new IRongCoreCallback.ISendMessageCallback() {

            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
                if (listener != null) {
                    listener.onUserReceiveKickOutRoom(userId, mCurrentUserId);
                }
                onSuccessWithCheck(callback);
            }

            @Override
            public void onError(Message message, IRongCoreEnum.CoreErrorCode coreErrorCode) {
                onErrorWithCheck(callback, coreErrorCode.getValue(), coreErrorCode.getMessage());
            }
        });

    }

    @Override
    public void lockSeat(final int seatIndex, final boolean isLocked, final RCVoiceRoomCallback callback) {
        final RCVoiceSeatInfo seatInfo = getSeatInfoByIndex(seatIndex);
        if (seatInfo == null) {
            onErrorWithCheck(callback, VoiceRoomErrorCode.SEAT_NOT_EXIST);
            return;
        }
        if (seatInfo.getStatus() == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusUsed || seatInfo.getUserId() != null) {
            onErrorWithCheck(callback, VoiceRoomErrorCode.SEAT_IS_NOT_IDLE);
            return;
        }

        final RCVoiceSeatInfo seatInfoClone = seatInfo.clone();
        seatInfoClone.setStatus(isLocked ? RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusLock : RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty);
        updateKvSeatInfo(seatInfoClone, seatIndex, new RCVoiceRoomCallback() {
            @Override
            public void onSuccess() {
                seatInfo.setStatus(seatInfoClone.getStatus());
                RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
                if (listener != null) {
                    listener.onSeatLock(seatIndex, isLocked);
                    listener.onSeatInfoUpdate(mRCVoiceSeatInfoList);
                }
                onSuccessWithCheck(callback);
            }

            @Override
            public void onError(int code, String message) {
                onErrorWithCheck(callback, code, message);
            }
        });
    }

    @Override
    public void muteSeat(final int seatIndex, final boolean isMute, final RCVoiceRoomCallback callback) {
        final RCVoiceSeatInfo seatInfo = getSeatInfoByIndex(seatIndex);
        if (seatInfo == null) {
            onErrorWithCheck(callback, VoiceRoomErrorCode.SEAT_NOT_EXIST);
            return;
        }
        final RCVoiceSeatInfo seatInfoClone = seatInfo.clone();
        seatInfoClone.setMute(isMute);
        updateKvSeatInfo(seatInfoClone, seatIndex, new RCVoiceRoomCallback() {
            @Override
            public void onSuccess() {
                seatInfo.setMute(seatInfoClone.isMute());
                RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
                if (listener != null) {
                    listener.onSeatMute(seatIndex, isMute);
                    listener.onSeatInfoUpdate(mRCVoiceSeatInfoList);
                }
                onSuccessWithCheck(callback);
            }

            @Override
            public void onError(int code, String message) {
                onErrorWithCheck(callback, code, message);
            }
        });
    }

    @Override
    public void muteOtherSeats(boolean isMute) {
        mRoomInfo.setMuteAll(isMute);
        final List<RCVoiceSeatInfo> list = new ArrayList<>();
        for (int i = 0; i < mRCVoiceSeatInfoList.size(); i++) {
            final RCVoiceSeatInfo seatInfo = mRCVoiceSeatInfoList.get(i);
            if (seatInfo == null) {
                continue;
            }
            if (!TextUtils.isEmpty(seatInfo.getUserId()) &&
                    TextUtils.equals(seatInfo.getUserId(), mCurrentUserId)) {

                // 不处理自己
                continue;
            }
            list.add(mRCVoiceSeatInfoList.get(i));
        }

        final AtomicInteger requestingCount = new AtomicInteger(list.size());
        for (int i = 0; i < list.size(); i++) {
            final RCVoiceSeatInfo seatInfo = list.get(i);
            final RCVoiceSeatInfo seatInfoClone = seatInfo.clone();
            seatInfoClone.setMute(isMute);
            int index = getIndexBySeatInfo(seatInfo);
            if (index < 0) {
                requestingCount.decrementAndGet();
                continue;
            }
            updateKvSeatInfo(seatInfoClone, index, new RCVoiceRoomCallback() {
                @Override
                public void onSuccess() {
                    seatInfo.setMute(seatInfoClone.isMute());
                    if (requestingCount.decrementAndGet() == 0) {
                        RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
                        if (listener != null) {
                            listener.onSeatInfoUpdate(mRCVoiceSeatInfoList);
                            listener.onRoomInfoUpdate(mRoomInfo);
                        }
                    }
                }

                @Override
                public void onError(int code, String message) {
                    if (requestingCount.decrementAndGet() == 0) {
                        RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
                        if (listener != null) {
                            listener.onSeatInfoUpdate(mRCVoiceSeatInfoList);
                            listener.onRoomInfoUpdate(mRoomInfo);
                        }
                    }
                }
            });
        }
    }


    @Override
    public void muteAllRemoteStreams(boolean isMute) {
        if (mRoom != null) {
            mRoom.muteAllRemoteAudio(isMute);
        }
    }

    @Override
    public void lockOtherSeats(boolean isLock) {
        mRoomInfo.setLockAll(isLock);
        List<RCVoiceSeatInfo> list = new ArrayList<>();
        for (int i = 0; i < mRCVoiceSeatInfoList.size(); i++) {
            RCVoiceSeatInfo seatInfo = getSeatInfoByIndex(i);
            if (seatInfo == null) {
                continue;
            }
            if (seatInfo.getStatus() == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusUsed || seatInfo.getUserId() != null) {
                continue;
            }
            list.add(seatInfo);
        }

        final AtomicInteger atomicInteger = new AtomicInteger(list.size());
        for (int i = 0; i < list.size(); i++) {
            final RCVoiceSeatInfo seatInfo = list.get(i);
            final RCVoiceSeatInfo seatInfoClone = seatInfo.clone();
            seatInfoClone.setStatus(isLock ? RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusLock : RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty);
            int index = getIndexBySeatInfo(seatInfo);
            if (index < 0) {
                atomicInteger.decrementAndGet();
                continue;
            }
            updateKvSeatInfo(seatInfoClone, index, new RCVoiceRoomCallback() {
                @Override
                public void onSuccess() {
                    seatInfo.setStatus(seatInfoClone.getStatus());
                    if (atomicInteger.decrementAndGet() == 0) {
                        RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
                        if (listener != null) {
                            listener.onSeatInfoUpdate(mRCVoiceSeatInfoList);
                            listener.onRoomInfoUpdate(mRoomInfo);
                        }
                    }
                }

                @Override
                public void onError(int code, String message) {
                    if (atomicInteger.decrementAndGet() == 0) {
                        RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
                        if (listener != null) {
                            listener.onSeatInfoUpdate(mRCVoiceSeatInfoList);
                            listener.onRoomInfoUpdate(mRoomInfo);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void sendMessage(MessageContent message, final RCVoiceRoomCallback callback) {
        RongCoreClient.getInstance().sendMessage(Conversation.ConversationType.CHATROOM, mRoomId, message, null, null, new IRongCoreCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                onSuccessWithCheck(callback);
            }

            @Override
            public void onError(Message message, IRongCoreEnum.CoreErrorCode coreErrorCode) {
                onErrorWithCheck(callback, coreErrorCode.getValue(), coreErrorCode.getMessage());
            }
        });
    }

    @Override
    public void setRoomInfo(final RCVoiceRoomInfo roomInfo, final RCVoiceRoomCallback callback) {
        if (roomInfo.getSeatCount() != mRCVoiceSeatInfoList.size()) {
            resetSeatCount(roomInfo.getSeatCount(), new CompletionListener() {

                @Override
                public void onComplete() {
                    roomInfo.setMuteAll(false);
                    roomInfo.setLockAll(false);
                    updateKvRoomInfo(roomInfo, new RCVoiceRoomCallback() {
                        @Override
                        public void onSuccess() {
                            mRoomInfo = roomInfo;
                            onSuccessWithCheck(callback);
                            RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
                            if (listener != null) {
                                listener.onRoomInfoUpdate(roomInfo);
                            }
                        }

                        @Override
                        public void onError(int code, String message) {
                            onErrorWithCheck(callback, code, message);
                        }
                    });
                }
            });
        } else {
            updateKvRoomInfo(roomInfo, new RCVoiceRoomCallback() {
                @Override
                public void onSuccess() {
                    mRoomInfo = roomInfo;
                    onSuccessWithCheck(callback);
                    RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
                    if (listener != null) {
                        listener.onRoomInfoUpdate(roomInfo);
                    }
                }

                @Override
                public void onError(int code, String message) {
                    onErrorWithCheck(callback, code, message);
                }
            });
        }

    }

    private void resetSeatCount(final int seatCount, final CompletionListener completionListener) {
        synchronized (TAG) {
            final List<RCVoiceSeatInfo> list = new ArrayList<>();
            int maxCount = Math.max(mRoomInfo.getSeatCount(), seatCount);
            for (int i = 0; i < maxCount; i++) {
                if (i == seatIndexWithUserSit(mCurrentUserId)) {
                    list.add(getSeatInfoByIndex(i));
                    continue;
                }
                RCVoiceSeatInfo info = new RCVoiceSeatInfo();
                list.add(info);
            }

            final AtomicInteger seatCountTemp = new AtomicInteger(list.size());
            for (int i = 0; i < list.size(); i++) {
                updateKvSeatInfo(list.get(i), i, new RCVoiceRoomCallback() {
                    @Override
                    public void onSuccess() {
                        if (seatCountTemp.decrementAndGet() == 0) {
                            handleSeatCountChange(list, seatCount, completionListener);
                        }
                    }

                    @Override
                    public void onError(int code, String message) {
                        if (seatCountTemp.decrementAndGet() == 0) {
                            handleSeatCountChange(list, seatCount, completionListener);
                        }
                    }
                });
            }
        }
    }

    private void handleSeatCountChange(List<RCVoiceSeatInfo> list, int seatCount, CompletionListener completionListener) {
        synchronized (TAG) {
            List<RCVoiceSeatInfo> seatInfoList = list.subList(0, seatCount);
            mRCVoiceSeatInfoList.clear();
            mRCVoiceSeatInfoList.addAll(seatInfoList);
            mUserOnSeatMap.clear();
            for (RCVoiceSeatInfo seatInfo : seatInfoList) {
                if (!TextUtils.isEmpty(seatInfo.getUserId())) {
                    mUserOnSeatMap.put(seatInfo.getUserId(), seatInfo);
                }
            }
            RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
            if (listener != null) {
                listener.onSeatInfoUpdate(mRCVoiceSeatInfoList);
            }
            completionListener.onComplete();
        }
    }


    @Override
    public void disableAudioRecording(boolean isDisable) {
        RCRTCEngine.getInstance().getDefaultAudioStream().setMicrophoneDisable(isDisable);
    }

    @Override
    public void setAudioQuality(RCRTCParamsType.AudioQuality audioQuality, RCRTCParamsType.AudioScenario scenario) {
        RCRTCEngine.getInstance().getDefaultAudioStream().setAudioQuality(audioQuality, scenario);
    }

    @Override
    public void enableSpeaker(boolean isEnable) {
        RCRTCEngine.getInstance().enableSpeaker(isEnable);
    }

    @Override
    public void requestSeat(final RCVoiceRoomCallback callback) {
        RongChatRoomClient.getInstance().getAllChatRoomEntries(mRoomId, new IRongCoreCallback.ResultCallback<Map<String, String>>() {
            @Override
            public void onSuccess(Map<String, String> stringStringMap) {
                List<String> requestKeys = new ArrayList<>();
                for (String key : stringStringMap.keySet()) {
                    if (key.contains(mCurrentUserId) && RC_REQUEST_SEAT_CONTENT_REQUEST.equals(stringStringMap.get(key))) {
                        onErrorWithCheck(callback, VoiceRoomErrorCode.JOIN_MIC_QUEUE_ALREADY);
                        return;
                    }
                    if (key.startsWith(RC_REQUEST_SEAT_PREFIX_KEY)) {
                        requestKeys.add(key);
                    }
                }

                if (requestKeys.size() > MAX_MIC_QUEUE_NUMBER) {
                    onErrorWithCheck(callback, VoiceRoomErrorCode.TOO_MUCH_PEOPLE_IN_MIC_QUEUE);
                    return;
                }
                updateRequestSeatKvWithUserId(mCurrentUserId, RC_REQUEST_SEAT_CONTENT_REQUEST, callback);
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode e) {
                onErrorWithCheck(callback, e.getValue(), e.getMessage());
            }
        });
    }

    @Override
    public void cancelRequestSeat(final RCVoiceRoomCallback callback) {
        RongChatRoomClient.getInstance().removeChatRoomEntry(mRoomId, requestSeatKvKey(mCurrentUserId), false, "", new IRongCoreCallback.OperationCallback() {
            @Override
            public void onSuccess() {
                onSuccessWithCheck(callback);
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                onErrorWithCheck(callback, coreErrorCode.getValue(), coreErrorCode.getMessage());
            }
        });
    }

    @Override
    public void acceptRequestSeat(String userId, RCVoiceRoomCallback callback) {
        updateRequestSeatKvWithUserId(userId, RC_REQUEST_SEAT_CONTENT_ACCEPT, callback);
    }

    @Override
    public void getRequestSeatUserIds(final RCVoiceRoomResultCallback<List<String>> callback) {
        RongChatRoomClient.getInstance().getAllChatRoomEntries(mRoomId, new IRongCoreCallback.ResultCallback<Map<String, String>>() {
            @Override
            public void onSuccess(Map<String, String> stringStringMap) {
                List<String> result = new ArrayList<>();
                for (String key : stringStringMap.keySet()) {
                    if (key.startsWith(RC_REQUEST_SEAT_PREFIX_KEY)) {
                        String[] list = key.split("_");
                        if (list.length == 2 && RC_REQUEST_SEAT_CONTENT_REQUEST.equals(stringStringMap.get(key))) {
                            result.add(list[1]);
                        }
                    }
                }
                onSuccessWithCheck(callback, result);
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode e) {
                onErrorWithCheck(callback, e.getValue(), e.getMessage());
            }
        });
    }

    @Override
    public void sendInvitation(String content, final RCVoiceRoomResultCallback<String> callback) {
        final String uuid = UUID.randomUUID().toString();
        RCVoiceRoomInviteMessage inviteMessage = new RCVoiceRoomInviteMessage();
        inviteMessage.setInvitationId(uuid);
        inviteMessage.setSendUserId(mCurrentUserId);
        inviteMessage.setType(RCVoiceRoomInviteMessage.RCInviteCmdType.RCInviteCmdTypeRequest);
        inviteMessage.setContent(content);
        RongCoreClient.getInstance().sendMessage(Conversation.ConversationType.CHATROOM, mRoomId, inviteMessage, null, null, new IRongCoreCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                onSuccessWithCheck(callback, uuid);
            }

            @Override
            public void onError(Message message, IRongCoreEnum.CoreErrorCode coreErrorCode) {
                onErrorWithCheck(callback, coreErrorCode.getValue(), coreErrorCode.getMessage());
            }
        });
    }

    @Override
    public void rejectInvitation(String invitationId, final RCVoiceRoomCallback callback) {
        RCVoiceRoomInviteMessage inviteMessage = new RCVoiceRoomInviteMessage();
        inviteMessage.setInvitationId(invitationId);
        inviteMessage.setSendUserId(mCurrentUserId);
        inviteMessage.setType(RCVoiceRoomInviteMessage.RCInviteCmdType.RCInviteCmdTypeReject);
        RongCoreClient.getInstance().sendMessage(Conversation.ConversationType.CHATROOM,
                mRoomId,
                inviteMessage,
                null,
                null,
                new IRongCoreCallback.ISendMessageCallback() {
                    @Override
                    public void onAttached(Message message) {

                    }

                    @Override
                    public void onSuccess(Message message) {
                        onSuccessWithCheck(callback);
                    }

                    @Override
                    public void onError(Message message, IRongCoreEnum.CoreErrorCode coreErrorCode) {
                        onErrorWithCheck(callback, coreErrorCode.getValue(), coreErrorCode.getMessage());
                    }
                });
    }

    @Override
    public void acceptInvitation(String invitationId, final RCVoiceRoomCallback callback) {
        RCVoiceRoomInviteMessage inviteMessage = new RCVoiceRoomInviteMessage();
        inviteMessage.setInvitationId(invitationId);
        inviteMessage.setSendUserId(mCurrentUserId);
        inviteMessage.setType(RCVoiceRoomInviteMessage.RCInviteCmdType.RCInviteCmdTypeAccept);
        RongCoreClient.getInstance().sendMessage(Conversation.ConversationType.CHATROOM,
                mRoomId,
                inviteMessage,
                null,
                null,
                new IRongCoreCallback.ISendMessageCallback() {
                    @Override
                    public void onAttached(Message message) {

                    }

                    @Override
                    public void onSuccess(Message message) {
                        onSuccessWithCheck(callback);
                    }

                    @Override
                    public void onError(Message message, IRongCoreEnum.CoreErrorCode coreErrorCode) {
                        onErrorWithCheck(callback, coreErrorCode.getValue(), coreErrorCode.getMessage());
                    }
                });
    }

    @Override
    public void cancelInvitation(String invitationId, final RCVoiceRoomCallback callback) {
        RCVoiceRoomInviteMessage inviteMessage = new RCVoiceRoomInviteMessage();
        inviteMessage.setInvitationId(invitationId);
        inviteMessage.setSendUserId(mCurrentUserId);
        inviteMessage.setType(RCVoiceRoomInviteMessage.RCInviteCmdType.RCInviteCmdTypeCancel);
        RongCoreClient.getInstance().sendMessage(Conversation.ConversationType.CHATROOM,
                mRoomId,
                inviteMessage,
                null,
                null,
                new IRongCoreCallback.ISendMessageCallback() {
                    @Override
                    public void onAttached(Message message) {

                    }

                    @Override
                    public void onSuccess(Message message) {
                        onSuccessWithCheck(callback);
                    }

                    @Override
                    public void onError(Message message, IRongCoreEnum.CoreErrorCode coreErrorCode) {
                        onErrorWithCheck(callback, coreErrorCode.getValue(), coreErrorCode.getMessage());
                    }
                });
    }

    @Override
    public void notifyVoiceRoom(String name, String content) {
        RCVoiceRoomRefreshMessage refreshMessage = new RCVoiceRoomRefreshMessage();
        refreshMessage.setName(name);
        refreshMessage.setContent(content);
        sendMessage(refreshMessage, new RCVoiceRoomCallback() {
            @Override
            public void onSuccess() {
                // TODO: 2021/6/3 啥都不做？
            }

            @Override
            public void onError(int code, String message) {

            }
        });
    }

    @Override
    public void disConnect() {
        RongCoreClient.getInstance().disconnect(true);
    }

    private RCVoiceRoomEventListener getCurrentRoomEventListener() {
        if (mRoomEventListener == null) {
            return null;
        }
        return mRoomEventListener.get();
    }

    @Override
    public boolean onReceived(Message message, int left) {
        if (ifCouldTransfer(message)) {
            for (IRongCoreListener.OnReceiveMessageListener onReceiveMessageListener : mMessageReceiveListenerList) {
                onReceiveMessageListener.onReceived(message, left);
            }
            RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
            if (listener != null) {
                listener.onMessageReceived(message);
            }
        } else {
            handleInvitationMessage(message);
            handleRefreshMessage(message);
        }
        return false;
    }

    private boolean ifCouldTransfer(Message message) {
        return !(message.getContent() instanceof RCVoiceRoomRefreshMessage || message.getContent() instanceof RCVoiceRoomInviteMessage);
    }

    private void handleRefreshMessage(Message message) {
        if (message.getContent() instanceof RCVoiceRoomRefreshMessage) {
            if (message.getConversationType() == Conversation.ConversationType.CHATROOM && TextUtils.equals(message.getTargetId(), mRoomId)) {
                RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
                if (listener == null) {
                    return;
                }
                RCVoiceRoomRefreshMessage refreshMessage = (RCVoiceRoomRefreshMessage) message.getContent();
                if (RC_AUDIENCE_JOIN_ROOM.equals(refreshMessage.getName())) {
                    listener.onAudienceEnter(refreshMessage.getContent());
                } else if (RC_AUDIENCE_LEAVE_ROOM.equals(refreshMessage.getName())) {
                    listener.onAudienceExit(refreshMessage.getContent());
                }
                if (!TextUtils.isEmpty(refreshMessage.getName()) && refreshMessage.getName().contains(RC_USER_ON_SEAT_SPEAKING_KEY)) {
                    String[] list = refreshMessage.getName().split("_");
                    if (list.length == 2) {
                        int seatIndex = Integer.parseInt(list[1]);
                        RCVoiceSeatInfo seatInfo = getSeatInfoByIndex(seatIndex);
                        boolean isSpeaking = "1".equals(refreshMessage.getContent());
                        if (seatInfo != null) {
                            seatInfo.setSpeaking(isSpeaking);
                        }
                        if (containSeatIndex(seatIndex)) {
                            listener.onSpeakingStateChanged(seatIndex, isSpeaking);
                        }
                    }
                }
                listener.onRoomNotificationReceived(refreshMessage.getName(), refreshMessage.getContent());
            }
        }
    }

    private void handleInvitationMessage(Message message) {
        if (message.getContent() instanceof RCVoiceRoomInviteMessage) {
            if (message.getConversationType() == Conversation.ConversationType.CHATROOM && TextUtils.equals(message.getTargetId(), mRoomId)) {
                RCVoiceRoomInviteMessage inviteMessage = (RCVoiceRoomInviteMessage) message.getContent();
                RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
                if (listener == null) {
                    return;
                }
                switch (inviteMessage.getType()) {
                    case RCInviteCmdTypeRequest:
                        if (RC_PICKER_USER_SEAT_CONTENT.equals(inviteMessage.getContent()) && TextUtils.equals(inviteMessage.getTargetId(), mCurrentUserId)) {
                            listener.onPickSeatReceivedFrom(inviteMessage.getSendUserId());
                            break;
                        }
                        if (RC_KICK_USER_OUT_ROOM_CONTENT.equals(inviteMessage.getContent())) {
                            listener.onUserReceiveKickOutRoom(inviteMessage.getTargetId(), inviteMessage.getSendUserId());
                            break;
                        }
                        listener.onInvitationReceived(inviteMessage.getInvitationId(), inviteMessage.getSendUserId(), inviteMessage.getContent());
                        break;
                    case RCInviteCmdTypeAccept:
                        listener.onInvitationAccepted(inviteMessage.getInvitationId());
                        break;
                    case RCInviteCmdTypeCancel:
                        listener.onInvitationCancelled(inviteMessage.getInvitationId());
                        break;
                    case RCInviteCmdTypeReject:
                        listener.onInvitationRejected(inviteMessage.getInvitationId());
                        break;
                    default:
                }
            }
        }
    }

    @Override
    public void onChatRoomKVSync(String roomId) {

    }

    @Override
    public void onChatRoomKVUpdate(String roomId, Map<String, String> chatRoomKvMap) {
        Log.d(TAG, "onChatRoomKVUpdate : " + "roomId = " + roomId + "," + "chatRoomKvMap = " + chatRoomKvMap);
        for (String key : chatRoomKvMap.keySet()) {
            if (key.contains(RC_ROOM_INFO_KEY) || key.contains(RC_MIC_SEAT_INFO_PREFIX_KEY)) {
                currentKvMap.put(key, chatRoomKvMap.get(key));
            }
        }
        updateRoomInfoFromEntry(currentKvMap);
        resetSeatInfoWithCount(mRoomInfo.getSeatCount());
        updateSeatInfoFromEntry(currentKvMap);
        handleRequestSeatKvUpdated(chatRoomKvMap);
    }

    private void handleRequestSeatKvUpdated(Map<String, String> chatRoomKvMap) {
        boolean hasUserWaitingSeat = false;
        for (String key : chatRoomKvMap.keySet()) {
            if (key.startsWith(RC_REQUEST_SEAT_PREFIX_KEY)) {
                String content = chatRoomKvMap.get(key);
                if (TextUtils.equals(content, RC_REQUEST_SEAT_CONTENT_REQUEST)) {
                    hasUserWaitingSeat = true;
                }
                String[] list = key.split("_");
                if (list.length == 2) {
                    String userId = list[1];
                    if (TextUtils.equals(userId, mCurrentUserId)) {
                        if (RC_REQUEST_SEAT_CONTENT_ACCEPT.equals(content)) {
                            RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
                            if (listener != null) {
                                listener.onRequestSeatAccepted();
                            }
                            forceRemoveKey(key);
                        } else if (RC_REQUEST_SEAT_CONTENT_DENY.equals(content)) {
                            RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
                            if (listener != null) {
                                listener.onRequestSeatRejected();
                            }
                            forceRemoveKey(key);
                        }
                    }
                }
            }
        }
        if (hasUserWaitingSeat) {
            RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
            if (listener != null) {
                listener.onRequestSeatListChanged();
            }
        }
    }

    private void forceRemoveKey(String key) {
        RongChatRoomClient.getInstance().forceRemoveChatRoomEntry(mRoomId, key, false, "", new IRongCoreCallback.OperationCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {

            }
        });
    }

    private void updateSeatInfoFromEntry(Map<String, String> chatRoomKvMap) {
        synchronized (TAG) {
            List<RCVoiceSeatInfo> oldInfoList = new ArrayList<>(mRCVoiceSeatInfoList);
            List<RCVoiceSeatInfo> latestInfoList = latestMicInfoListFromEntry(chatRoomKvMap);

            RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
            if (listener != null) {
                listener.onSeatInfoUpdate(latestInfoList);
            }

            for (int i = 0; i < mRoomInfo.getSeatCount(); i++) {
                RCVoiceSeatInfo newInfo = latestInfoList.get(i);
                RCVoiceSeatInfo oldInfo = oldInfoList.get(i);
                if (oldInfo.getStatus() != newInfo.getStatus()) {
                    switch (newInfo.getStatus()) {
                        case RCSeatStatusEmpty:
                            if (oldInfo.getStatus() == RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusLock) {
                                oldInfo.setStatus(RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty);
                                if (listener != null) {
                                    listener.onSeatLock(i, false);
                                }
                            } else {
                                if (TextUtils.equals(oldInfo.getUserId(), mCurrentUserId)) {
                                    oldInfo.setUserId(null);
                                    if (listener != null) {
                                        listener.onKickSeatReceived(i);
                                    }
                                    Log.d(TAG, "updateSeatInfoFromEntry: switchRole to Audience");
                                    switchRole(RCRTCLiveRole.AUDIENCE, null);
                                } else {
                                    String userId = oldInfo.getUserId();
                                    oldInfo.setUserId(null);
                                    if (listener != null) {
                                        listener.onUserLeaveSeat(i, userId);
                                    }
                                }

                            }
                            break;
                        case RCSeatStatusUsed:
                            oldInfo.setUserId(newInfo.getUserId());
                            if (listener != null) {
                                listener.onUserEnterSeat(i, newInfo.getUserId());
                            }
                            break;
                        case RCSeatStatusLock:
                            oldInfo.setStatus(RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusLock);
                            if (listener != null) {
                                listener.onSeatLock(i, true);
                            }
                            break;
                    }
                }
                if (oldInfo.isMute() != newInfo.isMute()) {
                    if (listener != null) {
                        listener.onSeatMute(i, newInfo.isMute());
                    }
                }
            }
            mRCVoiceSeatInfoList.clear();
            mRCVoiceSeatInfoList.addAll(latestInfoList);
            mUserOnSeatMap.clear();
            for (RCVoiceSeatInfo seatInfo : mRCVoiceSeatInfoList) {
                if (!TextUtils.isEmpty(seatInfo.getUserId())) {
                    mUserOnSeatMap.put(seatInfo.getUserId(), seatInfo);
                }
            }
            muteSelfIfNeed();
        }
    }

    private void resetSeatInfoWithCount(int seatCount) {
        if (seatCount != mRCVoiceSeatInfoList.size()) {
            synchronized (TAG) {
                mUserOnSeatMap.clear();
                mRCVoiceSeatInfoList.clear();
                for (int i = 0; i < seatCount; i++) {
                    RCVoiceSeatInfo seatInfo = new RCVoiceSeatInfo();
                    seatInfo.setStatus(RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty);
                    mRCVoiceSeatInfoList.add(seatInfo);
                }
            }
        }
    }

    private void updateRoomInfoFromEntry(Map<String, String> chatRoomKvMap) {
        if (chatRoomKvMap.containsKey(RC_ROOM_INFO_KEY)) {
            String infoJsonString = chatRoomKvMap.get(RC_ROOM_INFO_KEY);
            RCVoiceRoomInfo info = JsonUtils.fromJson(infoJsonString, RCVoiceRoomInfo.class);
            mRoomInfo = info;
            RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
            if (listener != null) {
                listener.onRoomInfoUpdate(info.clone());
            }
        }
    }

    @Override
    public void onChatRoomKVRemove(String roomId, Map<String, String> chatRoomKvMap) {
        handleRequestSeatCancelled(chatRoomKvMap);
    }

    private void handleRequestSeatCancelled(Map<String, String> chatRoomKvMap) {
        for (String key : chatRoomKvMap.keySet()) {
            if (key.startsWith(RC_REQUEST_SEAT_PREFIX_KEY)) {
                RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
                if (listener != null) {
                    listener.onRequestSeatListChanged();
                }
            }
        }
    }

    private boolean containSeatIndex(int index) {
        return index >= 0 && index < mRCVoiceSeatInfoList.size();
    }

    private RCVoiceSeatInfo getSeatInfoByIndex(int index) {
        if (containSeatIndex(index)) {
            return mRCVoiceSeatInfoList.get(index);
        }
        return null;
    }

    private int getIndexBySeatInfo(RCVoiceSeatInfo info) {
        return mRCVoiceSeatInfoList.indexOf(info);
    }

    private RCVoiceSeatInfo getSeatInfoByUserId(String userId) {
        return mUserOnSeatMap.get(userId);
    }

    private boolean isUserOnSeat(String userId) {
        return mUserOnSeatMap.containsKey(userId);
    }

    private void switchRole(final RCRTCLiveRole role, final RCVoiceRoomCallback callback) {
        if (mCurrentRole != role) {
            mCurrentRole = role;
            // FIXME: 2021/6/21 暂时延迟处理
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            RCRTCEngine.getInstance().leaveRoom(new IRCRTCResultCallback() {
                @Override
                public void onSuccess() {
                    // FIXME: 2021/6/21 暂时延迟处理
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "switchRole: role = "+role.getType());
                    joinRTCRoom(mRoomId, role, callback);
                }

                @Override
                public void onFailed(RTCErrorCode errorCode) {
                    onErrorWithCheck(callback, errorCode.getValue(), errorCode.getReason());
                }
            });
        }
    }

    private void joinRTCRoom(String roomId, final RCRTCLiveRole role, final RCVoiceRoomCallback callback) {
        RCRTCRoomConfig config = RCRTCRoomConfig
                .Builder
                .create()
                .setRoomType(RCRTCRoomType.LIVE_AUDIO)
                .setLiveRole(role)
                .build();
        RCRTCEngine.getInstance().joinRoom(roomId, config, new IRCRTCResultDataCallback<RCRTCRoom>() {
            @Override
            public void onSuccess(RCRTCRoom data) {
                mRoom = data;
                data.registerRoomListener(mVREventListener);
                Log.d(TAG, "joinRTCRoom: role = "+role.getType());
                if (RCRTCLiveRole.BROADCASTER.equals(role)) {
                    data.getLocalUser().publishDefaultStreams(new IRCRTCResultCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "publishDefaultStreams:onSuccess: ");
                            RCRTCEngine.
                                    getInstance().registerStatusReportListener(mVoiceStatusReportListener);
                        }

                        @Override
                        public void onFailed(RTCErrorCode errorCode) {
                            Log.d(TAG, "publishDefaultStreams:onFailed : " + "errorCode = " + errorCode.getValue());
                            onErrorWithCheck(callback, errorCode.getValue(), errorCode.getReason());
                        }
                    });
                    List<RCRTCInputStream> list = new ArrayList<>();
                    for (RCRTCRemoteUser remoteUser : data.getRemoteUsers()) {
                        list.addAll(remoteUser.getStreams());
                    }
                    if (list.size() > 0) {
                        data.getLocalUser().subscribeStreams(list, new IRCRTCResultCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "subscribeStreams:onSuccess: ");
                            }

                            @Override
                            public void onFailed(RTCErrorCode errorCode) {
                                Log.d(TAG, "subscribeStreams:onFailed : " + "errorCode = " + errorCode.getValue());
                            }
                        });
                    }
                    muteSelfIfNeed();
                } else {
                    List<RCRTCInputStream> list = new ArrayList<>();
                    for (RCRTCRemoteUser remoteUser : data.getRemoteUsers()) {
                        list.addAll(remoteUser.getStreams());
                    }
                    if (list.size() > 0) {
                        data.getLocalUser().subscribeStreams(data.getLiveStreams(), new IRCRTCResultCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "subscribeStreams:onSuccess: ");
                            }

                            @Override
                            public void onFailed(RTCErrorCode errorCode) {
                                Log.d(TAG, "subscribeStreams:onFailed : " + "errorCode = " + errorCode.getValue());
                            }
                        });
                    }
                    enableSpeaker(true);
                }
                onSuccessWithCheck(callback);
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                onErrorWithCheck(callback, errorCode.getValue(), errorCode.getReason());
            }
        });
    }

    private void updateKvRoomInfo(RCVoiceRoomInfo roomInfo, final RCVoiceRoomCallback callback) {
        RongChatRoomClient.getInstance().forceSetChatRoomEntry(mRoomId, RC_ROOM_INFO_KEY, roomInfo.toJson(), false, false, "", new IRongCoreCallback.OperationCallback() {
            @Override
            public void onSuccess() {
                onSuccessWithCheck(callback);
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                onErrorWithCheck(callback, coreErrorCode.getValue(), coreErrorCode.getMessage());
            }
        });
    }

    private void updateKvSeatInfo(RCVoiceSeatInfo info, int seatIndex, final RCVoiceRoomCallback callback) {
        RongChatRoomClient.getInstance().forceSetChatRoomEntry(mRoomId, seatInfoKvKey(seatIndex), info.toJson(), false, false, "", new IRongCoreCallback.OperationCallback() {
            @Override
            public void onSuccess() {
                onSuccessWithCheck(callback);
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                onErrorWithCheck(callback, coreErrorCode.code, coreErrorCode.msg);
            }
        });
    }

    private String seatInfoKvKey(int index) {
        return String.format(Locale.getDefault(), "%s_%d", RC_MIC_SEAT_INFO_PREFIX_KEY, index);
    }

    private String speakingKey(int index) {
        return String.format(Locale.getDefault(), "%s_%d", RC_USER_ON_SEAT_SPEAKING_KEY, index);
    }

    private String requestSeatKvKey(String content) {
        return String.format(Locale.getDefault(), "%s_%s", RC_REQUEST_SEAT_PREFIX_KEY, content);
    }

    private int seatIndexWithUserSit(String userId) {
        RCVoiceSeatInfo rcVoiceSeatInfo = mUserOnSeatMap.get(userId);
        if (rcVoiceSeatInfo == null) {
            return -1;
        }
        return mRCVoiceSeatInfoList.indexOf(rcVoiceSeatInfo);
    }


    private void updateRequestSeatKvWithUserId(String userId, String content, final RCVoiceRoomCallback callback) {
        RongChatRoomClient.getInstance().forceSetChatRoomEntry(mRoomId, requestSeatKvKey(userId), content, false, true, "", new IRongCoreCallback.OperationCallback() {
            @Override
            public void onSuccess() {
                onSuccessWithCheck(callback);
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                onErrorWithCheck(callback, coreErrorCode.getValue(), coreErrorCode.getMessage());
            }
        });
    }

    private boolean checkCallback(RCVoiceRoomBaseCallback callback) {
        return callback != null;
    }

    private void onSuccessWithCheck(RCVoiceRoomCallback callback) {
        if (checkCallback(callback)) {
            callback.onSuccess();
        }
    }

    private <T> void onSuccessWithCheck(RCVoiceRoomResultCallback<T> callback, T data) {
        if (checkCallback(callback)) {
            callback.onSuccess(data);
        }
    }

    private void onErrorWithCheck(RCVoiceRoomBaseCallback callback, int code, String message) {
        if (checkCallback(callback)) {
            callback.onError(code, message);
        }
    }

    private void onErrorWithCheck(RCVoiceRoomBaseCallback callback, VoiceRoomErrorCode errorCode) {
        if (checkCallback(callback)) {
            callback.onError(errorCode.getCode(), errorCode.getMessage());
        }
    }

    private void userEnterSeat(RCVoiceSeatInfo seatInfo, String userId) {
        synchronized (TAG) {
            seatInfo.setUserId(userId);
            seatInfo.setStatus(RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusUsed);
            mUserOnSeatMap.put(userId, seatInfo);
        }
    }

    private void userLeaveSeat(RCVoiceSeatInfo seatInfo) {
        synchronized (TAG) {
            mUserOnSeatMap.remove(seatInfo.getUserId());
            seatInfo.setUserId(null);
            seatInfo.setStatus(RCVoiceSeatInfo.RCSeatStatus.RCSeatStatusEmpty);
        }
    }

    private List<RCVoiceSeatInfo> latestMicInfoListFromEntry(Map<String, String> map) {
        synchronized (TAG) {
            List<RCVoiceSeatInfo> list = new ArrayList<>();
            for (int i = 0; i < mRoomInfo.getSeatCount(); i++) {
                String seatKey = seatInfoKvKey(i);
                RCVoiceSeatInfo newInfo = null;
                if (map.containsKey(seatKey)) {
                    newInfo = JsonUtils.fromJson(map.get(seatKey), RCVoiceSeatInfo.class);
                }
                if (newInfo == null) {
                    newInfo = getSeatInfoByIndex(i);
                }
                list.add(newInfo);
            }
            return list;
        }
    }


    private void clearAll() {
        mRoom.unregisterRoomListener();
        RCRTCEngine.
                getInstance().unregisterStatusReportListener();
        mRoomId = null;
        mRoomInfo = null;
        mRCVoiceSeatInfoList.clear();
        mRoom = null;
        mRoomEventListener.clear();
        currentKvMap.clear();
        mRoomEventListener = null;
    }


    private class IRCRTCVoiceRoomEventsListener extends IRCRTCRoomEventsListener {


        @Override
        public void onRemoteUserPublishResource(RCRTCRemoteUser remoteUser, List<RCRTCInputStream> streams) {
            if (mRoom != null && mCurrentRole == RCRTCLiveRole.BROADCASTER) {
                mRoom.getLocalUser().subscribeStreams(streams, new IRCRTCResultCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailed(RTCErrorCode errorCode) {

                    }
                });
            }
        }

        @Override
        public void onRemoteUserMuteAudio(RCRTCRemoteUser remoteUser, RCRTCInputStream stream, boolean mute) {

        }

        @Override
        public void onRemoteUserMuteVideo(RCRTCRemoteUser remoteUser, RCRTCInputStream stream, boolean mute) {

        }

        @Override
        public void onRemoteUserUnpublishResource(RCRTCRemoteUser remoteUser, List<RCRTCInputStream> streams) {

        }

        @Override
        public void onUserJoined(RCRTCRemoteUser remoteUser) {

        }

        @Override
        public void onUserLeft(RCRTCRemoteUser remoteUser) {

        }

        @Override
        public void onUserOffline(RCRTCRemoteUser remoteUser) {

        }

        @Override
        public void onPublishLiveStreams(List<RCRTCInputStream> streams) {
            if (mRoom != null && mCurrentRole == RCRTCLiveRole.AUDIENCE) {
                mRoom.getLocalUser().subscribeStreams(streams, new IRCRTCResultCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailed(RTCErrorCode errorCode) {

                    }
                });
            }
        }

        @Override
        public void onUnpublishLiveStreams(List<RCRTCInputStream> streams) {

        }

        @Override
        public void onLeaveRoom(int reasonCode) {

        }
    }

    private class VoiceStatusReportListener extends IRCRTCStatusReportListener {

        @Override
        public void onConnectionStats(StatusReport statusReport) {
            RCVoiceSeatInfo seatInfo = getSeatInfoByUserId(mCurrentUserId);
            if (seatInfo != null) {
                for (StatusBean statusBean : statusReport.statusAudioSends.values()) {
                    if (statusBean.isSend && TextUtils.equals(statusBean.mediaType, RCRTCMediaType.AUDIO.getDescription())) {
                        String speaking;
                        if (statusBean.audioLevel > 0) {
                            speaking = "1";
                        } else {
                            speaking = "0";
                        }

                        int index = getIndexBySeatInfo(seatInfo);
                        if (index >= 0) {
                            notifyVoiceRoom(speakingKey(index), speaking);
                            RCVoiceRoomEventListener listener = getCurrentRoomEventListener();
                            boolean isSpeaking = "1".equals(speaking);
                            seatInfo.setSpeaking(isSpeaking);
                            if (listener != null) {
                                listener.onSpeakingStateChanged(index, isSpeaking);
                            }
                        }
                    }
                }
            }
        }
    }

    private interface CompletionListener {
        void onComplete();
    }

}
