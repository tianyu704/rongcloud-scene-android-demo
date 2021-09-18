package cn.rong.combusis.api;

import com.rongcloud.common.net.ApiConstant;

public class VRApi {
    private final static String HOST = ApiConstant.INSTANCE.getBASE_URL();
    /**
     * 粉丝或关注列表
     */
    public final static String FOLLOW_LIST = HOST + "user/follow/list";
    /**
     * 关注或取消关注
     */
    private static String FOLLOW = HOST + "user/follow/";

    public static String followUrl(String userId) {
        return FOLLOW + userId;
    }

    /**
     * 房间列表
     */
    public static String ROOM_LIST = HOST + "mic/room/list";
    /**
     * 创建房间
     */
    public final static String ROOM_CREATE = HOST + "mic/room/create";

    /**
     * 在线房主
     */
    public static String ONLINE_CREATER = HOST + "mic/room/online/created/list";

    /**
     * 更改用户所属房间
     */
    public static String USER_ROOM_CHANGE = HOST + "user/change";

    /**
     * 文件上传
     */
    public final static String FILE_UPLOAD = HOST + "file/upload";
    /**
     * 上传文件后，文件的前缀
     */
    public static final String FILE_PATH = HOST + "file/show?path=";
}
