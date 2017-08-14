package com.example.tom.aussiefinal;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
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
import android.widget.ArrayAdapter;


public class EaglesActivity extends AppCompatActivity {

    private static final String TAG = "Testing: ";

    AppCompatButton teamsClick;

    //Need to override parent onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //super to call parent method
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eagles);

        teamsClick = (AppCompatButton) findViewById(R.id.teamsButton);

        teamsClick.setOnClickListener(new GetTeamsListener());

        Log.v(TAG, "we in teams onCreate");

    }





    //GET Listener
    public class GetTeamsListener implements Button.OnClickListener {


        public OkHttpClient teamsOkHttpClient;

        @Override
        public void onClick(View view) {

            Log.v(TAG, "we in onClick");

            String myKey = "tt6zwg44stk6b64fdhvuc239";

            //West Coast Eagles:
            //String teamsString = "http://api.sportradar.us/football-t1/australian/en/competitors/sr:competitor:4449/profile.xml?api_key=tt6zwg44stk6b64fdhvuc239";
            String teamsString = "http://api.sportradar.us/football-t1/australian/en/competitors/sr:competitor:4449/profile.json?api_key=tt6zwg44stk6b64fdhvuc239";
            //Western bulldogs
            //http://api.sportradar.us/football-t1/australian/en/competitors/sr:competitor:4455/profile.xml?api_key=tt6zwg44stk6b64fdhvuc239

            //For loop to get all the teams? There are teams at :4442 through :4457


            HttpUrl teamsUrl = HttpUrl.parse(teamsString);
            teamsOkHttpClient = new OkHttpClient();

            Request teamsRequest = new Request.Builder()
                    .url(teamsUrl)
                    //  .url("https://www.googleapis.com/plusDomains/v1/people/me/activities/user")
                    .addHeader("Authorization", "Bearer " + myKey)
                    .build();


            teamsOkHttpClient.newCall(teamsRequest).enqueue(new Callback() {


                //Boilerplate Autogenerated onFailure
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.v(TAG, "callback failed");
                }
                //Handle the response here
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.v(TAG, "we in onResponse");

                    String myStr = response.body().string();

                    {
                        Log.v(TAG, myStr);
                    }

                    try {

                        //Make a JSON object and get the conferences array
                        JSONObject myObj = new JSONObject(myStr);

                        if(myObj != null)
                        {  Log.v(TAG, "got a JSON object for WCE");
                        }

                        JSONArray playersArr = myObj.getJSONArray("players");

                        if(playersArr != null)
                        {  Log.v(TAG, "got players array");
                        }

                        List<Map<String, String>> WCEList = new ArrayList<Map<String, String>>();

                        ArrayList<String> simpList = new ArrayList<String>();
                        //String[] simpArr = new String[30];


                        for (int p=0; p < playersArr.length(); p++)
                        {
                            simpList.add(playersArr.getJSONObject(p).getString("name"));
                            Log.v(TAG, "simp list loop");

                        }

                        final ArrayAdapter<String> arrAdapter = new ArrayAdapter<String>(EaglesActivity.this,
                                android.R.layout.simple_list_item_1,
                                simpList);



   //                     for (int p=0; p < playersArr.length(); p++)
     //                   {
       //                     HashMap<String, String> playerMap = new HashMap<String, String>();
         //                   playerMap.put("player: ", playersArr.getJSONObject(p).getString("name"));
           //                 WCEList.add(playerMap);
             //               Log.v(TAG, playersArr.getJSONObject(p).getString("name"));
               //             Log.v(TAG, "player loop ran");

                 //       }



//                        final SimpleAdapter teamAdapter = new SimpleAdapter(
  //                              EaglesActivity.this,
    //                            simpList,
      //                          R.layout.activity_teamslist,
        //                        new String[]{"team"},
          //                      new int[]{R.id.teamsText});

                        //have to force to run in "main" per se
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Log.v(TAG, "team aa heloo");
                                ((ListView) findViewById(R.id.teams_list)).setAdapter(arrAdapter);
                            }
                        });
                    }



                    //required to have catch
                    catch (JSONException e) {
                        // Do nothing
                    }
                }
            });




//intent PlayerIntent{
//        }
        }
    }
}
