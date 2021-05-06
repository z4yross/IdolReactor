package idolreactor.idolreactor;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN_ACTIVITY";

    private final static int SEND_SMS_PERMISSION_REQ = 1;
    private Button btn;
    private Button btn2;
    private EditText editText;

    public static String number;

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.button);
        btn2 = findViewById(R.id.button2);
        editText = findViewById(R.id.editTextPhone);

        btn.setEnabled(false);
        btn2.setEnabled(false);

        if (checkPermission(Manifest.permission.SEND_SMS) &&
                checkPermission(Manifest.permission.CAMERA) &&
                checkPermission(Manifest.permission.RECORD_AUDIO) &&
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                checkPermission(Manifest.permission.READ_SMS) &&
                checkPermission(Manifest.permission.CALL_PHONE) &&
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                checkPermission(Manifest.permission.INTERNET) &&
                checkPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
            btn.setEnabled(true);
            btn2.setEnabled(true);

            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
            startActivity(intent);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE
            }, SEND_SMS_PERMISSION_REQ);
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!String.valueOf(editText.getText()).equals("")){
                    AppService.number = String.valueOf(editText.getText());
                } else {
                    Toast.makeText(getApplicationContext(), "Ingrese un numero de telefono", Toast.LENGTH_LONG).show();
                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppService.running == 0) {
                    Intent serviceIntent = new Intent(getApplicationContext(), AppService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        startForegroundService(serviceIntent);
                    else startService(serviceIntent);
                    btn2.setText("Detener Servicio");
                }else {
                    stopService(new Intent(getApplicationContext(), AppService.class));
                    btn2.setText("Crear Servicio");
                }
            }
        });
    }


    private boolean checkPermission(String perm) {
        int checkpermission = ContextCompat.checkSelfPermission(this, perm);
        return checkpermission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SEND_SMS_PERMISSION_REQ:
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    btn.setEnabled(true);
                    btn2.setEnabled(true);
                    File file = new File(Environment.getExternalStorageDirectory(), "data");

                    if (!file.exists()) {
                        file.mkdirs();
                        if (file.isDirectory()) {
                            Toast.makeText(getApplicationContext(), file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            String message = "Message: No se pudo crear el directorio" + "\nPath:" + Environment.getExternalStorageDirectory() + "\nmkdirs" + file.mkdirs();
                            builder.setMessage(message);
                            builder.show();
                        }
                    }
                }
                break;
        }
    }

}
