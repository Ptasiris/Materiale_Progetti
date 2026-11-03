package com.example.melanomaly;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegistrationActivity extends AppCompatActivity {
    private TextView tv_reg_msg;
    private EditText et_reg_nome;
    private EditText et_reg_cognome;
    private EditText et_reg_eta;
    private EditText et_reg_mail;
    private EditText et_reg_password;
    private Button btn_registra;
    private DB_Helper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        tv_reg_msg = findViewById(R.id.registration_msg);
        et_reg_nome = findViewById(R.id.et_nome);
        et_reg_cognome = findViewById(R.id.et_cognome);
        et_reg_eta = findViewById(R.id.et_eta);
        et_reg_mail = findViewById(R.id.et_mail);
        et_reg_password = findViewById(R.id.et_password);
        btn_registra = findViewById(R.id.btn_done);

        myDB=new DB_Helper(this);

        btn_registra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nome=et_reg_nome.getText().toString();
                String cognome=et_reg_cognome.getText().toString();
                Integer eta=Integer.parseUnsignedInt("+"+et_reg_eta.getText().toString());
                String mail=et_reg_mail.getText().toString();
                String password=et_reg_password.getText().toString();

                //.getInputType() mi da in output InputType.TYPE_CLASS_NUMBER  che è una costante che vale 2

                if(nome.isEmpty() || cognome.isEmpty() || eta.toString().isEmpty() || eta>120 || mail.isEmpty() || password.isEmpty()){
                    Toast.makeText(getApplicationContext(),"inserire tutti i campi con valori coerenti!",Toast.LENGTH_SHORT).show();
                }

                else {

                    if (myDB.insertUtente(nome,cognome,eta,mail,password)) {
                        Toast.makeText(getApplicationContext(), "inserimento riuscito!", Toast.LENGTH_SHORT).show();}
                    else{
                        Toast.makeText(getApplicationContext(), "mail già usata!", Toast.LENGTH_LONG).show();}

                    Intent ireg= new Intent(RegistrationActivity.this,LoginActivity.class);
                    startActivity(ireg);
                }
            }
        });
    }
}