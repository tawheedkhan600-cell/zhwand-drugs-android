package com.zhwand.drugs;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private static final String APP_URL =
            "https://zhwand-drugs-wholesale-nhnsia.v2.appdeploy.ai/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        // Better mobile rendering inside the Android APK
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(false);
        settings.setTextZoom(100);
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Keep login/session cookies working
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Apply APK-specific mobile cleanup after every page load
                injectAndroidAppLayout(view);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onPermissionRequest(
                    final PermissionRequest request) {

                runOnUiThread(() -> {

                    if (ContextCompat.checkSelfPermission(
                            MainActivity.this,
                            Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {

                        request.grant(request.getResources());

                    } else {

                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                CAMERA_PERMISSION_CODE
                        );
                    }
                });
            }
        });

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE
            );
        }

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(APP_URL);
        }

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (webView.canGoBack()) {
                            webView.goBack();
                        } else {
                            finish();
                        }
                    }
                }
        );
    }

    private void injectAndroidAppLayout(WebView view) {

        String javascript =
                "(function(){" +

                "if(document.getElementById('zhwand-android-style'))return;" +

                "document.documentElement.classList.add('zhwand-android-app');" +

                "var style=document.createElement('style');" +
                "style.id='zhwand-android-style';" +

                "style.textContent=`" +

                "html,body{" +
                "max-width:100%!important;" +
                "overflow-x:hidden!important;" +
                "-webkit-text-size-adjust:100%!important;" +
                "}" +

                "@media(max-width:768px){" +

                "body{" +
                "padding-bottom:75px!important;" +
                "}" +

                "main{" +
                "width:100%!important;" +
                "max-width:100%!important;" +
                "}" +

                "button,select,input{" +
                "min-height:40px;" +
                "}" +

                "[class*='install-banner']," +
                "[class*='installBanner']," +
                "[class*='pwa-install']," +
                "[class*='pwaInstall']{" +
                "display:none!important;" +
                "}" +

                "}" +

                "`;" +

                "document.head.appendChild(style);" +

                "function cleanApkUI(){" +

                "var nodes=document.querySelectorAll(" +
                "'button,a,div,section,aside');" +

                "nodes.forEach(function(el){" +

                "var text=(el.innerText||'')" +
                ".trim().toLowerCase();" +

                "if(" +
                "text==='install app'||" +
                "text==='download app'||" +
                "text.indexOf('add to home screen')!==-1||" +
                "text.indexOf('on android chrome')!==-1" +
                "){" +
                "el.style.setProperty(" +
                "'display','none','important');" +
                "}" +

                "});" +

                "}" +

                "cleanApkUI();" +

                "new MutationObserver(cleanApkUI)" +
                ".observe(document.documentElement,{" +
                "childList:true," +
                "subtree:true" +
                "});" +

                "})();";

        view.evaluateJavascript(javascript, null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if (webView != null) {
            webView.saveState(outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {

        if (webView != null) {
            webView.onPause();
        }

        CookieManager.getInstance().flush();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onDestroy() {

        if (webView != null) {

            CookieManager.getInstance().flush();

            webView.stopLoading();
            webView.destroy();
            webView = null;
        }

        super.onDestroy();
    }
}
