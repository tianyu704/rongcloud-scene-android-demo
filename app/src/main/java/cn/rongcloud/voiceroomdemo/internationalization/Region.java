package cn.rongcloud.voiceroomdemo.internationalization;

import com.google.gson.JsonObject;

import java.io.Serializable;

import cn.rongcloud.voiceroomdemo.internationalization.sort.Cn2Spell;

public class Region implements Serializable, Comparable<Region> {
    String region;
    Locale locale;

    public Region(JsonObject object, boolean zh) {
        if (null != object) {
            if (object.has("region")) {
                this.region = object.get("region").getAsString();
            }
            if (object.has("locale")) {
                this.locale = new Locale(object.get("locale").getAsJsonObject(), zh);
            }
        }
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getFirstLetter() {
        return null == locale ? "#" : locale.firstLetter;
    }

    @Override
    public int compareTo(Region o) {
        return this.locale.compareTo(o.locale);
    }

    public static class Locale implements Serializable, Comparable<Locale> {
        private String en;
        private String zh;
        // 业务字段
        private String pinyin; // 姓名对应的拼音
        private String firstLetter;

        public Locale(JsonObject object, boolean Zh) {
            if (null != object) {
                if (object.has("en")) {
                    en = object.get("en").getAsString();
                }
                if (object.has("zh")) {
                    zh = object.get("zh").getAsString();
                }
            }
            init(Zh);
        }

        void init(boolean Zh) {
            pinyin = Zh ? Cn2Spell.getPinYin(zh) : en;
            firstLetter = pinyin.substring(0, 1).toUpperCase(); // 获取拼音首字母并转成大写
            if (!firstLetter.matches("[A-Z]")) { // 如果不在A-Z中则默认为“#”
                firstLetter = "#";
            }
        }

        public String getEn() {
            return en;
        }

        public void setEn(String en) {
            this.en = en;
        }

        public String getZh() {
            return zh;
        }

        public void setZh(String zh) {
            this.zh = zh;
        }

        @Override
        public int compareTo(Locale o) {
            if (firstLetter.equals("#") && !o.firstLetter.equals("#")) {
                return 1;
            } else if (!firstLetter.equals("#") && o.firstLetter.equals("#")) {
                return -1;
            } else {
                return pinyin.compareToIgnoreCase(o.pinyin);
            }
        }

    }
}
