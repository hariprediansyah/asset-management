package com.rama.myasset.ui.asset;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.util.Util;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rama.myasset.App;
import com.rama.myasset.AppCode;
import com.rama.myasset.Asset;
import com.rama.myasset.ImageOptimizer;
import com.rama.myasset.ProgressHelper;
import com.rama.myasset.R;
import com.rama.myasset.RegisterActivity;
import com.rama.myasset.databinding.FragmentAddBinding;
import com.rama.myasset.databinding.FragmentAttachmentBinding;
import com.rama.myasset.databinding.FragmentEditBinding;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class EditFragment extends Fragment {
    private static final String ARG_ASSET = "asset";
    public FragmentEditBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Asset asset;
    BottomSheetDialog dialog;
    FragmentAttachmentBinding attachmentBinding;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_PICK = 2;
    int selectedImage;

    Bitmap bitmapImage1;
    Bitmap bitmapImage2;
    Bitmap bitmapImage3;
    Bitmap bitmapImage4;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    public EditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditBinding.inflate(inflater, container, false);
        asset = App.asset;
        binding.txtAssetName.setText(asset.getAsset_name());
        binding.txtDescription.setText(asset.getDescription());
        binding.statusSwitch.setChecked(asset.getStatus().equals("Aktif"));
        loadImage(binding.image1, asset.getImage1());
        loadImage(binding.image2, asset.getImage2());
        loadImage(binding.image3, asset.getImage3());
        loadImage(binding.image4, asset.getImage4());
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
        binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCode.showConfirmationDialog("Apakah Anda yakin ingin delete asset ini?", requireContext(), new AppCode.OnEvent() {
                    @Override
                    public void ok() {
                        db.collection("assets").document(asset.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                storageRef.child("images/" + asset.getImage1()).delete().addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                                storageRef.child("images/" + asset.getImage2()).delete().addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                                storageRef.child("images/" + asset.getImage3()).delete().addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                                storageRef.child("images/" + asset.getImage4()).delete().addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                                Toast.makeText(getContext(), "Asset deleted", Toast.LENGTH_SHORT).show();
                                requireActivity().getOnBackPressedDispatcher().onBackPressed();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Failed delete data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
        binding.image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                selectedImage = 1;
            }
        });
        binding.image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                selectedImage = 2;
            }
        });
        binding.image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                selectedImage = 3;
            }
        });
        binding.image4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                selectedImage = 4;
            }
        });
        dialog = new BottomSheetDialog(requireContext());
        attachmentBinding = FragmentAttachmentBinding.inflate(getLayoutInflater());
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
        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressHelper.showDialog(requireContext(), "Loading ...");
                storageRef.child("images/" + asset.getImage1()).delete().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
                storageRef.child("images/" + asset.getImage2()).delete().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
                storageRef.child("images/" + asset.getImage3()).delete().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
                storageRef.child("images/" + asset.getImage4()).delete().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

                HashMap<String, Object> assetEdit = new HashMap<>();
                assetEdit.put("asset_name", binding.txtAssetName.getText().toString());
                assetEdit.put("description", binding.txtDescription.getText().toString());
                assetEdit.put("status", binding.statusSwitch.isChecked() ? "Aktif" : "Nonaktif");
                assetEdit.put("user_input", asset.getUser_input());
                assetEdit.put("time_input", asset.getTime_input());
                assetEdit.put("user_edit", App.user.getName());
                assetEdit.put("time_edit", FieldValue.serverTimestamp());

                bitmapImage1 = getBitmapFromImageView(binding.image1);
                bitmapImage2 = getBitmapFromImageView(binding.image2);
                bitmapImage3 = getBitmapFromImageView(binding.image3);
                bitmapImage4 = getBitmapFromImageView(binding.image4);

                if (bitmapImage1 != null){
                    String filename1 = asset.getId() + "image1" + System.currentTimeMillis() + ".jpg";
                    uploadImageToFirebase(bitmapImage1, filename1);
                    assetEdit.put("image1", filename1);
                }else {
                    assetEdit.put("image1", null);
                }
                if (bitmapImage2 != null){
                    String filename2 = asset.getId() + "image2" + System.currentTimeMillis() + ".jpg";
                    uploadImageToFirebase(bitmapImage2, filename2);
                    assetEdit.put("image2", filename2);
                }else {
                    assetEdit.put("image2", null);
                }
                if (bitmapImage3 != null){
                    String filename3 = asset.getId() + "image3" + System.currentTimeMillis() + ".jpg";
                    uploadImageToFirebase(bitmapImage3, filename3);
                    assetEdit.put("image3", filename3);
                }else {
                    assetEdit.put("image3", null);
                }
                if (bitmapImage4 != null){
                    String filename4 = asset.getId() + "image4" + System.currentTimeMillis() + ".jpg";
                    uploadImageToFirebase(bitmapImage4, filename4);
                    assetEdit.put("image4", filename4);
                }else {
                    assetEdit.put("image4", null);
                }

                db.collection("assets").document(asset.getId()).set(assetEdit).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        ProgressHelper.dismissDialog();
                        Toast.makeText(requireContext(), "Success save data", Toast.LENGTH_SHORT).show();
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ProgressHelper.dismissDialog();
                        Toast.makeText(requireContext(), "Failed save data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return binding.getRoot();
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
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                switch (selectedImage) {
                    case 1:
                        binding.image1.setImageBitmap(imageBitmap);
                        binding.image1.setTag(null);
                        break;
                    case 2:
                        binding.image2.setImageBitmap(imageBitmap);
                        binding.image2.setTag(null);
                        break;
                    case 3:
                        binding.image3.setImageBitmap(imageBitmap);
                        binding.image3.setTag(null);
                        break;
                    default:
                        binding.image4.setImageBitmap(imageBitmap);
                        binding.image4.setTag(null);
                        break;
                }
//                uploadImageToFirebase(imageBitmap);
            }
        }else if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri imageUri = data.getData();
                try {
                    Uri hasil = ImageOptimizer.optimize(requireContext(),imageUri, Bitmap.CompressFormat.JPEG, 1024, 1024, false, 80, 0, 0);
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), hasil);
                    switch (selectedImage) {
                        case 1:
                            binding.image1.setImageBitmap(imageBitmap);
                            binding.image1.setTag(null);
                            break;
                        case 2:
                            binding.image2.setImageBitmap(imageBitmap);
                            binding.image2.setTag(null);
                            break;
                        case 3:
                            binding.image3.setImageBitmap(imageBitmap);
                            binding.image3.setTag(null);
                            break;
                        default:
                            binding.image4.setImageBitmap(imageBitmap);
                            binding.image4.setTag(null);
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                }
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

    public void loadImage(ImageView imageView, String fileName){
        if (fileName == null){
            imageView.setTag("default");
            return;
        }
        StorageReference imagesRef = storageRef.child("images/" + fileName);

        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(requireContext())
                        .load(uri)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.image_background) // Gambar placeholder sebelum gambar di-load
                                .error(R.drawable.image_background) // Gambar error jika gagal load
                                .diskCacheStrategy(DiskCacheStrategy.ALL)) // 12 adalah radius sudut melengkung
                        .into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @androidx.annotation.Nullable Transition<? super Drawable> transition) {
                                imageView.setImageDrawable(resource);
                                imageView.setTag(null); // Menghapus tag setelah gambar diubah
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

    private Bitmap getBitmapFromImageView(ImageView imageView) {
        if ("default".equals(imageView.getTag())) {
            return null;
        }
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(imageView.getDrawingCache());
        imageView.setDrawingCacheEnabled(false);
        return bitmap;
    }
}