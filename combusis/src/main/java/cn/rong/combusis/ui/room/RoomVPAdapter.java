package cn.rong.combusis.ui.room;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.kit.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gyn
 * @date 2021/9/17
 */
public class RoomVPAdapter extends FragmentStateAdapter {

    private List<String> mRoomList = new ArrayList<>();
    private AbsRoomActivity mFragmentActivity;

    public RoomVPAdapter(@NonNull AbsRoomActivity fragmentActivity) {
        super(fragmentActivity);
        mFragmentActivity = fragmentActivity;
    }

    public void setData(List<String> roomList) {
        if (roomList != null) {
            this.mRoomList = roomList;
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Logger.e("=================createFragment:" + position);
        return mFragmentActivity.getFragment(getItemData(position));
    }

    @Override
    public int getItemCount() {
        return mRoomList.size();
    }

    public String getItemData(int position) {
        if (position >= 0 && position < mRoomList.size()) {
            return mRoomList.get(position);
        }
        return null;
    }
}
