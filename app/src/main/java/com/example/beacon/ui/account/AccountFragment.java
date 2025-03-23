package com.example.beacon.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beacon.MyOrganizationActivity;
import com.example.beacon.databinding.FragmentAccountBinding;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        com.example.beacon.ui.Account.AccountViewModel accountViewModel =
                new ViewModelProvider(this).get(com.example.beacon.ui.Account.AccountViewModel.class);

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        Button btnMyOrganization = binding.btnMyOrganization;
        btnMyOrganization.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MyOrganizationActivity.class);
            startActivity(intent);
        });





        // Observe text changes
        final TextView textView = binding.textAccount;
        accountViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
