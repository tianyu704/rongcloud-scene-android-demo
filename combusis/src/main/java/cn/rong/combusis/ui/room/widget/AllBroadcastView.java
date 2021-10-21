package cn.rong.combusis.ui.room.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.kit.utils.Logger;

import cn.rong.combusis.R;
import cn.rong.combusis.manager.AllBroadcastManager;
import cn.rong.combusis.message.RCAllBroadcastMessage;

/**
 * @author gyn
 * @date 2021/10/19
 */
public class AllBroadcastView extends AppCompatTextView {

    private OnClickBroadcast onClickBroadcast;

    public AllBroadcastView(@NonNull Context context) {
        this(context, null);
    }

    public AllBroadcastView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllBroadcastView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        setAlpha(0);
        setVisibility(INVISIBLE);
        setBackgroundResource(R.color.bg_broadcast);
        setTextSize(12);
        setTextColor(getResources().getColor(R.color.white));
        setMarqueeRepeatLimit(-1);
        setHorizontallyScrolling(true);
        setHorizontalFadingEdgeEnabled(true);
        int left = getResources().getDimensionPixelOffset(R.dimen.dimen_room_padding);
        int top = getResources().getDimensionPixelOffset(R.dimen.dimen_5dp);
        setPadding(left, top, left, top);
        AllBroadcastManager.getInstance().setListener(message -> {
            showMessage(message);
        });
    }

    public void setOnClickBroadcast(OnClickBroadcast onClickBroadcast) {
        this.onClickBroadcast = onClickBroadcast;
    }

    private void showMessage(RCAllBroadcastMessage message) {
        if (message == null) {
            animation(false);
        } else {
            animation(true);
            setText(buildMessage(message));
            setMovementMethod(LinkMovementMethod.getInstance());
            setEllipsize(TextUtils.TruncateAt.MARQUEE);
            setSingleLine();
            setSelected(true);
            setFocusableInTouchMode(true);
            setFocusable(true);
        }
    }

    private void animation(boolean show) {
        float alpha = 0;
        if (show) {
            alpha = 1;
        }
        animate().alpha(alpha).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!show) {
                    setVisibility(INVISIBLE);
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (show) {
                    setVisibility(VISIBLE);
                }
            }
        });
    }

    private SpannableStringBuilder buildMessage(RCAllBroadcastMessage message) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (message != null) {
            int start = 0, end = 0;
            if (TextUtils.isEmpty(message.getTargetId())) {
                String userName = message.getUserName();
                builder.append(userName);
                end = userName.length();
                builder.setSpan(new ForegroundColorSpan(Color.parseColor("#78FFFFFF")),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                String gift = String.format(" 全麦打赏 %sx%s ", message.getGiftName(), message.getGiftCount());
                builder.append(gift);
                start = end + gift.length();
            } else {
                String userName = message.getUserName();
                builder.append(userName);
                end = userName.length();
                builder.setSpan(new ForegroundColorSpan(Color.parseColor("#78FFFFFF")),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                String sendStr = " 送给 ";
                builder.append(sendStr);
                start = end + sendStr.length();
                String targetName = message.getTargetName();
                builder.append(targetName);
                end = start + targetName.length();
                builder.setSpan(new ForegroundColorSpan(Color.parseColor("#78FFFFFF")),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                String gift = String.format(" %sx%s ", message.getGiftName(), message.getGiftCount());
                builder.append(gift);
                start = end + gift.length();
            }
            String clickStr = "点击进入房间围观";
            builder.append(clickStr);
            end = start + clickStr.length();
            builder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Logger.e("12312312312312312312312313123123213");
                    if (onClickBroadcast != null) {
                        onClickBroadcast.clickBroadcast(message);
                    }
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.parseColor("#FFEB61"));
                    ds.setUnderlineText(false);
                }
            }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

    @Override
    protected void onDetachedFromWindow() {
        AllBroadcastManager.getInstance().removeListener();
        super.onDetachedFromWindow();
    }

    public interface OnClickBroadcast {
        void clickBroadcast(RCAllBroadcastMessage message);
    }
}
