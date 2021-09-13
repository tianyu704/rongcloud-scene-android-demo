package cn.rong.combusis.ui.friend;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.bcq.adapter.recycle.RcyHolder;
import com.bcq.adapter.recycle.RcySAdapter;
import com.bumptech.glide.Glide;

import cn.rong.combusis.R;
import cn.rong.combusis.model.Friend;

public class FriendAdapter extends RcySAdapter<Friend, RcyHolder> {

    public FriendAdapter(Context context, int itemLayoutId) {
        super(context, itemLayoutId);
    }

    @Override
    public void convert(RcyHolder holder, Friend friend, int position) {
        ImageView avatar = holder.getView(R.id.civ_avatar);
        Glide.with(avatar).load(friend.getPortrait()).into(avatar);
        holder.setText(R.id.tv_name, friend.getName());
        holder.setChecked(R.id.cb_follow, friend.isFollow());
    }
}
