package com.example.friendschats.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.friendschats.Constant.Constants;
import com.example.friendschats.Constant.FirebaseConstants;
import com.example.friendschats.Models.User;
import com.example.friendschats.R;
import com.example.friendschats.databinding.ActivitySetUpProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashMap;

public class SetUpProfileActivity extends AppCompatActivity {
    ActivitySetUpProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;

    ProgressDialog progressDialog;
    Uri profilePic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetUpProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile...");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();


        if (getIntent().hasExtra(Constants.USER_NAME))
        {
            binding.etUserName.setText(getIntent().getStringExtra(Constants.USER_NAME));
        }

        getSupportActionBar().hide();

        /**
         * Selecting image
         */
        binding.ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, Constants.PICK_IMAGE_CODE);
            }
        });

        /**
         * Setting up profile
         */
        binding.btnSetupProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                if(binding.etUserName.getText().toString().trim().isEmpty())
                {
                    binding.etUserName.setError("Please enter your name");
                    progressDialog.dismiss();
                    return;
                }

                //Take user name and about.

                 String userName = binding.etUserName.getText().toString().trim();

                //Taking about user field
                 String about ="Hey there!";
                  if (!binding.etAbout.getText().toString().equals(""))
                  {
                      about = binding.etAbout.getText().toString().trim();
                  }

                if (profilePic != null)
                {
                    //Setting up profile
                    setProfileWithImage(userName, about, profilePic);
                }else
                {
                    //Setting up profile without image
                    setProfileWithoutImage(userName, about);
                }
            }
        });

    }


    /***
     *Selecting image after pick up
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.PICK_IMAGE_CODE && resultCode == RESULT_OK)
        {
            if (data != null && data.getData() != null)
            {
                Uri profileImage = data.getData();

                binding.ivProfileImage.setImageURI(profileImage);
                profilePic = data.getData();
            }
        }
    }


    /**
     * Settting up profile with following parameters
     * @param userName
     * @param about
     * @param profilePicUri
     */
  public void setProfileWithImage(String userName, String about, Uri profilePicUri)
  {
      StorageReference reference = firebaseStorage.getReference()
              .child(FirebaseConstants.PROFILES)
              .child(auth.getUid());

      String finalAbout = about;
      reference.putFile(profilePicUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
          @Override
          public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
              if (task.isSuccessful())
              {
                  reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                      @Override
                      public void onSuccess(Uri uri) {
                          String imageUrl = uri.toString();

                          //Create object of image in firebase
                          uploadImageToStorage(uri);
                          String uid = auth.getUid();
                          String email = auth.getCurrentUser().getEmail();
                          User user = new User(uid, userName, finalAbout,email,imageUrl);

                          firebaseDatabase.getReference()
                                  .child(FirebaseConstants.USER)
                                  .child(auth.getUid())
                                  .setValue(user)
                                  .addOnSuccessListener(new OnSuccessListener<Void>() {
                                      @Override
                                      public void onSuccess(Void unused) {
                                          progressDialog.dismiss();
                                          Intent intent = new Intent(SetUpProfileActivity.this, MainActivity.class);
                                          startActivity(intent);
                                          Toast.makeText(SetUpProfileActivity.this, "Successfully updated!", Toast.LENGTH_SHORT).show();
                                      }
                                  });
                      }
                  });
              }
          }
      });
  }


    /**
     * Setting up profile without image
     * @param userName
     * @param about
     */
  public void setProfileWithoutImage(String userName, String about)
  {
      String uid = auth.getUid();
      String email = auth.getCurrentUser().getEmail();
      User user = new User(uid, userName, about,email,"No image");

      firebaseDatabase.getReference()
              .child(FirebaseConstants.USER)
              .child(auth.getUid())
              .setValue(user)
              .addOnSuccessListener(new OnSuccessListener<Void>() {
                  @Override
                  public void onSuccess(Void unused) {
                      progressDialog.dismiss();
                      Intent intent = new Intent(SetUpProfileActivity.this, MainActivity.class);
                      startActivity(intent);
                      Toast.makeText(SetUpProfileActivity.this, "Successfully updated!", Toast.LENGTH_SHORT).show();
                  }
              });
  }


    /***
     * U8ploading Image to firebase Storage
     */
    public void uploadImageToStorage(Uri profileImageUri)
    {
        String filePath = profileImageUri.toString();

        //Making object to put in firebase
        HashMap<String, Object> obj = new HashMap<>();
        obj.put(FirebaseConstants.IMAGE, filePath);

        firebaseDatabase.getReference().child(FirebaseConstants.USER)
                .child(FirebaseAuth.getInstance().getUid())
                .updateChildren(obj).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
    }
}