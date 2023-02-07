package com.example.TimeLess;

import android.content.Intent;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class CreateUser extends AppCompatActivity {
    TextView banner, rturn;
    EditText username, password, cfmpword, email;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);
        email = (EditText) findViewById(R.id.new_user_email_entry);
        username = (EditText) findViewById(R.id.new_user_username_entry);
        password = (EditText) findViewById(R.id.new_user_password_entry);
        cfmpword = (EditText) findViewById(R.id.new_user_confirm_password);
        banner = (TextView) findViewById(R.id.add_user_progress);
        TextView cancel = (TextView) findViewById(R.id.new_user_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                /*username.setText("");
                password.setText("");*/
            }
        });

        mAuth = FirebaseAuth.getInstance();

        Button create = (Button) findViewById(R.id.new_user_create);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user = username.getText().toString();
                String pword = password.getText().toString();
                String cpword = cfmpword.getText().toString();
                String eml = email.getText().toString();

                if(validate_user_input(user,pword,cpword,eml)){
                    //Create a new User in database
                    mAuth.createUserWithEmailAndPassword(eml,pword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast toast = Toast.makeText(getApplicationContext(),"New User Created",Toast.LENGTH_SHORT);
                                toast.show();
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference users_database = database.getReference("users");
                                Map<String, String> userData = new HashMap<String, String>();
                                FirebaseUser user_current = FirebaseAuth.getInstance().getCurrentUser();
                                if(user_current!=null){
                                    userData.put("email",eml);
                                    userData.put("username", user);
                                    users_database.child(user_current.getUid()).setValue(userData);
                                }
                                start_activity();
                            }
                            else {
                                Toast toast = Toast.makeText(getApplicationContext(),"New User Creation Failed",Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    });
                }
            }
        });


    }

    private void start_activity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user",username.getText().toString());
        intent.putExtra("pword",password.getText().toString());
        startActivity(intent);
    }

    private boolean validate_user_input(String uname, String pass, String cfmpass, String eml){
        int count =0;
        if (!Patterns.EMAIL_ADDRESS.matcher(eml).matches()) {
            email.setError("Please provide valid email!");
        } else {
            count +=1;
        }
        if(uname.length()>5){
            count +=1;
        } else {
            username.setError("username must be at least 6 characters long");
        }
        if(pass.length()>5){
            count+=1;
        } else {
            password.setError("password must be at least 6 characters long");
        }
        if (pass.equals(cfmpass)){
            count+=1;
        } else {
            cfmpword.setError("passwords do not match!");
        }
        if(count==4) return true;
        else return false;

    }


}
