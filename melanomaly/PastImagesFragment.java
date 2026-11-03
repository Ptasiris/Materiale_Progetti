package com.example.melanomaly;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.graphics.Bitmap;
import android.content.Context;

import com.example.melanomaly.placeholder.PlaceholderContent;


public class PastImagesFragment extends Fragment implements AvvisaMedico {
    private static final String NUMERO_DI_RECORD = "NUMERO_DI_RECORD";
    private static final String SAVE_IMAGE_TO_INTERNAL_STORAGE = "SAVE_IMAGE_TO_INTERNAL_STORAGE";
    private static final String PERCORSO_DEL_FILE = "PERCORSO_DEL_FILE";
    private static final String URI_TAG = "URI_TAG";
    private static final String DELETE_FROM_INTERNAL_STORAGE = "DELETE_FROM_INTERNAL_STORAGE";
    private int id;
    private String mail;
    private View view;
    private DB_Helper myDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            id = getArguments().getInt("id_key");
            mail = getArguments().getString("mail_key");
        }
        //myDB=new DB_Helper(getActivity().getApplicationContext());
        //prelevatoreDiDati(myDB);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myDB=new DB_Helper(getActivity().getApplicationContext());
        prelevatoreDiDati(myDB);

        view = inflater.inflate(R.layout.fragment_past_images_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setAdapter(new MyItemRecyclerViewAdapter(context, PlaceholderContent.ITEMS,this));
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
        return view;
    }


    public void onDestroyView(){
        super.onDestroyView();
        PlaceholderContent.ITEMS.clear();
    }
/*
    @Override
    public void onDestroy() {
        super.onDestroy();
        PlaceholderContent.ITEMS.clear();
    }
*/

    public void prelevatoreDiDati(DB_Helper db){

        //qui ci metti la query che ti da il cursore coi dati relativi all'utente con quell'id
        Cursor cursor=db.prelevaDati(id);

        //qui metti i dati in ITEMS
        int n = cursor.getCount();
        Log.d(NUMERO_DI_RECORD, "prelevatoreDiDati: ho trvato "+n+" record");
        cursor.moveToFirst();

        for (int i = 1; i<=n; i++){
            int idx_id_imago=cursor.getColumnIndex(T_JAM_IMAGO.T_JAM_IMAGO_ID);
            int idx_score=cursor.getColumnIndex(T_JAM_IMAGO.T_JAM_IMAGO_SCORE);
            int idx_imago=cursor.getColumnIndex(T_JAM_IMAGO.T_JAM_IMAGO_IMMAGINE);

            int label_id_imago=cursor.getInt(idx_id_imago);
            float label_score=cursor.getFloat(idx_score);
            byte[] label_imago=cursor.getBlob(idx_imago);
            int int_icon=R.drawable.ic_send_to_mobile;

            PlaceholderContent.ITEMS.add(new PlaceholderContent.PlaceholderItem(label_id_imago,label_score,BitmapFactory.decodeByteArray(label_imago,0,label_imago.length),int_icon));
            cursor.moveToNext();
        }
        cursor.close();
    }

    @Override
    public void onIconClick(int pos, Bitmap bitmap){

        //devo prima salvare la bitmap nella memoria interna
        saveImageToInternalStorage(getContext(), bitmap, "neo_in_questione.png");
        File file = new File(getActivity().getFilesDir(), "neo_in_questione.png");
        Log.d(PERCORSO_DEL_FILE, "onIconClick: Percorso del file: " + file.getAbsolutePath());

        //devo creare un FileProvider nel manifest
        Uri uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
        Log.d(URI_TAG, "onIconClick: Uri creata, "+uri.toString());

        //creo l'intent implicito
        Intent medicalIntent=new Intent(Intent.ACTION_SEND);
        medicalIntent.setType("image/*");

        medicalIntent.putExtra(Intent.EXTRA_SUBJECT,"Melanomaly: richiesta controllo neo");
        medicalIntent.putExtra(Intent.EXTRA_TEXT,"Le invio la foto di un neo. Mi faccia sapere se va bene o no");
        medicalIntent.putExtra(Intent.EXTRA_STREAM,uri);
        startActivity(Intent.createChooser(medicalIntent,"Invia email tramite"));
    }

    public void saveImageToInternalStorage(Context context, Bitmap bitmap, String fileName) {
        FileOutputStream fos = null;
        try {
            // Creo un file nella directory dei file interni dell'app
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();

            Log.d(SAVE_IMAGE_TO_INTERNAL_STORAGE, "Immagine salvata con successo: " + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //non uso questo metodo nel progetto
    public void deleteFileFromInternalStorage(String fileName) {
        boolean deleted = getActivity().deleteFile(fileName);
        if (deleted) {
            Log.d(DELETE_FROM_INTERNAL_STORAGE, "File cancellato correttamente: "+fileName);
        } else {
            Log.d(DELETE_FROM_INTERNAL_STORAGE, "Impossibile cancellare il file: " + fileName);
        }
    }

}