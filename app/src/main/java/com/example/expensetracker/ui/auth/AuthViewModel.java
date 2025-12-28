package com.example.expensetracker.ui.auth;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.expensetracker.data.entity.User;
import com.example.expensetracker.data.repository.UserRepository;
import com.example.expensetracker.utils.PasswordUtil;

public class AuthViewModel extends AndroidViewModel {

    private final UserRepository repository;

    // LiveData now holds the full User object on success
    public MutableLiveData<User> authSuccess = new MutableLiveData<>();
    public MutableLiveData<String> error = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application.getApplicationContext());
    }

    // ==================== REGISTER ====================

    public void register(String username, String password, String fullName) {
        // Input validation
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(fullName)) {
            error.postValue("Tous les champs sont obligatoires");
            return;
        }

        if (!PasswordUtil.isValidPassword(password)) {
            error.postValue(PasswordUtil.getPasswordStrengthMessage(password));
            return;
        }

        // Call repository to handle registration
        repository.register(username, password, fullName, new UserRepository.RegisterCallback() {
            @Override
            public void onSuccess(User user) {
                authSuccess.postValue(user); // Post the full user object
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    // ==================== LOGIN ====================

    public void login(String username, String password) {
        // Input validation
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            error.postValue("Veuillez remplir tous les champs");
            return;
        }

        // Call repository to handle login
        repository.login(username, password, new UserRepository.LoginCallback() {
            @Override
            public void onSuccess(User user) {
                authSuccess.postValue(user); // Post the full user object
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }
}

