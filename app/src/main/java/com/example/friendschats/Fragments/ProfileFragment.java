package com.example.friendschats.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.friendschats.Activities.ViewImageActivity;
import com.example.friendschats.Constant.Constants;
import com.example.friendschats.Constant.FirebaseConstants;
import com.example.friendschats.Models.User;
import com.example.friendschats.R;
import com.example.friendschats.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;


public class ProfileFragment extends Fragment {


    FragmentProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    ProgressDialog progressDialog;

    private User user;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();
        Intent intent = getActivity().getIntent();
        String name = intent.getStringExtra(Constants.USER_NAME);
        String about = intent.getStringExtra(Constants.ABOUT);
        String url = intent.getStringExtra(Constants.PROFILE_IMAGE);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();


        //Setting all views with user details
        binding.etUserName.setText(name);
        binding.etAbout.setText(about);
        Glide.with(this).load(url)
                .placeholder(R.drawable.avatar).into(binding.ivProfileImage);

        //Selecting new Profile
        binding.ivAddProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, Constants.PICK_IMAGE_CODE);
            }
        });

        //Viewing profile
        binding.ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(getActivity(), ViewImageActivity.class);
                intent1.putExtra(Constants.PROFILE_PICTURE_URL,user.getProfilePic());
                intent1.putExtra(Constants.ACTIVITY_NAME,"Profile");
                startActivity(intent1);
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (binding.etUserName.getText().toString().trim().equals(""))
                {
                    binding.etUserName.setError("Please enter name");
                    progressDialog.dismiss();
                    return;
                }
                String about = "Hey there!";

                progressDialog.show();
                if (!binding.etAbout.getText().toString().trim().equals(""))
                {
                    about = binding.etAbout.getText().toString().trim();
                }
                String userName = binding.etUserName.getText().toString().trim();

                //Updating user profile
                updateUser(userName, about);
            }
        });


        return binding.getRoot();
    }



    /**
     * This method set value to current log in user to User object.
     */
    private void getCurrentUser() {


        FirebaseDatabase.getInstance().getReference()
                .child(FirebaseConstants.USER)
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( @NotNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);

                        binding.etUserName.setText(user.getName());
                        binding.etAbout.setText(user.getAbout());
                        Picasso.get()
                                .load(user.getProfilePic())
                                .placeholder(R.drawable.avatar)
                                .into(binding.ivProfileImage);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled( @NotNull DatabaseError error) {
                        progressDialog.dismiss();
                    }
                });
    }



    /**
     * Updating user Profile.
     * @param userName
     * @param about
     */
    private void updateUser(String userName, String about)
    {
        HashMap<String, Object> object = new HashMap<>();
        object.put(FirebaseConstants.NAME,userName);
        object.put(FirebaseConstants.ABOUT,about);

        firebaseDatabase.getReference()
                .child(FirebaseConstants.USER)
                .child(auth.getUid())
                .updateChildren(object).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Successfully Updated!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        progressDialog.show();
        getCurrentUser();
    }



    /***
     * Uploading image to firebase storage and update user
     * @param profileImage
     */
    private void saveImageToFirebase(Uri  profileImage)
    {
        binding.ivProfileImage.setImageURI(profileImage);

        StorageReference reference = firebaseStorage.getReference()
                .child(FirebaseConstants.PROFILES)
                .child(auth.getUid());
        reference.putFile(profileImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            firebaseDatabase.getReference()
                                    .child(FirebaseConstants.USER)
                                    .child(auth.getUid())
                                    .child(FirebaseConstants.PROFILE_PIC)
                                    .setValue(uri.toString())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            binding.progressBar.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    });
                }else {
                    binding.progressBar.setVisibility(View.GONE);
                }
            }
        });
    }


    //Selecting profile picture and uploading it onto firebase
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null )
        {
            if (data.getData() != null)
            {
                binding.progressBar.setVisibility(View.VISIBLE);
                Uri profileImage = data.getData();
                saveImageToFirebase(profileImage);

            }
        }
    }




}