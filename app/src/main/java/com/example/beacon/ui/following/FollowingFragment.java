package com.example.beacon.ui.following;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beacon.MapsActivity;
import com.example.beacon.databinding.FragmentFollowingBinding;

public class FollowingFragment extends Fragment {

    private FragmentFollowingBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FollowingViewModel followingViewModel =
                new ViewModelProvider(this).get(FollowingViewModel.class);

        binding = FragmentFollowingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set text from ViewModel
        final TextView textView = binding.textFollowing;
        followingViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Get views
        EditText editName = binding.editName;
        EditText editAddress = binding.editAddress;
        Button btnGoToMap = binding.btnGoToMap;

        // Set button click listener
        btnGoToMap.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String address = editAddress.getText().toString().trim();

            if (!name.isEmpty() && !address.isEmpty()) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("address", address);
                startActivity(intent);
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