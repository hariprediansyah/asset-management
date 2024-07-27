package com.rama.myasset;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        TextInputEditText txtUsername = findViewById(R.id.txtUsername);
        TextInputEditText txtName = findViewById(R.id.txtName);
        TextInputEditText txtPassword = findViewById(R.id.txtPassword);
        TextInputEditText txtConfirmPassword = findViewById(R.id.txtConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);

        if (txtUsername.getText().toString().isEmpty() ||
            txtName.getText().toString().isEmpty() ||
            txtPassword.getText().toString().isEmpty() ||
            txtConfirmPassword.getText().toString().isEmpty()){
            btnRegister.setEnabled(false);
        }else {
            btnRegister.setEnabled(true);
        }
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtUsername.getText().toString().isEmpty() ||
                        txtName.getText().toString().isEmpty() ||
                        txtPassword.getText().toString().isEmpty() ||
                        txtConfirmPassword.getText().toString().isEmpty()){
                    btnRegister.setEnabled(false);
                }else {
                    btnRegister.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        txtUsername.addTextChangedListener(textWatcher);
        txtName.addTextChangedListener(textWatcher);
        txtPassword.addTextChangedListener(textWatcher);
        txtConfirmPassword.addTextChangedListener(textWatcher);


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!txtPassword.getText().toString().equals(txtConfirmPassword.getText().toString())){
                    AppCode.showAlert("Confirm password tidak sama", RegisterActivity.this);
                    return;
                }
                ProgressHelper.showDialog(RegisterActivity.this, "Loading ...");

                btnRegister.setEnabled(false);
                HashMap<String, Object> user = new HashMap<>();
                user.put("username", txtUsername.getText().toString());
                user.put("name", txtName.getText().toString());
                user.put("password", txtPassword.getText().toString());
                user.put("profile_pic", null);
                user.put("register_date", FieldValue.serverTimestamp());

                db.collection("users").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        db.collection("users").document(documentReference.getId())
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User user = documentSnapshot.toObject(User.class);
                                user.setId(documentSnapshot.getId());
                                App.user = user;

                                ProgressHelper.dismissDialog();
                                Toast.makeText(RegisterActivity.this, "Success Register", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                ProgressHelper.dismissDialog();
                                Toast.makeText(RegisterActivity.this, "Register Failed", Toast.LENGTH_SHORT).show();
                                btnRegister.setEnabled(true);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ProgressHelper.dismissDialog();
                        Toast.makeText(RegisterActivity.this, "Register Failed", Toast.LENGTH_SHORT).show();
                        btnRegister.setEnabled(true);
                    }
                });
            }
        });
    }
}