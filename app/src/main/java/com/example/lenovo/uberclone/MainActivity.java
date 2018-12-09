package com.example.lenovo.uberclone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    enum State{
        SINGUP,LOGIN
    }
    private EditText edtUsername, edtPassword, edtOneTimeLogin;
    private Button btnSignUp, btnOneTime;
    private RadioGroup userType;
    private RadioButton radioPassenger,radioDriver;
    private State state;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ParseUser.getCurrentUser() !=null){

           // ParseUser.logOut();
            transactionToPassengerActivity();
            transactionToDriverRequestListActivity();
        }

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtOneTimeLogin = findViewById(R.id.edtOneTimeLogin);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnOneTime = findViewById(R.id.btnOneTime);
        userType = findViewById(R.id.userType);
        radioPassenger = findViewById(R.id.radioPassenger);
        radioDriver = findViewById(R.id.radioDriver);



        state = State.SINGUP;

        btnSignUp.setOnClickListener(MainActivity.this);
        btnOneTime.setOnClickListener(MainActivity.this);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @SuppressLint("SetTextI18n")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logIn:
                if(state == State.SINGUP){
                    state = State.LOGIN;
                    item.setTitle("Sign Up");
                    btnSignUp.setText(String.format("LOGIN"));
                }else if(state == State.LOGIN){
                    state = State.SINGUP;
                    item.setTitle("Log IN");
                    btnSignUp.setText(String.format("SIGN UP"));
                }
                break;
        }


        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnSignUp:

                if(state==State.SINGUP) {

                    if(radioDriver.isChecked() == false && radioPassenger.isChecked() == false){
                        Toast.makeText(MainActivity.this,"Are u driver or passenger",Toast.LENGTH_LONG).show();
                        return;
                    }
                    final ParseUser appUser = new ParseUser();
                    appUser.setUsername(edtUsername.getText().toString());
                    appUser.setPassword(edtPassword.getText().toString());
                    if(radioDriver.isChecked()){
                        appUser.put("as","Driver");
                    } else if(radioPassenger.isChecked()){
                        appUser.put("as","Passenger");
                    }
                    final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Signing up" + edtUsername.getText().toString());
                    progressDialog.show();
                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(MainActivity.this,
                                        appUser.get("username") + " is signed up successfully",
                                        Toast.LENGTH_LONG).show();
                                transactionToPassengerActivity();
                                transactionToDriverRequestListActivity();
                                }
                            }
                        });
                    progressDialog.dismiss();
                }else if(state==State.LOGIN){
                    ParseUser.logInInBackground(edtUsername.getText().toString(), edtPassword.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if(user !=null && e ==null){
                                Toast.makeText(MainActivity.this,
                                        "User logged in",Toast.LENGTH_LONG).show();
                                transactionToPassengerActivity();
                                transactionToDriverRequestListActivity();
                            }
                        }
                    });
                }
            break;
            case R.id.btnOneTime:
                if(edtOneTimeLogin.getText().toString().equals("Driver") ||
                        edtOneTimeLogin.getText().toString().equals("Passenger")){
                    if(ParseUser.getCurrentUser()==null){
                        ParseAnonymousUtils.logIn(new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if(user!=null && e==null){
                                    Toast.makeText(MainActivity.this,
                                            "We have an anoynmus user",Toast.LENGTH_SHORT).show();
                                    user.put("as",edtOneTimeLogin.getText().toString());
                                    user.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            transactionToPassengerActivity();
                                            transactionToDriverRequestListActivity();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }else{
                    Toast.makeText(MainActivity.this,
                            "Are u driver or passenger",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void transactionToPassengerActivity(){

        if(ParseUser.getCurrentUser() !=null){
            if(ParseUser.getCurrentUser().get("as").equals("Passenger")){
                Intent intent = new Intent(MainActivity.this,PassengerActivity.class);
                startActivity(intent);
            }
        }

    }
    public void transactionToDriverRequestListActivity(){
        if(ParseUser.getCurrentUser() !=null) {
            if (ParseUser.getCurrentUser().get("as").equals("Driver")){
                Intent intent = new Intent(MainActivity.this, DriverRequestListActivity.class);
                startActivity(intent);
            }

        }
    }
}


