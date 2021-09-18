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
                if (!tvPk.isSelected()) {//未邀请
                    dialog = new RoomOwerDialog(activity, new IResultBack<Boolean>() {
                        @Override
                        public void onResult(Boolean aBoolean) {
                            if (aBoolean) {//邀请成功
                                tvPk.setSelected(true);
                                tvPk.setText("等待接收");
                            }
                        }
                    });
                } else {//已邀请 撤销邀请
                    dialog = new CancelPKDialog(activity, new IResultBack<Boolean>() {
                        @Override
                        public void onResult(Boolean aBoolean) {
                            if (aBoolean) {//撤销成功
                                tvPk.setSelected(false);
                                tvPk.setText("发起pk");
                            }
                        }
                    });
                }
                dialog.show();
            }
        });
    }
}
