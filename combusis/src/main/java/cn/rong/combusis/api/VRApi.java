package cn.rong.combusis.api;

import com.rongcloud.common.net.ApiConstant;

public class VRApi {
    private final static String host = ApiConstant.INSTANCE.getBASE_URL();
    public final static String followList = ApiConstant.INSTANCE.getBASE_URL() + "user/follow/list";
    private static String follow = ApiConstant.INSTANCE.getBASE_URL() + "user/follow/";

    public static String followUrl(String userId) {
        return follow + userId;
    }

    // 在线房主
    public static String online_creater = host + "/mic/room/online/created/list";
}
