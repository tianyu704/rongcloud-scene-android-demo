package cn.rong.combusis.message;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;
import com.kit.cache.GsonUtil;

import java.nio.charset.StandardCharsets;

import cn.rong.combusis.common.utils.JsonUtils;
import cn.rong.combusis.provider.user.User;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * @author gyn
 * @date 2021/10/11
 */

@MessageTag(value = "RC:VRFollowMsg")
public class RCFollowMsg extends MessageContent {
    public static final Creator<RCFollowMsg> CREATOR = new Creator<RCFollowMsg>() {
        @Override
        public RCFollowMsg createFromParcel(Parcel source) {
            return new RCFollowMsg(source);
        }

        @Override
        public RCFollowMsg[] newArray(int size) {
            return new RCFollowMsg[size];
        }
    };
    private static final String TAG = "RCFollowMsg";
    @SerializedName("_userInfo")
    private User user;
    @SerializedName("_targetUserInfo")
    private User targetUser;

    public RCFollowMsg(byte[] data) {
        super(data);
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        RCFollowMsg temp = JsonUtils.fromJson(jsonStr, RCFollowMsg.class);
        if (temp != null) {
            this.user = temp.user;
            this.targetUser = temp.targetUser;
        }
    }

    public RCFollowMsg() {
    }

    protected RCFollowMsg(Parcel in) {
        this.user = GsonUtil.json2Obj(in.readString(), User.class);
        this.targetUser = GsonUtil.json2Obj(in.readString(), User.class);
    }

    @Override
    public byte[] encode() {
        return JsonUtils.toJson(this).getBytes(StandardCharsets.UTF_8);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(GsonUtil.obj2Json(this.user));
        dest.writeString(GsonUtil.obj2Json(this.targetUser));
    }

    public void readFromParcel(Parcel source) {
        this.user = GsonUtil.json2Obj(source.readString(), User.class);
        this.targetUser = GsonUtil.json2Obj(source.readString(), User.class);
    }
}
