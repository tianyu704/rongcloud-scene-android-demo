package cn.rongcloud.voiceroomdemo.mvp.activity;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import cn.rong.combusis.ui.AbsSwitchActivity;
import cn.rong.combusis.ui.friend.FriendFragment;

public class VoiceRoomListActivity2 extends AbsSwitchActivity {

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, VoiceRoomListActivity2.class));
    }

    @Override
    public Fragment onCreateLeftFragment() {
        return FriendFragment.getInstance();
    }

    @Override
    public Fragment onCreateRightFragment() {
        return FriendFragment.getInstance();
    }
}
