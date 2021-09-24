package cn.rongcloud.widget;

import static java.lang.System.currentTimeMillis;

import android.Manifest;
import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rongcloud.common.utils.FileUtil;
import com.rongcloud.common.utils.UiUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import cn.rong.combusis.common.utils.AudioRecorderUtil.AudioRecordListener;
import cn.rong.combusis.common.utils.AudioRecorderUtil.AudioRecorder;
import cn.rong.combusis.common.utils.AudioRecorderUtil.RecordStreamListener;
import cn.rong.combusis.common.utils.EncryUtils;
import cn.rong.combusis.common.utils.MediaPlayTools;
import cn.rong.combusis.common.utils.PermissionUtil;
import cn.rongcloud.voiceroomdemo.R;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.rong.imkit.picture.tools.ToastUtils;

/**
 * Created by 李浩 on 2021/09/16.  录音弹窗
 */

public class RecordVoicePopupWindow {

    public final static int LOADING_MODE = 1;
    public final static int RECORDING_MODE = 2;
    public final static int CANCEL_MODE = 3;
    public final static int TOO_SHORT_MODE = 4;
    public final static int RECORD_FINISH_MODE = 5;
    public final static int AMPLITUDE_MODE = 6;
    private static final String TAG = "RecordVoicePopupWindow";

    private Activity context;
    private PopupWindow popupWindow;
    private View rootView;
    private View mVoiceRecordHintTooShort;
    private View mVoiceRecordHintGoing;
    private View mVoiceRecordHintLoading;
    private View mVoiceRecordHitCancel;

    private RelativeLayout mVoiceRecordAnim;
    private TextView mVoiceRecordCountDown;

    private ImageView mVoiceRecordTooLong;
    private TextView mVoiceNormalWording;
    private ImageView mVoiceHintAnimIv;

    private static final int ampValue[] = {
            0, 15, 30, 45, 60, 75, 90, 100
    };
    private static final int ampIcon[] = {
            R.drawable.icon_voice_anim_1,
            R.drawable.icon_voice_anim_2,
            R.drawable.icon_voice_anim_3,
            R.drawable.icon_voice_anim_4,
            R.drawable.icon_voice_anim_5,
            R.drawable.icon_voice_anim_6,
    };
    //倒计时数字
    private int secondsRemaining;

    private long currentTimeMillis = 0;

    private boolean isVoiceRecording = false;

    private  RecordCallBack callBack;

    public RecordVoicePopupWindow(Activity context, RecordCallBack callBack) {
        this.context = context;
        this.callBack=callBack;
        init();
    }

    private void init() {
        rootView = View.inflate(context, R.layout.view_voice_rcd_hint_window, null);
        popupWindow = new PopupWindow(rootView,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        mVoiceRecordHintLoading = rootView.findViewById(R.id.voice_record_hint_loading);
        mVoiceRecordHitCancel = rootView.findViewById(R.id.voice_record_hint_cancel);
        mVoiceRecordHintGoing = rootView.findViewById(R.id.voice_record_hint_going);
        mVoiceRecordHintTooShort = rootView.findViewById(R.id.voice_record_hint_too_short);

        mVoiceRecordAnim = ((RelativeLayout) rootView.findViewById(R.id.voice_record_anim));
        mVoiceRecordCountDown = ((TextView) rootView.findViewById(R.id.voice_record_count_down));
        mVoiceRecordTooLong = ((ImageView) rootView.findViewById(R.id.voice_record_too_long));
        mVoiceHintAnimIv = ((ImageView) rootView.findViewById(R.id.voice_rcd_hint_anim));
        mVoiceNormalWording = ((TextView) rootView.findViewById(R.id.voice_record_normal_wording));

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                secondsRemaining = 60;
            }
        });
    }

    /**
     * 绑定触发弹窗的view
     */
    public void bindView(View sendVoiceView) {
        AudioRecorder.getInstance().setAudioRecordListener(new AudioRecordListener() {
            @Override
            public void startAudioRecord() {

            }

            @Override
            public void endAudioRecord(File file) {
                sendVoiceView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isCancel()) {
                            changeRecordMode(RECORD_FINISH_MODE);
                            return;
                        }
                        if (file.exists()) {
                            int localVideoDuration = MediaPlayTools.getLocalVideoDuration(file.getPath());
                            if (localVideoDuration < 1000) {
                                changeRecordMode(TOO_SHORT_MODE);
                                Observable.timer(2, TimeUnit.SECONDS)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<Long>() {
                                            @Override
                                            public void accept(Long aLong) throws Throwable {
                                                changeRecordMode(RECORD_FINISH_MODE);
                                            }
                                        });
                            } else {
                                callBack.onSendVoiceMessage(file);
                                changeRecordMode(RECORD_FINISH_MODE);
                            }
                        } else {
                            changeRecordMode(RECORD_FINISH_MODE);
                        }
                    }
                });

            }
        });
        sendVoiceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //判断当前是否有存储卡
                if (!FileUtil.INSTANCE.isExistExternalStore()) {
                    ToastUtils.s(context, context.getString(R.string.media_ejected));
                    return true;
                }
                //判断存储空间
                if (FileUtil.INSTANCE.getAvailaleSize() < 10) {
                    ToastUtils.s(context, context.getString(R.string.media_no_memory));
                    return true;
                }
                //判断当前事件是否是有效时间
                long time = currentTimeMillis() - currentTimeMillis;
                if (time <= 1000) {
                    Log.e(TAG, "Invalid click ");
                    currentTimeMillis = currentTimeMillis();
                    return true;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //开始准备录音,检查是否有录音权限和存储权限，有权限，开始录音，并且修改弹窗状态去弹出弹窗
                        if (PermissionUtil.checkPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO}) && PermissionUtil.getRecordState() == PermissionUtil.STATE_SUCCESS) {
                            isVoiceRecording = true;
                            changeRecordMode(RecordVoicePopupWindow.LOADING_MODE);
                            show(sendVoiceView);
                            String mAmrPathName = EncryUtils.md5(String.valueOf(currentTimeMillis()));
                            AudioRecorder.getInstance().createDefaultAudio(mAmrPathName);
                            //监控录音数据，根据音量显示动画
                            AudioRecorder.getInstance().startRecord(new RecordStreamListener() {
                                @Override
                                public void recordOfByte(byte[] data, int begin, int end) {

                                }

                                @Override
                                public void recordDb(Double db) {
                                    displayAmplitude(db);
                                }
                            });
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //在这里拦截外部的滑动事件
                        sendVoiceView.getParent().requestDisallowInterceptTouchEvent(true);
                        if (event.getX() <= 0.0f || event.getY() <= -60 || event.getX() >= sendVoiceView.getWidth()) {
                            changeRecordMode(RecordVoicePopupWindow.CANCEL_MODE);
                        } else {
                            changeRecordMode(RecordVoicePopupWindow.RECORDING_MODE);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        sendVoiceView.getParent().requestDisallowInterceptTouchEvent(false);
                        AudioRecorder.getInstance().stopRecord();
                        isVoiceRecording = false;
                        if (isCancel()) {
                            //取消发送
                        } else {
                            //确定发送,判断录音文件是否可以发送来判断Ui逻辑

                        }
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 展示录音弹出
     *
     * @param anchorView
     */
    public final void show(View anchorView) {
        rootView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int mHeight = rootView.getMeasuredHeight();
        UiUtils.INSTANCE.getScreenHeight((Activity) context);
        int screenHeight = UiUtils.INSTANCE.getScreenHeight((Activity) context);
        // 获取锚点View在屏幕上的左上角坐标位置
        int anchorLoc[] = new int[2];
        anchorView.getLocationOnScreen(anchorLoc);
        int anchorHeight = anchorView.getHeight();
        int yLocation = (screenHeight - anchorHeight) / 2 - mHeight / 2;
        popupWindow.showAtLocation(anchorView, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, yLocation);
    }

    public void dismiss() {
        //最后一秒的时候，然后显示
        if (secondsRemaining == 1) {
            //如果显示超时
            Observable.just(secondsRemaining)
                    .delay(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(new Function<Integer, Integer>() {
                        @Override
                        public Integer apply(Integer integer) throws Throwable {
                            mVoiceRecordTooLong.setVisibility(View.VISIBLE);
                            mVoiceRecordAnim.setVisibility(View.GONE);
                            mVoiceRecordCountDown.setVisibility(View.GONE);
                            mVoiceNormalWording.setText(R.string.chatfooter_too_long);
                            return integer;
                        }
                    })
                    .delay(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) throws Throwable {
                            if (popupWindow != null) {
                                popupWindow.dismiss();
                            }
                        }
                    });
        } else {
            if (popupWindow != null) {
                popupWindow.dismiss();
            }
        }
    }

    /**
     * 动画震荡幅度
     *
     * @param amplitude
     */
    public void displayAmplitude(double amplitude) {
        for (int i = 0; i < ampIcon.length; i++) {
            if (amplitude < ampValue[i] || amplitude >= ampValue[i + 1]) {
                continue;
            }
            mVoiceHintAnimIv.setBackgroundDrawable(context.getResources().getDrawable(ampIcon[i]));
            if ((amplitude == -1) && (this.popupWindow != null)) {
                popupWindow.dismiss();
                changeRecordMode(LOADING_MODE);
            }
            return;
        }
    }

    /**
     * 倒计时显示,可以根据选择是否开启
     *
     * @param millisUntilFinished
     */
    public void onTick(long millisUntilFinished) {
        //直接除以1000会丢失精度，所以+1，为了补回精度
        secondsRemaining = (int) (millisUntilFinished / 1000) + 1;
        if (secondsRemaining < 0) {
            return;
        }
        if (secondsRemaining <= 10) {
            //倒计时显示
            mVoiceRecordCountDown.setVisibility(View.VISIBLE);
            mVoiceRecordCountDown.setText(secondsRemaining + "");
            mVoiceRecordAnim.setVisibility(View.GONE);
        }
    }

    public boolean isCancel() {
        return mVoiceRecordHitCancel != null && mVoiceRecordHitCancel.getVisibility() == View.VISIBLE;
    }

    public void changeRecordMode(int mode) {
        switch (mode) {
            case LOADING_MODE:
                mVoiceRecordHintLoading.setVisibility(View.VISIBLE);
                mVoiceRecordHintGoing.setVisibility(View.GONE);
                mVoiceRecordHitCancel.setVisibility(View.GONE);
                mVoiceRecordHintTooShort.setVisibility(View.GONE);
                //初始化控件显示
                mVoiceRecordAnim.setVisibility(View.VISIBLE);
                mVoiceRecordTooLong.setVisibility(View.GONE);
                mVoiceRecordCountDown.setVisibility(View.GONE);
                mVoiceNormalWording.setText(R.string.chatfooter_cancel_rcd);
                break;
            case RECORDING_MODE:
                mVoiceRecordHintLoading.setVisibility(View.GONE);
                mVoiceRecordHintGoing.setVisibility(View.VISIBLE);
                mVoiceRecordHitCancel.setVisibility(View.GONE);
                mVoiceRecordHintTooShort.setVisibility(View.GONE);
                break;
            case CANCEL_MODE:
                mVoiceRecordHintLoading.setVisibility(View.GONE);
                mVoiceRecordHintGoing.setVisibility(View.GONE);
                mVoiceRecordHitCancel.setVisibility(View.VISIBLE);
                mVoiceRecordHintTooShort.setVisibility(View.GONE);
                break;
            case TOO_SHORT_MODE:
                mVoiceRecordHintLoading.setVisibility(View.GONE);
                mVoiceRecordHintGoing.setVisibility(View.GONE);
                mVoiceRecordHitCancel.setVisibility(View.GONE);
                mVoiceRecordHintTooShort.setVisibility(View.VISIBLE);
                break;
            case RECORD_FINISH_MODE:
                dismiss();
                break;
            default:
                break;
        }
    }

    public void onFinish() {
    }

    public interface RecordCallBack {

        void onSendVoiceMessage(File voiceFile);
    }
}
