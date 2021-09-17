package cn.rongcloud.voiceroom.pk;

import android.view.View;
import android.widget.TextView;

import com.basis.ui.BaseFragment;
import com.basis.widget.BottomDialog;
import com.kit.wapper.IResultBack;

import cn.rongcloud.voiceroom.R;

public class VoomFragment extends BaseFragment {
    @Override
    public int setLayoutId() {
        return R.layout.fragment_test_room;
    }

    private BottomDialog dialog;
    TextView tvPk;
    @Override
    public void init() {
        tvPk = getView(R.id.tv_pk);
        tvPk.setSelected(false);
        tvPk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != dialog) {
                    dialog.dismiss();
                }
                tvPk.setSelected(!tvPk.isSelected());
                if (tvPk.isSelected()){//已邀请
                    dialog = new RoomOwerDialog(activity, new IResultBack() {
                        @Override
                        public void onResult(Object o) {
                            tvPk.setSelected(true);
                        }
                    });
                }else {//撤销邀请
                    dialog = new CancelPKDialog(activity);
                }
                dialog.show();
            }
        });
    }
}
