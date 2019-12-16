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
import android.widget.TextView;
import android.widget.Toast;

import com.breiter.chatter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView signUpTextView;
    private TextView resetPasswordTextView;
    private boolean isHidden = true;
    private ImageView showPasswordImageView;
    private ImageView hidePasswordImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Log in");

        bindViews(); //1

        //If user is logged redirect to AccountActivity
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    startWithCurrentUser(); //2
                }
            }
        };

        //When login button is clicked, retrieve current user from Firebase & redirect
        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String emailInput = emailEditText.getText().toString().trim();
                String passwordInput = passwordEditText.getText().toString().trim();

                if (credentialsValid(emailInput, passwordInput)) {
                    mAuth.signInWithEmailAndPassword(emailInput, passwordInput)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful())
                                        startWithCurrentUser();
                                    else
                                        Toast.makeText(LoginActivity.this, "Authentication failed!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        //When SignUp is clicked, redirect to SignupActivity
        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        //When ResetPassword is clicked, redirect to ResetPasswordActivity
        resetPasswordTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    //1
    private void bindViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.resetButton);
        signUpTextView = findViewById(R.id.signUpTextView);
        resetPasswordTextView = findViewById(R.id.resetPasswordTextView);
        showPasswordImageView = findViewById(R.id.showPasswordImageView);
        hidePasswordImageView = findViewById(R.id.hidePasswordImageView);
    }

    //2. Start intent service and redirect to AccountActivity
    private void startWithCurrentUser() {
        Intent intent = new Intent(LoginActivity.this, AccountActivity.class);
        startActivity(intent);
    }

    //In order to login, check, if email or password inputs aren't empty
    private boolean credentialsValid(String emailInput, String passwordInput) {
        if (TextUtils.isEmpty(emailInput) || TextUtils.isEmpty(passwordInput)) {
            Toast.makeText(LoginActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        } else
            return true;

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
        InputMethodManager inputMethodManager = (InputMethodManager) LoginActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (LoginActivity.this.getCurrentFocus() != null && inputMethodManager != null)
            inputMethodManager.hideSoftInputFromWindow(LoginActivity.this.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

}
