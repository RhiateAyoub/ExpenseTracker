package com.example.expensetracker.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.expensetracker.R;

public class ForgotPasswordFragment extends Fragment {

    private AuthViewModel viewModel;

    // Vues
    private LinearLayout layoutEmail, layoutCode, layoutNewPassword;
    private EditText etEmail, etCode, etNewPass, etConfirmNewPass;
    private Button btnSendCode, btnVerifyCode, btnResetPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Init Vues
        layoutEmail = view.findViewById(R.id.layoutEmail);
        layoutCode = view.findViewById(R.id.layoutCode);
        layoutNewPassword = view.findViewById(R.id.layoutNewPassword);

        etEmail = view.findViewById(R.id.etForgotEmail);
        etCode = view.findViewById(R.id.etForgotCode);
        etNewPass = view.findViewById(R.id.etNewPassword);
        etConfirmNewPass = view.findViewById(R.id.etConfirmNewPassword);

        btnSendCode = view.findViewById(R.id.btnSendCode);
        btnVerifyCode = view.findViewById(R.id.btnVerifyCode);
        btnResetPassword = view.findViewById(R.id.btnResetPassword);

        // État initial : Afficher seulement l'email
        showStep(1);

        // 1. Envoyer le code
        btnSendCode.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                viewModel.sendPasswordResetCode(email);
            } else {
                Toast.makeText(getContext(), "Entrez votre email", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Vérifier le code
        btnVerifyCode.setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            if (viewModel.verifyResetCode(code)) {
                showStep(3); // Passer à l'étape mot de passe
            } else {
                Toast.makeText(getContext(), "Code incorrect", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Changer le mot de passe
        btnResetPassword.setOnClickListener(v -> {
            String pass = etNewPass.getText().toString().trim();
            String confirm = etConfirmNewPass.getText().toString().trim();

            if (TextUtils.isEmpty(pass) || !pass.equals(confirm)) {
                Toast.makeText(getContext(), "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.resetPassword(pass);
        });

        // OBSERVERS
        viewModel.codeSentSuccess.observe(getViewLifecycleOwner(), sent -> {
            if (sent) {
                Toast.makeText(getContext(), "Code envoyé !", Toast.LENGTH_SHORT).show();
                showStep(2); // Passer à l'étape code
            }
        });

        viewModel.passwordResetSuccess.observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Mot de passe modifié avec succès", Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(this).navigateUp(); // Retour au login
            }
        });

        viewModel.error.observe(getViewLifecycleOwner(), msg ->
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show()
        );

        return view;
    }

    private void showStep(int step) {
        layoutEmail.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        layoutCode.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        layoutNewPassword.setVisibility(step == 3 ? View.VISIBLE : View.GONE);
    }
}
