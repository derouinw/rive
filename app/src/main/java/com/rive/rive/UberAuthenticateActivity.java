package com.rive.rive;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;


public class UberAuthenticateActivity extends ActionBarActivity {
    String UBER_CLIENT_ID;
    String UBER_SECRET;

    WebView wv;
    OAuthService uberService;
    String uberAuthCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uber_authenticate);

        UBER_CLIENT_ID = getString(R.string.uber_client_id);
        UBER_SECRET = getString(R.string.uber_secret);

        wv = (WebView)findViewById(R.id.webView);
        uberService = UberServiceSingleton.getUberService(UBER_CLIENT_ID, UBER_SECRET);
        String authUrl = uberService.getAuthorizationUrl(null);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl(authUrl + "?response_type=code&client_id="+ UBER_CLIENT_ID);

        wv.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.contains("?code=")) {
                    Uri uri = Uri.parse(url);
                    uberAuthCode = uri.getQueryParameter("code");
                    Log.d("CODE", uberAuthCode);

                    //new GetUberAccessTokenTask().execute(UBER_ACCESS_TOKEN_PATH);
                }
                /*if (url.contains("?code=") && authComplete != true) {
                    Uri uri = Uri.parse(url);
                    authCode = uri.getQueryParameter("code");
                    Log.i("", "CODE : " + authCode);
                    authComplete = true;
                    resultIntent.putExtra("code", authCode);
                    MainActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString("Code", authCode);
                    edit.commit();
                    auth_dialog.dismiss();
                    new TokenGet().execute();
                    Toast.makeText(getApplicationContext(),"Authorization Code is: " +authCode, Toast.LENGTH_SHORT).show();
                }else if(url.contains("error=access_denied")){
                    Log.i("", "ACCESS_DENIED_HERE");
                    resultIntent.putExtra("code", authCode);
                    authComplete = true;
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();
                    auth_dialog.dismiss();
                }*/
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_uber_authenticate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
