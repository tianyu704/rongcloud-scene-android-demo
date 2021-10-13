package cn.rong.combusis.ui.roomlist;

import android.content.Context;

import com.basis.adapter.recycle.RcyHolder;
import com.basis.adapter.recycle.RcySAdapter;
import com.rongcloud.common.utils.ImageLoaderUtil;

import cn.rong.combusis.R;
import cn.rong.combusis.provider.voiceroom.VoiceRoomBean;
import cn.rong.combusis.ui.OnItemClickListener;
import cn.rong.combusis.ui.OnItemClickRoomListListener;

/**
 * @author gyn
 * @date 2021/9/15
 */
public class RoomListAdapter extends RcySAdapter<VoiceRoomBean, RcyHolder> {

    private OnItemClickRoomListListener<VoiceRoomBean> mOnItemClickListener;

    public RoomListAdapter(Context context, int itemLayoutId) {
        super(context, itemLayoutId);
    }

    public void setOnItemClickListener(OnItemClickRoomListListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @Override
    public void convert(RcyHolder holder, VoiceRoomBean voiceRoomBean, int position) {
        ImageLoaderUtil.INSTANCE.loadImage(context, holder.getView(R.id.iv_room_cover), voiceRoomBean.getThemePictureUrl(), R.drawable.img_default_room_cover);
        ImageLoaderUtil.INSTANCE.loadImage(context, holder.getView(R.id.iv_room_creator), voiceRoomBean.getCreateUserPortrait(), R.drawable.default_portrait);
        holder.setText(R.id.tv_room_name, voiceRoomBean.getRoomName());
        holder.setText(R.id.tv_room_creator_name, voiceRoomBean.getCreateUserName());
        holder.setText(R.id.tv_room_people_number, String.valueOf(voiceRoomBean.getUserTotal()));
        holder.setVisible(R.id.iv_room_locked, voiceRoomBean.isPrivate());
        holder.itemView.setOnClickListener(v -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.clickItem(voiceRoomBean, position,false);
            }
        });
    }
}
