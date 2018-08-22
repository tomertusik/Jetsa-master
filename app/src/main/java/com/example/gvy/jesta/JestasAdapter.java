package com.example.gvy.jesta;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.Target;
import com.example.gvy.jetsa.databinding.JestaHolderBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tomer on 26/05/2018.
 */

public class JestasAdapter extends RecyclerView.Adapter {

    private List<Jesta> jestas;
    private final Context context;
    private final JestasTypesActivity.Types type;
    private JestasAdapterListener listener;

    public interface JestasAdapterListener {
        void onJestaClick(Jesta jesta, Drawable image);

        void setJestasList(List<Jesta> jestas);
    }

    public JestasAdapter(List<Jesta> jestas, Context context, JestasAdapterListener listener, JestasTypesActivity.Types type) {
        this.jestas = jestas;
        this.context = context;
        this.listener = listener;
        this.type = type;
        if (type != null){
            sortByType();
        }
    }

    private void sortByType() {
        List<Jesta> tempJestas = jestas;
        jestas = new ArrayList<>();
        for (Jesta jesta : tempJestas){
            if (jesta.getCategory().equals(type.toString())){
                jestas.add(jesta);
            }
        }
        listener.setJestasList(jestas);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        JestaHolderBinding binding = JestaHolderBinding.inflate(LayoutInflater.from(parent.getContext()));
        JestaViewHolder viewHolder = new JestaViewHolder(binding);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        JestaViewHolder jestaViewHolder = (JestaViewHolder) holder;
        Jesta currJesta = jestas.get(position);
        jestaViewHolder.binding.category.setText(currJesta.getCategory());
        jestaViewHolder.binding.price.setText(currJesta.getPrice());
        jestaViewHolder.binding.adress.setText(currJesta.getAdress());
        jestaViewHolder.binding.desc.setText(currJesta.getDesc() + "...");
        String localFileName = Model.instance.getLocalImageFileName(currJesta.getImageURL());
        Bitmap image = Model.instance.loadImageFromFile(localFileName);
        if (image != null){
            jestaViewHolder.binding.image.setImageBitmap(image);
            jestaViewHolder.binding.getRoot().setOnClickListener(new OnJestaClickListener(currJesta, jestaViewHolder.binding.image.getDrawable()));
        } else {
            Glide.with(context).load(currJesta.getImageURL()).listener(new DoneLoadingListener(jestaViewHolder,currJesta)).into(jestaViewHolder.binding.image);
        }
    }

    @Override
    public int getItemCount() {
        return jestas.size();
    }

    public class JestaViewHolder extends RecyclerView.ViewHolder {

        public final JestaHolderBinding binding;

        public JestaViewHolder(JestaHolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private class OnJestaClickListener implements View.OnClickListener {

        private final Jesta currJesta;
        private final Drawable image;

        public OnJestaClickListener(Jesta currJesta, Drawable drawable) {
            this.currJesta = currJesta;
            this.image = drawable;
        }

        @Override
        public void onClick(View view) {
            listener.onJestaClick(currJesta,image);
        }
    }

    private class DoneLoadingListener implements com.bumptech.glide.request.RequestListener<Drawable> {

        private final JestaViewHolder jestaViewHolder;
        private final Jesta currJesta;

        public DoneLoadingListener(JestaViewHolder jestaViewHolder, Jesta currJesta) {
            this.jestaViewHolder = jestaViewHolder;
            this.currJesta = currJesta;
        }

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            jestaViewHolder.binding.getRoot().setOnClickListener(new OnJestaClickListener(currJesta, resource));
            return false;
        }
    }
}
