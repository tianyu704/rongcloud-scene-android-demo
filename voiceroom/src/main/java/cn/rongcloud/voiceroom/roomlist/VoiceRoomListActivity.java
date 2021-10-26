package cn.rongcloud.voiceroom.roomlist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.basis.UIStack;
import com.yhao.floatwindow.FloatWindow;
import com.yhao.floatwindow.IFloatWindow;

import cn.rong.combusis.ui.friend.FriendListFragment;
import cn.rong.combusis.ui.roomlist.AbsSwitchActivity;
import cn.rong.combusis.widget.miniroom.MiniRoomManager;

/**
 * @author gyn
 * @date 2021/9/14
 */
public class VoiceRoomListActivity extends AbsSwitchActivity {

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, VoiceRoomListActivity.class));
    }
    @Override
    protected void onStart() {
        IFloatWindow iFloatWindow = FloatWindow.get(MiniRoomManager.TAG);
        if (UIStack.getInstance().isTaskTop(this)||iFloatWindow!=null) {
            //当前用户是否在顶部，如果在不做任何操作
            //如果不在顶部，但是当前是顶部窗口是最小化的，那么也不做判断
        }else {
            //如果不在顶部，跳转
            Activity topActivity = UIStack.getInstance().getTopActivity();
            Intent intent = new Intent(this, topActivity.getClass());
            startActivity(intent);
        }
        super.onStart();
    }

    @Override
    public Fragment onCreateLeftFragment() {
        return VoiceRoomListFragment.getInstance();
    }

    @Override
    public Fragment onCreateRightFragment() {
        return FriendListFragment.getInstance();
    }

    @Override
    public String[] onSetSwitchTitle() {
        return new String[]{"语聊房", "好友"};
    }
}
