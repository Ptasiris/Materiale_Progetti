package com.example.melanomaly;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    TextView tv_msg_login;
    EditText et_mail;
    EditText et_password;
    Button btn_accedi;
    private DB_Helper myDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tv_msg_login=findViewById(R.id.scritta_login);
        et_mail=findViewById(R.id.login_mail);
        et_password=findViewById(R.id.login_password);
        btn_accedi=findViewById(R.id.login_btn);

        myDB=new DB_Helper(this);



        btn_accedi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail=et_mail.getText().toString();
                String password=et_password.getText().toString();

                if (mail.isEmpty() || password.isEmpty()){
                    Toast.makeText(getApplicationContext(),"ci sono dei campi vuoti!",Toast.LENGTH_SHORT).show();
                }

                else{
                    Cursor cursor= myDB.checkloginUtente(mail,password);

                    if (cursor.getCount()==1){
                        cursor.moveToFirst();

                        int idx_id=cursor.getColumnIndex(T_JAM_UTENTE.T_JAM_UTENTE_ID);
                        int idx_nome=cursor.getColumnIndex(T_JAM_UTENTE.T_JAM_UTENTE_NOME);
                        int idx_cognome=cursor.getColumnIndex(T_JAM_UTENTE.T_JAM_UTENTE_COGNOME);
                        int idx_eta=cursor.getColumnIndex(T_JAM_UTENTE.T_JAM_UTENTE_ETA);
                        int idx_mail=cursor.getColumnIndex(T_JAM_UTENTE.T_JAM_UTENTE_MAIL);
                        int idx_password=cursor.getColumnIndex(T_JAM_UTENTE.T_JAM_UTENTE_PASSWORD);

                        Integer label_id=cursor.getInt(idx_id);
                        String label_nome=cursor.getString(idx_nome);
                        String label_cognome=cursor.getString(idx_cognome);
                        Integer label_eta=cursor.getInt(idx_eta);
                        String label_mail=cursor.getString(idx_mail);
                        String label_password=cursor.getString(idx_password);

                        cursor.close();

                        Intent i_log= new Intent(LoginActivity.this,MainActivity.class);
                        i_log.putExtra("login_id",label_id);
                        i_log.putExtra("login_nome",label_nome);
                        i_log.putExtra("login_cognome",label_cognome);
                        i_log.putExtra("login_eta",label_eta);
                        i_log.putExtra("login_mail",label_mail);
                        i_log.putExtra("login_password",label_password);
                        startActivity(i_log);
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"utente non registrato",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}