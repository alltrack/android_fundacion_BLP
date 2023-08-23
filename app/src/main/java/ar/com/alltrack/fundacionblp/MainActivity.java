package ar.com.alltrack.fundacionblp;

import static android.net.http.SslError.SSL_UNTRUSTED;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.FrameLayout;

import androidx.appcompat.widget.Toolbar;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    WebView myWebView;
    Context ctx;

    private FrameLayout customViewContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private View mCustomView;
    private myWebChromeClient mWebChromeClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myWebView = findViewById(R.id.webView);
        ctx = this;

        customViewContainer = (FrameLayout) findViewById(R.id.customViewContainer);

        myWebView.setWebViewClient(new WebViewClient());
        mWebChromeClient = new myWebChromeClient();
        myWebView.setWebChromeClient(mWebChromeClient);

        final ProgressDialog progressBar = new ProgressDialog(MainActivity.this);
        progressBar.setMessage("Cargando...");

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        //webSettings.setAppCacheEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
        webSettings.setDefaultTextEncodingName("utf-8");

        myWebView.setScrollBarStyle(android.view.View.SCROLLBARS_INSIDE_OVERLAY);
        myWebView.setWebViewClient(new WebViewClient() {

/*
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                SslCertificate serverCertificate = error.getCertificate();
                if (error.hasError(SSL_UNTRUSTED)) {
                    // Check if Cert-Domain equals the Uri-Domain
                    String certDomain = serverCertificate.getIssuedTo().getCName();
                    try {
                        if (certDomain.equals(new URL(error.getUrl()).getHost())) {
                            handler.proceed();
                        } else {
                            handler.cancel();
                            final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                            builder.setMessage("El sistema Operativo, no confia en el certidicado de seguidad. Esposible que esto se deba a una configuracion incorrecta o a que un atacante intercepto la conexion. No es posible continuar");

                            builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                            final AlertDialog dialog = builder.create();
                            dialog.show();

                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                } else {
                    super.onReceivedSslError(view, handler, error);
                }
            }

*/
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                super.onPageStarted(view, url, favicon);

                try {
                    if (!progressBar.isShowing()) {
                        progressBar.show();
                    }
                } catch (Exception e) {
                    Log.e("CRISTIAN", e.toString());
                }
            }

            public void onPageFinished(WebView view, String url) {
                try {
                    if (progressBar.isShowing()) {
                        progressBar.dismiss();
                    }
                } catch (Exception e) {
                    Log.e("CRISTIAN", e.toString());
                }
            }

            /*public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                try {
                    if (progressBar.isShowing()) {
                        progressBar.dismiss();
                    }
                } catch (Exception e) {
                    Log.e("CRISTIAN", e.toString());
                }
            }*/

        });


        myWebView.loadUrl("https://capacitacion.fundacionbancopampa.com.ar/app/");
        //myWebView.loadUrl("https://youtube.com");

    }

    @Override
    protected void onSaveInstanceState(Bundle status) {
        super.onSaveInstanceState(status);
        status.putString("url", myWebView.getUrl().toString());

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //myWebView.loadUrl(savedInstanceState.getString("url"));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (myWebView.canGoBack()) {
                    myWebView.goBack();
                } else {
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            finish();
        }
    }


    class myWebChromeClient extends WebChromeClient {
        private Bitmap mDefaultVideoPoster;
        private View mVideoProgressView;

        @Override
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
            onShowCustomView(view, callback);    //To change body of overridden methods use File | Settings | File Templates.
        }


        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {

            // if a view already exists then immediately terminate the new one
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            mCustomView = view;
            myWebView.setVisibility(View.GONE);
            customViewContainer.setVisibility(View.VISIBLE);
            customViewContainer.addView(view);
            customViewCallback = callback;

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getSupportActionBar().hide();


        }

        @Override
        public View getVideoLoadingProgressView() {

            if (mVideoProgressView == null) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                mVideoProgressView = inflater.inflate(R.layout.video_progreso, null);
            }
            return mVideoProgressView;
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();    //To change body of overridden methods use File | Settings | File Templates.
            if (mCustomView == null)
                return;

            myWebView.setVisibility(View.VISIBLE);
            customViewContainer.setVisibility(View.GONE);

            // Hide the custom view.
            mCustomView.setVisibility(View.GONE);

            // Remove the custom view from its container.
            customViewContainer.removeView(mCustomView);
            customViewCallback.onCustomViewHidden();

            mCustomView = null;
        }
    }
}





    /*
    public boolean inCustomView() {
        return (mCustomView != null);
    }

    public void hideCustomView() {
        mWebChromeClient.onHideCustomView();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().show();

    }*/
/*
    class myWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);

            final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setMessage("El sistema Operativo, no confia en el certidicado de seguidad. Esposible que esto se deba a una configuracion incorrecta o a que un atacante intercepto la conexion. ¿Desea Continuar?");
            builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                    finish();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();

        }
    }
*/



                    /*
            final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setMessage("El sistema Operativo, no confia en el certidicado de seguidad. Esposible que esto se deba a una configuracion incorrecta o a que un atacante intercepto la conexion. ¿Desea Continuar?");
                builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                        finish();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
                /*      handler.proceed();
                else
                    handler.cancel();
                //super.onReceivedSslError(view, handler, error);

            }*/