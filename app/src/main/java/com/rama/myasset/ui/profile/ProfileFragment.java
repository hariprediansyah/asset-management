package com.rama.myasset.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rama.myasset.App;
import com.rama.myasset.AppCode;
import com.rama.myasset.ImageOptimizer;
import com.rama.myasset.LoginActivity;
import com.rama.myasset.MainActivity;
import com.rama.myasset.R;
import com.rama.myasset.User;
import com.rama.myasset.databinding.FragmentAttachmentBinding;
import com.rama.myasset.databinding.FragmentProfileBinding;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_PICK = 2;
    BottomSheetDialog dialog;
    FragmentAttachmentBinding attachmentBinding;
    Bitmap bitmapProfile;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.txtName.setText(App.user.getName());
        binding.txtUsername.setText(App.user.getUsername());
        binding.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(container.getContext(), LoginActivity.class));
                getActivity().finish();
            }
        });

        if (App.user.getProfile_pic() != null){
            StorageReference imagesRef = storageRef.child("images/" + App.user.getProfile_pic());

            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(requireContext())
                            .load(uri)
                            .apply(new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(new CustomTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @androidx.annotation.Nullable Transition<? super Drawable> transition) {
                                    binding.imageProfile.setImageDrawable(resource);
                                }

                                @Override
                                public void onLoadCleared(@androidx.annotation.Nullable Drawable placeholder) {

                                }
                            });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(requireActivity().getApplicationContext(), "Failed load image", Toast.LENGTH_SHORT).show();
                }
            });
        }

        binding.btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Objects.requireNonNull(binding.txtPassword.getText()).toString().equals(Objects.requireNonNull(binding.txtConfirmPassword.getText()).toString())){
                    AppCode.showAlert("Confirm password tidak sama", requireContext());
                    return;
                }
                AppCode.showConfirmationDialog("Konfirmasi change password?",requireContext(), new AppCode.OnEvent() {
                    @Override
                    public void ok() {
                        HashMap<String, Object> user = new HashMap<>();
                        user.put("username", App.user.getUsername());
                        user.put("name", App.user.getName());
                        user.put("password", binding.txtPassword.getText().toString());
                        user.put("profile_pic", App.user.getProfile_pic());
                        user.put("register_date", App.user.getRegister_date());
                        db.collection("users").document(App.user.getId()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                AppCode.showAlert("Password berhasil diubah", requireContext());
                                binding.txtPassword.setText("");
                                binding.txtConfirmPassword.setText("");
                                binding.txtPassword.setFocusable(false);
                                binding.txtConfirmPassword.setFocusable(false);

                                reloadUser();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Failed change password", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

        binding.btnChangePassword.setEnabled(!binding.txtPassword.getText().toString().isEmpty() && !Objects.requireNonNull(binding.txtConfirmPassword.getText()).toString().isEmpty());
        binding.txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.btnChangePassword.setEnabled(!binding.txtPassword.getText().toString().isEmpty() && !Objects.requireNonNull(binding.txtConfirmPassword.getText()).toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BottomNavigationView) getActivity().findViewById(R.id.nav_view)).setSelectedItemId(R.id.navigation_home);
            }
        });

        binding.txtConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.btnChangePassword.setEnabled(!binding.txtPassword.getText().toString().isEmpty() && !Objects.requireNonNull(binding.txtConfirmPassword.getText()).toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        dialog = new BottomSheetDialog(requireContext());
        attachmentBinding = FragmentAttachmentBinding.inflate(getLayoutInflater());
        attachmentBinding.txtTitle.setText("Change profile");
        dialog.setContentView(attachmentBinding.getRoot());
        attachmentBinding.btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        attachmentBinding.btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchGalleryIntent();
            }
        });

        return root;
    }

    private void reloadUser() {
        db.collection("users").document(App.user.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                user.setId(documentSnapshot.getId());
                App.user = user;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireContext(), "Error get data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchGalleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                String fileName = App.user.getId() + ".jpg";
                HashMap<String, Object> user = new HashMap<>();
                user.put("username", App.user.getUsername());
                user.put("name", App.user.getName());
                user.put("password", App.user.getPassword());
                user.put("profile_pic", fileName);
                user.put("register_date", App.user.getRegister_date());
                db.collection("users").document(App.user.getId()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        AppCode.showAlert("Profile image berhasil diubah", requireContext());
                        Bundle extras = data.getExtras();
                        bitmapProfile = (Bitmap) extras.get("data");
                        binding.imageProfile.setImageBitmap(bitmapProfile);
                        uploadImageToFirebase(bitmapProfile, App.user.getId() + ".jpg");
                        reloadUser();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed change password", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }else if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri imageUri = data.getData();
                String fileName = App.user.getId() + ".jpg";
                HashMap<String, Object> user = new HashMap<>();
                user.put("username", App.user.getUsername());
                user.put("name", App.user.getName());
                user.put("password", App.user.getPassword());
                user.put("profile_pic", fileName);
                user.put("register_date", App.user.getRegister_date());
                db.collection("users").document(App.user.getId()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        try {
                            Uri hasil = ImageOptimizer.optimize(requireContext(),imageUri, Bitmap.CompressFormat.JPEG, 1024, 1024, false, 80, 0, 0);
                            bitmapProfile = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), hasil);
                            binding.imageProfile.setImageBitmap(bitmapProfile);
                            AppCode.showAlert("Profile image berhasil diubah", requireContext());
                            uploadImageToFirebase(bitmapProfile, App.user.getId() + ".jpg");
                            reloadUser();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed change password", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        dialog.dismiss();
    }

    public String uploadImageToFirebase(Bitmap bitmap, String fileName) {
        StorageReference imagesRef = storageRef.child("images/" + fileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imagesRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireActivity().getApplicationContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return imagesRef.getPath();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}