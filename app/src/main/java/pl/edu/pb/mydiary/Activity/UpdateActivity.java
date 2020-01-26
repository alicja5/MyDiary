package pl.edu.pb.mydiary.Activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;

import pl.edu.pb.mydiary.Database.SqliteDatabase;
import pl.edu.pb.mydiary.R;

public class UpdateActivity extends AppCompatActivity {
    EditText subjectEt,descriptionEt;
    Button cancelBt,updateBt,shareBtOnUpdate;
    SqliteDatabase dbUpdate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_activity);
        //passing Update Activity's context to database alass
        dbUpdate = new SqliteDatabase(this);
        SQLiteDatabase sqliteDatabase = dbUpdate.getWritableDatabase();

        subjectEt = findViewById(R.id.subjectEditTextIdUpdate);
        descriptionEt = findViewById(R.id.descriptionEditTextIdUpdate);


        cancelBt = findViewById(R.id.cacelButtonIdUpdate);
        updateBt = findViewById(R.id.saveButtonIdUpdate);
        shareBtOnUpdate = findViewById(R.id.shareButtonIdUpdate);

        Intent intent = getIntent();
        Bundle bundle = getIntent().getExtras();
        String sub = intent.getStringExtra("subject");
        String des = intent.getStringExtra("description");
        final String id = intent.getStringExtra("listId");

        subjectEt.setText(sub);
        descriptionEt.setText(des);


        //for sharing data to social media
        shareBtOnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent =  new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String sub = subjectEt.getText().toString();
                shareIntent.putExtra(Intent.EXTRA_SUBJECT,sub);
                String des = descriptionEt.getText().toString();
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,des);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            }
        });

        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //for updating database data
        updateBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date date = new Date();
                String d = (String) android.text.format.DateFormat.format("dd/MM/yyyy  hh:mm:ss",date);

                if(dbUpdate.update(subjectEt.getText().toString(),descriptionEt.getText().toString(),d,id)){
                    Toast.makeText(getApplicationContext(),getString(R.string.data_updated), Toast.LENGTH_SHORT).show();
                    backToMain();
                }
            }
     });
    }

    //this method to clearing top activity and starting new activity
    public void backToMain()
    {
        Intent intent = new Intent(UpdateActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(intent);
    }
}
