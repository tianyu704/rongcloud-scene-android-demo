package cn.rong.combusis.ui.room.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import cn.rong.combusis.R;
import cn.rong.combusis.manager.AllBroadcastManager;

/**
 * @author gyn
 * @date 2021/9/17
 */
public class RoomTitleBar extends ConstraintLayout {
    private View mRootView;
    private TextView mNameTextView;
    private TextView mIDTextView;
    private TextView mOnlineTextView;
    private TextView mDelayTextView;
    private ImageButton mMenuButton;
    private ConstraintLayout mLeftView;

    public RoomTitleBar(@NonNull Context context) {
        this(context, null);
    }

    public RoomTitleBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mRootView = LayoutInflater.from(context).inflate(R.layout.view_room_title_bar, this);
        initView();
    }

    private void initView() {
        mNameTextView = mRootView.findViewById(R.id.tv_room_name);
        mLeftView = mRootView.findViewById(R.id.cl_left);
        mIDTextView = mRootView.findViewById(R.id.tv_room_id);
        mOnlineTextView = mRootView.findViewById(R.id.tv_room_online);
        mDelayTextView = mRootView.findViewById(R.id.tv_room_delay);
        mMenuButton = mRootView.findViewById(R.id.btn_menu);
    }

    public void setOnMemberClickListener(OnClickListener v) {
        mLeftView.setOnClickListener(v);
    }

    public void setOnMenuClickListener(OnClickListener v) {
        mMenuButton.setOnClickListener(v);
    }

    public void setData(String name, int id) {
        setRoomName(name);
        setRoomId(id);
    }

    public void setRoomId(int id) {
        mIDTextView.setText(String.format("ID %s", id));
    }

    public void setRoomName(String name) {
        mNameTextView.setText(name);
    }

    public void setOnlineNum(int num) {
        mOnlineTextView.setText(String.format("在线 %s", num));
    }

    public void setDelay(int delay) {
        setDelay(delay, true);
    }

    public void setDelay(int delay, boolean isShow) {
        if (isShow) {
            mDelayTextView.setVisibility(View.VISIBLE);
            mDelayTextView.setText(String.valueOf(delay) + "ms");
            int leftPicId;
            if (delay < 100) {
                leftPicId = R.drawable.ic_room_delay_1;
            } else if (delay < 299) {
                leftPicId = R.drawable.ic_room_delay_2;
            } else {
                leftPicId = R.drawable.ic_room_delay_3;
            }
            mDelayTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(leftPicId, 0, 0, 0);
        } else {
            mDelayTextView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        AllBroadcastManager.getInstance().removeListener();
        super.onDetachedFromWindow();
    }
}
