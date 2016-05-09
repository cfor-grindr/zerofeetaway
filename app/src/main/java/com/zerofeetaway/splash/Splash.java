/*
 * Copyright (c) 2016. Nathan Fu and Sainath Varanasi
 *
 *  Most of our work is based on open source projects and libraries
 *  We use them to an extent to learn and also to contribute back to these libraries
 *
 *   Coding is fun and I love doing it.
 *
 *   @ Aryabhata Inc.
 *
 */

package com.zerofeetaway.splash;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zerofeetaway.MainActivity;
import com.zerofeetaway.R;

public class Splash extends FragmentActivity {

    private WebView webView;

    String url = "file:///android_asset/splash/index.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.webview);
        webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new HelloWebViewClient());
        webView.setWebChromeClient(new HelloWebChromeClient());
        webView.getSettings().setBuiltInZoomControls(false);
        webView.loadUrl(url);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Intent eventIntent = new Intent(Splash.this, MainActivity.class);
                Splash.this.startActivity(eventIntent);
                Splash.this.finish();
            }
        }, 6300);

    }

    public class HelloWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            view.loadUrl(url);
            return true;
        }
    }

    public class HelloWebChromeClient extends WebChromeClient {

    }
}
