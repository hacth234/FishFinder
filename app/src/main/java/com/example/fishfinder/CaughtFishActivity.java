package com.example.fishfinder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.fishfinder.data.GeneralTest;
import com.example.fishfinder.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class CaughtFishActivity extends AppCompatActivity {

    private EditText edtSaveTitle;
    private EditText edtSaveFishName;
    private ImageButton imgButtonTakePicture;
    private Button btnSubmitSave;
    private EditText edtSaveLatitude;
    private EditText edtSaveLongitude;

    private final int TAKE_PICTURE = 9990;

    private String latitudeVal;
    private String longitudeVal;

    FirebaseDatabase firebase;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String userId;
    String userEmail;



    private Bitmap bitMapToSave;

    private int databaseSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caught_fish);

        edtSaveTitle = (EditText) findViewById(R.id.edtSaveTitle);
        edtSaveFishName = (EditText) findViewById(R.id.edtSaveFishName);
        imgButtonTakePicture = (ImageButton) findViewById(R.id.imgButtonTakePicture);
        btnSubmitSave = (Button) findViewById(R.id.btnSubmitSave);

        edtSaveLatitude = (EditText) findViewById(R.id.edtSaveLatitude);
        edtSaveLongitude = (EditText) findViewById(R.id.edtSaveLongitude);

        firebase = FirebaseDatabase.getInstance(); //get the root node point of the database, this is so we can get the references based on the root node to get the desired data references

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser(); //get the current user based on the auth

        userId = firebaseUser.getUid().toString();
        userEmail = firebaseUser.getEmail().toString();




        firebase.getReference("GeneralTest").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    databaseSize = (int) dataSnapshot.getChildrenCount(); //grab the count for the children.
                }
                else {
                    //if that database does not exist
                    databaseSize = 0; //init the database size to be 0 so we can use it as an id.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                latitudeVal = "";
                longitudeVal = "";

            } else {
                longitudeVal = extras.getString("latitude");
                latitudeVal = extras.getString("longitude");

                edtSaveLongitude.setText(longitudeVal);
                edtSaveLatitude.setText(latitudeVal);

            }
        } else {
//            newString= (String) savedInstanceState.getSerializable("STRING_I_NEED");
        }

        imgButtonTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(i, TAKE_PICTURE);
            }
        });



        //save to database history in general, all submissions will be here regardless of whether the user wants to save to his own profile page or not. Or regardless to whether the person want to publish on community page
        btnSubmitSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save to firebase first as a general untracked object that is just part of the database
                //easier to retrieve it later if the user decides to save officially to their profile and or publish it.

                //a test database
                GeneralTest toAdd = new GeneralTest();
                toAdd.setLatitude(latitudeVal);
                toAdd.setLongitude(longitudeVal);
                toAdd.setTitle(edtSaveTitle.getText().toString());
                toAdd.setUserId(userId);
                toAdd.setEmail(userEmail);
//                firebase.getReference("GeneralTest").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                firebase.getReference("GeneralTest").child(String.valueOf(databaseSize)).setValue(toAdd);

                Toast.makeText(v.getContext(), FirebaseAuth.getInstance().getCurrentUser().getUid().toString(), Toast.LENGTH_SHORT).show(); //testing if the user auth makes it here, and yes it does

                Intent goToConfirmSavePublishActivity = new Intent(v.getContext(), ConfirmSavePublishActivity.class);

                startActivity(goToConfirmSavePublishActivity);
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        if (!(resultCode == RESULT_OK)) {

            return;
        }

        switch (requestCode) {
            case TAKE_PICTURE:
                Bundle bundleData = data.getExtras();
                Bitmap photo = (Bitmap) bundleData.get("data");
                imgButtonTakePicture.setImageBitmap(photo); //set the photo on the imgbutton to show the user the picture that was just taken
                bitMapToSave = photo; //remember the last photo's bitmap so if we need to save it later we can just call this reference and turn it to byte array to upload to firebase
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
        return;
    }
}