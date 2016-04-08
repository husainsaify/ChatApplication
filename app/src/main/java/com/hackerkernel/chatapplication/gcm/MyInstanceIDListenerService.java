package com.hackerkernel.chatapplication.gcm;

import android.content.Intent;
import android.util.Log;
import com.google.android.gms.iid.InstanceIDListenerService;

public class MyInstanceIDListenerService extends InstanceIDListenerService {
    private static final String TAG = MyInstanceIDListenerService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        Log.d(TAG,"HUS: onTokenRefresh");
        //fetch updated instanceId token
        Intent intent = new Intent(this,GcmIntentService.class);
        startService(intent);
    }
}
