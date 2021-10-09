package cn.rong.combusis.ui.friend;

import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;

import com.basis.mvp.BasePresenter;
import com.basis.ui.BaseFragment;
import com.rongcloud.common.utils.FragmentUtils;

import cn.rong.combusis.R;

public class FriendFragment extends BaseFragment {

    public static FriendFragment getInstance() {
        return new FriendFragment();
    }

    @Override
    public BasePresenter createPresent() {
        return null;
    }

    @Override
    public int setLayoutId() {
        return R.layout.fragment_friend;
    }

    @Override
    public void init() {
        RadioGroup radioGroup = getView().findViewById(R.id.rg_friend);
        // 粉丝列表
        Fragment followerFragment = FriendListFragment.getInstance(2);
        // 关注列表
        Fragment followFragment = FriendListFragment.getInstance(1);
        Fragment[] fragments = {followerFragment, followFragment};
        FragmentUtils.INSTANCE.switchFragment(getChildFragmentManager(), R.id.fl_content, followerFragment, fragments, 0, 0);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_follower) {
                FragmentUtils.INSTANCE.switchFragment(getChildFragmentManager(), R.id.fl_content, followerFragment, fragments, 0, 0);
            } else if (checkedId == R.id.rb_follow) {
                FragmentUtils.INSTANCE.switchFragment(getChildFragmentManager(), R.id.fl_content, followFragment, fragments, 0, 0);
            }
        });
    }

    @Override
    public void initListener() {

    }
}
