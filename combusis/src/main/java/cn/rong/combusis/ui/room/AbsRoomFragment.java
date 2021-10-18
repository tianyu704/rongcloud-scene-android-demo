package cn.rong.combusis.ui.room;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.basis.mvp.BasePresenter;
import com.basis.ui.BaseFragment;
import com.kit.utils.Logger;

/**
 * @author gyn
 * @date 2021/9/17
 */
public abstract class AbsRoomFragment<P extends BasePresenter> extends BaseFragment<P> implements SwitchRoomListener {

    public static final String ROOM_ID = "ROOM_ID";

    // 是否执行了joinRoom
    private boolean isExecuteJoinRoom = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("==================================onCreate:" + getTag());
    }

    @Override
    public void initListener() {
        addSwitchRoomListener();
    }

    @Override
    public void joinRoom() {
        isExecuteJoinRoom = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d("==================================onStart:" + getTag());
    }

    @Override
    public void onResume() {
        super.onResume();
        // viewPager2的onPageSelected，当从列表点击最后一个时，viewpager2选中最后一个时，会先执行onPageSelected，才执行fragment的onCreate,
        // 导致addSwitchRoomListener没执行，joinRoom就不会执行，这里判断一下没执行再执行一下。
        if (!isExecuteJoinRoom) {
            joinRoom();
        }
        Logger.d("==================================onResume:" + getTag());
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.d("==================================onPause:" + getTag());
    }

    @Override
    public void onStop() {
        super.onStop();
        Logger.d("==================================onStop:" + getTag());
    }

    @Override
    public void onDestroyView() {
        removeSwitchRoomListener();
        super.onDestroyView();
    }

}
