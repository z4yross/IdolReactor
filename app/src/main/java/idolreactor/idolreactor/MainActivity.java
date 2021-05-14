package idolreactor.idolreactor;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Telephony;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wafflecopter.multicontactpicker.ContactResult;
import com.wafflecopter.multicontactpicker.MultiContactPicker;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN_ACTIVITY";
    private static final int CONTACT_PICKER_REQUEST = 1;

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

        CardView cardView = findViewById(R.id.card_view);
        cardView.setBackgroundResource(R.drawable.corner_card_view);

        btn.setEnabled(false);
        btn2.setEnabled(false);

        if (AppService.running == 1)  btn2.setText("Detener Servicio");

        if (checkPermission(Manifest.permission.SEND_SMS) &&
                checkPermission(Manifest.permission.CAMERA) &&
                checkPermission(Manifest.permission.RECORD_AUDIO) &&
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                checkPermission(Manifest.permission.READ_SMS) &&
                checkPermission(Manifest.permission.CALL_PHONE) &&
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                checkPermission(Manifest.permission.INTERNET) &&
                checkPermission(Manifest.permission.ACCESS_NETWORK_STATE) &&
                checkPermission(Manifest.permission.READ_CONTACTS) &&
                checkPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
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
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.SYSTEM_ALERT_WINDOW
            }, SEND_SMS_PERMISSION_REQ);
        }

        if (!AppService.number.equals("")){
            number = AppService.number;
            editText.setText(number);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                number = s.toString();
                AppService.number = s.toString();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new MultiContactPicker.Builder(MainActivity.this) //Activity/fragment context
                        .searchIconColor(Color.WHITE) //Option - default: White
                        .setChoiceMode(MultiContactPicker.CHOICE_MODE_SINGLE) //Optional - default: CHOICE_MODE_MULTIPLE
                        .handleColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)) //Optional - default: Azure Blue
                        .bubbleColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)) //Optional - default: Azure Blue
                        .bubbleTextColor(Color.WHITE) //Optional - default: White
                        .setTitleText("Seleccionar contacto") //Optional - default: Select Contacts
                        .setLoadingType(MultiContactPicker.LOAD_ASYNC) //Optional - default LOAD_ASYNC (wait till all loaded vs stream results)
                        .setActivityAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                                android.R.anim.fade_in,
                                android.R.anim.fade_out) //Optional - default: No animation overrides
                        .showPickerForResult(CONTACT_PICKER_REQUEST);

//                if (!String.valueOf(editText.getText()).equals("")){
//                    AppService.number = String.valueOf(editText.getText());
//                } else {
//                    Toast.makeText(getApplicationContext(), "Ingrese un numero de telefono", Toast.LENGTH_LONG).show();
//                }
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

        if (savedInstanceState != null) {
            String myString = savedInstanceState.getString("number");

            AppService.number = myString;
            number = myString;
            editText.setText(myString);
            Log.d("RESTORE", myString);
        }
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
                    Toast.makeText(getApplicationContext(), getApplicationContext().getExternalMediaDirs()[0].getAbsolutePath(), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CONTACT_PICKER_REQUEST){
            if(resultCode == RESULT_OK) {
                List<ContactResult> results = MultiContactPicker.obtainResult(data);
                String res = results.get(0).getPhoneNumbers().get(0).getNumber();
                editText.setText(res);
                AppService.number = res;
                number = res;
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("number", editText.getText().toString());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String myString = savedInstanceState.getString("number");

        AppService.number = myString;
        number = myString;
        editText.setText(myString);
        Log.d("RESTORE", myString);
    }

}
