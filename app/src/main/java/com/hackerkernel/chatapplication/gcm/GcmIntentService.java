package com.hackerkernel.chatapplication.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.hackerkernel.chatapplication.R;
import com.hackerkernel.chatapplication.app.Config;

import java.io.IOException;

public class GcmIntentService extends IntentService {
    private static final String TAG = GcmIntentService.class.getSimpleName();

    public GcmIntentService() {
        super(TAG);
    }

    public static final String KEY = "key";
    public static final String TOPIC = "topic";
    public static final String SUBSCRIBE = "subscribe";
    public static final String UNSUBSCRIBE = "unsubscribe";

    @Override
    protected void onHandleIntent(Intent intent) {
        String key = intent.getStringExtra(KEY);
        switch (key){
            case SUBSCRIBE:
                String topic = intent.getStringExtra(TOPIC);
                subscribeToTopic(topic);
                break;
            case UNSUBSCRIBE:
                String topic1 = intent.getStringExtra(TOPIC);
                unsubscribeToTopic(topic1);
                break;
            default:
                registerGCM();
                break;
        }
    }

    private void registerGCM() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String token = null;
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE,null);

            Log.d(TAG,"HUS: registerGCM: "+token);

            //send registration id to the server
            sendRegistrationToServer(token);

            sharedPreferences.edit().putBoolean(Config.SENT_TOKEN_TO_SERVER,true);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "HUS: failed to complete token refresh " + e.getMessage());
            sharedPreferences.edit().putBoolean(Config.SENT_TOKEN_TO_SERVER,false);
        }

        //Notify ui that registration has completed, hide pb
        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token",token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void sendRegistrationToServer(String token) {
        //TODO: send the registration token to our server to save it in MySql
    }

    private void subscribeToTopic(String topic) {
        GcmPubSub pubSub = GcmPubSub.getInstance(getApplicationContext());
        InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
        String token;
        try {
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),GoogleCloudMessaging.INSTANCE_ID_SCOPE,null);
            if (token != null){
                pubSub.subscribe(token,"/topics/"+topic,null);
                Log.d(TAG,"HUS: subscribeToTopic: Subscribe to topic "+token);
            }else {
                Log.d(TAG,"HUS: subscribeToTopic: GCM reg id was null");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "HUS: subscribeToTopic: Topic subscribe error. Topic: " + topic + ", error: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Topic subscribe error. Topic: " + topic + ", error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void unsubscribeToTopic(String topic1) {
        GcmPubSub pubSub = GcmPubSub.getInstance(getApplicationContext());
        InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
        String token;
        try {
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),GoogleCloudMessaging.INSTANCE_ID_SCOPE,null);
            if (token != null){
                pubSub.unsubscribe(token,"");
                Log.d(TAG,"HUS: unsubscribed from topic "+topic1);
            }else{
                Log.d(TAG,"HUS: error: gcm registration id is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "HUS: Topic unsubscribe error. Topic: " + topic1 + ", error: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Topic subscribe error. Topic: " + topic1 + ", error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
