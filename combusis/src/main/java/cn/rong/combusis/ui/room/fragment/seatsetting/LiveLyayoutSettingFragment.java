package cn.rong.combusis.ui.room.fragment.seatsetting;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rongcloud.common.base.BaseFragment;

import cn.rong.combusis.R;

/**
 * @author lihao
 * @project RongRTCDemo
 * @date 2021/11/19
 * @time 11:43 上午
 * 布局设置
 */
public class LiveLyayoutSettingFragment extends BaseFragment {


    private RecyclerView rcLayoutSetting;

    public LiveLyayoutSettingFragment() {
        super(R.layout.fragment_live_layout_setting);
    }

    @Override
    public void initView() {
        rcLayoutSetting = (RecyclerView) getView().findViewById(R.id.rc_layout_setting);
        rcLayoutSetting.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }

    @NonNull
    @Override
    public String getTitle() {
        return "布局设置";
    }

//    class MyAdapter extends RcyAdapter<String,>
//    }
}
