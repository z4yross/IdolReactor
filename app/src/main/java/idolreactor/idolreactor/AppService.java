package idolreactor.idolreactor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
//import android.support.annotation.Nullable;
//import android.support.v4.app.NotificationCompat;


public class AppService extends android.app.Service {

    public static final String CHANNEL_ID = "exampleServiceChannel";
    public static int running = 0;
    public static String number = "";

    @Override
    public void onCreate() {
        super.onCreate();
        running = 1;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Intent intent1 = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, 0);

        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_layout);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("IdolReactor")
//                .setContentText("Esperando")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayout)
//                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        running = 1;

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        running = 0;
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
