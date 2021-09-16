package cn.rong.combusis.ui.friend;


import android.os.Bundle;

import com.basis.ui.ListFragment;
import com.bcq.adapter.interfaces.IAdapte;
import com.bcq.adapter.recycle.RcyHolder;
import com.bcq.net.OkApi;
import com.bcq.net.WrapperCallBack;
import com.bcq.net.api.Method;
import com.bcq.net.wrapper.Wrapper;

import java.util.HashMap;
import java.util.Map;

import cn.rong.combusis.R;
import cn.rong.combusis.api.VRApi;
import cn.rong.combusis.model.Friend;

public class FriendListFragment extends ListFragment<Friend, Friend, RcyHolder> implements FriendAdapter.OnFollowClickListener {
    // 1 我关注的 2 我的粉丝
    private final static String FRIEND_TYPE = "FRIEND_TYPE";

    private FriendAdapter mAdapter;
    private int mType;

    public static FriendListFragment getInstance(int type) {
        FriendListFragment fragment = new FriendListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(FRIEND_TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void initView() {
        Map<String, Object> params = new HashMap<>(8);
        params.put("type", mType);
        request("Loading...", VRApi.FOLLOW_LIST, params, Method.get, true);
    }

    @Override
    public IAdapte<Friend, RcyHolder> onSetAdapter() {
        mType = getArguments().getInt(FRIEND_TYPE, 1);
        mAdapter = new FriendAdapter(getContext(), R.layout.layout_friend_item);
        mAdapter.setType(mType);
        mAdapter.setOnFollowClickListener(this);
        return mAdapter;
    }

    @Override
    public int setLayoutId() {
        return R.layout.fragment_friend_list;
    }

    @Override
    public void onRefresh(Object obj) {
        super.onRefresh(obj);
    }

//    @Override
//    public List<Friend> onPreSetData(List<Friend> netData) {
//        Friend friend;
//        List<Friend> list = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            friend = new Friend();
//            friend.setName("name" + i);
//            friend.setStatus(i % 2 == 0 ? 1 : 0);
//            friend.setPortrait("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg42.51tietu.net%2Fpic%2F2017-031205%2F201703120531151gzfhftzdy0330994.jpg&refer=http%3A%2F%2Fimg42.51tietu.net&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1634115552&t=541ec79ea9cbbf05e1dae8046a255173");
//            list.add(friend);
//        }
//        return list;
//    }

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
}
