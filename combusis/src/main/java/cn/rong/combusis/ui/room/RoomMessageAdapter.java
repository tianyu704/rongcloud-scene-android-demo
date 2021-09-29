package cn.rong.combusis.ui.room;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.basis.adapter.recycle.RcyAdapter;
import com.basis.adapter.recycle.RcyHolder;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.R;
import cn.rong.combusis.manager.AudioPlayManager;
import cn.rong.combusis.manager.IAudioPlayListener;
import cn.rong.combusis.message.RCChatroomAdmin;
import cn.rong.combusis.message.RCChatroomBarrage;
import cn.rong.combusis.message.RCChatroomEnter;
import cn.rong.combusis.message.RCChatroomGift;
import cn.rong.combusis.message.RCChatroomGiftAll;
import cn.rong.combusis.message.RCChatroomKickOut;
import cn.rong.combusis.message.RCChatroomLocationMessage;
import cn.rong.combusis.message.RCChatroomSeats;
import cn.rong.combusis.message.RCChatroomVoice;
import cn.rong.combusis.ui.room.model.MemberCache;
import io.rong.imlib.model.MessageContent;

/**
 * @author gyn
 * @date 2021/9/23
 */
public class RoomMessageAdapter extends RcyAdapter<MessageContent, RcyHolder> {
    OnClickMessageUserListener mOnClickMessageUserListener;
    private String mRoomCreateId = "";

    public RoomMessageAdapter(Context context, OnClickMessageUserListener onClickMessageUserListener) {
        this(context, R.layout.item_message_system, R.layout.item_message_normal, R.layout.item_message_voice);
        this.mOnClickMessageUserListener = onClickMessageUserListener;
    }

    public RoomMessageAdapter(Context context, int... itemLayoutId) {
        super(context, itemLayoutId);
    }

    public void setRoomCreateId(String roomCreateId) {
        this.mRoomCreateId = roomCreateId;
    }

    @Override
    public int getItemLayoutId(MessageContent item, int position) {
        if (item instanceof RCChatroomLocationMessage) {
            return R.layout.item_message_system;
        } else if (item instanceof RCChatroomVoice) {
            return R.layout.item_message_voice;
        } else {
            return R.layout.item_message_normal;
        }
    }

    @Override
    public void convert(RcyHolder holder, MessageContent messageContent, int position, int layoutId) {
        if (messageContent instanceof RCChatroomLocationMessage) {
            setSystemMessage(holder, (RCChatroomLocationMessage) messageContent);
        } else if (messageContent instanceof RCChatroomVoice) {
            setVoiceMessage(holder, (RCChatroomVoice) messageContent);
        } else {
            setNormalMessage(holder, messageContent);
        }
    }

    /**
     * 设置系统消息数据
     *
     * @param holder         holder
     * @param messageContent messageContent
     */
    private void setSystemMessage(RcyHolder holder, RCChatroomLocationMessage messageContent) {
        holder.setText(R.id.tv_message_system, messageContent.getContent());
    }

    /**
     * 设置语音消息数据
     *
     * @param holder  holder
     * @param message messageContent
     */
    private void setVoiceMessage(RcyHolder holder, RCChatroomVoice message) {
        EmojiTextView messageTextView = holder.getView(R.id.tv_message_content);
        List<MsgInfo> list = new ArrayList<>(4);
        list.add(new MsgInfo(String.format("%s: ", message.getUserName()), message.getUserId(), true, 0, 0));
        updateRole(message.getUserId(), messageTextView);
        messageTextView.setText(styleBuilder(list));
        messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
        holder.setText(R.id.tv_voice_duration, String.format("%s''", message.getDuration()));
        AnimationDrawable animationDrawable = (AnimationDrawable) ((ImageView) holder.getView(R.id.iv_paly_voice_id)).getDrawable();
        animationDrawable.stop();
        holder.setOnClickListener(R.id.ll_paly_voice_id, v -> {
            if (AudioPlayManager.getInstance().isPlaying() && TextUtils.equals(message.getPath(), AudioPlayManager.getInstance().getPlayingUri().toString())) {
                //当前正在播放,并且点击的就是当前的
                AudioPlayManager.getInstance().stopPlay();
                return;
            }
            AudioPlayManager.getInstance().startPlay(context, Uri.parse(message.getPath()), new IAudioPlayListener() {

                @Override
                public void onStart(Uri uri) {
                    //开始动画
                    animationDrawable.start();
                }

                @Override
                public void onStop(Uri uri) {
                    //音频被停止,停止动画
                    animationDrawable.stop();
                    animationDrawable.selectDrawable(0);
                }

                @Override
                public void onComplete(Uri uri) {
                    //停止动画
                    animationDrawable.stop();
                    animationDrawable.selectDrawable(0);
                }
            });
        });
    }

    /**
     * 设置普通消息数据
     *
     * @param holder  holder
     * @param message messageContent
     */
    private void setNormalMessage(RcyHolder holder, MessageContent message) {
        EmojiTextView messageTextView = holder.getView(R.id.tv_message_normal);
        List<MsgInfo> list = new ArrayList<>(4);
        messageTextView.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                0,
                0
        );
        messageTextView.setCompoundDrawablePadding(0);
        if (message instanceof RCChatroomBarrage) {
            list.add(new MsgInfo(String.format("%s: ", ((RCChatroomBarrage) message).getUserName()), ((RCChatroomBarrage) message).getUserId(), true, 0, 0));
            list.add(new MsgInfo(((RCChatroomBarrage) message).getContent(), "", false, 0, 0));
            updateRole(((RCChatroomBarrage) message).getUserId(), messageTextView);
        } else if (message instanceof RCChatroomEnter) {
            list.add(new MsgInfo(String.format("%s ", ((RCChatroomEnter) message).getUserName()), ((RCChatroomEnter) message).getUserId(), true, 0, 0));
            list.add(new MsgInfo("进来了", "", false, 0, 0));
            updateRole(((RCChatroomEnter) message).getUserId(), messageTextView);
        } else if (message instanceof RCChatroomKickOut) {
            list.add(new MsgInfo(String.format("%s 被 ", ((RCChatroomKickOut) message).getTargetName()), "", false, 0, 0));
            list.add(new MsgInfo(String.format("%s ", ((RCChatroomKickOut) message).getUserName()), ((RCChatroomKickOut) message).getUserId(), true, 0, 0));
            list.add(new MsgInfo(" 踢出去了", "", false, 0, 0));
        } else if (message instanceof RCChatroomGiftAll) {
            list.add(new MsgInfo(String.format("%s ", ((RCChatroomGiftAll) message).getUserName()), ((RCChatroomGiftAll) message).getUserId(), true, 0, 0));
            list.add(new MsgInfo(String.format("全麦打赏 %s x%s", ((RCChatroomGiftAll) message).getGiftName(), ((RCChatroomGiftAll) message).getNumber()), "", false, 0, 0));
            updateRole(((RCChatroomGiftAll) message).getUserId(), messageTextView);
        } else if (message instanceof RCChatroomGift) {
            list.add(new MsgInfo(String.format("%s: ", ((RCChatroomGift) message).getUserName()), ((RCChatroomGift) message).getUserId(), true, 0, 0));
            list.add(new MsgInfo(" 送给 ", "", false, 0, 0));
            list.add(new MsgInfo(String.format("%s ", ((RCChatroomGift) message).getTargetName()), ((RCChatroomGift) message).getTargetId(), true, 0, 0));
            list.add(new MsgInfo(String.format(" %s x%s", ((RCChatroomGift) message).getGiftName(), ((RCChatroomGift) message).getNumber()), "", false, 0, 0));
            updateRole(((RCChatroomGift) message).getUserId(), messageTextView);
        } else if (message instanceof RCChatroomAdmin) {
            list.add(new MsgInfo(String.format("%s: ", ((RCChatroomAdmin) message).getUserName()), ((RCChatroomAdmin) message).getUserId(), true, 0, 0));
            if (((RCChatroomAdmin) message).isAdmin()) {
                list.add(new MsgInfo(" 成为管理员", "", false, 0, 0));
            } else {
                list.add(new MsgInfo(" 被撤回管理员", "", false, 0, 0));
            }
            updateRole(((RCChatroomAdmin) message).getUserId(), messageTextView);
        } else if (message instanceof RCChatroomSeats) {
            list.add(new MsgInfo(String.format("房间更换为 %s 座模式，请重新上麦", ((RCChatroomSeats) message).getCount()), "", false, 0, 0));
        } else if (message instanceof RCChatroomLocationMessage) {
            list.add(new MsgInfo(((RCChatroomLocationMessage) message).getContent(), "", false, 0, 0));
        }
        messageTextView.setText(styleBuilder(list));
        messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void updateRole(String userId, TextView view) {
        if (TextUtils.equals(userId, mRoomCreateId)) {
            view.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_creator,
                    0,
                    0,
                    0
            );
            view.setCompoundDrawablePadding(2);
        } else if (MemberCache.getInstance().isAdmin(userId)) {
            view.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_is_admin,
                    0,
                    0,
                    0
            );
            view.setCompoundDrawablePadding(2);
        }
    }

    private SpannableStringBuilder styleBuilder(List<MsgInfo> infos) {
        SpannableStringBuilder style = new SpannableStringBuilder();
        int start = 0;
        MsgInfo info;
        for (int i = 0; i < infos.size(); i++) {
            info = infos.get(i);
            info.start = start;
            start += info.getContent().length();
            info.end = start;
            style.append(info.getContent());
            if (info.isClicked()) {
                MsgInfo finalInfo = info;
                style.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        mOnClickMessageUserListener.clickMessageUser(finalInfo.getClickId());
                    }
                }, info.start, info.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                style.setSpan(
                        new ForegroundColorSpan(Color.parseColor("#78FFFFFF")),
                        info.start,
                        info.end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }
        return style;
    }

    public interface OnClickMessageUserListener {
        void clickMessageUser(String userId);
    }

    class MsgInfo {
        private String content = "";
        private String clickId = "";
        private boolean clicked = false;
        private int start = 0;
        private int end = 0;

        public MsgInfo(String content, String clickId, boolean clicked, int start, int end) {
            this.content = content;
            this.clickId = clickId;
            this.clicked = clicked;
            this.start = start;
            this.end = end;
        }

        public String getContent() {
            return content;
        }

        public String getClickId() {
            return clickId;
        }

        public boolean isClicked() {
            return clicked;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }
}