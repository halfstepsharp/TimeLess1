package com.example.TimeLess;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    //Contents of the login page
    private EditText Email;
    private EditText Password;
    private Button login_button;
    private String email, password;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Email = (EditText) findViewById(R.id.email);
        Password = (EditText) findViewById(R.id.password);
        login_button = (Button) findViewById(R.id.loginbutton);

        mAuth = FirebaseAuth.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.purpledark));
        }

        //Check if login credentials match or not
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uEmail = Email.getText().toString();
                String uPword = Password.getText().toString();
                if (validate_emailpassword(uEmail, uPword) == true) {
                    authenticate_user(Email.getText().toString(), Password.getText().toString());
                    //startActivity(new Intent(getApplicationContext(), Slidermenu.class));
                }
            }
        });


        //Create a new Account
        TextView new_account = (TextView) findViewById(R.id.create_new_account);
        new_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateUser.class);
                startActivity(intent);
            }
        });


        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            email = extras.getString("user");
            if(email!=null){
                Email.setText(email);
            }
            password = extras.getString("pword");
            if(password!=null){
                Password.setText(password);
            }
        }
        FirebaseUser current_user = mAuth.getCurrentUser();
        updateUI(current_user);
    }

    private boolean validate_emailpassword(String uEmail, String uPword) {
        int count =0;
        if (!Patterns.EMAIL_ADDRESS.matcher(uEmail).matches()) {
            Email.setError("Please provide valid email!");
        } else {
            count +=1;
        }
        if(uPword.length()>5 || !(uPword.isEmpty())){
            count+=1;
        } else {
            Password.setError("password must be at least 6 characters long");
        }
        if(count==2) return true;
        else return false;
    }

    private void authenticate_user (String email, String password){
        //Needs to query database here for password and username

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference usersDatabase = database.getReference("users");
                    usersDatabase.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String username = dataSnapshot.child("username").getValue().toString();
                                // Do something with the username
                                SharedPreferences prefs = getSharedPreferences("TimeLess", MODE_PRIVATE);
                                prefs.edit().putString("username", username).commit();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(), "no username found!", Toast.LENGTH_SHORT).show();
                            Email.setText("no username found");
                        }
                    });
                    String username = user.getDisplayName();
                    updateUI(user);
                    Toast.makeText(getApplicationContext(), "Authentication succeeded.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, Slidermenu.class);
                    //intent.putExtra("username",username);
                    startActivity(intent);
                }
                else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    //Password.setText("Incorrect Password");
                }
            }
        });
    }
    public String return_username(){
        return email;
    }

    private void updateUI(FirebaseUser user) {
        if(user!=null){
            if(user.isEmailVerified()){
                Email.setText((user.getEmail()));
                Password.setText(user.getUid());
                Intent intent = new Intent(MainActivity.this, Slidermenu.class);
                intent.putExtra("username",this.email);
                Toast.makeText(getApplicationContext(), "User has previously \nlogged in", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }

        }
    }

}
