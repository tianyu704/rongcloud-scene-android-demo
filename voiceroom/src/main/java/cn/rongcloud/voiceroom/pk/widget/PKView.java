package cn.rongcloud.voiceroom.pk.widget;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.basis.adapter.recycle.RcyAdapter;
import com.basis.adapter.recycle.RcyHolder;
import com.basis.adapter.recycle.RcySAdapter;
import com.kit.utils.ImageLoader;
import com.kit.utils.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.voiceroom.R;

public class PKView extends LinearLayout implements IPK {
    private final static String TAG = "PKView";
    //    private final static int MAX = 180;
    private final static int MAX = 10;

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
        rvSender.setLayoutManager(new GridLayoutManager(context, 3, RecyclerView.HORIZONTAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        });
        rvReceiver.setLayoutManager(new GridLayoutManager(context, 3, RecyclerView.HORIZONTAL, true) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        });
    }

    @Override
    public synchronized void pkStart(OnTimerEndListener listener) {
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
        // 开启 pk记时
        timer = new Timer(tvTime, listener);
        timer.start();
    }

    @Override
    public synchronized void punishStart(OnTimerEndListener listener) {
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
        // 开启 惩罚记时
        timer = new Timer(tvTime, listener);
        timer.start();
    }

    @Override
    public synchronized void pkStop() {
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
    }


    @Override
    public void setPKScore(int left, int right) {
        pkProcessbar.setPKValue(left, right);
    }

    @Override
    public void setGiftRank(List<String> lefts, List<String> rights) {
        int ls = null != lefts ? lefts.size() : 0;
        List<RankInfo> llis = new ArrayList<>();
        for (int i = ls - 1; i > -1; i--) {
            llis.add(new RankInfo(lefts.get(i), ls - i));
        }
        int rs = null != rights ? rights.size() : 0;
        List<RankInfo> rlis = new ArrayList<>();
        for (int i = 0; i < rs; i++) {
            rlis.add(new RankInfo(rights.get(i), i + 1));
        }
        lAdapter.setData(llis, true);
        rAdapter.setData(rlis, true);
    }


    public static class RankInfo {
        private String portrait;
        private int rank = 0;

        public RankInfo(String portrait, int rank) {
            this.portrait = portrait;
            this.rank = rank;
        }
    }

    public static class PKAdapter extends RcySAdapter<RankInfo, RcyHolder> {
        private boolean receiveFlag;

        public PKAdapter(Context context, boolean receiveFlag) {
            super(context, R.layout.layout_pk_view_member);
            this.receiveFlag = receiveFlag;
        }

        @Override
        public void convert(RcyHolder holder, RankInfo info, int position) {
            holder.setText(R.id.tv_count, info.rank + "");
            ImageView imageView = holder.getView(R.id.iv_gift);
            ImageLoader.loadUrl(imageView, info.portrait, R.drawable.default_portrait, ImageLoader.Size.SZ_100);
            holder.setSelected(R.id.tv_count, receiveFlag);
        }
    }

    public class Timer extends CountDownTimer {
        private WeakReference<TextView> reference;
        private WeakReference<OnTimerEndListener> listenerWeakReference;

        Timer(TextView textView, OnTimerEndListener listener) {
            super(MAX * 1000, 1000);
            this.reference = new WeakReference<>(textView);
            this.listenerWeakReference = new WeakReference<>(listener);
        }

        @Override
        public void onTick(long l) {
            Logger.e("Timer", "l = " + l);
            if (null != reference && reference.get() != null) {
                reference.get().setText(msToShow(l));
            }
        }

        @Override
        public void onFinish() {
            Logger.e("Timer", "onFinish");
            if (null != listenerWeakReference && null != listenerWeakReference.get()) {
                listenerWeakReference.get().onTimerEnd();
            }
        }

        private String msToShow(long ms) {
            long min = ms / 1000 / 60;
            long s = ms % (1000 * 60) / 1000;
            return "0" + min + ":" + (s < 10 ? "0" + s : "" + s);
        }
    }
}
