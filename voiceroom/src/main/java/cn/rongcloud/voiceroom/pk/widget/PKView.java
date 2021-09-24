package cn.rongcloud.voiceroom.pk.widget;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.basis.adapter.recycle.RcyAdapter;
import com.basis.adapter.recycle.RcyHolder;
import com.basis.adapter.recycle.RcySAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.voiceroom.R;

public class PKView extends LinearLayout implements IPK {
    private final static int MAX = 180;

    public PKView(Context context) {
        this(context, null, -1);
    }

    public PKView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PKView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private PKProcessbar pkProcessbar;
    private RecyclerView rvSender, rvReceiver;
    private RcyAdapter lAdapter, rAdapter;
    private Timer timer;
    private TextView tvTime;

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_pk_customer, this);
        tvTime = view.findViewById(R.id.tv_time);
        pkProcessbar = view.findViewById(R.id.pk_sb);
        pkProcessbar.setBarResource(R.drawable.ic_score_selected);
        pkProcessbar.setPKValue(4, 10);
        rvSender = view.findViewById(R.id.rv_sender);
        rvReceiver = view.findViewById(R.id.rv_receiver);
        lAdapter = new PKAdapter(context, false);
        rAdapter = new PKAdapter(context, true);
        rvSender.setAdapter(lAdapter);
        rvReceiver.setAdapter(rAdapter);
        rvSender.setLayoutManager(new GridLayoutManager(context, 3));
        rvReceiver.setLayoutManager(new GridLayoutManager(context, 3));
        testInitData();
        pkStart(null);
    }

    @Override
    public void pkStart(OnEndListener listener) {
        if (null != timer) {
            timer.cancel();
        }
        timer = new Timer(tvTime, listener);
        timer.start();
    }


    @Override
    public void pkStop() {
        if (null != timer) {
            timer.cancel();
        }
    }

    @Override
    public void setPKValues(int left, int right) {
        if (null != pkProcessbar) {
            pkProcessbar.setPKValue(left, right);
        }
    }

    @Override
    public void setGiftSenders(List<String> lefts, List<String> rights) {

    }

    private void testInitData() {
        List<Info> lefts = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            lefts.add(new Info());
        }
        lAdapter.setData(lefts, true);

        List<Info> rights = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            rights.add(new Info());
        }
        rAdapter.setData(rights, true);
    }

    public static class Info {
        private String userId;
        private int count = 1;
    }

    public static class PKAdapter extends RcySAdapter<Info, RcyHolder> {
        private boolean receiveFlag = false;

        public PKAdapter(Context context, boolean receiveFlag) {
            super(context, R.layout.layout_pk_view_member);
            this.receiveFlag = receiveFlag;
        }

        @Override
        public void convert(RcyHolder holder, Info info, int position) {
            holder.setText(R.id.tv_count, info.count + "");
            holder.setSelected(R.id.tv_count, receiveFlag);
        }
    }

    public class Timer extends CountDownTimer {
        private WeakReference<TextView> reference;
        private WeakReference<OnEndListener> listenerWeakReference;

        Timer(TextView textView, OnEndListener listener) {
            super(MAX * 1000, 1000);
            this.reference = new WeakReference<>(textView);
            this.listenerWeakReference = new WeakReference<>(listener);
        }

        @Override
        public void onTick(long l) {
            if (null != reference && reference.get() != null) {
                reference.get().setText(msToShow(l));
            }
            if (l < 1000 && null != listenerWeakReference && null != listenerWeakReference.get()) {
                listenerWeakReference.get().onEnd();
            }
        }

        @Override
        public void onFinish() {
        }

        private String msToShow(long ms) {
            long min = ms / 1000 / 60;
            long s = ms % (1000 * 60) / 1000;
            return "0" + min + ":" + (s < 10 ? "0" + s : "" + s);
        }
    }
}
