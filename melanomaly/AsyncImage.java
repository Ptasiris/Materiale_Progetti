package com.example.melanomaly;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

public class AsyncImage extends AsyncTask<ImageView,Void, Mat> {
    private static final String BITMAP_ERROR = "BITMAP_ERROR";
    private static final String SALVATAGGIO_FILE = "SALVATAGGIO_FILE";
    private static final String CONVERSIONE_FILE = "CONVERSIONE_FILE";
    private WeakReference<ImageView> iv;
    private WeakReference<ImageClassification> imageClassificationWeakReference;
    private WeakReference<DB_Helper> myDBWeakReference;
    private WeakReference<InfoSave> infoSaveWeakReference;




    public AsyncImage(ImageView imageView,ImageClassification imageClassification,DB_Helper db_helper,InfoSave infoSave) {
        iv=new WeakReference<>(imageView);
        imageClassificationWeakReference=new WeakReference<>(imageClassification);
        myDBWeakReference=new WeakReference<>(db_helper);
        infoSaveWeakReference=new WeakReference<>(infoSave);

    }

    @Override
    protected void onPreExecute() {super.onPreExecute();}

    @Override
    protected Mat doInBackground(ImageView... imageViews) {
        ImageView imageView=imageViews[0];
        Drawable drawable = imageView.getDrawable();
        Bitmap bitmap = null;

        //data l'ImageView, ottengo la bitmap
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
            Log.d(BITMAP_ERROR, "doInBackground: bitmap ottenuta!");
        }
        else {Log.d(BITMAP_ERROR, "doInBackground: non è possibile ottenere la bitmap!");}

        //data la bitmap, ottengo la Mat
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);

        //riconoscimento tramite rete neurale
        mat=imageClassificationWeakReference.get().recognizeImage(mat);
        infoSaveWeakReference.get().setScore(imageClassificationWeakReference.get().getVal_prediction());
        return mat;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Mat mat) {
        super.onPostExecute(mat);
        //ricorda che devi fare delle conversioni
        //1- passare da mat a bitmap
        //2- associare la bitmap all'imageView
        Bitmap bitmap =null;
        bitmap=Bitmap.createBitmap(mat.cols(),mat.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat,bitmap);
        iv.get().setImageBitmap(bitmap);
        byte[] image_converted_to_byte_array;
        image_converted_to_byte_array=imageViewToByteArray(iv.get());
        Log.d(CONVERSIONE_FILE, "immagine convertita in byte array di dimensione "+image_converted_to_byte_array.length);

        if( myDBWeakReference.get().insertSalvaImmagine(    image_converted_to_byte_array,
                infoSaveWeakReference.get().getUser_id(),
                infoSaveWeakReference.get().getScore()
        )
        )
        {
            Log.d(SALVATAGGIO_FILE, "immagine salvata!");
            /*
            Cursor cursor= myDBWeakReference.get().controllaScore(infoSaveWeakReference.get().getUser_id());
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
        }
        else
        {Log.d(SALVATAGGIO_FILE, "ERRORE: immagine non salvata!");}

    }
    public byte[] imageViewToByteArray(ImageView imageView){
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
        return stream.toByteArray();
    }
}
