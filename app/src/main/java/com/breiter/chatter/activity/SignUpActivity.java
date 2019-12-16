package com.breiter.chatter.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.breiter.chatter.R;
import com.breiter.chatter.tool.InputValidator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signUpButton;
    private boolean isHidden = true;
    private ImageView showPasswordImageView;
    private ImageView hidePasswordImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sign up!");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bindViews(); //1

        mAuth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerNewUser(); //2
            }
        });
    }

    //1.
    private void bindViews() {
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        showPasswordImageView = findViewById(R.id.showPasswordImageView);
        hidePasswordImageView = findViewById(R.id.hidePasswordImageView);
    }

    //2. register a new user in Firebase
    private void registerNewUser() {
        final String usernameInput = usernameEditText.getText().toString().trim();
        String emailInput = emailEditText.getText().toString().trim();
        String passwordInput = passwordEditText.getText().toString().trim();

        if (credentialsValid(usernameInput, emailInput, passwordInput)) { //2a
            mAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                    .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                assert user != null;
                                String userId = user.getUid();

                                reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                                Map<String, String> hashMap = new HashMap<>();
                                hashMap.put("userId", userId);
                                hashMap.put("username", usernameInput);
                                hashMap.put("imageURL", "default");
                                hashMap.put("status", "offline");
                                hashMap.put("search", usernameInput.toLowerCase());

                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                            redirect();  //2b
                                    }
                                });

                            } else {
                                Toast.makeText(SignUpActivity.this, "This username or email address already exist",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    //2a. In order to register a new user, verify if user's inputs are valid
    private boolean credentialsValid(String usernameInput, String emailInput, String passwordInput) {
        if (TextUtils.isEmpty(usernameInput) || TextUtils.isEmpty(emailInput) || TextUtils.isEmpty(passwordInput)) {
            Toast.makeText(SignUpActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;

        } else if (!InputValidator.isEmailValid(emailInput)) {
            Toast.makeText(SignUpActivity.this, "E-mail not valid", Toast.LENGTH_SHORT).show();
            return false;

        } else if (!InputValidator.isPasswordValid(passwordInput)) {
            Toast.makeText(SignUpActivity.this, "Password must contain at least 8 characters, 1 digit, 1 capital and one special character", Toast.LENGTH_LONG).show();
            return false;

        } else
            return true;
    }

    //2b. Start intent service and redirect to AccountActivity
    private void redirect() {
        Intent intent = new Intent(SignUpActivity.this, AccountActivity.class);
        startActivity(intent);
    }

    //Password visibility options
    public void showOrHidePassword(View view) {
        if (isHidden) {
            //Show password
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            passwordEditText.setSelection(passwordEditText.length());
            showPasswordImageView.animate().alpha(0).setDuration(0);
            hidePasswordImageView.animate().alpha(1).setDuration(0);
            isHidden = false;

        } else {
            //Hide password
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordEditText.setSelection(passwordEditText.length());
            hidePasswordImageView.animate().alpha(0).setDuration(0);
            showPasswordImageView.animate().alpha(1).setDuration(0);
            isHidden = true;
        }
    }

    //Once layout or logo are tapped, keyboard's dismissed
    public void dismissKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) SignUpActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (SignUpActivity.this.getCurrentFocus() != null && inputMethodManager != null)
            inputMethodManager.hideSoftInputFromWindow(SignUpActivity.this.getCurrentFocus().getWindowToken(), 0);
    }
}
