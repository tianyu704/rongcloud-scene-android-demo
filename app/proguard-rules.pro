# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.umeng.** {*;}

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.zui.**{*;}
-keep class com.miui.**{*;}
-keep class com.heytap.**{*;}
-keep class a.**{*;}
-keep class com.vivo.**{*;}

-keep class android.support.** {*;}
-keep class cn.rongcloud.rtc.core.** {*;}
-keep class cn.rongcloud.rtc.api.** {*;}
-keep class cn.rongcloud.rtc.base.** {*;}
-keep class cn.rongcloud.rtc.utils.** {*;}
-keep class cn.rongcloud.rtc.media.http.** {*;}
-keep class cn.rongcloud.rtc.engine.view** {*;}
-keep class cn.rongcloud.rtc.proxy.message.** {*;}
-keep class cn.rongcloud.rtc.RongRTCExtensionModule {*;}
-keep class cn.rongcloud.rtc.RongRTCMessageRouter {*;}
# 保留api相关保
-keep class cn.rongcloud.voiceroom.api.** {*;}
-keep class cn.rongcloud.voiceroom.model.** {*;}
-keep class cn.rongcloud.voiceroom.utils.** {*;}
-keep class cn.rongcloud.messager.** {*;}