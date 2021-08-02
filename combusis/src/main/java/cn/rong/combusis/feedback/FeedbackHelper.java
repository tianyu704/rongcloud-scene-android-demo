package cn.rong.combusis.feedback;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rongcloud.common.dao.database.DatabaseManager;
import com.rongcloud.common.dao.entities.CallRecordEntityKt;
import com.rongcloud.common.net.ApiConstant;
import com.rongcloud.common.net.IResultBack;
import com.rongcloud.common.utils.AccountStore;
import com.rongcloud.common.utils.SharedPreferUtil;
import com.rongcloud.common.utils.UIKit;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rong.combusis.R;
import cn.rong.combusis.oklib.Core;
import cn.rong.combusis.oklib.GsonUtil;
import cn.rong.combusis.oklib.OCallBack;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FeedbackHelper {
    private final static String TAG = "FeedbackHelper";
    private final static String KEY_TIME = "score_time";
    private final static String KEY_SCORE_FLAG = "score_falg";
    private final static int LIMT = 3;
    private final static FeedbackHelper helper = new FeedbackHelper();
    private final static String[] DEF_REASON = new String[]{
            "场景功能",
            "音质质量",
            "使用流程",
            "交互体验"
    };
    private FeedEnableListener feedEnableListener;

    public interface FeedEnableListener {
        void onFeedback();
    }

    private FeedbackHelper() {
    }

    public static FeedbackHelper getHelper() {
        return helper;
    }

    public void setFeedEnableListener(FeedEnableListener feedEnableListener) {
        this.feedEnableListener = feedEnableListener;
    }

    public void unregisteObservice() {
        setFeedEnableListener(null);
        dismissDialog();
        selectedDowns.clear();
    }

    public void registeFeedbackObservice(@NonNull Activity activity) {
        final WeakReference<Activity> con = new WeakReference(activity);
        setFeedEnableListener(new FeedEnableListener() {
            @Override
            public void onFeedback() {
                if (null != con && null != con.get()) {
                    showScoreDialog(con.get());
                }
            }
        });
        //注册后立即检测状态
        if (enableScore() && null != feedEnableListener) {
            feedEnableListener.onFeedback();
        }
    }

    /**
     * 是否显示统计打分
     *
     * @return
     */
    public boolean enableScore() {
        //未打分 且次数大于limt
        return !SharedPreferUtil.getBoolean(KEY_SCORE_FLAG) &&
                SharedPreferUtil.get(KEY_TIME, 0) >= LIMT;
    }

    /**
     * 统计次数 累加
     */
    public void statistics() {
        int last = SharedPreferUtil.get(KEY_TIME, 0);
        SharedPreferUtil.set(KEY_TIME, last + 1);
        if (enableScore() && null != feedEnableListener) {
            feedEnableListener.onFeedback();
        }
    }

    /**
     * 清空统计：取消评价后清空
     */
    public void clearStatistics() {
        SharedPreferUtil.set(KEY_TIME, 0);
    }

    /**
     * 设置已打分：评价后设置
     */
    public void alreadyScore() {
        SharedPreferUtil.set(KEY_SCORE_FLAG, true);
    }

    private void dismissDialog() {
        if (null != feedbackDialog) {
            feedbackDialog.dismiss();
        }
        feedbackDialog = null;
    }

    private FeedbackDialog feedbackDialog;

    public void showScoreDialog(Activity activity) {
        if (null == feedbackDialog || !feedbackDialog.enable()) {
            feedbackDialog = new FeedbackDialog(activity, new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    feedbackDialog = null;
                }
            });
        }
        feedbackDialog.replaceContent("请留下您的使用感受吧",
                "稍后再说", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissDialog();
                        clearStatistics();
                    }
                }, "", null, initScoreView());
        feedbackDialog.show();
    }

    private View initScoreView() {
        View view = feedbackDialog.getLayoutInflater().inflate(R.layout.layout_score_tip, null);
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
    private void sendFovLikes() {
        reportFeedback(true, "", new IResultBack<FeedResult>() {
            @Override
            public void onResult(FeedResult feedResult) {
                if (null != feedbackDialog) {
                    String message = "点赞" + (feedResult.code == 10000 ? "成功" : "失败");
                    feedbackDialog.showToast(message);
                }
                alreadyScore();
                dismissDialog();
            }
        });
    }

    /**
     * 提交反馈
     */
    private void sendReport() {
        Log.e("ScoreUtil", "selecteds = " + selectedDowns);
        Log.e("ScoreUtil", "selecteds len = " + selectedDowns.size());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < selectedDowns.size(); i++) {
            builder.append(DEF_REASON[selectedDowns.get(i)]);
        }
        reportFeedback(false, builder.toString(), new IResultBack<FeedResult>() {
            @Override
            public void onResult(FeedResult feedResult) {
                if (feedResult.code == 10000) {
                    Log.e(TAG, "反馈成功");
                }
                alreadyScore();
                // 提交成功后显示推荐活动
                showLastPromotion();
            }
        });
    }

    /**
     * 显示吐槽弹框
     */
    private void shwoDownDialog() {
        if (null == feedbackDialog) return;
        feedbackDialog.replaceContent("请问哪个方面需要改进呢？",
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

    private final List<Integer> selectedDowns = new ArrayList();

    private View initDownView() {
        View view = feedbackDialog.getLayoutInflater().inflate(R.layout.layout_score_down, null);
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
    private void showLastPromotion() {
        if (null == feedbackDialog) return;
        feedbackDialog.replaceContent("融云最近活动，了解一下？",
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
                }, feedbackDialog.getLayoutInflater().inflate(R.layout.layout_promotion, null));
    }

    private void jumpPromotionPage() {
        if (null != feedbackDialog && feedbackDialog.enable()) {
            Intent intent = new Intent("io.rong.intent.action.commonwebpage");
            intent.putExtra("key_url", "https://m.rongcloud.cn/activity/rtc20");
            intent.putExtra("key_basic", "最近活动");
            feedbackDialog.startActivty(intent);
        }
        dismissDialog();
    }

    public static class FeedResult {
        protected int code;
        protected int message;
    }

    private void reportFeedback(boolean goodFeedback, String reason, IResultBack<FeedResult> resultBack) {
        Map<String, Object> params = new HashMap<>(4);
        params.put("isGoodFeedback", goodFeedback);
        params.put("reason", reason);
        String url = ApiConstant.INSTANCE.getBASE_URL() + "feedback/create";
        Core.core().post(null, url, params, new OCallBack<FeedResult>() {
            @Override
            public FeedResult onParse(Response response) throws Exception {
                String string = response.body().string();
                Log.e(TAG, "string = " + string);
                JsonObject jsonObj = JsonParser.parseString(string).getAsJsonObject();
                return GsonUtil.json2Obj(jsonObj, FeedResult.class);
            }

            @Override
            public void onResult(FeedResult result) {
                resultBack.onResult(result);
            }
        });
    }
}
