package com.example.expensetracker.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // Import TextView
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
    private TextView tvInscrire; // TextView for navigating to register

    private AuthViewModel viewModel;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // ==================== VIEW INITIALIZATION ====================
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnSeConnecter);
        tvInscrire = view.findViewById(R.id.tvInscrire); // Find the TextView

        // ==================== VIEWMODEL & SESSION ====================
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        sessionManager = new SessionManager(requireContext());

        // ==================== CLICK LISTENERS ====================
        btnLogin.setOnClickListener(v ->
                viewModel.login(
                        etUsername.getText().toString().trim(),
                        etPassword.getText().toString().trim()
                )
        );
        // Dans onCreateView
        TextView tvForgot = view.findViewById(R.id.tvForgotPassword);
        tvForgot.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        );

        // Navigate to RegisterFragment when "S'inscrire" is clicked
        tvInscrire.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_loginFragment_to_registerFragment)
        );

        // ==================== OBSERVERS ====================
        viewModel.authSuccess.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                // Create session
                sessionManager.createLoginSession(user.getId(), user.getUsername(), user.getFullName());

                // Navigate to home and clear the back stack
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_loginFragment_to_homeFragment);
            }
        });

        viewModel.error.observe(getViewLifecycleOwner(), msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        );

        return view;
    }
}
