package com.example.melanomaly;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;

public class FirstActivity extends AppCompatActivity {
    TextView tv_first;
    Button btn_first_register;
    Button btn_first_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        tv_first = findViewById(R.id.msg1);
        btn_first_register = findViewById(R.id.reg1);
        btn_first_login = findViewById(R.id.login1);

        if (OpenCVLoader.initLocal()){
            Log.d("loaded", "OpenCV funziona ");
        }
        else {
            Log.d("loaded", "Errore!");
        }

        btn_first_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i_reg= new Intent(FirstActivity.this,RegistrationActivity.class);
                startActivity(i_reg);
            }
        });
        btn_first_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i_login=new Intent(FirstActivity.this,LoginActivity.class);
                startActivity(i_login);
            }
        });
    }
}