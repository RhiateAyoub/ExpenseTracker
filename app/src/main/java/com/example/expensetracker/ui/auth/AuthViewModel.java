package com.example.expensetracker.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.expensetracker.data.entity.User;
import com.example.expensetracker.data.repository.UserRepository;

public class AuthViewModel extends AndroidViewModel {

    private final UserRepository repository;

    public MutableLiveData<Integer> authSuccess = new MutableLiveData<>();
    public MutableLiveData<String> error = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    // ==================== REGISTER ====================

    public void register(String username, String password, String fullName) {
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            error.postValue("Tous les champs sont obligatoires");
            return;
        }

        String hashedPassword = PasswordUtils.hashPassword(password);

        User user = new User(
                username,
                hashedPassword,
                fullName,
                System.currentTimeMillis()
        );

        repository.register(user, new UserRepository.RegisterCallback() {
            @Override
            public void onResult(long userId) {
                authSuccess.postValue((int) userId);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    // ==================== LOGIN ====================

    public void login(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            error.postValue("Veuillez remplir tous les champs");
            return;
        }

        repository.login(username, new UserRepository.LoginCallback() {
            @Override
            public void onResult(User user) {
                if (PasswordUtils.verifyPassword(password, user.getPassword())) {
                    authSuccess.postValue(user.getId());
                } else {
                    error.postValue("Mot de passe incorrect");
                }
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }
}
