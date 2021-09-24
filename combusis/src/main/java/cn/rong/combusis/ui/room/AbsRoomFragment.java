package cn.rong.combusis.ui.room;

import com.basis.ui.BaseFragment;

import cn.rong.combusis.provider.voiceroom.RoomOwnerType;

/**
 * @author gyn
 * @date 2021/9/17
 */
public abstract class AbsRoomFragment<T> extends BaseFragment {

    private RoomOwnerType mRoomOwnerType;

    public abstract void destroyRoom();

    public abstract void joinRoom(T t);

    public abstract void onBackPressed();

    public RoomOwnerType getRoomOwnerType() {
        return mRoomOwnerType;
    }

    public void setRoomOwnerType(RoomOwnerType mRoomOwnerType) {
        this.mRoomOwnerType = mRoomOwnerType;
    }
}
