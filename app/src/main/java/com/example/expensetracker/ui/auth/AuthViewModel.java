package com.example.expensetracker.ui.auth;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.expensetracker.data.entity.User;
import com.example.expensetracker.data.repository.UserRepository;
import com.example.expensetracker.utils.EmailUtil;
import com.example.expensetracker.utils.PasswordUtil;

public class AuthViewModel extends AndroidViewModel {

    private final UserRepository repository;

    // LiveData
    public MutableLiveData<User> authSuccess = new MutableLiveData<>();
    public MutableLiveData<String> error = new MutableLiveData<>();
    public MutableLiveData<Boolean> codeSentSuccess = new MutableLiveData<>(); // Pour dire à la vue d'afficher la popup

    // Stockage temporaire des données (avant validation)
    private String tempUsername, tempPassword, tempFullName, tempEmail, generatedCode;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application.getApplicationContext());
    }

    // ÉTAPE 1 : L'utilisateur clique sur "S'inscrire"
    public void startRegistration(String username, String password, String fullName, String email) {
        // Validation basique
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email)) {
            error.postValue("Tous les champs sont obligatoires");
            return;
        }

        if (!PasswordUtil.isValidPassword(password)) {
            error.postValue("Le mot de passe doit contenir 8 caractères, 1 majuscule, 1 chiffre.");
            return;
        }

        // Vérifier d'abord si l'utilisateur existe déjà en base
        repository.checkIfUserExists(username, email, new UserRepository.CheckCallback() {
            @Override
            public void onResult(boolean exists, String message) {
                if (exists) {
                    error.postValue(message);
                } else {
                    // Si le compte n'existe pas, on lance l'envoi du mail
                    sendVerificationEmail(username, password, fullName, email);
                }
            }
        });
    }

    // ÉTAPE 2 : Envoi du mail (En arrière-plan)
    private void sendVerificationEmail(String username, String password, String fullName, String email) {
        new Thread(() -> {
            try {
                generatedCode = EmailUtil.generateCode();

                // Sauvegarde temporaire
                tempUsername = username;
                tempPassword = password;
                tempFullName = fullName;
                tempEmail = email;

                // Envoi réel
                EmailUtil.sendEmail(email, generatedCode);

                // Succès -> La vue doit afficher la popup
                codeSentSuccess.postValue(true);

            } catch (Exception e) {
                e.printStackTrace();
                error.postValue("Erreur d'envoi du mail : " + e.getMessage());
            }
        }).start();
    }

    // ÉTAPE 3 : L'utilisateur entre le code
    public void verifyCodeAndSave(String inputCode) {
        if (generatedCode != null && generatedCode.equals(inputCode)) {
            // Le code est bon ! On crée vraiment le compte maintenant.
            repository.register(tempUsername, tempPassword, tempFullName, tempEmail, new UserRepository.RegisterCallback() {
                @Override
                public void onSuccess(User user) {
                    authSuccess.postValue(user);
                }

                @Override
                public void onError(String message) {
                    error.postValue(message);
                }
            });
        } else {
            error.postValue("Code incorrect !");
        }
    }

    // Login reste inchangé...
    public void login(String username, String password) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            error.postValue("Veuillez remplir tous les champs");
            return;
        }
        repository.login(username, password, new UserRepository.LoginCallback() {
            @Override
            public void onSuccess(User user) {
                authSuccess.postValue(user);
            }
            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }
}
