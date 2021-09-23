package cn.rong.combusis.ui.room;

import android.content.Context;

import com.bcq.adapter.recycle.RcyAdapter;
import com.bcq.adapter.recycle.RcyHolder;

import io.rong.imlib.model.MessageContent;

/**
 * @author gyn
 * @date 2021/9/23
 */
public class RoomMessageAdapter extends RcyAdapter<MessageContent, RcyHolder> {
    public RoomMessageAdapter(Context context, int... itemLayoutId) {
        super(context, itemLayoutId);
    }

    @Override
    public int getItemLayoutId(MessageContent item, int position) {
        return 0;
    }

    @Override
    public void convert(RcyHolder holder, MessageContent messageContent, int position, int layoutId) {

    }
}
