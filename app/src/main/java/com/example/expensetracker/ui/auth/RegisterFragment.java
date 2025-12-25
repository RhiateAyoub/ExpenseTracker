package com.example.expensetracker.ui.auth;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Vérification Email");
        builder.setMessage("Un code a été envoyé à " + etEmail.getText().toString() + "\nEntrez le code :");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Vérifier", (dialog, which) -> {
            String code = input.getText().toString();
            // On envoie le code au ViewModel pour vérification finale
            viewModel.verifyCodeAndSave(code);
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());
        builder.setCancelable(false); // Empêche de fermer en cliquant à côté
        builder.show();
    }
}
