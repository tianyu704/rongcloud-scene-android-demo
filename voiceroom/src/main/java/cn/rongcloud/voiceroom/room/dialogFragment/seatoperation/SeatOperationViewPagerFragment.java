package cn.rongcloud.voiceroom.room.dialogFragment.seatoperation;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.rongcloud.common.base.BaseFragment;

import java.util.ArrayList;

import cn.rong.combusis.common.base.BaseBottomSheetDialogFragment;
import cn.rongcloud.voiceroom.R;
import cn.rongcloud.voiceroom.room.NewVoiceRoomModel;

/**
 * 麦位操作的fragment
 */
public class SeatOperationViewPagerFragment extends BaseBottomSheetDialogFragment {


    private TabLayout tlTitle;
    private ViewPager2 vpPage;
    private NewVoiceRoomModel newVoiceRoomModel;
    private int index;

    public SeatOperationViewPagerFragment(NewVoiceRoomModel newVoiceRoomModel, int index) {
        super(R.layout.fragment_new_viewpage_list);
        this.newVoiceRoomModel=newVoiceRoomModel;
        this.index=index;
    }

    @Override
    public void initView() {
        tlTitle = (TabLayout) getView().findViewById(R.id.tl_title);
        vpPage = (ViewPager2) getView().findViewById(R.id.vp_page);

        ArrayList<BaseFragment> fragments = new ArrayList<>();
        fragments.add(new RequestSeatFragment(newVoiceRoomModel));
        fragments.add(new InviteSeatFragment(newVoiceRoomModel));

        vpPage.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        FragmentStateAdapter fragmentStateAdapter = new FragmentStateAdapter(getChildFragmentManager(), getLifecycle()) {
            @Override
            public int getItemCount() {
                return 2;
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
        vpPage.setCurrentItem(index,false);
    }
}
