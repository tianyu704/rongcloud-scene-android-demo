package cn.rongcloud.voiceroomdemo.webview;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * @author: BaiCQ
 * @ClassName:
 * @Description:
 */
public class CommonUtil {

    /**
     * 是否连接上网络
     * @return 连接上true，未连接上false
     * @date: 2017-1-16 下午17:52
     */
    public static boolean isNetworkConnected(Context context) {
        // 网络连接的状态
        boolean isConnected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            // 建立网络数组
            NetworkInfo[] net_info = connectivityManager.getAllNetworkInfo();
            if (net_info != null) {
                for (int i = 0; i < net_info.length; i++) {
                    // 判断获得的网络状态是否是处于连接状态
                    if (net_info[i].getState() == NetworkInfo.State.CONNECTED) {
                        isConnected = true;
                        break;
                    }
                }
            }
        }
        return isConnected;

    }

    /**
     * 判断当前网络是否为wifi状态
     * @return wifi状态true，非wifi状态false
     * @date: 2017-1-16 下午17:52
     */

    public static boolean isWifi(Context context) {
        // 判断是否为wifi的状态
        boolean isWifi = false;
        if (isNetworkConnected(context)) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

            if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {

                isWifi = true;
            }
        }
        return isWifi;
    }

    /**
     * 根据网络情况设置webview的缓存模式
     *
     * @param webView WebVeiew
     * @param isHttps 是否是https
     */
    public static void openWebViewCache(Context context,WebView webView, boolean isHttps) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setUseWideViewPort(true);//设置此属性，可任意比例缩放
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(true); // 设置显示缩放按钮
        webSettings.setSupportZoom(true); // 支持缩放
        //加载https 的不要设置
//        if (isHttps) {
            webSettings.setJavaScriptEnabled(true);
//        }
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        //设置缓存模式
        if (!isNetworkConnected(context)) {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //优先加载缓存
        } else {
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT); //默认
        }
        // 开启 DOM storage API 功能
        webSettings.setDomStorageEnabled(true);
        //开启 database storage API 功能
        webSettings.setDatabaseEnabled(true);
        //开启 ApplicationCaches 功能
        webSettings.setAppCacheEnabled(true);

        //https与http混合资源处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
//        net::ERR_ACCESS_DENIED
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
    }

    /**
     * 将cookie同步到WebView
     *
     * @param url WebView要加载的url
     * @return true 同步cookie成功，false同步cookie失败
     * @Author JPH
     */
    public static void loadUrl(Context context,WebView webView, String url) {
        if (!isNetworkConnected(context)) {
            Toast.makeText(webView.getContext(),"请检查网络！",Toast.LENGTH_LONG).show();
            return;
        }
        webView.loadUrl(url);
    }


    public static boolean syncCookie(Activity activity, String url) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager manager = CookieSyncManager.createInstance(activity);
        }
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
//        String cookie = SharedPreferencesUtil.get(Constant.COOKIE_FILE_NAME, Uri.parse(url).getHost());
        String cookie = "";
        Log.e("syncCookie","cookie = " + cookie);
        cookieManager.setCookie(url, cookie);
        String newCookie = cookieManager.getCookie(url);
        Log.e("syncCookie","cookie = " + cookie);
        CookieSyncManager.getInstance().sync();
        return TextUtils.isEmpty(newCookie);
    }

    private static long lastClickTime;

    /**
     * 判断是否快速双击 250毫秒未临界值
     *
     * @return
     */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 250) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
