package com.rama.myasset.ui.asset;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AssetViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AssetViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Asset");
    }

    public LiveData<String> getText() {
        return mText;
    }
}