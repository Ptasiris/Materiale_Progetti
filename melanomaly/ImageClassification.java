package com.example.melanomaly;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class ImageClassification {
    private static final String IMAGE_CLASSIFICATION = "IMAGE_CLASSIFICATION";
    private static final String DATI_MATRICE_HEIGTH = "DATI_MATRICE_HEIGTH";
    private static final String DATI_MATRICE_WIDTH = "DATI_MATRICE_WIDTH";
    private Interpreter interpreter;
    private int INPUT_SIZE;
    private int PIXEL_SIZE=3;
    private int IMAGE_MEAN=0;
    private float IMAGE_STD=255.0f;

    private float threshold=0.1f;
    private float val_prediction=0.0f;

    private GpuDelegate gpuDelegate;
    private int height=0;
    private int width=0;

    private Scalar red=new Scalar(255,0,0,50);
    private Scalar green=new Scalar(0,255,0,50);

    public ImageClassification(AssetManager assetManager, String modelPath, int inputSize) throws IOException {
        INPUT_SIZE=inputSize;
        Interpreter.Options options=new Interpreter.Options();
        gpuDelegate=new GpuDelegate();
        options.addDelegate(gpuDelegate);
        options.setNumThreads(6);
        interpreter=new Interpreter(loadModelFile(assetManager,modelPath),options);
    }

    public Mat recognizeImage(Mat mat){

        height=mat.height();//217 per telefono e 160 per emulatore
        Log.d(DATI_MATRICE_HEIGTH, "recognizeImage: height= "+height);
        width=mat.width();//217 per telefono e 120 per emulatore
        Log.d(DATI_MATRICE_WIDTH, "recognizeImage: width= "+width);

        //(width*70)/100 per telefono e (width*95)/100 per emulatore
        Rect roi_cropped=new Rect(( width-(width*90)/100 )/2,( height-(height*90)/100 )/2,(width*90)/100,(height*90)/100);
        Mat cropped_image=new Mat(mat,roi_cropped);

        Bitmap bitmap=null;
        bitmap=Bitmap.createBitmap(cropped_image.cols(),cropped_image.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(cropped_image,bitmap);

        Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap,INPUT_SIZE,INPUT_SIZE,false);

        ByteBuffer byteBuffer=createBitmapToByteBuffer(scaledBitmap);

        float[][] output=new float[1][1];
        Object[] out=new Object[1];
        out[0]=output;

        Object[] input=new Object[1];
        input[0]=byteBuffer;

        interpreter.run(byteBuffer,output);
        Log.d(IMAGE_CLASSIFICATION, "Out: "+ Arrays.deepToString(output));

        val_prediction=(float) Array.get(Array.get(output,0),0);
        //se val_prediction tende a 0 =>benigno
        //se val_prediction tende a 1 =>maligno
        if (val_prediction>threshold){
            //(width*70)/100 per telefono e (width*95)/100 per emulatore
            Imgproc.putText(mat,"neo maligno",new Point((( width-(width*70)/100 )),( height-(height*85)/100 )),1,1,red,1);
            Imgproc.rectangle(mat,new Point(( width-(width*90)/100 )/2,( height-(height*90)/100 )/2),new Point((( width+(width*90)/100 )/2),( height+(height*90)/100 )/2),red,1);
        }
        else{
            //(width*70)/100 per telefono e (width*95)/100 per emulatore
            Imgproc.putText(mat,"neo benigno",new Point((( width-(width*70)/100 )),( height-(height*85)/100 )),1,1,green,1);
            Imgproc.rectangle(mat,new Point(( width-(width*90)/100 )/2,( height-(height*90)/100 )/2),new Point((( width+(width*90)/100 )/2),( height+(height*90)/100 )/2),green,1);
        }
        return mat;
    }

    private ByteBuffer createBitmapToByteBuffer(Bitmap scaledBitmap) {
        ByteBuffer byteBuffer;
        byteBuffer=ByteBuffer.allocateDirect(4*INPUT_SIZE*INPUT_SIZE*PIXEL_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues=new int[INPUT_SIZE*INPUT_SIZE];

        scaledBitmap.getPixels(intValues,0,scaledBitmap.getWidth(),0,0,scaledBitmap.getWidth(),scaledBitmap.getHeight());

        int pixel=0;
        for(int i =0;i<INPUT_SIZE;++i) {
            for (int j = 0; j < INPUT_SIZE; ++j){

                final int val=intValues[pixel++];
                byteBuffer.putFloat( ( ( (val>>16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                byteBuffer.putFloat( ( ( (val>>8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                byteBuffer.putFloat( ( ( (val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);

            }
        }
        return byteBuffer;
    }

    private ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {

        AssetFileDescriptor assetFileDescriptor=assetManager.openFd(modelPath);
        FileInputStream inputStream=new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffSet=assetFileDescriptor.getStartOffset();
        long declaredLength=assetFileDescriptor.getLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffSet,declaredLength);
    }

    public float getVal_prediction() {
        return val_prediction;
    }

    public void setVal_prediction(float val_prediction) {
        this.val_prediction = val_prediction;
    }

}
