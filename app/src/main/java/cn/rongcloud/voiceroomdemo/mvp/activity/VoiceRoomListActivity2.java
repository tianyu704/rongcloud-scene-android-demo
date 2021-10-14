package cn.rongcloud.voiceroomdemo.mvp.activity;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import cn.rong.combusis.ui.friend.FriendListFragment;
import cn.rong.combusis.ui.roomlist.AbsSwitchActivity;

public class VoiceRoomListActivity2 extends AbsSwitchActivity {

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, VoiceRoomListActivity2.class));
    }

    @Override
    public Fragment onCreateLeftFragment() {
        return FriendListFragment.getInstance();
    }

    @Override
    public Fragment onCreateRightFragment() {
        return FriendListFragment.getInstance();
    }
}
