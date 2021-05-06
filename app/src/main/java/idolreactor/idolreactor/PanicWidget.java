package idolreactor.idolreactor;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Surface;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.CAMERA_SERVICE;

//import android.provider.Telephony;

public class PanicWidget extends AppWidgetProvider {

    private static final String MyOnClick = "myOnClickTag";

    private static final String TAG = "WIDGET";

    CameraManager manager;

    CameraDevice mCamera;
    MediaRecorder mediaRecorder = new MediaRecorder();
    CaptureRequest mCaptureRequest;
    CameraCaptureSession mSession;

    boolean recording = false;

    protected PendingIntent getPendingSelfIntent(Context context) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(PanicWidget.MyOnClick);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        final int N = appWidgetIds.length;

        for(int i = 0; i < N; i++){
            int appWidgetId = appWidgetIds[i];
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.panic_widget);
            views.setOnClickPendingIntent(R.id.panicButtonImageView, getPendingSelfIntent(context));
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        super.onReceive(context, intent);

        String number = AppService.number;

        if (MyOnClick.equals(intent.getAction())) {
            if (!recording) openCamera(context);

            Intent cIntent = new Intent(Intent.ACTION_CALL);
            cIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            cIntent.setData(Uri.fromParts("tel", number, null));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, cIntent, PendingIntent.FLAG_ONE_SHOT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 32 * 1000, pendingIntent);

            Intent msnIntent = new Intent(context, AlarmBroadcastReceiver.class);
            msnIntent.putExtra("number", number);

            PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 1, msnIntent,  PendingIntent.FLAG_ONE_SHOT);
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 5 * 1000, pendingIntent1);

            for(int i = 0; i <= 5; i++){
                pendingIntent1 = PendingIntent.getBroadcast(context, i + 2, msnIntent,  PendingIntent.FLAG_ONE_SHOT);
                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + (i + 1) * 5 * 60 * 1000, pendingIntent1);
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    if (recording) {
                        mediaRecorder.stop();
                        mediaRecorder.reset();
                        mediaRecorder.release();

                        mCamera.close();
                        mCamera = null;
                        recording = false;
                    }
                }
            }, 30 * 1000);


        }
    }

    private void setUpMediaRecorder(Context context) throws IOException {
        final File tempFile = new File(Environment.getExternalStorageDirectory(), "rcdr.mp4");
        tempFile.createNewFile();

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
        profile.audioBitRate = 128000;
        profile.audioCodec = MediaRecorder.AudioEncoder.AAC;
        profile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
        profile.videoCodec = MediaRecorder.VideoEncoder.H264;
        profile.videoBitRate = 9000 * 1000;
        profile.videoFrameRate = 30;


        mediaRecorder.setProfile(profile);
        mediaRecorder.setOrientationHint(90);
        mediaRecorder.setOutputFile(tempFile.getAbsolutePath());
        mediaRecorder.prepare();
    }


    public void startRecording(final Context context) {
        //Check the camera and TextureView are ready
        if (null == mCamera) {
            return;
        }
        try {
            if (mSession != null) mSession.close();
            setUpMediaRecorder(context);

            List<Surface> list = new ArrayList<>();
            list.add(mediaRecorder.getSurface());

            mCamera.createCaptureSession(list, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mSession = session;

                    try {
                        CaptureRequest.Builder captureRequest = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                        captureRequest.addTarget(mediaRecorder.getSurface());

                        mCaptureRequest = captureRequest.build();
                        mediaRecorder.start();
                        recording = true;

                        Toast.makeText(context, "Recording", Toast.LENGTH_SHORT).show();

                        mSession.setRepeatingRequest(mCaptureRequest, new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {

                            }
                        }, null);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        recording = false;
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("MissingPermission")
    public void openCamera(final Context context){
        manager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        try {
            String[] cameras = manager.getCameraIdList();

            manager.openCamera(cameras[0], new CameraDevice.StateCallback() {

                @Override
                public void onOpened(CameraDevice camera) {
                    mCamera = camera;
                    startRecording(context);
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    recording = false;
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    Log.d(TAG, "EROOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOR" + error);
                    Toast.makeText(context, "ERROR: " + error, Toast.LENGTH_SHORT).show();
                    recording = false;
                }
            },null);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

}


