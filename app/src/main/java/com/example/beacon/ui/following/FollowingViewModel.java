package com.example.beacon.ui.following;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FollowingViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public FollowingViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is following fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}