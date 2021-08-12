/*
 * Copyright © 2021 RongCloud. All rights reserved.
 */

package cn.rongcloud.voiceroom.api;

import java.lang.reflect.Proxy;

import cn.rongcloud.voiceroom.aroom.RCVoiceRoomEngineHandler;
import cn.rongcloud.voiceroom.aroom.RCVoiceRoomEngineImpl;

/**
 * RCVoiceRoomEngine的代理对象
 */
public class RCVoiceRoomEngineProxy {

    private RCVoiceRoomEngineProxy() {
    }

    private static volatile IRCVoiceRoomEngine INSTANCE = null;

    public static IRCVoiceRoomEngine getInstance() {
        if (INSTANCE == null) {
            synchronized (RCVoiceRoomEngineProxy.class) {
                if (INSTANCE == null) {
                    IRCVoiceRoomEngine rcVoiceRoomEngine = RCVoiceRoomEngineImpl.getInstance();
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
