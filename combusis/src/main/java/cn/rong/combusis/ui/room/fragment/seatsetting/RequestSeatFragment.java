package cn.rong.combusis.ui.room.fragment.seatsetting;


import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rongcloud.common.base.BaseFragment;
import com.rongcloud.common.extension.ExtensKt;

import java.util.ArrayList;
import java.util.List;

import cn.rong.combusis.R;
import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.sdk.event.wrapper.EToast;
import cn.rong.combusis.ui.room.fragment.ClickCallback;
import cn.rongcloud.voiceroom.room.dialogFragment.seatoperation.BaseListAdapter;
import cn.rongcloud.voiceroom.room.dialogFragment.seatoperation.BaseViewHolder;

/**
 * 请求连麦fragment
 */
public class RequestSeatFragment extends BaseFragment {


    private RecyclerView rvList;
    private MyAdapter myAdapter;
    private ArrayList<User> requestSeats;

    public RequestSeatFragment(ArrayList<User> requestSeats) {
        super(R.layout.layout_list);
        this.requestSeats = requestSeats;
    }

    @Override
    public void initView() {
        rvList = (RecyclerView) getView().findViewById(R.id.rv_list);
        rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        myAdapter = new MyAdapter();
        rvList.setAdapter(myAdapter);
        refreshData(requestSeats);
    }

    @NonNull
    @Override
    public String getTitle() {
        return "申请连麦";
    }

    /**
     * 刷新列表
     *
     * @param uiMemberModels
     */
    public void refreshData(List<User> uiMemberModels) {
        if (myAdapter != null) {
            myAdapter.refreshData(uiMemberModels);
        }
    }

    /**
     * 创建适配器
     */
    class MyAdapter extends BaseListAdapter<MyAdapter.MyViewHolder> {

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(parent);
        }


        class MyViewHolder extends BaseViewHolder {

            public MyViewHolder(@NonNull ViewGroup parent) {
                super(parent);
            }

            @Override
            public void bindView(@NonNull User uiMemberModel, @NonNull View itemView) {

                ImageView iv_user_portrait = itemView.findViewById(R.id.iv_user_portrait);
                TextView tv_member_name = itemView.findViewById(R.id.tv_member_name);
                TextView tv_operation = itemView.findViewById(R.id.tv_operation);
                ExtensKt.loadPortrait(iv_user_portrait, uiMemberModel.getPortraitUrl());
                tv_member_name.setText(uiMemberModel.getUserName());
                tv_operation.setText("接受");
                tv_operation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tv_operation.setEnabled(false);
                        if (getParentFragment() instanceof SeatOperationViewPagerFragment) {
                            ((SeatOperationViewPagerFragment) getParentFragment())
                                    .getSeatActionClickListener().acceptRequestSeat(uiMemberModel.getUserId(), new ClickCallback<Boolean>() {
                                @Override
                                public void onResult(Boolean result, String msg) {
                                    if (result) {
                                        ((SeatOperationViewPagerFragment) getParentFragment()).dismiss();
                                    } else {
                                        EToast.showToast(msg);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
    }

}
