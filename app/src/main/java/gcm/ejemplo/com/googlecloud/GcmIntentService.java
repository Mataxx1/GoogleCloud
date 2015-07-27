package gcm.ejemplo.com.googlecloud;

        import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;

        import static android.support.v4.content.WakefulBroadcastReceiver.completeWakefulIntent;
        import static gcm.ejemplo.com.googlecloud.GcmBroadcastReceiver.*;

/**
 * Created by Erick on 25/07/2015.
 */
public class GcmIntentService extends IntentService {
    final String TAG="MI intent service";
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    JSONArray jsonArray;
    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("algo","Send error: " + extras.toString(),"");
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("algo","Deleted messages on server: " +
                        extras.toString(),"");
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.


                // Post notification of received message.
                //JSONObject;
                sendNotification(extras.getString("title").toString(),extras.getString("mensaje".toString()),extras.getString("link"));
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        completeWakefulIntent(intent);
    }
    private void sendNotification(String tit,String msg, String link) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_plusone_small_off_client)
                        .setContentTitle(tit)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg).setDefaults(Notification.DEFAULT_LIGHTS).setDefaults(Notification.DEFAULT_VIBRATE);

        mBuilder.setContentIntent(contentIntent).setAutoCancel(true);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());



    }
}
