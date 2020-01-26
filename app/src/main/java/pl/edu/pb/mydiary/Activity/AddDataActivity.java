package pl.edu.pb.mydiary.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import pl.edu.pb.mydiary.Database.SqliteDatabase;
import pl.edu.pb.mydiary.R;

public class AddDataActivity extends AppCompatActivity {
    EditText subjectEt, descriptionEt;
    Button cancelBt, saveBt, shareBt;
    SqliteDatabase mydb;
    private Button buttonGPS;
    private TextView textViewGPS;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data);

        textViewGPS = (TextView) findViewById(R.id.textViewGPS);
        buttonGPS = (Button) findViewById(R.id.buttonGPS);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                textViewGPS.setText(getString(R.string.lat) + String.format("%.2f", location.getLatitude()) + getString(R.string.lon) + (String.format("%.2f",location.getLongitude())));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET
            }, 10);
            return;
        } else {
            configureButton();
        }


        mydb = new SqliteDatabase(this);

        subjectEt = findViewById(R.id.subjectEditTextId);
        descriptionEt = findViewById(R.id.descriptionEditTextId);

        cancelBt = findViewById(R.id.cacelButtonId);
        saveBt = findViewById(R.id.saveButtonId);
        shareBt = findViewById(R.id.shareButtonId);

        shareBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //passing data via intent
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String sub = subjectEt.getText().toString();
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, sub);
                String des = descriptionEt.getText().toString();
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, des);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            }
        });

        saveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertData();
                backToMain();
            }
        });

        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToMain();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    configureButton();
                break;
            default:
                break;
        }
    }

    private void configureButton() {
        buttonGPS.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                locationManager.requestLocationUpdates("gps", 5000, 5, locationListener);
            }
        });
    };


    //for inserting new data
    public void insertData(){
        long l = -1;

        Date date = new Date();
        String d = (String) android.text.format.DateFormat.format("dd/MM/yyyy  hh:mm:ss",date);


        if(subjectEt.getText().length() == 0){
            Toast.makeText(getApplicationContext(),getString(R.string.no_subject), Toast.LENGTH_SHORT).show();
        }
        else{
            l = mydb.insertData(subjectEt.getText().toString(),
                    descriptionEt.getText().toString(),
                    d);
        }

        if(l>=0){
            Toast.makeText(getApplicationContext(),getString(R.string.data_added), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), getString(R.string.data_not_added), Toast.LENGTH_SHORT).show();
        }
    }
    public void backToMain()
    {
        Intent intent = new Intent(AddDataActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(intent);
    }

    String currentImagePath = null;
    private static final int IMAGE_REQUEST = 1;

    public void captureImage(View view)
    {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(cameraIntent.resolveActivity(getPackageManager())!=null)
        {
            File imageFile = null;

            try {
                imageFile = getImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(imageFile!=null)
            {
                Uri imageUri = FileProvider.getUriForFile(this, "pl.edu.pb.mydiary.fileprovider", imageFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, IMAGE_REQUEST);

            }
        }
    }

    public void displayImage(View view)
    {
        Intent intent = new Intent(this, DisplayImage.class);
        intent.putExtra("image_path", currentImagePath);
        startActivity(intent);

    }

    private File getImageFile() throws IOException
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd__HHmmss").format(new Date());
        String imageName = "jpg_"+timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        currentImagePath = imageFile.getAbsolutePath();
        return imageFile;
    }

}
