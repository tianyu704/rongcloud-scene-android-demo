package cn.rongcloud.radioroom.ui.room;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.kit.utils.Logger;
import com.rongcloud.common.utils.ImageLoaderUtil;
import com.rongcloud.common.utils.UiUtils;
import com.yhao.floatwindow.FloatWindow;
import com.yhao.floatwindow.IFloatWindow;
import com.yhao.floatwindow.MoveType;
import com.yhao.floatwindow.PermissionListener;
import com.yhao.floatwindow.Screen;
import com.yhao.floatwindow.ViewStateListener;

import java.util.List;

import cn.rong.combusis.R;
import cn.rong.combusis.common.ui.widget.WaveView;
import cn.rongcloud.radioroom.IRCRadioRoomEngine;
import cn.rongcloud.radioroom.helper.RadioEventHelper;
import cn.rongcloud.radioroom.helper.RadioRoomListener;
import io.rong.imlib.model.Message;

/**
 * 语聊房的最小窗口管理器
 */
public class RadioRoomMiniManager implements RadioRoomListener {

    private static final String TAG = "RadioRoomMiniManager";
    private View miniWindows;
    private WaveView waveView;
    private ImageView bgView;
    /**
     * 滑动状态监听
     */
    private ViewStateListener viewStateListener = new ViewStateListener() {
        @Override
        public void onPositionUpdate(int x, int y) {

        }

        @Override
        public void onShow() {

        }

        @Override
        public void onHide() {

        }

        @Override
        public void onDismiss() {

        }

        @Override
        public void onMoveAnimStart() {

        }

        @Override
        public void onMoveAnimEnd() {

        }

        @Override
        public void onBackToDesktop() {

        }
    };

    public static RadioRoomMiniManager getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void onLoadMessageHistory(List<Message> messages) {

    }

    @Override
    public void onMessageReceived(Message message) {

    }

    @Override
    public void onAudienceEnter(String s) {

    }

    @Override
    public void onAudienceLeave(String s) {

    }

    @Override
    public void onRadioRoomKVUpdate(IRCRadioRoomEngine.UpdateKey updateKey, String s) {
        Logger.e("===================" + TAG + updateKey.name());
        if (miniWindows == null) {
            return;
        }
        switch (updateKey) {
            case RC_SPEAKING:
                boolean isSpeaking = TextUtils.equals(s, "1");
                if (isSpeaking) {
                    waveView.start();
                } else {
                    waveView.stop();
                }
                break;

            case RC_BGNAME:
                ImageLoaderUtil.INSTANCE.loadImage(bgView.getContext(), bgView, s, R.color.black);
                break;
        }
    }

    public void show(Context context, String background, Intent intent, PermissionListener permissionListener) {
        IFloatWindow iFloatWindow = FloatWindow.get(TAG);
        if (iFloatWindow == null) {

            miniWindows = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.view_voice_room_mini, null);
            waveView = miniWindows.findViewById(R.id.wv_creator_background);
            bgView = miniWindows.findViewById(R.id.iv_room_creator_portrait);
            FloatWindow.with(context.getApplicationContext())
                    .setTag(TAG)
                    .setView(miniWindows)
                    .setWidth(UiUtils.INSTANCE.dp2Px(context, 120))
                    .setHeight(UiUtils.INSTANCE.dp2Px(context, 120))
                    .setX(Screen.width, 0.7f)
                    .setY(Screen.height, 0.75f)
                    .setMoveType(MoveType.active)
                    .setDesktopShow(true)
                    .setViewStateListener(viewStateListener)
                    .setPermissionListener(permissionListener)
                    .setFilter(false, RadioRoomActivity.class)
                    .build();

            miniWindows.setOnClickListener(v -> {
                RadioEventHelper.getInstance().removeRadioEventListener(this);
                close();
                // TODO 打开电台房
                context.startActivity(intent);
            });
        }
        ImageLoaderUtil.INSTANCE.loadImage(bgView.getContext(), bgView, background, R.drawable.img_default_room_cover);
        RadioEventHelper.getInstance().addRadioEventListener(this);
        if (iFloatWindow != null && !iFloatWindow.isShowing()) {
            iFloatWindow.show();
        }
    }

    public void close() {
        FloatWindow.destroy(TAG);
    }

    public boolean isShowing() {
        IFloatWindow iFloatWindow = FloatWindow.get(TAG);
        if (iFloatWindow == null) {
            return false;
        }
        return iFloatWindow.isShowing();
    }

    private static class Holder {
        private final static RadioRoomMiniManager INSTANCE = new RadioRoomMiniManager();
    }
}
