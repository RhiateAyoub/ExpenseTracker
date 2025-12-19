package com.example.expensetracker.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.expensetracker.R;
import com.example.expensetracker.utils.SessionManager;

public class LoginFragment extends Fragment {

    private EditText etUsername, etPassword;
    private Button btnLogin;

    private AuthViewModel viewModel;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnSeConnecter);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        sessionManager = new SessionManager(requireContext());

        btnLogin.setOnClickListener(v ->
                viewModel.login(
                        etUsername.getText().toString().trim(),
                        etPassword.getText().toString().trim()
                )
        );

        viewModel.authSuccess.observe(getViewLifecycleOwner(), userId -> {
            sessionManager.saveUserId(userId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.homeFragment);
        });

        viewModel.error.observe(getViewLifecycleOwner(), msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        );

        return view;
    }
}
