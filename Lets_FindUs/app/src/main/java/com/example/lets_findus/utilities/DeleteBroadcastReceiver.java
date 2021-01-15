package com.example.lets_findus.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//receiver per l'eliminazione periodica dei meeting, quando viene attivato chiama l'intent dell'eliminazione
public class DeleteBroadcastReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, DeleteOldMeetingService.class);
        context.startService(i);
    }
}
