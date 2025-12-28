package com.example.expensetracker.ui.auth;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.expensetracker.R;

public class RegisterFragment extends Fragment {

    private EditText etUsername, etPassword, etFullName, etEmail;
    private Button btnRegister;
    private TextView tvSeConnecter;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Init vues
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etEmail);
        btnRegister = view.findViewById(R.id.btnInscrire);
        tvSeConnecter = view.findViewById(R.id.tvSeConnecter);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // CLIC SUR INSCRIPTION
        btnRegister.setOnClickListener(v -> {
            // On lance le processus (Vérif DB -> Envoi Mail)
            viewModel.startRegistration(
                    etUsername.getText().toString().trim(),
                    etPassword.getText().toString().trim(),
                    etFullName.getText().toString().trim(),
                    etEmail.getText().toString().trim()
            );
        });

        // Navigation vers Login
        tvSeConnecter.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // OBSERVER : Code envoyé avec succès ? -> Afficher la popup
        viewModel.codeSentSuccess.observe(getViewLifecycleOwner(), sent -> {
            if (sent) {
                showVerificationDialog();
            }
        });

        // OBSERVER : Compte créé avec succès (après code valide)
        viewModel.authSuccess.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                Toast.makeText(requireContext(), "Compte vérifié et créé !", Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(this).navigate(R.id.action_registerFragment_to_loginFragment);
            }
        });

        // OBSERVER : Erreurs
        viewModel.error.observe(getViewLifecycleOwner(), msg -> {
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    // Affiche la popup pour entrer le code
    private void showVerificationDialog() {
        // 1. Créer le Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // 2. Gonfler (Inflate) notre layout personnalisé
        // Si cette ligne est rouge, c'est que le fichier XML manque (voir étape 2)
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_verification, null);
        builder.setView(dialogView);

        // 3. Initialiser les vues du dialog
        TextView tvMessage = dialogView.findViewById(R.id.tvDialogMessage);
        EditText etCode = dialogView.findViewById(R.id.etDialogCode);
        Button btnVerify = dialogView.findViewById(R.id.btnDialogVerify);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);

        // Mettre à jour le message avec l'email
        tvMessage.setText("Un code de vérification a été envoyé à :\n" + etEmail.getText().toString());

        // 4. Créer le dialog
        AlertDialog dialog = builder.create();

        // Fond transparent pour les coins arrondis
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // 5. Gérer les clics
        btnVerify.setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            if (!code.isEmpty()) {
                viewModel.verifyCodeAndSave(code);
                dialog.dismiss(); // Fermer le dialog après vérification
            } else {
                Toast.makeText(requireContext(), "Veuillez entrer le code", Toast.LENGTH_SHORT).show();
            }
        });

        // Gestion du bouton Annuler
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Empêche de fermer en cliquant à côté
        dialog.setCancelable(false);

        // Afficher le dialog
        dialog.show();
    }
}
