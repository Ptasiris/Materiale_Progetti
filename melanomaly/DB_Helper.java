package com.example.melanomaly;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.Nullable;

public class DB_Helper extends SQLiteOpenHelper {

    public static final String DB_NAME="NEO_DB";

    private SQLiteDatabase readableDB,writableDB;

    private static final String q_utente="CREATE TABLE "+T_JAM_UTENTE.T_JAM_UTENTE_TABLE_NAME+
            " ( "+T_JAM_UTENTE.T_JAM_UTENTE_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
            T_JAM_UTENTE.T_JAM_UTENTE_NOME+" VARCHAR2(50) NOT NULL,"+
            T_JAM_UTENTE.T_JAM_UTENTE_COGNOME+" VARCHAR2(50) NOT NULL,"+
            T_JAM_UTENTE.T_JAM_UTENTE_ETA+" INTEGER NOT NULL,"+
            T_JAM_UTENTE.T_JAM_UTENTE_MAIL+" VARCHAR2(50) NOT NULL,"+
            T_JAM_UTENTE.T_JAM_UTENTE_PASSWORD+" VARCHAR2(50) NOT NULL,"+
            "CHECK ( 0<="+T_JAM_UTENTE.T_JAM_UTENTE_ETA+"<=120))";

    private static final String q_imago="CREATE TABLE "+T_JAM_IMAGO.T_JAM_IMAGO_TABLE_NAME+
            " ( "+T_JAM_IMAGO.T_JAM_IMAGO_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
            T_JAM_IMAGO.T_JAM_IMAGO_IMMAGINE+" BLOB,"+
            T_JAM_IMAGO.T_JAM_IMAGO_SCORE+" FLOAT DEFAULT 0.0,"+
            T_JAM_IMAGO.T_JAM_UTENTE_ID+" INTEGER,"+
            "CONSTRAINT FK_UTENTE_IMAGO FOREIGN KEY ("+T_JAM_IMAGO.T_JAM_UTENTE_ID+") REFERENCES "+
            T_JAM_UTENTE.T_JAM_UTENTE_TABLE_NAME+" ("+T_JAM_UTENTE.T_JAM_UTENTE_ID+"))";

    public DB_Helper(@Nullable Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(q_utente);
        db.execSQL(q_imago);
        readableDB=null;
        writableDB=null;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public boolean insertUtente(String nome,String cognome, Integer eta, String mail, String password){
        ContentValues cv=new ContentValues();
        cv.put(T_JAM_UTENTE.T_JAM_UTENTE_NOME,nome);
        cv.put(T_JAM_UTENTE.T_JAM_UTENTE_COGNOME,cognome);
        cv.put(T_JAM_UTENTE.T_JAM_UTENTE_ETA,eta);
        cv.put(T_JAM_UTENTE.T_JAM_UTENTE_MAIL,mail);
        cv.put(T_JAM_UTENTE.T_JAM_UTENTE_PASSWORD,password);
        if (writableDB==null){
            writableDB=getWritableDatabase();
        }

        if(duplicati(mail).getCount()>0){
            return false;
        }
        else{
            return(writableDB.insert(T_JAM_UTENTE.T_JAM_UTENTE_TABLE_NAME,null,cv)>0);
        }
    }

    public Cursor duplicati(String mail){
        String q="SELECT "+T_JAM_UTENTE.T_JAM_UTENTE_MAIL+" FROM "+T_JAM_UTENTE.T_JAM_UTENTE_TABLE_NAME+
                " WHERE "+T_JAM_UTENTE.T_JAM_UTENTE_MAIL+"= ?";

        if(readableDB==null){
            readableDB=getReadableDatabase();
        }
        Cursor cursor=readableDB.rawQuery(q, new String[] {mail});
        return cursor;
    }

    public Cursor checkloginUtente(String mail, String password){
        String q="SELECT * FROM "+T_JAM_UTENTE.T_JAM_UTENTE_TABLE_NAME+
                " WHERE "+T_JAM_UTENTE.T_JAM_UTENTE_MAIL+"= ?"+
                " AND "+T_JAM_UTENTE.T_JAM_UTENTE_PASSWORD+"= ?";

        if(readableDB==null){
            readableDB=getReadableDatabase();
        }
        Cursor cursor=readableDB.rawQuery(q, new String[] {mail,password});
        return cursor;
    }

    public boolean insertSalvaImmagine(byte[] img, int id,float score){
        //devo aggiungere un terzo parametro in input che è lo score, ma prima devo aggiungerlo nella create
        ContentValues cv=new ContentValues();
        cv.put(T_JAM_IMAGO.T_JAM_IMAGO_IMMAGINE, img);
        cv.put(T_JAM_IMAGO.T_JAM_IMAGO_SCORE,score);
        cv.put(T_JAM_IMAGO.T_JAM_UTENTE_ID,id);

        if (writableDB==null){
            writableDB=getWritableDatabase();
        }
        return (writableDB.insert(T_JAM_IMAGO.T_JAM_IMAGO_TABLE_NAME,null,cv)>0);
    }

    public Cursor prelevaDati(Integer id_utente){
        String q="SELECT "+T_JAM_IMAGO.T_JAM_IMAGO_ID+","+
                T_JAM_IMAGO.T_JAM_IMAGO_SCORE+","+
                T_JAM_IMAGO.T_JAM_IMAGO_IMMAGINE+
                " FROM "+T_JAM_IMAGO.T_JAM_IMAGO_TABLE_NAME+
                " WHERE "+T_JAM_IMAGO.T_JAM_UTENTE_ID+"= ?";

        if(readableDB==null) {
            readableDB = getReadableDatabase();
        }

        Cursor cursor=readableDB.rawQuery(q,new String[] {id_utente.toString()});
        return cursor;
    }

    //da qui in poi scrivo dei metodi che non servono per l'app finale

    // questo metodo non serve per l'app finale
    public Cursor selectLook (){
        String q="SELECT "+ T_JAM_UTENTE.T_JAM_UTENTE_NOME+","+
                T_JAM_UTENTE.T_JAM_UTENTE_COGNOME+","+
                T_JAM_UTENTE.T_JAM_UTENTE_ETA+
                " FROM "+T_JAM_UTENTE.T_JAM_UTENTE_TABLE_NAME;

        if(readableDB==null){
            readableDB=getReadableDatabase();
        }

        return readableDB.rawQuery(q,new String[] {});
    }

    // questo metodo non serve per l'app finale
    public boolean updateEta(Integer eta,String mail){
        ContentValues cv=new ContentValues();
        cv.put(T_JAM_UTENTE.T_JAM_UTENTE_ETA,eta);
        if (writableDB==null){
            writableDB=getWritableDatabase();
        }
        return(writableDB.update(T_JAM_UTENTE.T_JAM_UTENTE_TABLE_NAME,cv,T_JAM_UTENTE.T_JAM_UTENTE_MAIL+"=?",new String[] {mail})>0);
    }

    // questo metodo non serve per l'app finale
    //mi serve un metodo per controllare
    // che le nuove modifiche alla tabella delle immagini siano corrette
    // dovrebbe essere:
    public Cursor controllaScore(Integer id_utente){
        String q="SELECT "+T_JAM_IMAGO.T_JAM_IMAGO_ID+","+
                T_JAM_IMAGO.T_JAM_IMAGO_SCORE+","+
                T_JAM_IMAGO.T_JAM_UTENTE_ID+
                " FROM "+T_JAM_IMAGO.T_JAM_IMAGO_TABLE_NAME+
                " WHERE "+T_JAM_IMAGO.T_JAM_UTENTE_ID+"= ?";

        if(readableDB==null){
            readableDB=getReadableDatabase();
        }

        Cursor cursor=readableDB.rawQuery(q,new String[] {id_utente.toString()});
        return cursor;
    }

}
