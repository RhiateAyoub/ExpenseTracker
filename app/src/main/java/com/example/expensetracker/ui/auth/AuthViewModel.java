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
    public MutableLiveData<Boolean> passwordResetSuccess = new MutableLiveData<>();
    // Stockage temporaire des données (avant validation)
    private String tempUsername, tempPassword, tempFullName, tempEmail, generatedCode;


    private String resetEmail; // Pour se souvenir de l'email pendant le processus

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
    // 1. Demande de réinitialisation (Vérifie si email existe -> Envoie mail)
    public void sendPasswordResetCode(String email) {
        // Vérifier si l'utilisateur existe avec cet email
        repository.checkIfUserExists("", email, new UserRepository.CheckCallback() {
            @Override
            public void onResult(boolean exists, String message) {
                if (exists) {
                    // L'utilisateur existe, on peut envoyer le code
                    // (Note: on utilise le message "déjà utilisé" qui signifie ici "email trouvé")
                    sendVerificationEmailForReset(email);
                } else {
                    error.postValue("Aucun compte associé à cet email.");
                }
            }
        });
    }
    // Envoi du mail spécifique pour le reset
    private void sendVerificationEmailForReset(String email) {
        new Thread(() -> {
            try {
                generatedCode = EmailUtil.generateCode();
                resetEmail = email; // On garde l'email en mémoire pour l'étape finale

                // On réutilise votre EmailUtil existant
                EmailUtil.sendEmail(email, generatedCode);

                // On utilise le même LiveData que pour l'inscription pour dire "Code envoyé"
                codeSentSuccess.postValue(true);
            } catch (Exception e) {
                error.postValue("Erreur d'envoi : " + e.getMessage());
            }
        }).start();
    }
    // 2. Vérification du code (Local)
    public boolean verifyResetCode(String inputCode) {
        return generatedCode != null && generatedCode.equals(inputCode);
    }

    // 3. Réinitialisation finale en base de données
    public void resetPassword(String newPassword) {
        if (!PasswordUtil.isValidPassword(newPassword)) {
            error.postValue(PasswordUtil.getPasswordStrengthMessage(newPassword));
            return;
        }

        repository.resetPasswordByEmail(resetEmail, newPassword, new UserRepository.UpdateCallback() {
            @Override
            public void onSuccess() {
                passwordResetSuccess.postValue(true);
            }

            @Override
            public void onError(String msg) {
                error.postValue(msg);
            }
        });
    }

}
