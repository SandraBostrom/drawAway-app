package com.example.drawawaytest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
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
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private EditText mNameField;
    private TextInputLayout mShopField;
    private TextInputLayout mCategoryField;
    private TextInputLayout mDescriptionField;

    private Button mBack, mConfirm;

    private ImageView mProfileImage;

    private FirebaseAuth mAuth;

    private DatabaseReference mUserDatabase;

    private String userId, shop, description, category, name, profileImageUrl;


    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_settings);

        mNameField = (EditText) findViewById(R.id.name);
        mCategoryField = (TextInputLayout) findViewById(R.id.category);

        mShopField = (TextInputLayout) findViewById(R.id.shop);

        mDescriptionField = (TextInputLayout) findViewById(R.id.description);

        mProfileImage = (ImageView) findViewById(R.id.profileImage);

        mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        getUserInfo();
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }

        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }

    private void getUserInfo(){
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("namn") !=null){
                        name = map.get("namn").toString();
                        mNameField.setText(name);
                    }
                    if(map.get("shop") !=null){
                        shop = map.get("shop").toString();
                        mShopField.getEditText().setText(shop);
                    }

                    if(map.get("kategori") !=null){
                        category = map.get("kategori").toString();
                        switch (category) {
                            case "defaultCategory":
                                mCategoryField.getEditText().setText("Uppdatera mig");
                                break;
                            default:
                                mCategoryField.getEditText().setText(category);
                        }
                    }

                    if(map.get("beskrivning") !=null){
                        description = map.get("beskrivning").toString();
                        switch (description) {
                            case "defaultDescription":
                                mDescriptionField.getEditText().setText("Uppdatera");
                                break;
                            default:
                                mDescriptionField.getEditText().setText(description);
                        }
                    }

                    Glide.clear(mProfileImage);
                    if(map.get("profilBildUrl") !=null){
                        profileImageUrl = map.get("profilBildUrl").toString();

                        switch (profileImageUrl){
                            case "default":
                                mProfileImage.setImageResource(R.mipmap.default_drawaway);
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
        name = mNameField.getText().toString();
        shop = mShopField.getEditText().getText().toString();
        category = mCategoryField.getEditText().getText().toString();
        description = mDescriptionField.getEditText().getText().toString();


        Map userInfo = new HashMap();
        userInfo.put("namn", name);
        userInfo.put("shop", shop);
        userInfo.put("kategori", category);
        userInfo.put("beskrivning", description);
        mUserDatabase.updateChildren(userInfo);

        if(resultUri != null){

            StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profilBilder").child(userId);
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            }catch (IOException e){
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
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
                            Map newImage = new HashMap();
                            newImage.put("profilBildUrl", uri.toString());
                            mUserDatabase.updateChildren(newImage);

                            finish();
                            return;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                         finish();
                         return;
                        }
                    });

                }
            });
        }else{
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileImage.setImageURI(resultUri);
        }

    }
}