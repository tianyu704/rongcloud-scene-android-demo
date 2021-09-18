package cn.rongcloud.voiceroomdemo.utils;

/**
 * 录音接口
 */
public interface OnRecordVoiceListener {

    void onPrepareRecordVoice();

    void onRecordVoiceInitRequest();

    void onRecordVoiceStartRequest();

    void onVoiceRecordStopRequest();

    void onVoiceRecordCancelRequest();
}
