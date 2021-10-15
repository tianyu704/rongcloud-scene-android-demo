package cn.rong.combusis.ui.friend;


import android.widget.RadioGroup;

import com.basis.adapter.interfaces.IAdapte;
import com.basis.adapter.recycle.RcyHolder;
import com.basis.mvp.BasePresenter;
import com.basis.net.oklib.OkApi;
import com.basis.net.oklib.WrapperCallBack;
import com.basis.net.oklib.api.Method;
import com.basis.net.oklib.wrapper.Wrapper;
import com.basis.ui.ListFragment;

import java.util.HashMap;
import java.util.Map;

import cn.rong.combusis.R;
import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.ui.friend.model.Friend;

public class FriendListFragment extends ListFragment<Friend, Friend, RcyHolder> implements FriendAdapter.OnFollowClickListener {
    private FriendAdapter mAdapter;
    private int mType = 2;// 1 我关注的 2 我的粉丝
    private SendPrivateMessageFragment sendPrivateMessageFragment;

    public static FriendListFragment getInstance() {
        return new FriendListFragment();
    }

    @Override
    public void initView() {
        RadioGroup radioGroup = getView().findViewById(R.id.rg_friend);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_follower) {
                mType = 2;
                mAdapter.setType(mType);
                loadData();
            } else if (checkedId == R.id.rb_follow) {
                mType = 1;
                mAdapter.setType(mType);
                loadData();
            }
        });
        sendPrivateMessageFragment = new SendPrivateMessageFragment();
        loadData();
    }

    private void loadData() {
        Map<String, Object> params = new HashMap<>(8);
        params.put("type", mType);
        request("Loading...", VRApi.FOLLOW_LIST, params, Method.get, true);
    }

    @Override
    public IAdapte<Friend, RcyHolder> onSetAdapter() {
        mAdapter = new FriendAdapter(getContext(), R.layout.item_friend);
        mAdapter.setType(mType);
        mAdapter.setOnFollowClickListener(this);
        return mAdapter;
    }

    @Override
    public BasePresenter createPresent() {
        return null;
    }

    @Override
    public int setLayoutId() {
        return R.layout.fragment_friend_list;
    }

    @Override
    public void initListener() {

    }

    @Override
    public void clickFollow(Friend friend) {
        Friend.FollowStatus status = friend.getFollowStatus(mType);
        friend.changeFollowStatus(mType);
        mAdapter.notifyDataSetChanged();

        OkApi.get(VRApi.followUrl(friend.getUid()), null, new WrapperCallBack() {
            @Override
            public void onResult(Wrapper result) {
                if (!result.ok()) {
                    friend.setFollowStatus(status);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(int code, String msg) {
                friend.setFollowStatus(status);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void clickItem(Friend friend) {
        sendPrivateMessageFragment.showDialog(getChildFragmentManager(), friend);
    }

}
