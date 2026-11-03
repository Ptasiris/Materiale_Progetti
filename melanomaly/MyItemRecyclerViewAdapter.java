package com.example.melanomaly;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.melanomaly.databinding.FragmentPastImagesBinding;
import com.example.melanomaly.placeholder.PlaceholderContent;
import com.example.melanomaly.placeholder.PlaceholderContent.PlaceholderItem;


import java.util.List;


public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {
    //istanzio l'interfaccia
    private final AvvisaMedico avvisaMedico;
    //in mValues ci vanno i dati presi dal DB e li inserirai qui grazie al costruttore
    private final List<PlaceholderContent.PlaceholderItem> mValues;
    private final Context context;

    //il costruttore del recycler view non prende in input il dato-riga, bensì una lista di dato-riga
    public MyItemRecyclerViewAdapter(Context context, List<PlaceholderContent.PlaceholderItem> items, AvvisaMedico avvisaMedico) {
        this.mValues = items;
        this.context=context;
        this.avvisaMedico=avvisaMedico;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentPastImagesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false),avvisaMedico,mValues);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //qui dopo l'uguale (assegnazione) devo usare le setter per i vari tipi di View (non quelle che ho creato io)
        //qui dopo il .get(position)devo usare le getter che ho creato
        holder.imageView.setImageBitmap(mValues.get(position).getBitmap_imago());

        String id_imago_string="Immagine n. "+mValues.get(position).getId_imago();
        holder.tv_id.setText(id_imago_string);

        String score_string="Score: "+mValues.get(position).getScore().toString();
        holder.tv_score.setText(score_string);

        holder.icon.setImageResource(mValues.get(position).getIcon());

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    //qui definisci gli oggetti View che conterranno i tuoi dati (ImageView,TextView,Button,ProgressBar,ecc.)
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView,icon;
        TextView tv_id,tv_score;


        //qui si fa la stessa cosa del findViewById()
        public ViewHolder(FragmentPastImagesBinding binding,AvvisaMedico avvisaMedico,List<PlaceholderContent.PlaceholderItem> mValues) {
            super(binding.getRoot());
            imageView=binding.imageNeo;
            tv_id=binding.textViewIdImago;
            tv_score=binding.textViewScore;
            icon=binding.imageViewIcon;

            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (avvisaMedico!=null){
                        int pos=getAbsoluteAdapterPosition();

                        if (pos!=RecyclerView.NO_POSITION){
                            Bitmap bitmap= mValues.get(pos).getBitmap_imago();
                            avvisaMedico.onIconClick(pos,bitmap);
                        }
                    }
                }
            });
        }
    }
}