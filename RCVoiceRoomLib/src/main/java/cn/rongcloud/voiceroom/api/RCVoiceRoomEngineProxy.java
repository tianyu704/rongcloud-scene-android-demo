/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.api;

import java.lang.reflect.Proxy;

/**
 * @author gusd
 * @Date 2021/07/30
 */
public class RCVoiceRoomEngineProxy {
    private static final String TAG = "RCVoiceRoomEngineProxy";

    private RCVoiceRoomEngineProxy() {
    }

    private static volatile IRCVoiceRoomEngine INSTANCE = null;

    public static IRCVoiceRoomEngine getInstance() {
        if (INSTANCE == null) {
            synchronized (RCVoiceRoomEngineProxy.class) {
                if (INSTANCE == null) {
                    RCVoiceRoomEngineImpl rcVoiceRoomEngine = (RCVoiceRoomEngineImpl) RCVoiceRoomEngineImpl.getInstance();
                    ClassLoader classLoader = rcVoiceRoomEngine.getClass().getClassLoader();
                    Class<?>[] interfaces = rcVoiceRoomEngine.getClass().getInterfaces();
                    RCVoiceRoomEngineHandler handler = RCVoiceRoomEngineHandler.getInstance();
                    handler.start(rcVoiceRoomEngine);
                    INSTANCE = (IRCVoiceRoomEngine) Proxy.newProxyInstance(classLoader, interfaces, handler);
                }
            }
        }
        return INSTANCE;
    }
}
