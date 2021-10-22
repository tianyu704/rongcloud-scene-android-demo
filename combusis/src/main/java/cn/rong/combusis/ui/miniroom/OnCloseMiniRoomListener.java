package cn.rong.combusis.ui.miniroom;

public interface OnCloseMiniRoomListener {

    void onCloseMiniRoom(CloseResult closeResult);

    interface CloseResult {
        void onClose();
    }
}