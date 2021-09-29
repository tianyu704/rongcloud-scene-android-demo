package cn.rongcloud.radioroom.ui.roomlist;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import cn.rong.combusis.ui.friend.FriendFragment;
import cn.rong.combusis.ui.roomlist.AbsSwitchActivity;

/**
 * @author gyn
 * @date 2021/9/14
 */
public class RadioRoomListActivity extends AbsSwitchActivity {

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, RadioRoomListActivity.class));
    }

    @Override
    public Fragment onCreateLeftFragment() {
        return RadioRoomListFragment.getInstance();
    }

    @Override
    public Fragment onCreateRightFragment() {
        return FriendFragment.getInstance();
    }

    @Override
    public String[] onSetSwitchTitle() {
        return new String[]{"电台", "好友"};
    }
}