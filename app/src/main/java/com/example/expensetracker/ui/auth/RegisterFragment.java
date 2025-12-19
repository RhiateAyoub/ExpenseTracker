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
import com.example.expensetracker.data.entity.User; // Keep this import
import com.example.expensetracker.utils.SessionManager; // Keep this import

public class RegisterFragment extends Fragment {

    private EditText etUsername, etPassword, etFullName;
    private Button btnRegister;
    private TextView tvSeConnecter; // TextView for navigating back to login

    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register, container, false);

        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        etFullName = view.findViewById(R.id.etFullName);
        btnRegister = view.findViewById(R.id.btnInscrire);
        tvSeConnecter = view.findViewById(R.id.tvSeConnecter); // Find the TextView

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // ==================== CLICK LISTENERS ====================
        btnRegister.setOnClickListener(v ->
                viewModel.register(
                        etUsername.getText().toString().trim(),
                        etPassword.getText().toString().trim(),
                        etFullName.getText().toString().trim()
                )
        );

        // Navigate back to LoginFragment when "Se connecter" is clicked
        tvSeConnecter.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigateUp() // Simply go back to the previous screen (Login)
        );

        // ==================== OBSERVERS ====================
        viewModel.authSuccess.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                // Registration was successful
                Toast.makeText(requireContext(), "Compte créé avec succès ! Veuillez vous connecter.", Toast.LENGTH_LONG).show();

                // Navigate back to the login screen
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_registerFragment_to_loginFragment);
            }
        });

        viewModel.error.observe(getViewLifecycleOwner(), msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        );

        return view;
    }
}
