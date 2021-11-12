package cn.rongcloud.liveroom.room;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.basis.mvp.BasePresenter;
import com.basis.ui.BaseFragment;

import cn.rongcloud.liveroom.R;

/**
 * lihao
 * 创建直播房fragment
 */
public class CreatLiveRoomFragment extends BaseFragment {

    public static Fragment getInstance() {
        Bundle bundle = new Bundle();
        Fragment fragment = new CreatLiveRoomFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public BasePresenter createPresent() {
        return null;
    }

    @Override
    public int setLayoutId() {
        return R.layout.fragment_create_liveroom_layout;
    }

    @Override
    public void init() {

    }

    @Override
    public void initListener() {

    }
}
