package com.example.woo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.GenericTranscodeRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private EditText mPhoneField,mAboutUser;
    private TextView mEdit_1,mEdit_2, mUserMailId, mUserLocation,mConfirmImage;
    private ImageView mProfileImage;
    private FirebaseAuth mAuth;
    private TextView mNameField;
    private DatabaseReference mUserDatabase;
    private String userId, name, phone, profileImageUrl, userSex, userMail,AboutUser;
    private Uri resultUri;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mNameField = (TextView) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mProfileImage = (ImageView) findViewById(R.id.profileImage);
        mEdit_1 = (TextView) findViewById(R.id.edit_1);
        mAuth = FirebaseAuth.getInstance();
        mConfirmImage = (TextView)findViewById(R.id.confirmImage);
        userId = mAuth.getCurrentUser().getUid();
        userMail = mAuth.getCurrentUser().getEmail();
        mUserMailId = (TextView) findViewById(R.id.userMailId);
        mUserMailId.setText(userMail);
        mEdit_2 = (TextView)findViewById(R.id.edit_2);
        mAboutUser = (EditText) findViewById(R.id.aboutUser);
        mUserLocation = (TextView) findViewById(R.id.userLocation);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        getUserInfo();
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
                mConfirmImage.setText("Confirm");
            }
        });

        mConfirmImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
                mConfirmImage.setText("");
            }
        });
        mEdit_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEdit_1.getText().toString().equals("Edit")) {
                    mPhoneField.setEnabled(true);
                    mEdit_1.setText("Confirm");
                } else {
                    mPhoneField.setEnabled(false);
                    saveUserInformation();
                    mEdit_1.setText("Edit");
                }
            }
        });
        mEdit_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mEdit_2.getText().toString().equals("Edit")){
                    mEdit_2.setText("Confirm");
                    mAboutUser.setEnabled(true);
                }else{
                    AboutUser = mAboutUser.getText().toString();
                    saveUserInformation();
                    mEdit_1.setText("Edit");
                }
            }
        });
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {

                    try {
                        Geocoder geocoder = new Geocoder(SettingsActivity.this, Locale.getDefault());
                        List<Address> address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                        mUserLocation.setText(address.get(0).getLocality());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    private void getUserInfo(){
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0 ){
                    Map<String,Object> map = (Map<String,Object>)dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        name = map.get("name").toString();
                        mNameField.setText(name);
                    }
                    if(map.get("phone")!=null){
                        phone = map.get("phone").toString();
                        mPhoneField.setText(phone);
                    }
                    if(map.get("sex")!=null){
                        userSex = map.get("sex").toString();
                    }
                    if(map.get("aboutUser")!=null){
                        AboutUser = map.get("aboutUser").toString();
                        mAboutUser.setText(AboutUser);
                    }
                    Glide.clear(mProfileImage);
                    if(map.get("profileImageUrl")!=null){
                        profileImageUrl = map.get("profileImageUrl").toString();
                        switch(profileImageUrl){
                            case "default":
                                Glide.with(getApplication()).load(R.drawable.image_background).into(mProfileImage);
                                break;
                            default:
                                Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveUserInformation() {
    name= mNameField.getText().toString();
    phone = mPhoneField.getText().toString();
    Map userInfo = new HashMap();
    userInfo.put("name",name);
    userInfo.put("phone",phone);
    userInfo.put("aboutUser",AboutUser);
    mUserDatabase.updateChildren(userInfo);
    if(resultUri!=null){
        final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImages").child(userId);
        Bitmap bitmap = null;
        try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),resultUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,20,baos );
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = filepath.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                finish();
            }
        });
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
              filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                  @Override
                  public void onSuccess(Uri uri) {
                    final Uri downloadUrl = uri;
                      Map userInfo = new HashMap();
                      userInfo.put("profileImageUrl",downloadUrl.toString());
                      mUserDatabase.updateChildren(userInfo);
                      finish();
                      return;
                  }
              });
            }
        });
    }
    else{
        finish();
    }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode== Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileImage.setImageURI(resultUri);
        }
    }
}