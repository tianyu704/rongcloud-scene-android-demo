//package cn.rongcloud.voiceroomdemo.utils;
//
//import android.app.Activity;
//import android.content.Context;
//import android.media.AudioManager;
//import android.media.ToneGenerator;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Vibrator;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.File;
//
//import static android.content.Context.AUDIO_SERVICE;
//
//import static com.rongcloud.common.utils.PermissionUtil.PermissionsReadExternalStorage;
//import static com.rongcloud.common.utils.PermissionUtil.PermissionsWriteExternalStorage;
//
//import com.rongcloud.common.utils.EncryUtils;
//import com.rongcloud.common.utils.MediaPlayTools;
//import com.rongcloud.common.utils.PermissionUtil;
//
//import cn.rongcloud.widget.RecordVoicePopupWindow;
//import io.rong.imkit.RongIM;
//
///**
// * Created by 李浩 on 2021/9/16.
// */
//
//public class RecordVoiceImpl implements OnRecordVoiceListener{
//
//    public static final int RECORD_IDLE = 0;
//    public static final int RECORD_ING = 1;
//    public static final int RECORD_DONE = 2;
//    public static final int TONE_LENGTH_MS = 200;
//    private static final float TONE_RELATIVE_VOLUME = 100.0F;
//    private static final int WHAT_ON_COMPUTATION_TIME = 10000;
//    private static final int MIX_TIME = 1000;
//
//    private Activity context;
////    private ECChatManager mChatManager;  聊天管理工具，用融云自带的
//
////    private ECMessage mPreMessage;  封装成一条语音消息
//    protected String mAmrPathName;
//    public int mRecordState = RECORD_IDLE;
//    private boolean isVoiceRecording = false;
//
//    private RecordCallBack callBack;
//    private Object mToneGeneratorLock = new Object();
//    private Vibrator mVibrator;
//    private ToneGenerator mToneGenerator;
//    private Handler mHandler = new Handler(Looper.getMainLooper());
//    private Object mLock = new Object();
//    private int mVoiceRecodeTime = 0;
//    private boolean isRecordAndSend = false;
//    private AudioManager mAudioManager;
//
//    public RecordVoiceImpl(Activity context, RecordCallBack callBack){
//        this.context = context;
//        this.callBack = callBack;
////        mChatManager = ECDevice.getECChatManager();
//    }
//
//    @Override
//    public void onPrepareRecordVoice() {
//        if (callBack != null) {
//            if(MediaPlayTools.getInstance(context.getApplicationContext()).requestAudioFocus()){
//                callBack.onPrepareRecord();
//            }
//        }
////        mChatManager.stopVoiceRecording(null);
//    }
//
//    @Override
//    public void onRecordVoiceInitRequest() {
//        //先拿到读写内存卡的权限
//        PermissionUtil.checkPermissions(context,new String[]{PermissionsReadExternalStorage,PermissionsWriteExternalStorage});
//        mAmrPathName = EncryUtils.md5(String.valueOf(System.currentTimeMillis())) + ".amr";
//        if (FileAccessor.getVoicePathName() == null) {
//            ToastUtil.showMessage(R.string.ytx_path_to_file);
//            mAmrPathName = null;
//            return;
//        }
//        if (getRecordState() != RECORD_ING) {
//            setRecordState(RECORD_ING);
//            readyOperation();
//            callBack.notifyRecordState(ChattingPresenter.ImUserState.RECORD_VOICE);
//            callBack.notifyVoiceRecordMode(RecordVoicePopupWindow.LOADING_MODE);//通知弹窗显示加载中
////            if (mChatManager == null) {
////                return;
////            }
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
////                    if (EasyPermissionsEx.hasPermissions(context, needPermissionsVoice)) {
////                        try {
////                            ECMessage message = ECMessage.createECMessage(ECMessage.Type.VOICE);
////                            message.setTo(callBack.getMessageRecipients());
////                            message.setSessionId(callBack.getMessageRecipients());
////                            ECVoiceMessageBody messageBody = new ECVoiceMessageBody(new File(FileAccessor.getVoicePathName(), mAmrPathName), 0);
////                            message.setBody(messageBody);
////                            mPreMessage = message;
////                            if (callBack.isBurnMode()) {
////                                mPreMessage.setApsAlert("[阅后即焚]");
////                            }
////                            // 仅录制语音消息，录制完成后需要调用发送接口发送消息
////                            mChatManager.startVoiceRecording(messageBody, new ECChatManager.OnRecordTimeoutListener() {
////                                @Override
////                                public void onRecordingTimeOut(long duration) {
////                                    handleRecordResult(false, true,true);
////                                }
////
////                                @Override
////                                public void onRecordingAmplitude(
////                                        double amplitude) {
////                                    if (getRecordState() == RECORD_ING) {
////                                        callBack.displayAmplitude(amplitude);
////                                        if (!isVoiceRecording) {
////                                            isVoiceRecording = true;
////                                            callBack.notifyVoiceRecordMode(RecordVoicePopupWindow.RECORDING_MODE);
////                                        }
////                                    }
////                                }
////                            });
////                        } catch (Exception e) {
////                            e.printStackTrace();
////                        }
////                    } else {
////                        callBack.notifyVoiceRecordMode(RecordVoicePopupWindow.TOO_SHORT_MODE);
////                        EasyPermissionsEx.requestPermissions(context, context.getString(R.string.rationaleVoice),
////                                PERMISSIONS_REQUEST_VOICE, needPermissionsVoice);
////                    }
//                }
//            });
//        }
//    }
//
//    /**
//     * 开始录音
//     */
//    @Override
//    public void onRecordVoiceStartRequest() {
//        mHandler.removeMessages(WHAT_ON_COMPUTATION_TIME);
//        mHandler.sendEmptyMessageDelayed(WHAT_ON_COMPUTATION_TIME, TONE_LENGTH_MS);
//    }
//
//    /**
//     * 结束录音
//     */
//    @Override
//    public void onVoiceRecordStopRequest() {
////        callBack.notifyRecordState(ChattingPresenter.ImUserState.NONE);
//        if (getRecordState() == RECORD_ING) {
//            handleVoiceRecordAction(false, true);
//        }
//    }
//
//    /**
//     * 取消录音
//     */
//    @Override
//    public void onVoiceRecordCancelRequest() {
////        callBack.notifyRecordState(ChattingPresenter.ImUserState.NONE);
//        if (getRecordState() == RECORD_ING) {
//            handleVoiceRecordAction(true, false);
//        }
//    }
//
//    /**
//     * 处理语音录制结束事件
//     * @param cancel 是否取消或者停止录制
//     */
//    private void handleVoiceRecordAction(final boolean cancel, final boolean isSend) {
////        if (mChatManager == null) {
////            return;
////        }
//        mHandler.post(new Runnable() {
//
//            @Override
//            public void run() {
//                // 停止或者取消普通模式语音
////                mChatManager.stopVoiceRecording(new ECChatManager.OnStopVoiceRecordingListener() {
////                    @Override
////                    public void onRecordingComplete() {
////                        handleRecordResult(cancel, isSend);
////                        MediaPlayTools.getInstance().releaseAudioFocus();
////                    }
////                });
//            }
//        });
//    }
//    private void handleRecordResult(boolean cancel, boolean isSend) {
//        handleRecordResult(cancel,isSend,false);
//    }
//    private void handleRecordResult(boolean cancel, boolean isSend,boolean timeOut) {
//        isVoiceRecording = false;
//        int timeMax = 60;
//        if (getRecordState() == RECORD_ING) {
//            // 当前是否有正在录音的操作
//            // 定义标志位判断当前所录制的语音文件是否符合发送条件
//            // 只有当录制的语音文件的长度超过1s才进行发送语音
//            boolean isVoiceTooShort = false;
//            File amrPathFile = new File(FileAccessor.getVoicePathName(), mAmrPathName);
//            if (amrPathFile.exists()) {
//                mVoiceRecodeTime = DemoUtils.calculateVoiceTime(amrPathFile.getAbsolutePath());
//                if (!isRecordAndSend) {
//                    if (mVoiceRecodeTime * 1000 < MIX_TIME) {
//                        isVoiceTooShort = true;
//                    }
//                }
//            } else {
//                isVoiceTooShort = true;
//            }
//            // 设置录音空闲状态
//            setRecordState(RECORD_IDLE);
//            if (isVoiceTooShort && !cancel) {
//                // 提示语音文件长度太短
//                callBack.notifyVoiceRecordMode(RecordVoicePopupWindow.TOO_SHORT_MODE);
//                return;
//            }
//            // 关闭语音录制对话框
//            callBack.notifyVoiceRecordMode(RecordVoicePopupWindow.RECORD_FINISH_MODE);
//            if (!cancel && mPreMessage != null && isSend) {
//                ECVoiceMessageBody body = (ECVoiceMessageBody) mPreMessage.getBody();
//                body.setDuration(timeOut?timeMax:mVoiceRecodeTime);
//                try {
//                    mPreMessage.setUserData(new JSONObject().put("duration",mVoiceRecodeTime).toString());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                mPreMessage.setBody(body);
//                callBack.onSendMessage(mPreMessage);
//                return;
//            }
//            // 删除语音文件
//            amrPathFile.deleteOnExit();
//            // 重置语音时间长度统计
//            mVoiceRecodeTime = 0;
//        }
//    }
//
//    public int getRecordState() {
//        synchronized (mLock) {
//            return mRecordState;
//        }
//    }
//
//    public void setRecordState(int state) {
//        synchronized (mLock) {
//            this.mRecordState = state;
//        }
//    }
//
//    private void readyOperation() {
//        playTone(ToneGenerator.TONE_PROP_BEEP, TONE_LENGTH_MS);
//        new Handler().postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                stopTone();
//            }
//        }, TONE_LENGTH_MS);
//        vibrate(50L);
//    }
//
//    private void initToneGenerator() {
//        AudioManager mAudioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
//        if (mToneGenerator == null) {
//            try {
//                int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                int streamMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//                int volume = (int) (TONE_RELATIVE_VOLUME * (streamVolume / streamMaxVolume));
//                mToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, volume);
//
//            } catch (RuntimeException e) {
//                mToneGenerator = null;
//            }
//        }
//    }
//
//    private void stopTone() {
//        if (mToneGenerator != null)
//            mToneGenerator.stopTone();
//    }
//
//    private void playTone(int tone, int durationMs) {
//        synchronized (mToneGeneratorLock) {
//            initToneGenerator();
//            if (mToneGenerator == null) {
//                return;
//            }
//            mToneGenerator.startTone(tone, durationMs);
//        }
//    }
//
//    private synchronized void vibrate(long milliseconds) {
//        Vibrator mVibrator = (Vibrator) context.getSystemService(
//                Context.VIBRATOR_SERVICE);
//        if (mVibrator == null) {
//            return;
//        }
//        mVibrator.vibrate(milliseconds);
//    }
//
//    public interface RecordCallBack {
//        String getMessageRecipients();
//        boolean isBurnMode();
//        void onPrepareRecord();
//        void notifyRecordState(ChattingPresenter.ImUserState state);
//        void notifyVoiceRecordMode(int mode);
//        void displayAmplitude(double amplitude);
//        void onSendMessage(ECMessage message);
//    }
//}
