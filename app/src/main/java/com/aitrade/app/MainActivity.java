package com.aitrade.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * AI Trade - a thin app around the pages your own server already serves:
 *   /table   the flip table
 *   /charts  the candle charts
 *   /        the chat page
 * The app holds no trading data. It shows the server's pages full screen, with a bottom bar
 * to switch between them, and remembers the server address and login on this phone only.
 */
public class MainActivity extends Activity {

    private static final String DEFAULT_SERVER = "http://185.184.68.222:5000";
    private static final int MAX_AUTH_TRIES = 8;

    private SharedPreferences prefs;
    private WebView web;
    private int authTries = 0;
    private boolean dialogOpen = false;
    private String currentPath = "/table";

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        prefs = getSharedPreferences("aitrade", Context.MODE_PRIVATE);
        getWindow().setStatusBarColor(Color.parseColor("#0F1117"));
        getWindow().setNavigationBarColor(Color.parseColor("#1A1D27"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0F1117"));

        web = new WebView(this);
        root.addView(web, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(buildBar(), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(root);

        configureWebView();

        if (prefs.getString("user", "").isEmpty()) {
            showSettings("Enter your server address and login to get started.", false);
        } else {
            openPath(currentPath);
        }
    }

    // ---------- bottom bar ----------
    private LinearLayout buildBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setBackgroundColor(Color.parseColor("#1A1D27"));
        bar.addView(barButton("Table", v -> openPath("/table")));
        bar.addView(barButton("Charts", v -> openPath("/charts")));
        bar.addView(barButton("Chat", v -> openPath("/")));
        bar.addView(barButton("Reload", v -> reload()));
        bar.addView(barButton("Settings", v ->
                showSettings("Change the server address or login.", true)));
        return bar;
    }

    private Button barButton(String label, android.view.View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(13);
        b.setBackgroundColor(Color.parseColor("#242838"));
        b.setOnClickListener(listener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(3, 3, 3, 3);
        b.setLayoutParams(lp);
        return b;
    }

    // ---------- web view ----------
    private void configureWebView() {
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);                 // the pages save your filters on the phone
        s.setMediaPlaybackRequiresUserGesture(false); // lets the page's alert sound play
        web.setBackgroundColor(Color.parseColor("#0F1117"));

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                Uri u = req.getUrl();
                String serverHost = Uri.parse(server()).getHost();
                if (serverHost != null && serverHost.equals(u.getHost())) {
                    return false;                     // your own server stays inside the app
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, u));
                } catch (Exception ignored) {
                }
                return true;
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler,
                                                  String host, String realm) {
                String u = prefs.getString("user", "");
                String p = prefs.getString("pass", "");
                if (!u.isEmpty() && authTries < MAX_AUTH_TRIES) {
                    authTries++;
                    handler.proceed(u, p);
                } else {
                    handler.cancel();
                    authTries = 0;
                    showSettings("The server rejected the login. Check the username and password.", true);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                authTries = 0;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame()) {
                    String html = "<html><body style='background:#0f1117;color:#e6e8ef;"
                            + "font-family:sans-serif;padding:24px'>"
                            + "<h3>Can't reach the server</h3>"
                            + "<p>Check that your VPS is running and that the server address "
                            + "in Settings is right, then tap Reload.</p></body></html>";
                    view.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null);
                }
            }
        });
    }

    private String server() {
        String s = prefs.getString("server", DEFAULT_SERVER).trim();
        if (s.isEmpty()) s = DEFAULT_SERVER;
        if (!s.startsWith("http://") && !s.startsWith("https://")) s = "http://" + s;
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    private void openPath(String path) {
        currentPath = path;
        web.loadUrl(server() + path);
    }

    private void reload() {
        String u = web.getUrl();
        if (u != null && u.startsWith(server())) {
            web.reload();
        } else {
            openPath(currentPath);
        }
    }

    // ---------- settings dialog ----------
    private EditText field(String hint, String value, int type) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setText(value);
        e.setInputType(type);
        e.setSingleLine(true);
        return e;
    }

    private void showSettings(String message, boolean cancelable) {
        if (dialogOpen) return;
        dialogOpen = true;

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad / 2, pad, 0);

        final EditText server = field("Server address", prefs.getString("server", DEFAULT_SERVER),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        final EditText user = field("Username", prefs.getString("user", ""),
                InputType.TYPE_CLASS_TEXT);
        final EditText pass = field("Password", prefs.getString("pass", ""),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        box.addView(server);
        box.addView(user);
        box.addView(pass);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Server settings")
                .setMessage(message)
                .setView(box)
                .setCancelable(cancelable)
                .setPositiveButton("Save", (d, which) -> {
                    prefs.edit()
                            .putString("server", server.getText().toString().trim())
                            .putString("user", user.getText().toString().trim())
                            .putString("pass", pass.getText().toString())
                            .apply();
                    authTries = 0;
                    clearAuthCache();
                    openPath(currentPath);
                });
        if (cancelable) builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(d -> dialogOpen = false);
        dialog.show();
    }

    @SuppressWarnings("deprecation")
    private void clearAuthCache() {
        try {
            WebViewDatabase.getInstance(this).clearHttpAuthUsernamePassword();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
