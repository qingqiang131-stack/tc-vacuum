package com.tc.vacuumstudy;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String PORTAL_HOST = "appassets.tc";
    private static final Set<String> PDF_FILES = new HashSet<>(Arrays.asList(
            "vacuum_lecture_00_04.pdf",
            "vacuum_lecture_05_06.pdf",
            "optical_film_lecture.pdf"
    ));
    private static final Set<String> WEB_ASSETS = new HashSet<>(Arrays.asList(
            "tc-vacuum-icon.png",
            "apple-touch-icon.png",
            "icon-192.png",
            "icon-512.png",
            "manifest.webmanifest",
            "sw.js"
    ));

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(8, 12, 20));
        getWindow().setNavigationBarColor(Color.rgb(8, 12, 20));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(8, 12, 20));
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        // The portal uses JavaScript confirm() before submitting a quiz. Without a
        // WebChromeClient, WebView does not show that dialog and confirm() returns
        // false, making the submit buttons appear unresponsive.
        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (PORTAL_HOST.equals(uri.getHost()) &&
                        ("/".equals(uri.getPath()) || "/index.html".equals(uri.getPath()))) {
                    try {
                        return new WebResourceResponse("text/html", "UTF-8",
                                getAssets().open("index.html"));
                    } catch (IOException ignored) {
                        return new WebResourceResponse("text/plain", "UTF-8", null);
                    }
                }
                if (PORTAL_HOST.equals(uri.getHost())) {
                    String path = uri.getPath();
                    String assetName = path != null && path.startsWith("/")
                            ? path.substring(1)
                            : "";
                    if (WEB_ASSETS.contains(assetName)) {
                        try {
                            String mimeType = "image/png";
                            if ("manifest.webmanifest".equals(assetName)) {
                                mimeType = "application/manifest+json";
                            } else if ("sw.js".equals(assetName)) {
                                mimeType = "text/javascript";
                            }
                            return new WebResourceResponse(mimeType, "UTF-8",
                                    getAssets().open(assetName));
                        } catch (IOException ignored) {
                            return new WebResourceResponse("text/plain", "UTF-8", null);
                        }
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (!PORTAL_HOST.equals(uri.getHost())) return true;
                String name = uri.getLastPathSegment();
                if (name != null && name.toLowerCase().endsWith(".pdf")) {
                    return openPdf(name);
                }
                return !"/".equals(uri.getPath()) && !"/index.html".equals(uri.getPath());
            }
        });

        setContentView(webView);
        webView.loadUrl("https://" + PORTAL_HOST + "/index.html");
    }

    private boolean openPdf(String name) {
        if (!PDF_FILES.contains(name)) return true;
        Uri uri = new Uri.Builder()
                .scheme("content")
                .authority(getPackageName() + ".documents")
                .appendPath(name)
                .build();
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, "application/pdf")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(Intent.createChooser(intent, "開啟課程講義"));
        } catch (ActivityNotFoundException error) {
            Toast.makeText(this, "請先安裝 PDF 閱讀器，再開啟講義。", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
