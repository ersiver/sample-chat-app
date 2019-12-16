package com.breiter.chatter.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.breiter.chatter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    private EditText emailEditText;
    private Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Reset Password");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.emailEditText);
        resetButton = findViewById(R.id.resetButton);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailInput = emailEditText.getText().toString().trim();

                if (emailInput.equals(""))
                    Toast.makeText(ResetPasswordActivity.this, "Email address is required",
                            Toast.LENGTH_SHORT).show();
                else {
                    mAuth.sendPasswordResetEmail(emailInput).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ResetPasswordActivity.this,
                                        "Please, check your mail box", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                startActivity(intent);
                            } else
                                Toast.makeText(ResetPasswordActivity.this,
                                       "Something went wrong...", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
    }

    //Dismiss keyboard once layout or logo are tapped
    public void dismissKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) ResetPasswordActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (ResetPasswordActivity.this.getCurrentFocus() != null && inputMethodManager != null)
            inputMethodManager.hideSoftInputFromWindow(ResetPasswordActivity.this.getCurrentFocus().getWindowToken(), 0);
    }
}
