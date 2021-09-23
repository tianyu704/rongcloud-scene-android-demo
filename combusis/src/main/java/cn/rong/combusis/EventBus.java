package cn.rong.combusis;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class EventBus {

    public class TAG {
        public final static String SPEACK = "voiceroom_speaking";
        public final static String MUSIC_LIST = "music_play_list";
        public final static String PK_STATE = "pk_state";
    }

    private final static EventBus _bus = new EventBus();
    private final LinkedHashMap<String, List<EventCallback>> events = new LinkedHashMap<>(16);

    private EventBus() {
    }

    public static EventBus get() {
        return _bus;
    }

    /**
     * 订阅
     *
     * @param tag
     * @param callback
     */
    public void on(String tag, EventCallback callback) {
        if (!events.containsValue(tag)) {
            List<EventCallback> cals = new ArrayList<>(4);
            cals.add(callback);
            events.put(tag, cals);
        } else {
            events.get(tag).add(callback);
        }
    }

    /**
     * 移除订阅
     *
     * @param tag
     * @param callback
     */
    public void off(String tag, @NonNull EventCallback callback) {
        if (null == callback) {
            events.remove(tag);
        } else {
            events.get(tag).remove(callback);
        }
    }

    /**
     * 触发事件
     *
     * @param tag
     * @param args
     */
    public void emit(String tag, Object... args) {
        List<EventCallback> cals = events.get(tag);
        int count = cals == null ? 0 : cals.size();
        for (int i = count - 1; i > -1; i--) {
            cals.get(i).onEvent(args);
        }
    }

    public interface EventCallback {
        void onEvent(Object... args);
    }
}
