package com.example.melanomaly;

import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;

public class CameraFragment extends Fragment {
    private static final String CAMERAFRAGMENT = "CAMERAFRAGMENT";
    private static final String LOADED = "LOADED";
    private static final String SALVATAGGIO_FILE = "SALVATAGGIO_FILE";
    private static final String CONVERSIONE_FILE = "CONVERSIONE_FILE";
    private View v;
    private Button btn_acquire;
    private int id;
    int Camera_code=101;
    int Permission_Camera_Code=102;
    private ImageView img_cam;
    private Bitmap bitmap;
    //private Mat mat;
    private ImageClassification imageClassification;
    private InfoSave infoSave=new InfoSave(0,0.0f);
    private DB_Helper myDB;
    private byte[] image_converted_to_byte_array;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_camera, container, false);

        if (OpenCVLoader.initLocal()) {
            Log.d(LOADED, "OpenCV funziona ");}
        else {Log.d(LOADED, "Errore!");}

        try {
            int inputSize=256;
            imageClassification=new ImageClassification(getActivity().getAssets(),"model.tflite",inputSize);
            Log.d(CAMERAFRAGMENT, "Modello caricato correttamente!");
        }
        catch (IOException e) {e.printStackTrace();}

        myDB=new DB_Helper(getActivity().getApplicationContext());

        if (getArguments() != null) {
            id = getArguments().getInt("id_key");
        }
        //qui ci dovrei mettere la chiamata a controlla score poichè un'immagine l'ho già inserita
/*
        Cursor cursor= myDB.controllaScore(id);
        int n= cursor.getCount();
        cursor.moveToFirst();
        for (int i = 1; i<=n; i++){
            int idx_pk=cursor.getColumnIndex(T_JAM_IMAGO.T_JAM_IMAGO_ID);
            int idx_sc=cursor.getColumnIndex(T_JAM_IMAGO.T_JAM_IMAGO_SCORE);
            int idx_fk=cursor.getColumnIndex(T_JAM_IMAGO.T_JAM_UTENTE_ID);

            int label_pk=cursor.getInt(idx_pk);
            float label_sc=cursor.getFloat(idx_sc);
            int label_fk=cursor.getInt(idx_fk);

            Log.d("record"+ i, label_pk+" "+label_sc+" "+label_fk);
            cursor.moveToNext();
        }
        cursor.close();
*/
        infoSave.setUser_id(id);
        if (id>=0){Log.d("id_ricevuto", "id= "+id);}

        getPermission();

        btn_acquire=v.findViewById(R.id.btn_acquire);
        img_cam=v.findViewById(R.id.imageView_camera);

        btn_acquire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,Camera_code);
            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Camera_code && data!=null){
            bitmap=(Bitmap) data.getExtras().get("data");
            img_cam.setImageBitmap(bitmap);
            AsyncImage asyncImage=new AsyncImage(img_cam,imageClassification,myDB,infoSave);
            asyncImage.execute(img_cam);
        }
    }

    public void getPermission(){
        if (checkSelfPermission(requireContext(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] {Manifest.permission.CAMERA},Permission_Camera_Code);

        }
    }

    public void onRequestPermissionsResult(int RequestCode, @NotNull String[] permissions, @NotNull int[] grantResults){
        super.onRequestPermissionsResult(RequestCode,permissions,grantResults);
        if(RequestCode==Permission_Camera_Code && grantResults.length>0){
            if (grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                getPermission();
            }
        }
    }

}