package com.rama.myasset;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rama.myasset.databinding.FragmentAttachmentBinding;
import com.rama.myasset.databinding.FragmentEditBinding;

public class AttachmentFragment extends Fragment {
    FragmentAttachmentBinding binding;
    public AttachmentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAttachmentBinding.inflate(inflater, container, false);


        return binding.getRoot();
    }
}