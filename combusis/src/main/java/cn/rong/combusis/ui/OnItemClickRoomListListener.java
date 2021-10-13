package cn.rong.combusis.ui;

public interface OnItemClickRoomListListener<T> {
    void clickItem(T item, int position,boolean isCreate);
}