package cn.rongcloud.voiceroom.pk.domain;

import java.util.List;

public class PKResult {
    private long timeDiff;
    private List<PKInfo> roomScores;

    public List<PKInfo> getRoomScores() {
        return roomScores;
    }

    public long getTimeDiff() {
        return timeDiff;
    }
}