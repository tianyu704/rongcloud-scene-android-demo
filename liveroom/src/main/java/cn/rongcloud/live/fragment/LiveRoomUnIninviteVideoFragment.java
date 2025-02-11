package cn.rongcloud.live.fragment;

import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

import cn.rong.combusis.common.base.BaseBottomSheetDialogFragment;
import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.ui.room.fragment.ClickCallback;
import cn.rong.combusis.ui.room.fragment.SeatActionClickListener;
import cn.rongcloud.live.R;

/**
 * @author 李浩
 * @date 2021/12/17
 */
public class LiveRoomUnIninviteVideoFragment extends BaseBottomSheetDialogFragment {

    SeatActionClickListener seatActionClickListener;
    private String userId;
    private AppCompatTextView btn_uninvite_video_id;
    private AppCompatTextView btn_cancel;

    public LiveRoomUnIninviteVideoFragment(String userId, SeatActionClickListener seatActionClickListener) {
        super(R.layout.fragment_live_room_uninvite_video);
        this.seatActionClickListener = seatActionClickListener;
        this.userId = userId;
    }

    @Override
    public void initView() {
        btn_uninvite_video_id = (AppCompatTextView) getView().findViewById(R.id.btn_uninvite_video_id);
        btn_cancel = (AppCompatTextView) getView().findViewById(R.id.btn_cancel);
    }

    @Override
    public void initListener() {
        super.initListener();
        if (seatActionClickListener == null) {
            return;
        }

        btn_uninvite_video_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seatActionClickListener.cancelInvitation(userId, new ClickCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean result, String msg) {
                        dismiss();
                    }
                });
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }


}
