package cn.rongcloud.voiceroom.room.dialogFragment.seatoperation;


import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rongcloud.common.base.BaseFragment;
import com.rongcloud.common.extension.ExtensKt;

import java.util.List;

import cn.rong.combusis.provider.user.User;
import cn.rong.combusis.sdk.event.wrapper.EToast;
import cn.rong.combusis.ui.room.fragment.ClickCallback;
import cn.rongcloud.voiceroom.R;
import cn.rongcloud.voiceroom.room.NewVoiceRoomModel;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * 邀请连麦fragment
 */
public class InviteSeatFragment extends BaseFragment {

    private RecyclerView rvList;
    private MyAdapter myAdapter;
    private NewVoiceRoomModel newVoiceRoomModel;

    public InviteSeatFragment(NewVoiceRoomModel newVoiceRoomModel) {
        super(R.layout.layout_list);
        this.newVoiceRoomModel=newVoiceRoomModel;
    }

    @Override
    public void initView() {
        rvList = (RecyclerView) getView().findViewById(R.id.rv_list);
        rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        myAdapter = new MyAdapter();
        rvList.setAdapter(myAdapter);
        refreshData(newVoiceRoomModel.getInviteSeats());
        //监听，房间人员离开，被踢出，上麦，下麦都操作都要时时刻刻影响可邀请的列表
        newVoiceRoomModel.obInviteSeatListChange().subscribe(new Consumer<List<User>>() {
            @Override
            public void accept(List<User> users) throws Throwable {
                refreshData(users);
            }
        });
    }

    @NonNull
    @Override
    public String getTitle() {
        return "邀请连麦";
    }

    /**
     * 刷新列表
     *
     * @param uiMemberModels
     */
    public void refreshData(List<User> uiMemberModels) {
        myAdapter.refreshData(uiMemberModels);
    }

    /**
     * 创建适配器
     */
    class MyAdapter extends BaseListAdapter<MyAdapter.MyViewHolder>{

        @NonNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyAdapter.MyViewHolder(parent);
        }


        class MyViewHolder extends BaseViewHolder{

            public MyViewHolder(@NonNull ViewGroup parent) {
                super(parent);
            }

            @Override
            public void bindView(@NonNull User uiMemberModel, @NonNull View itemView) {

                ImageView iv_user_portrait = itemView.findViewById(R.id.iv_user_portrait);
                TextView tv_member_name = itemView.findViewById(R.id.tv_member_name);
                TextView tv_operation = itemView.findViewById(R.id.tv_operation);
                ExtensKt.loadPortrait(iv_user_portrait,uiMemberModel.getPortrait());
                tv_member_name.setText(uiMemberModel.getUserName());
                tv_operation.setText("邀请");
//                tv_operation.setSelected(uiMemberModel.isInvitedInfoSeat());
                tv_operation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //开始执行邀请逻辑
                        newVoiceRoomModel.clickInviteSeat(uiMemberModel.getUserId(), new ClickCallback<Boolean>() {
                            @Override
                            public void onResult(Boolean result, String msg) {
                                if (result) {
                                    ((SeatOperationViewPagerFragment) getParentFragment()).dismiss();
                                    EToast.showToast("已邀请上麦");
                                } else {
                                    EToast.showToast(msg);
                                }
                            }
                        });
                    }
                });
            }
        }
    }
}
