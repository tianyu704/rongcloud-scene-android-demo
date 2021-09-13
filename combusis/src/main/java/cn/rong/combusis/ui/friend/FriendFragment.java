package cn.rong.combusis.ui.friend;

import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;

import com.rongcloud.common.utils.FragmentUtils;

import cn.rong.combusis.R;
import cn.rong.combusis.ui.BaseFragment;

public class FriendFragment extends BaseFragment {

    public static FriendFragment getInstance() {
        return new FriendFragment();
    }

    @Override
    public int setLayoutId() {
        return R.layout.fragment_friend;
    }

    @Override
    public void init() {
        RadioGroup radioGroup = getLayout().findViewById(R.id.rg_friend);

        Fragment followerFragment = FriendListFragment.getInstance(2);
        Fragment followFragment = FriendListFragment.getInstance(1);
        Fragment[] fragments = {followerFragment, followFragment};
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_follower) {
                FragmentUtils.INSTANCE.switchFragment(getChildFragmentManager(), R.id.fl_content, followerFragment, fragments, 0, 0);
            } else if (checkedId == R.id.rb_follow) {
                FragmentUtils.INSTANCE.switchFragment(getChildFragmentManager(), R.id.fl_content, followFragment, fragments, 0, 0);
            }
        });
    }
}
