package com.example.woo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {
    private Button mRegister;
    private EditText mEmail,mPassword,mName,mCheckPassword;
    private RadioGroup mRadioGroup;
    private FirebaseAuth mAuth;
    private TextView mAge;
    private int Age;
    private String BDate;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user !=null){
                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }
            };

        mRegister = (Button)findViewById(R.id.register);
        mEmail = (EditText)findViewById(R.id.email);
        mPassword=(EditText)findViewById(R.id.password);
        mName = (EditText)findViewById(R.id.name);
        mCheckPassword= (EditText)findViewById(R.id.checkPassword);
        mAge = (TextView)findViewById(R.id.age);
        mRadioGroup = (RadioGroup)findViewById(R.id.radioGroup);

        mAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dateDialog = new DatePickerDialog(view.getContext(),datePickerListner,mYear,mMonth,mDay);
                dateDialog.getDatePicker().setMaxDate(new Date().getTime());
                dateDialog.show();

            }
        });
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectID = mRadioGroup.getCheckedRadioButtonId();
                final RadioButton radioButton = (RadioButton) findViewById(selectID);
                if (radioButton.getText() == null) {
                    return;
                }
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String name = mName.getText().toString();
                if (password.equals(mCheckPassword.getText().toString())) {
                    if (Age >= 18) {
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(RegistrationActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                                } else {
                                    String userId = mAuth.getCurrentUser().getUid();
                                    DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                                    Map userInfo = new HashMap<>();
                                    userInfo.put("name", name);
                                    userInfo.put("sex", radioButton.getText().toString());
                                    userInfo.put("Age", Integer.toString(Age));
                                    userInfo.put("Birth Date", BDate);
                                    userInfo.put("profileImageUrl", "default");
                                    currentUserDb.updateChildren(userInfo);

                                }
                            }
                        });
                    }else{
                        Toast.makeText(RegistrationActivity.this,"Age not valid",Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(RegistrationActivity.this,"Check Your Password",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private DatePickerDialog.OnDateSetListener datePickerListner = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR,year);
            c.set(Calendar.MONTH,month);
            c.set(Calendar.DAY_OF_MONTH,day);
            String format = new SimpleDateFormat("dd MMM yyyy").format(c.getTime());
            mAge.setText(format);
            BDate = format;
            Age = calculateAge(c.getTimeInMillis());
            Toast.makeText(RegistrationActivity.this, Integer.toString(Age),Toast.LENGTH_SHORT).show();
        }
    };
    int calculateAge(long date){
        Calendar dob = Calendar.getInstance();
        dob.setTimeInMillis(date);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR)-dob.get(Calendar.YEAR);
        if(today.get(Calendar.DAY_OF_YEAR)< dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }
        return age;
    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }
}
