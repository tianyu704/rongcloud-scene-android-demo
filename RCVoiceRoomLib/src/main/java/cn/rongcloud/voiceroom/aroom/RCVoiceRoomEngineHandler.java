/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.aroom;

import android.os.Handler;
import android.os.HandlerThread;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cn.rongcloud.voiceroom.api.IRCVoiceRoomEngine;

/**
 * api执行线程
 */
public class RCVoiceRoomEngineHandler extends HandlerThread implements InvocationHandler {
    private static final String TAG = "RCVoiceRoomEngineHandler";
    private IRCVoiceRoomEngine mRcVoiceRoomEngine;
    private Handler mHandler;

    private RCVoiceRoomEngineHandler() {
        super(TAG);
    }

    public void start(IRCVoiceRoomEngine rcVoiceRoomEngine) {
        mRcVoiceRoomEngine = rcVoiceRoomEngine;
        start();
        mHandler = new Handler(getLooper());
    }

    private static volatile RCVoiceRoomEngineHandler INSTANCE = null;

    public static RCVoiceRoomEngineHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (RCVoiceRoomEngineHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RCVoiceRoomEngineHandler();
                }
            }
        }
        return INSTANCE;
    }

    public void post(final Runnable runnable) {
        if (mHandler != null && mHandler.getLooper() != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void postDelayed(final Runnable runnable, long delayMillis) {
        if (mHandler != null && mHandler.getLooper() != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, delayMillis);
        }
    }

    @Override
    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
        if (method.getReturnType().equals(Void.TYPE)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        method.invoke(mRcVoiceRoomEngine, args);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            });
            return null;
        } else {
            return method.invoke(mRcVoiceRoomEngine, args);
        }
    }
}
