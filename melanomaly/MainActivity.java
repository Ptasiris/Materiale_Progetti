package com.example.melanomaly;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView et_msg_home;
    private Button btn_new_acq;
    private Button btn_old_acq;
    //private DBHelper myDB;

    private Integer id;
    private String nome;
    private String cognome;
    private Integer eta;
    private String mail;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_msg_home=findViewById(R.id.home_msg);
        btn_new_acq=findViewById(R.id.new_acq);
        btn_old_acq=findViewById(R.id.old_acq);
/*
        myDB=new DBHelper(this);
        if (myDB.updateEta(27,"adriano.nisi12@gmail.com")){
            Log.d("modifica", "record modificato");
        }
        else{
            Log.d("modifica", "errore");
        }
 */

/*
        Cursor cursor =myDB.selectLook();
        int n= cursor.getCount();
        cursor.moveToFirst();
        for (int i = 1; i<=n; i++){
            int idx_n=cursor.getColumnIndex(T_JAM_UTENTE.T_JAM_UTENTE_NOME);
            int idx_c=cursor.getColumnIndex(T_JAM_UTENTE.T_JAM_UTENTE_COGNOME);
            int idx_e=cursor.getColumnIndex(T_JAM_UTENTE.T_JAM_UTENTE_ETA);

            String label_n=cursor.getString(idx_n);
            String label_c=cursor.getString(idx_c);
            int label_e=cursor.getInt(idx_e);

            Log.d("record"+ i, label_n+" "+label_c+" "+label_e);
            cursor.moveToNext();
        }
        cursor.close();
*/

        Intent intent = getIntent();
        id=intent.getIntExtra("login_id",0);
        nome=intent.getStringExtra("login_nome");
        cognome=intent.getStringExtra("login_cognome");
        eta= intent.getIntExtra("login_eta",0);
        mail=intent.getStringExtra("login_mail");
        password=intent.getStringExtra("login_password");
        //InputType.TYPE_CLASS_NUMBER  è una costante che vale 2

        String a="Benvenuto/a "+nome+" "+cognome+"!";
        et_msg_home.setText(a);

        btn_new_acq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fragment transaction
                changeFragment(new CameraFragment());
            }
        });

        btn_old_acq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fragment transaction
                changeFragment(new PastImagesFragment());
            }
        });
    }
    private void changeFragment(Fragment fragment) {

        Bundle bundle=new Bundle();
        bundle.putInt("id_key",id);
        bundle.putString("mail_key",mail);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_default,fragment).addToBackStack(null).commit();
    }
}