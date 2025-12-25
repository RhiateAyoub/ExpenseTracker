package com.example.expensetracker.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.expensetracker.data.entity.User;
import com.example.expensetracker.data.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository repository;

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    // You need to accept a RegisterCallback here
    // Dans UserViewModel.java (si utilisé)
    public void register(User user, String password, UserRepository.RegisterCallback callback) {
        repository.register(user.getUsername(), password, user.getFullName(), user.getEmail(), callback);
    }


    public void login(
            String email,
            String password,
            UserRepository.LoginCallback callback
    ) {
        repository.login(email, password, callback);
    }
}
