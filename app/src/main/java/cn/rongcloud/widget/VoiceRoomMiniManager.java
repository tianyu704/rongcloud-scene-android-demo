package cn.rongcloud.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.rongcloud.common.net.ApiConstant;
import com.rongcloud.common.utils.UiUtils;
import com.yhao.floatwindow.FloatWindow;
import com.yhao.floatwindow.IFloatWindow;
import com.yhao.floatwindow.MoveType;
import com.yhao.floatwindow.PermissionListener;
import com.yhao.floatwindow.Screen;
import com.yhao.floatwindow.ViewStateListener;

import cn.rong.combusis.common.ui.widget.WaveView;
import cn.rong.combusis.sdk.event.EventHelper;
import cn.rong.combusis.sdk.event.listener.StatusListener;
import cn.rongcloud.voiceroomdemo.R;
import de.hdodenhof.circleimageview.CircleImageView;
import io.rong.callkit.util.GlideUtils;

/**
 * 语聊房的最小窗口管理器
 */
public class VoiceRoomMiniManager {

    public static final String TAG="VoiceRoomMiniWindows";
    public static VoiceRoomMiniManager voiceRoomMiniManager;

    public static VoiceRoomMiniManager getInstance(){
        if (voiceRoomMiniManager==null){
            synchronized (VoiceRoomMiniManager.class){
                if (voiceRoomMiniManager==null){
                     voiceRoomMiniManager = new VoiceRoomMiniManager();
                }
            }
        }
        return voiceRoomMiniManager;
    }

    /**
     * 初始化构建
     * @param context
     * @param intent
     */
    public void init(Context context,Intent intent) {
        if (FloatWindow.get(TAG)==null){
            View miniWindows = LayoutInflater.from(context).inflate(R.layout.view_voice_room_mini, null);

            FloatWindow.with(context.getApplicationContext())
                    .setTag(TAG)
                    .setView(miniWindows)
                    .setWidth(UiUtils.INSTANCE.dp2Px(context,120))
                    .setHeight(UiUtils.INSTANCE.dp2Px(context,120))
                    .setX(Screen.width,0.7f)
                    .setY(Screen.height,0.75f)
                    .setMoveType(MoveType.active)
                    .setDesktopShow(true)
                    .setViewStateListener(viewStateListener)
                    .setPermissionListener(permissionListener)
                    .build();

            miniWindows.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(intent);
                    FloatWindow.destroy(TAG);
                }
            });
        }
    }

    /**
     * 展示最小化窗口
     * @param
     */
    public void showMiniWindows(){
        IFloatWindow iFloatWindow = FloatWindow.get(TAG);
        if (iFloatWindow!=null&&!iFloatWindow.isShowing()){
            iFloatWindow.show();
        }

    }

    /**
     * 动态去刷新当前用户的状态
     * @param roomId
     */
    public void refreshRoomOwner(String roomId){
        if (!EventHelper.helper().isInitlaized()){
            EventHelper.helper().regeister(roomId);
        }
        EventHelper.helper().addStatusListener(new StatusListener() {
            @Override
            public void onStatus(int delay) {

            }

            @Override
            public void onReceive(int unReadCount) {

            }

            @Override
            public void onSpeaking(int index, boolean speaking) {
                if (FloatWindow.get(TAG)!=null){
                    WaveView wv_creator_background = FloatWindow.get(TAG).getView().findViewById(R.id.wv_creator_background);
                    if (speaking) {
                        wv_creator_background.start();
                    }else {
                        wv_creator_background.stop();
                    }
                }
            }
        });
    }
    /**
     * 动态更改当前的背景色
     */
    public void setBackgroudPic(String url,Context context){
        CircleImageView circleImageView=FloatWindow.get(TAG).getView().findViewById(R.id.iv_room_creator_portrait);
        GlideUtils.showRemotePortrait(context,circleImageView, TextUtils.isEmpty(url)? Uri.parse(ApiConstant.INSTANCE.getDEFAULT_PORTRAIT_ULR()):Uri.parse(url));

    }
    /**
     * 滑动状态监听
     */
    private ViewStateListener viewStateListener=new ViewStateListener() {
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

    private PermissionListener permissionListener=new PermissionListener() {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onFail() {

        }
    };

    public void ondestroy(){

    }
}
