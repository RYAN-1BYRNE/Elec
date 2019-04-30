package com.example.ryan.electronicstore;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.ProgressDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ryan.electronicstore.Common.Common;
import com.example.ryan.electronicstore.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUp extends AppCompatActivity {


    EditText idNumber,username,password,email,address;
    Button btnSignUp;
    String admin = Common.currentUser.getIsAdmin();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        idNumber = (EditText)findViewById(R.id.idNumber);
        email = (EditText)findViewById(R.id.email);
        address = (EditText)findViewById(R.id.address);

        btnSignUp = (Button)findViewById(R.id.btnSignUp);

        //Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (admin.equals("true") && Common.isConnectedToInternet(getBaseContext())) {

                    final ProgressDialog mDialog = new ProgressDialog(SignUp.this);
                    mDialog.setMessage("Please Wait...");
                    mDialog.show();

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //check if already user phone
                            if (dataSnapshot.child(idNumber.getText().toString()).exists()) {

                                mDialog.dismiss();
                                Toast.makeText(SignUp.this, "User ID in system", Toast.LENGTH_SHORT).show();

                            } else if(!dataSnapshot.child(idNumber.getText().toString()).exists()){

                                mDialog.dismiss();
                                User user = new User(username.getText().toString(), password.getText().toString(),email.getText().toString(),address.getText().toString());
                                table_user.child(idNumber.getText().toString()).setValue(user);
                                sendMessage();
                                finish();
                                Toast.makeText(SignUp.this, "Sign up Successful", Toast.LENGTH_SHORT).show();

                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else if(admin.equals("false")){

                    Toast.makeText(SignUp.this, "Only admins can create accounts", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(SignUp.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }


        });
    }
    private void sendMessage() {
        final ProgressDialog dialog = new ProgressDialog(SignUp.this);
        //dialog.setTitle("Sending Email");
        //dialog.setMessage("Please wait");
        // dialog.show();
        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GMailSender sender = new GMailSender("trc42testing@gmail.com", "Liverpool1@");
                    sender.sendMail("Elec Store ACCOUNT CREATION",
                            "Your Username is:" + username.getText().toString() + "  " + "Your password is:" + password.getText().toString() +"  "+  "Your Userid is:" + idNumber.getText().toString(),
                            "trc42testing@gmail.com",
                            email.getText().toString());
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e("mylog", "Error: " + e.getMessage());
                }
            }
        });
        sender.start();
    }



}
