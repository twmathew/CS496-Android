
package com.google.codelabs.appauth;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.TokenResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.RequestBody;


import static com.google.codelabs.appauth.MainApplication.LOG_TAG;

public class MainActivity extends AppCompatActivity {

  private static final String SHARED_PREFERENCES_NAME = "AuthStatePreference";
  private static final String AUTH_STATE = "AUTH_STATE";
  private static final String USED_INTENT = "USED_INTENT";

  MainApplication mMainApplication;

  // state
  AuthState mAuthState;

  // views
  AppCompatButton mAuthorize;
  AppCompatButton mMakeApiCall;
  AppCompatButton mMakePostButton;
  AppCompatButton mSignOut;

  //Back in MainActivity.java, add the following methods to your MainActivity class to handle the intents from RedirectUriReceiverActivity.
  @Override
  protected void onNewIntent(Intent intent) {
    checkIntent(intent);
  }

  private void checkIntent(@Nullable Intent intent) {
    if (intent != null) {
      String action = intent.getAction();
      switch (action) {
        case "com.google.codelabs.appauth.HANDLE_AUTHORIZATION_RESPONSE":
          if (!intent.hasExtra(USED_INTENT)) {
            handleAuthorizationResponse(intent);
            intent.putExtra(USED_INTENT, true);
          }
          break;
        default:
          // do nothing
      }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    checkIntent(getIntent());
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {


    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mMainApplication = (MainApplication) getApplication();
    mAuthorize = (AppCompatButton) findViewById(R.id.authorize);
    mMakeApiCall = (AppCompatButton) findViewById(R.id.makeApiCall);
    mMakePostButton  = (AppCompatButton) findViewById(R.id.makePostButton);
    mSignOut = (AppCompatButton) findViewById(R.id.signOut);

    enablePostAuthorizationFlows();

    // wire click listeners
    mAuthorize.setOnClickListener(new AuthorizeListener());
  }

  private void enablePostAuthorizationFlows() {
    mAuthState = restoreAuthState();
    if (mAuthState != null && mAuthState.isAuthorized()) {
      if (mMakeApiCall.getVisibility() == View.GONE) {
        mMakeApiCall.setVisibility(View.VISIBLE);

        //Can only make API call if authorized
        mMakeApiCall.setOnClickListener(new MakeApiCallListener(this, mAuthState, new AuthorizationService(this)));
        //Can only POST if authorized
        mMakePostButton.setOnClickListener(new MakePostButtonListener(this, mAuthState, new AuthorizationService(this)));
      }
      if (mSignOut.getVisibility() == View.GONE) {
        mSignOut.setVisibility(View.VISIBLE);
        mSignOut.setOnClickListener(new SignOutListener(this));
      }
    } else {
      mMakeApiCall.setVisibility(View.GONE);
      mSignOut.setVisibility(View.GONE);
    }
  }



  /**
   * Exchanges the code, for the {@link TokenResponse}.
   *
   * @param intent represents the {@link Intent} from the Custom Tabs or the System Browser.
   */
  private void handleAuthorizationResponse(@NonNull Intent intent) {

    // code from the step 'Handle the Authorization Response' goes here.
    //Obtains the AuthorizationResponse from the Intent passed to the MainActivity
    AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
    AuthorizationException error = AuthorizationException.fromIntent(intent);
    //Create an AuthState object, a convenient way to store details from the authorization session.
    final AuthState authState = new AuthState(response, error);

    //Next we will exchange that authorization code for the refresh and access tokens, and update the AuthState instance with that response.
    if (response != null) {
      Log.i(LOG_TAG, String.format("Handled Authorization Response %s ", authState.toJsonString()));
      AuthorizationService service = new AuthorizationService(this);
      service.performTokenRequest(response.createTokenExchangeRequest(), new AuthorizationService.TokenResponseCallback() {
        @Override
        public void onTokenRequestCompleted(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException exception) {
          if (exception != null) {
            Log.w(LOG_TAG, "Token Exchange failed", exception);
          } else {
            if (tokenResponse != null) {
              authState.update(tokenResponse, exception);
              persistAuthState(authState);
              Log.i(LOG_TAG, String.format("Token Response [ Access Token: %s, ID Token: %s ]", tokenResponse.accessToken, tokenResponse.idToken));
            }
          }
        }
      });
    }
  }


  //We provided the method persistAuthState in the starter project as some boilerplate to save and load the AuthState object.
  // If you're adding AppAuth to your own app you may want to consider your own persistence design.
  private void persistAuthState(@NonNull AuthState authState) {
    getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
            .putString(AUTH_STATE, authState.toJsonString())
            .commit();
    enablePostAuthorizationFlows();
  }

  private void clearAuthState() {
    getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(AUTH_STATE)
            .apply();
  }

  @Nullable
  private AuthState restoreAuthState() {
    String jsonString = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(AUTH_STATE, null);
    if (!TextUtils.isEmpty(jsonString)) {
      try {
        return AuthState.fromJson(jsonString);
      } catch (JSONException jsonException) {
        // should never happen
      }
    }
    return null;
  }

  /**
   * Kicks off the authorization flow.
   */
  public static class AuthorizeListener implements Button.OnClickListener {
    @Override
    public void onClick(View view) {

      // code from the step 'Create the Authorization Request':
      AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
              Uri.parse("https://accounts.google.com/o/oauth2/v2/auth") /* auth endpoint */,
              Uri.parse("https://www.googleapis.com/oauth2/v4/token") /* token endpoint */
      );

      //Now we have an instance of AuthorizationServiceConfiguration. We can use to build an instance of AuthorizationRequest.
      //Describe actual authorization request, including your OAuth client id, and the scopes you are requesting.
      //Note that for the demo we are supplying a test OAuth client id. Be sure to register your own client ID when developing your own apps,
      //and update the clientId and redirectUri values with your own (and the custom scheme registered in the AndroidManifest.xml).
      //If using a different OAuth server, then you'll need to register a client for that server, following their documentation.
      // String clientId = "511828570984-fuprh0cm7665emlne3rnf9pk34kkn86s.apps.googleusercontent.com";

      String clientId = "71484946778-hr5sdush4550shbn9s2skg40geh3jq1n.apps.googleusercontent.com";
      Uri redirectUri = Uri.parse("com.google.codelabs.appauth:/oauth2callback");
      AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
              serviceConfiguration,
              clientId,
              AuthorizationRequest.RESPONSE_TYPE_CODE,
              redirectUri
      );
      //  builder.setScopes("profile");

      //Need to add scopes so the app can acces posts
      builder.setScopes("https://www.googleapis.com/auth/plus.me", "https://www.googleapis.com/auth/plus.stream.write", "https://www.googleapis.com/auth/plus.stream.read");
      AuthorizationRequest request = builder.build();


      // and now the step 'Perform the Authorization Request' goes here.
      //Create an instance of the AuthorizationService. Ideally, there is one instance of AuthorizationService per Activity.
      AuthorizationService authorizationService = new AuthorizationService(view.getContext());

      //Create the PendingIntent to handle the authorization response
      String action = "com.google.codelabs.appauth.HANDLE_AUTHORIZATION_RESPONSE";
      Intent postAuthorizationIntent = new Intent(action);
      PendingIntent pendingIntent = PendingIntent.getActivity(view.getContext(), request.hashCode(), postAuthorizationIntent, 0);
      //Then perform the authorization request with performAuthorizationRequest.
      authorizationService.performAuthorizationRequest(request, pendingIntent);

    }
  }

  public static class SignOutListener implements Button.OnClickListener {

    private final MainActivity mMainActivity;

    public SignOutListener(@NonNull MainActivity mainActivity) {
      mMainActivity = mainActivity;
    }

    @Override
    public void onClick(View view) {
      mMainActivity.mAuthState = null;
      mMainActivity.clearAuthState();
      mMainActivity.enablePostAuthorizationFlows();
    }
  }



  public String makeJsonThingFunc(String inputParam) {
    String theFullObj =
            "{" +
                    "\"title\":\"hellotes\"," +
                    "\"object\": {" +
                    "\"originalContent\":" + "\"" + inputParam + "\"" +
                    "}," +
                    "\"verb\":\"post\"," +
                    "\"access\":{" +
                    "\"items\":[" +
                    "{\"type\":\"domain\"" +
                    "}]," +
                    "\"domainRestricted\":true" +
                    "}" +
                    "}";
    return theFullObj;
  }



  //POST Button listener
  public class MakePostButtonListener implements Button.OnClickListener  {

    //Auth stuff
    public MainActivity mMainActivity;
    public AuthState mAuthState;
    public AuthorizationService mAuthorizationService;

    public MakePostButtonListener(@NonNull MainActivity mainActivity, @NonNull AuthState authState, @NonNull AuthorizationService authorizationService) {
      mMainActivity = mainActivity;
      mAuthState = authState;
      mAuthorizationService = authorizationService;
    }

    //Http client
    OkHttpClient postClient = new OkHttpClient();

    //On Click, do all the following:
    @Override
    public void onClick(View v)  {

      //Auth stuff
      mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
        @Override
        public void execute(
                String accessToken,
                String idToken,
                AuthorizationException ex) {
          if (ex != null) {
            // negotiation for fresh tokens failed, check ex for more details
            return;
          }

          //Do we have the access token?
          Log.i(LOG_TAG, "here is access token we are in post");
          Log.i(LOG_TAG, accessToken);


          //HTTP time

          //Get input from the EditText field
          String userInput = ((EditText) findViewById(R.id.edit_message)).getText().toString();
          //Define JSON type
          MediaType JSONType = MediaType.parse("application/json; charset=utf-8");

          //Did we get the input?
          Log.i(LOG_TAG, "here is the user input we are in post");
          Log.i(LOG_TAG, userInput);

          //Make the JSON object
          String myObj = makeJsonThingFunc(userInput);
          Log.i(LOG_TAG, "result of jsonThingFunc");
          Log.i(LOG_TAG, myObj);

          //Build the request using JSON object
          RequestBody postBod = RequestBody.create(JSONType, myObj);
          Log.i(LOG_TAG, "created post body");
          //Make the Request URL
          //       HttpUrl postUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/people/me/activities");

          final Request postRequest = new Request.Builder()
                  .url("https://www.googleapis.com/plusDomains/v1/people/me/activities")
                  .post(postBod)
                  .addHeader("Authorization", "Bearer " + accessToken)
                  .build();
          //           try (Response response = postClient.newCall(postRequest).execute()) {
          //               return response.body().string();
          //          }

          Log.i(LOG_TAG, "built the request. Posting...");

          //callback thing
          postClient.newCall(postRequest).enqueue(new Callback() {
            //Boilerplate Autogenerated onFailure
            @Override
            public void onFailure(Call call, IOException e) {
              Log.i(LOG_TAG, "http post didnt work ");
            }

            //Handle response
            @Override
            public void onResponse(Call call, Response response) throws IOException {

              try {

                //Did we get boy, whats happening
                String r = response.body().string();
                Log.i(LOG_TAG, r);

                //Did we get a 404 or what
                int responseCode = response.code();
                Log.i(LOG_TAG, String.format("%d", responseCode));

                //Now actually do it
                Log.i(LOG_TAG, "drumroll please");
                //               postClient.newCall(postRequest).execute();
                Log.i(LOG_TAG, "executed!!!");
              } catch (IOException ie) {
              }

            }


            //end of callback
          } );

          //End of ex
        }


        //end of performwithFresh
      });
      //end of onClick
    }
    //End of Listener
  }




  //GET Listener
  public class MakeApiCallListener implements Button.OnClickListener {

    public MainActivity mMainActivity;
    public AuthState mAuthState;
    public AuthorizationService mAuthorizationService;

    public MakeApiCallListener(@NonNull MainActivity mainActivity, @NonNull AuthState authState, @NonNull AuthorizationService authorizationService) {
      mMainActivity = mainActivity;
      mAuthState = authState;
      mAuthorizationService = authorizationService;
    }

    public OkHttpClient mOkHttpClient;

    @Override
    public void onClick(View view) {

      //Here we use performActionWithFreshTokens to get a fresh access token.
      mAuthState.performActionWithFreshTokens(mAuthorizationService, new AuthState.AuthStateAction() {
        @Override
        public void execute(
                String accessToken,
                String idToken,
                AuthorizationException ex) {
          if (ex != null) {
            // negotiation for fresh tokens failed, check ex for more details
            return;
          }

          //HTTP time, lets get the google + posts
          mOkHttpClient = new OkHttpClient();
          HttpUrl myUrl = HttpUrl.parse("https://www.googleapis.com/plusDomains/v1/people/me/activities/user");

          Log.i(LOG_TAG, "here is access token");
          Log.i(LOG_TAG, accessToken);

          Request myRequest = new Request.Builder()
                  //        .url(myUrl)
                  .url("https://www.googleapis.com/plusDomains/v1/people/me/activities/user")
                  .addHeader("Authorization", "Bearer " + accessToken)
                  .build();
          mOkHttpClient.newCall(myRequest).enqueue(new Callback() {
            //Boilerplate Autogenerated onFailure
            @Override
            public void onFailure(Call call, IOException e) {
              Log.i(LOG_TAG, "http didnt work ");
            }
            //Handle the response here
            @Override
            public void onResponse(Call call, Response response) throws IOException {
              Log.i(LOG_TAG, "in onResponse");
              String myStr = response.body().string();

              {
                Log.i(LOG_TAG, myStr);
              }

              try {
                //Make a JSON object and get the "items" array from Google Plus
                JSONObject myObj = new JSONObject(myStr);

                if(myObj != null)
                {
                  Log.i(LOG_TAG, "got an object");
                }


                JSONArray myArr = myObj.getJSONArray("items");

                if(myArr != null)
                {
                  Log.i(LOG_TAG, "got an array from items");
                }
                //Make a new list
                // Link this stuff up
                List<Map<String, String>> posts = new ArrayList<Map<String, String>>();
                for (int i = 0; i < myArr.length(); i++) {
                  HashMap<String, String> myMap = new HashMap<String, String>();
                  String myString = myArr.getJSONObject(i).getString("title");
                  myMap.put("post", myArr.getJSONObject(i).getString("title"));
                  posts.add(myMap);
                  Log.i(LOG_TAG, String.format("post: %s", myString));
                }
                //Android adapter

                final SimpleAdapter myAdapter = new SimpleAdapter(
                        MainActivity.this,
                        posts,
                        R.layout.result_layout,
                        new String[]{"post"},
                        new int[]{R.id.postsresult});

                //have to force to run in "main" per se
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    ((ListView) findViewById(R.id.result_list)).setAdapter(myAdapter);
                  }
                });
              }

              //required to have catch
              catch (JSONException e) {
                // Do nothing
              }
            }
          });
          // use the access token to do something ...
          Log.i(LOG_TAG, String.format("TODO: make an API call with [Access Token: %s, ID Token: %s]", accessToken, idToken));
        }
      });






    }
  }

}
