package com.rive.rive;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;

import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;


public class UberAuthenticateActivity extends ActionBarActivity {
    public static final String TOKEN_EXTRA = "com.rive.rive.TOKEN_EXTRA";
    String UBER_CLIENT_ID;
    String UBER_SECRET;
    String UBER_REDIRECT;
    String UBER_ACCESS_TOKEN_PATH;
    String UBER_ACCESS_TOKEN_KEY;
    String UBER_REFRESH_TOKEN_KEY;
    String UBER_SCOPES;

    WebView wv;
    OAuthService uberService;
    String uberAuthCode;
    Token uberRequestToken, uberAccessToken, uberRefreshToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uber_authenticate);

        UBER_CLIENT_ID = getString(R.string.uber_client_id);
        UBER_SECRET = getString(R.string.uber_secret);
        UBER_REDIRECT = getString(R.string.uber_redirect);
        UBER_ACCESS_TOKEN_PATH = getString(R.string.uber_access_token_path);
        UBER_ACCESS_TOKEN_KEY = getString(R.string.key_access_token);
        UBER_REFRESH_TOKEN_KEY = getString(R.string.key_refresh_token);
        UBER_SCOPES = getString(R.string.uber_scopes);

        wv = (WebView)findViewById(R.id.webView);
        uberService = UberServiceSingleton.getUberService(UBER_CLIENT_ID, UBER_SECRET);
        uberRequestToken = new Token(UBER_CLIENT_ID, UBER_SECRET);

        Hawk.init(this, getString(R.string.hawk_password));
        if (/*Hawk.contains(UBER_ACCESS_TOKEN_KEY*/ 1 == 2) {
            // Already have old access key - don't need to again
            uberAccessToken = Hawk.get(UBER_ACCESS_TOKEN_KEY);
            Log.d("TOKEN", uberAccessToken.getToken());
            Toast.makeText(getApplicationContext(), "Uber already authenticated", Toast.LENGTH_SHORT).show();
            loadOrderUber();
        } else {

            // Authenticate uber from webpage
            String authUrl = uberService.getAuthorizationUrl(uberRequestToken);
            wv.getSettings().setJavaScriptEnabled(true);
            wv.loadUrl(authUrl + "?response_type=code&client_id=" + UBER_CLIENT_ID + "&scope=" + UBER_SCOPES);

            wv.setWebViewClient(new WebViewClient() {

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);

                    Log.d("URL", url);

                    if (url.contains("?code=")) {
                        Uri uri = Uri.parse(url);
                        uberAuthCode = uri.getQueryParameter("code");
                        Log.d("CODE", uberAuthCode);

                        new GetUberAccessTokenTask().execute(UBER_ACCESS_TOKEN_PATH, uberAuthCode);
                        authCodeReceived();
                    } else if (url.contains("error")) {
                       authCodeError();
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
    }

    // Replace web view with loading screen
    void authCodeReceived() {
        wv.setVisibility(View.GONE);
        TextView uberLoadingText = (TextView)findViewById(R.id.uber_loading_text);
        ProgressBar spinner = (ProgressBar)findViewById(R.id.uber_loading_spinner);

        uberLoadingText.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);

        // wait for access code to be received
    }

    // Return to previous screen
    void authCodeError() {
        Toast.makeText(getApplicationContext(), "You have to authorize with Uber to ride with Uber", Toast.LENGTH_SHORT).show();
        finish();
    }

    void loadOrderUber() {
        Intent intent = new Intent(getApplicationContext(), OrderUberActivity.class);
        intent.putExtra(TOKEN_EXTRA, uberAccessToken);
        startActivity(intent);
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

    private class GetUberAccessTokenTask extends AsyncTask<String, Void, Token> {

        @Override
        protected Token doInBackground(String... params) {

            // Exchange authentication code for access token
            OAuthRequest request = new OAuthRequest(Verb.POST, params[0]);
            request.addQuerystringParameter("client_secret", UBER_SECRET);
            request.addQuerystringParameter("client_id", UBER_CLIENT_ID);
            request.addQuerystringParameter("grant_type", "authorization_code");
            request.addQuerystringParameter("redirect_uri", UBER_REDIRECT);
            request.addQuerystringParameter("code", params[1]);

            Response response = request.send();
            Log.d("RESPONSE", response.getBody());

            // Parse access and refresh tokens from JSON data
            String uberAccessString = "";
            String uberRefreshString = "";

            try {
                JSONObject jo = new JSONObject(response.getBody());

                uberAccessString = jo.getString("access_token");
                uberRefreshString = jo.getString("refresh_token");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            uberAccessToken = new Token(uberAccessString, "");
            uberRefreshToken = new Token(uberRefreshString, "");
            //Toast.makeText(getApplicationContext(), "Uber successfully authenticated", Toast.LENGTH_SHORT).show();
            return uberAccessToken;
        }

        @Override
        protected void onPostExecute(Token accessToken) {
            // Store tokens
            Hawk.put(UBER_ACCESS_TOKEN_KEY, accessToken);
            Hawk.put(UBER_REFRESH_TOKEN_KEY, uberRefreshToken);

            // Load next page
            loadOrderUber();
        }
    }
}
