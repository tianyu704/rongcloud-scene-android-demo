package cn.rongcloud.voiceroom.pk.domain;

import java.io.Serializable;
import java.util.List;

import cn.rong.combusis.provider.user.User;

public class PKInfo implements Serializable {
    private int score;
    private long pkTime;
    private List<User> userInfoList;

    public int getScore() {
        return score;
    }

    public long getPkTime() {
        return pkTime;
    }

    public List<User> getUserInfoList() {
        return userInfoList;
    }

}
