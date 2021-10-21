package cn.rong.combusis.intent;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

/**
 * @author gyn
 * @date 2021/10/21
 */
public class IntentWrap {
    // keys
    public static final String KEY_ROOM_IDS = "KEY_ROOM_IDS";
    public static final String KEY_IS_CREATE = "KEY_IS_CREATE";
    public static final String KEY_ROOM_POSITION = "KEY_ROOM_POSITION";
    // actions
    private static final String ACTION_RADIO_ROOM = "cn.rongcloud.radio.ui.room.RadioRoomActivity";
    private static final String ACTION_VOICE_ROOM = "cn.rongcloud.voiceroom.room.NewVoiceRoomActivity";

    /**
     * 打开电台房间
     *
     * @param context  context
     * @param roomIds  房间列表id
     * @param position 打开的房间的位置
     */
    public static void launchRadioRoom(Context context, ArrayList<String> roomIds, int position) {
        Intent intent = new Intent();
        intent.setAction(ACTION_RADIO_ROOM);
        intent.putExtra(KEY_ROOM_IDS, roomIds);
        intent.putExtra(KEY_ROOM_POSITION, position);
        context.startActivity(intent);
    }

    /**
     * 打开语聊房
     *
     * @param context  context
     * @param roomIds  房间列表id
     * @param position 打开的房间的位置
     * @param isCreate 是否是创建
     */
    public static void launchVoiceRoom(Context context, ArrayList<String> roomIds, int position, boolean isCreate) {
        Intent intent = new Intent();
        intent.setAction(ACTION_VOICE_ROOM);
        intent.putExtra(KEY_ROOM_IDS, roomIds);
        intent.putExtra(KEY_ROOM_POSITION, position);
        intent.putExtra(KEY_IS_CREATE, isCreate);
        context.startActivity(intent);
    }
}
