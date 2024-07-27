package com.rama.myasset.ui.home;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rama.myasset.App;
import com.rama.myasset.LoginActivity;
import com.rama.myasset.MainActivity;
import com.rama.myasset.R;
import com.rama.myasset.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        if (App.user == null){
            startActivity(new Intent(requireActivity().getApplicationContext(), LoginActivity.class));
            requireActivity().finish();
        }
        binding.txtName.setText(App.user.getName());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("assets").whereEqualTo("status", "Aktif").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                binding.txtTotalActive.setText(queryDocumentSnapshots.size() + " Asset");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireContext().getApplicationContext(), "Failed get data", Toast.LENGTH_SHORT).show();
            }
        });
        db.collection("assets").whereEqualTo("status", "Nonaktif").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                binding.txtTotalInactive.setText(queryDocumentSnapshots.size() + " Asset");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireContext().getApplicationContext(), "Failed get data", Toast.LENGTH_SHORT).show();
            }
        });
        binding.btnViewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BottomNavigationView) getActivity().findViewById(R.id.nav_view)).setSelectedItemId(R.id.navigation_asset);
            }
        });

        binding.cardActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BottomNavigationView) getActivity().findViewById(R.id.nav_view)).setSelectedItemId(R.id.navigation_asset);
            }
        });

        binding.cardNonaktif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BottomNavigationView) getActivity().findViewById(R.id.nav_view)).setSelectedItemId(R.id.navigation_asset);
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

        binding.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BottomNavigationView) getActivity().findViewById(R.id.nav_view)).setSelectedItemId(R.id.navigation_profile);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}