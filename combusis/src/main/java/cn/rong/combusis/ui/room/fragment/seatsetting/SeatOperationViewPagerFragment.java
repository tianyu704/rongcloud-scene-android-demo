package cn.rong.combusis.ui.room.fragment.seatsetting;

import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.rongcloud.common.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.R;
import cn.rong.combusis.common.base.BaseBottomSheetDialogFragment;
import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.provider.voiceroom.RoomOwnerType;
import cn.rong.combusis.ui.room.fragment.LiveLayoutSettingCallBack;
import cn.rong.combusis.ui.room.fragment.SeatActionClickListener;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * 麦位操作的fragment
 */
public class SeatOperationViewPagerFragment extends BaseBottomSheetDialogFragment {


    public List<Disposable> disposableList = new ArrayList<>();
    private TabLayout tlTitle;
    private ViewPager2 vpPage;
    private int index;
    private ArrayList<User> requestSeats = new ArrayList<>();
    private ArrayList<User> inviteSeats = new ArrayList<>();
    private RequestSeatFragment requestSeatFragment;
    private InviteSeatFragment inviteSeatFragment;
    private SeatActionClickListener seatActionClickListener;
    private LiveLayoutSettingCallBack liveLayoutSettingCallBack;
    private RoomOwnerType roomOwnerType;

    public SeatOperationViewPagerFragment(RoomOwnerType roomOwnerType) {
        super(R.layout.fragment_viewpage_list);
        this.roomOwnerType = roomOwnerType;
    }

    public RoomOwnerType getRoomOwnerType() {
        return roomOwnerType;
    }

    public void setRequestSeats(ArrayList<User> requestSeats) {
        this.requestSeats.clear();
        this.requestSeats.addAll(requestSeats);
        if (requestSeatFragment != null) {
            requestSeatFragment.refreshData(requestSeats);
        }
    }

    public void setInviteSeats(ArrayList<User> inviteSeats) {
        this.inviteSeats.clear();
        this.inviteSeats.addAll(inviteSeats);
        if (inviteSeatFragment != null) {
            inviteSeatFragment.refreshData(inviteSeats);
        }
    }

    private int seatIndex = -1;

    public void setInviteSeatIndex(int seatIndex) {
        this.seatIndex = seatIndex;
    }

    @Override
    public void initView() {
        tlTitle = (TabLayout) getView().findViewById(R.id.tl_title);
        vpPage = (ViewPager2) getView().findViewById(R.id.vp_page);
        disposableList.clear();
        ArrayList<BaseFragment> fragments = new ArrayList<>();
        requestSeatFragment = new RequestSeatFragment(requestSeats);
        inviteSeatFragment = new InviteSeatFragment(inviteSeats);
        inviteSeatFragment.setInviteSeatIndex(seatIndex);
        LivelayoutSettingFragment livelayoutSettingFragment = new LivelayoutSettingFragment();
        fragments.add(requestSeatFragment);
        fragments.add(inviteSeatFragment);
        if (roomOwnerType == RoomOwnerType.LIVE_OWNER) {
            fragments.add(livelayoutSettingFragment);
        }
        vpPage.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        FragmentStateAdapter fragmentStateAdapter = new FragmentStateAdapter(getChildFragmentManager(), getLifecycle()) {
            @Override
            public int getItemCount() {
                return fragments.size();
            }

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return fragments.get(position);
            }
        };
        vpPage.setAdapter(fragmentStateAdapter);
        new TabLayoutMediator(tlTitle, vpPage, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(fragments.get(position).getTitle());
            }
        }).attach();
    }

    @Override
    public void onResume() {
        super.onResume();
        vpPage.setCurrentItem(index, false);
    }

    /**
     * 设置跳转的位置
     *
     * @param index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 设置监听邀请列表的变化
     *
     * @param obInviteSeatListChangeSuject
     */
    public void setObInviteSeatListChangeSuject(Observable<List<User>> obInviteSeatListChangeSuject) {
        disposableList.add(obInviteSeatListChangeSuject.subscribe(new Consumer<List<User>>() {
            @Override
            public void accept(List<User> users) throws Throwable {
                if (inviteSeatFragment != null) {
                    inviteSeatFragment.refreshData(users);
                }
            }
        }));
    }

    /**
     * 设置监听申请列表的变化
     *
     * @param obRequestSeatListChangeSuject
     */
    public void setObRequestSeatListChangeSuject(Observable<List<User>> obRequestSeatListChangeSuject) {
        disposableList.add(obRequestSeatListChangeSuject.subscribe(new Consumer<List<User>>() {
            @Override
            public void accept(List<User> users) throws Throwable {
                if (requestSeatFragment != null) {
                    requestSeatFragment.refreshData(users);
                }
            }
        }));
    }

    public SeatActionClickListener getSeatActionClickListener() {
        return seatActionClickListener;
    }

    /**
     * 设置回调接口
     *
     * @param seatActionClickListener
     */
    public void setSeatActionClickListener(SeatActionClickListener seatActionClickListener) {
        this.seatActionClickListener = seatActionClickListener;
    }


    /**
     * 布局设置接口
     *
     * @param liveLayoutSettingCallBack
     */
    public void setLiveLayoutSettingCallBack(LiveLayoutSettingCallBack liveLayoutSettingCallBack) {
        this.liveLayoutSettingCallBack = liveLayoutSettingCallBack;
    }

    public LiveLayoutSettingCallBack getLiveLayoutSettingCallBack() {
        return liveLayoutSettingCallBack;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        for (Disposable disposable : disposableList) {
            disposable.dispose();
        }
        disposableList.clear();
    }
}
