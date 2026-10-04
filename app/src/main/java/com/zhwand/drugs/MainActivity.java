package com.zhwand.drugs;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

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

        WebView.setWebContentsDebuggingEnabled(true);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        settings.setMediaPlaybackRequiresUserGesture(false);

        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(false);

        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Use a normal modern mobile browser User-Agent.
        String originalUA = settings.getUserAgentString();
        settings.setUserAgentString(
                originalUA.replace("; wv", "")
        );

        // Cookies / login session
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);
        cookieManager.flush();

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(
                    WebView view,
                    String url,
                    Bitmap favicon) {

                super.onPageStarted(view, url, favicon);
                Log.d("ZHWAND", "Page started: " + url);
            }

            @Override
            public void onPageFinished(
                    WebView view,
                    String url) {

                super.onPageFinished(view, url);

                CookieManager.getInstance().flush();

                Log.d("ZHWAND", "Page finished: " + url);
                Log.d(
                        "ZHWAND",
                        "Cookies: " +
                                CookieManager.getInstance().getCookie(url)
                );
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error) {

                super.onReceivedError(view, request, error);

                if (request.isForMainFrame()) {
                    String message =
                            "Web error: " + error.getDescription();

                    Log.e("ZHWAND", message);

                    Toast.makeText(
                            MainActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onReceivedHttpError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceResponse errorResponse) {

                super.onReceivedHttpError(
                        view,
                        request,
                        errorResponse
                );

                String message =
                        "HTTP " +
                        errorResponse.getStatusCode() +
                        " : " +
                        request.getUrl();

                Log.e("ZHWAND", message);

                // Show authentication/API errors.
                if (errorResponse.getStatusCode() >= 400) {
                    Toast.makeText(
                            MainActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onReceivedSslError(
                    WebView view,
                    SslErrorHandler handler,
                    SslError error) {

                Log.e(
                        "ZHWAND",
                        "SSL error: " + error.toString()
                );

                // Never bypass invalid SSL certificates.
                handler.cancel();

                Toast.makeText(
                        MainActivity.this,
                        "SSL connection error",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onConsoleMessage(
                    ConsoleMessage consoleMessage) {

                String message =
                        consoleMessage.message() +
                        " (line " +
                        consoleMessage.lineNumber() +
                        ")";

                Log.d("ZHWAND_JS", message);

                // Display JavaScript errors directly on phone.
                if (consoleMessage.messageLevel()
                        == ConsoleMessage.MessageLevel.ERROR) {

                    Toast.makeText(
                            MainActivity.this,
                            "JS Error: " +
                                    consoleMessage.message(),
                            Toast.LENGTH_LONG
                    ).show();
                }

                return true;
            }

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
                                new String[]{
                                        Manifest.permission.CAMERA
                                },
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
                    new String[]{
                            Manifest.permission.CAMERA
                    },
                    CAMERA_PERMISSION_CODE
            );
        }

        webView.loadUrl(APP_URL);

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

    @Override
    protected void onPause() {
       
