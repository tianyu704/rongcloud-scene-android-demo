package com.rongcloud.common.score;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rongcloud.common.R;
import com.rongcloud.common.utils.SharedPreferUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ScoreUtil {
    private final static String KEY_TIME = "score_time";
    private final static String KEY_SCORE_FLAG = "score_falg";
    private final static int LIMT = 3;

    /**
     * 是否显示统计打分
     *
     * @return
     */
    public static boolean enableScore() {
        //未打分 且次数大于limt
        return !SharedPreferUtil.getBoolean(KEY_SCORE_FLAG)
                && SharedPreferUtil.get(KEY_TIME, 0) >= LIMT;
    }

    /**
     * 统计次数 累加
     */
    public static void statistics() {
        int last = SharedPreferUtil.get(KEY_TIME, 0);
        SharedPreferUtil.set(KEY_TIME, last + 1);
    }

    /**
     * 清空统计：取消评价后清空
     */
    public static void clearStatistics() {
        SharedPreferUtil.set(KEY_TIME, 0);
    }

    /**
     * 设置已打分：评价后设置
     */
    public static void alreadyScore() {
        SharedPreferUtil.set(KEY_SCORE_FLAG, true);
    }

    private static void dismissDialog() {
        if (null != scoreDialog) {
            scoreDialog.dismiss();
        }
        scoreDialog = null;
    }

    private static ScoreDialog scoreDialog;

    public static void showScoreDialog(Activity activity) {
        if (null == scoreDialog || !scoreDialog.enable()) {
            scoreDialog = new ScoreDialog(activity, new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    scoreDialog = null;
                }
            });
        }
        scoreDialog.replaceContent("请留下您的使用感受吧", "稍后再说", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
                clearStatistics();
            }
        }, "", null, initScoreView());
        scoreDialog.show();
    }

    private static View initScoreView() {
        View view = scoreDialog.getLayoutInflater().inflate(R.layout.layout_score_tip, null);
        View up_selected = view.findViewById(R.id.iv_up_selected);
        View down_selected = view.findViewById(R.id.iv_down_selected);
        view.findViewById(R.id.cl_up).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    up_selected.setVisibility(View.VISIBLE);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    up_selected.setVisibility(View.GONE);
                }
                return false;
            }
        });
        view.findViewById(R.id.cl_down).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    down_selected.setVisibility(View.VISIBLE);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    down_selected.setVisibility(View.GONE);
                }
                return false;
            }
        });
        view.findViewById(R.id.cl_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFovLikes();
            }
        });
        view.findViewById(R.id.cl_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shwoDownDialog();
            }
        });
        return view;
    }

    /**
     * 点赞
     */
    private static void sendFovLikes() {

    }

    /**
     * 提交反馈
     */
    private static void sendReport() {
        Log.e("ScoreUtil", "selecteds = " + selectedDowns);
        Log.e("ScoreUtil", "selecteds len = " + selectedDowns.size());
        // TODO: 2021/7/30 提交成功后显示推荐活动
        alreadyScore();
        showLastPromotion();
    }

    /**
     * 显示吐槽弹框
     */
    private static void shwoDownDialog() {
        if (null == scoreDialog) return;
        scoreDialog.replaceContent("请问哪个方面需要改进呢？",
                "提交反馈", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != selectedDowns && !selectedDowns.isEmpty()) {
                            sendReport();
                        }
                    }
                }, "我再想想", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissDialog();
                    }
                }, initDownView());
    }

    private final static List<Integer> selectedDowns = new ArrayList();

    private static View initDownView() {
        View view = scoreDialog.getLayoutInflater().inflate(R.layout.layout_score_down, null);
        View[] views = new View[4];
        View[] selectVs = new View[4];
        views[0] = view.findViewById(R.id.cl_first);
        views[1] = view.findViewById(R.id.cl_second);
        views[2] = view.findViewById(R.id.cl_third);
        views[3] = view.findViewById(R.id.cl_fourth);
        // 右下角图标
        selectVs[0] = view.findViewById(R.id.iv_select_first);
        selectVs[1] = view.findViewById(R.id.iv_select_second);
        selectVs[2] = view.findViewById(R.id.iv_select_third);
        selectVs[3] = view.findViewById(R.id.iv_select_fourth);

        for (int i = 0; i < 4; i++) {
            final int index = i;
            views[index].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean selected = selectedDowns.contains(index);
                    if (selected) {
                        selectedDowns.remove((Integer) index);
                    } else {
                        selectedDowns.add(index);
                    }
                    v.setSelected(!selected);
                    selectVs[index].setVisibility(!selected ? View.VISIBLE : View.GONE);
                }
            });
        }
        return view;
    }

    /**
     * 显示最新活动
     */
    private static void showLastPromotion() {
        if (null == scoreDialog) return;
        scoreDialog.replaceContent("融云最近活动，了解一下？",
                "我想了解", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jumpPromotionPage();
                    }
                }, "我再想想", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissDialog();
                    }
                }, scoreDialog.getLayoutInflater().inflate(R.layout.layout_promotion, null));
    }

    private static void jumpPromotionPage() {
        if (null != scoreDialog && scoreDialog.enable()) {
            Intent intent = new Intent("io.rong.intent.action.commonwebpage");
            intent.putExtra("key_url", "https://m.rongcloud.cn/activity/rtc20");
            intent.putExtra("key_basic", "最近活动");
            scoreDialog.startActivty(intent);
        }
        dismissDialog();
    }
}
