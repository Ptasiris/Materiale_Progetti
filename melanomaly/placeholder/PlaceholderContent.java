package com.example.melanomaly.placeholder;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.example.melanomaly.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceholderContent {

    //questo conterrà tutti i dati da mostrare
    public static final List<PlaceholderItem> ITEMS = new ArrayList<PlaceholderItem>();

    //questa è la classe del dato-riga
    public static class PlaceholderItem {
        private Integer id_imago;
        private Float score;
        private Bitmap bitmap_imago;
        private int int_icon;

        //questo è il costruttore della classe del dato-riga
        public PlaceholderItem(Integer id_imago, Float score, Bitmap bitmap_imago, int int_icon) {
            this.id_imago=id_imago;
            this.score= score;
            this.bitmap_imago= bitmap_imago;
            this.int_icon= int_icon;
        }

        public Integer getId_imago() {return id_imago;}
        public void setId_imago(Integer id_imago) {this.id_imago = id_imago;}

        public Float getScore() {return score;}
        public void setScore(Float score) {this.score = score;}

        public Bitmap getBitmap_imago() {return bitmap_imago;}
        public void setBitmap_imago(Bitmap bitmap_imago) {this.bitmap_imago = bitmap_imago;}

        public int getIcon() {return int_icon;}
        public void setIcon(int int_icon) {this.int_icon = int_icon;}

    }
}