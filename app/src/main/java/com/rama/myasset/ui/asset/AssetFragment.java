package com.rama.myasset.ui.asset;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rama.myasset.App;
import com.rama.myasset.AppCode;
import com.rama.myasset.Asset;
import com.rama.myasset.AssetAdapter;
import com.rama.myasset.ProgressHelper;
import com.rama.myasset.R;
import com.rama.myasset.RegisterActivity;
import com.rama.myasset.databinding.FragmentAddBinding;
import com.rama.myasset.databinding.FragmentAssetBinding;

import java.util.HashMap;

public class AssetFragment extends Fragment {

    private FragmentAssetBinding binding;
    FirebaseFirestore db;
    FragmentAddBinding fragmentAddBinding;
    Dialog dialogAdd;
    AssetAdapter adapter;
    AssetAdapter adapterFilter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AssetViewModel assetViewModel =
                new ViewModelProvider(this).get(AssetViewModel.class);

        binding = FragmentAssetBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        db = FirebaseFirestore.getInstance();
        adapter = new AssetAdapter(getActivity().getApplicationContext());
        adapterFilter = new AssetAdapter(getActivity().getApplicationContext());
        adapter.setOnItemClickListener(new AssetAdapter.OnItemClickListener() {
            @Override
            public void onCLick(Asset asset) {
                Bundle bundle = new Bundle();
                App.asset = asset;
                Navigation.findNavController(root.getRootView()).navigate(R.id.action_navigation_asset_to_editAssetFragment, bundle);
            }
        });
        adapterFilter.setOnItemClickListener(new AssetAdapter.OnItemClickListener() {
            @Override
            public void onCLick(Asset asset) {
                Bundle bundle = new Bundle();
                App.asset = asset;
                Navigation.findNavController(root).navigate(R.id.action_navigation_asset_to_editAssetFragment, bundle);
            }
        });

        binding.refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext(),LinearLayoutManager.VERTICAL, false));
        binding.recycler.setAdapter(adapter);

        dialogAdd = new Dialog(getContext());
        fragmentAddBinding = FragmentAddBinding.inflate(getLayoutInflater());
        dialogAdd.setContentView(fragmentAddBinding.getRoot());
        dialogAdd.setCancelable(false);
        dialogAdd.getWindow().setWindowAnimations(R.style.DialogAnimation);
        fragmentAddBinding.btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAdd.dismiss();
            }
        });
        fragmentAddBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAdd.dismiss();
            }
        });
        fragmentAddBinding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressHelper.showDialog(requireContext(), "Loading ...");
                HashMap<String, Object> asset = new HashMap<>();
                asset.put("asset_name", fragmentAddBinding.txtAssetName.getText().toString());
                asset.put("description", fragmentAddBinding.txtDescription.getText().toString());
                asset.put("status", fragmentAddBinding.statusSwitch.isChecked() ? "Aktif" : "Nonaktif");
                asset.put("user_input", "yaya");
                asset.put("time_input", FieldValue.serverTimestamp());
                asset.put("user_edit", null);
                asset.put("time_edit", null);
                asset.put("image1", null);
                asset.put("image2", null);
                asset.put("image3", null);
                asset.put("image4", null);

                db.collection("assets").add(asset).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        AppCode.showAlert("Asset berhasil ditambahkan", requireContext());
                        loadData();
                        ProgressHelper.dismissDialog();
                        dialogAdd.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ProgressHelper.dismissDialog();
                        Toast.makeText(getActivity().getApplicationContext(), "Failed Add", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentAddBinding.txtAssetName.setText("");
                fragmentAddBinding.txtDescription.setText("");
                fragmentAddBinding.statusSwitch.setChecked(true);
                dialogAdd.show();
            }
        });

        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cari();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.btnSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sort();
            }
        });

        loadData();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void loadData(){
        db.collection("assets").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                adapter.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Asset asset = document.toObject(Asset.class);
                    asset.setId(document.getId());
                    adapter.add(asset);
                }
                cari();
                if (binding.btnSort.getRotation() == 0){
                    adapter.sort(false);
                    adapterFilter.sort(false);
                }
                else {
                    adapter.sort(true);
                    adapterFilter.sort(true);
                }
                if (binding.refresh.isRefreshing()){
                    binding.refresh.setRefreshing(false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity().getApplicationContext(), "Failed get data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void cari(){
        if (binding.editTextSearch.getText().equals("")){
            binding.recycler.setAdapter(adapter);
        }else {
            adapterFilter.clear();
            for (int i = 0; i < adapter.getItemCount(); i++) {
                try {
                    if (adapter.getItem(i).getAsset_name().toLowerCase().contains(binding.editTextSearch.getText().toString().toLowerCase()) ||
                            adapter.getItem(i).getDescription().toLowerCase().contains(binding.editTextSearch.getText().toString().toLowerCase())) {
                        adapterFilter.add(adapter.getItem(i));
                    }
                }catch (Exception exception){
                    Toast.makeText(getActivity().getApplicationContext(), "Error load data", Toast.LENGTH_SHORT).show();
                }
            }
            binding.recycler.setAdapter(adapterFilter);
        }
    }

    public void sort(){
        if (binding.btnSort.getRotation() == 180){
            adapter.sort(false);
            adapterFilter.sort(false);
            binding.btnSort.setRotation(0);
        }
        else {
            adapter.sort(true);
            adapterFilter.sort(true);
            binding.btnSort.setRotation(180);
        }
    }
}