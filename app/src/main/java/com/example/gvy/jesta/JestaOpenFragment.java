package com.example.gvy.jesta;

import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.Target;
import com.example.gvy.jetsa.R;
import com.example.gvy.jetsa.databinding.OpenJestaBinding;

/**
 * Created by Tomer on 01/06/2018.
 */

public class JestaOpenFragment extends Fragment {

    private Jesta jesta;
    private OpenJestaBinding binding;
    private Drawable image;
    private boolean isDelete;
    private ExpandDeleteListener listener;

    public interface ExpandDeleteListener{
        void onDelete();

        void stopLoading();

        void startLoading();
    }

    public static final JestaOpenFragment newInstance(Jesta jesta, Drawable image,boolean isDelete, ExpandDeleteListener listener) {
        JestaOpenFragment fragment = new JestaOpenFragment();
        fragment.jesta = jesta;
        fragment.image = image;
        fragment.isDelete = isDelete;
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.open_jesta, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isDelete){
            binding.delete.setVisibility(View.VISIBLE);
            binding.delete.setOnClickListener(new DeleteClickListener());
        }
        binding.category.setText(jesta.getCategory());
        binding.price.setText(jesta.getPrice());
        binding.adress.setText(jesta.getAdress());
        binding.description.setText(jesta.getDesc());
        if(image != null){
            binding.image.setImageDrawable(image);
        } else {
            listener.startLoading();
            Glide.with(getContext()).load(jesta.getImageURL()).listener(new DoneLoadingListener()).into(binding.image);
        }
        binding.mainLayout.setOnClickListener(null);
        binding.background.setOnClickListener(new ExitListener());
    }

    private class ExitListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private class DeleteClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Model.instance.deleteJesta(jesta, new DeleteListener());
        }
    }

    private class DeleteListener implements Model.JestasDeleteListener {

        @Override
        public void onJestaDeleted() {
            listener.onDelete();
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private class DoneLoadingListener implements com.bumptech.glide.request.RequestListener<Drawable> {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            listener.stopLoading();
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            listener.stopLoading();
            return false;
        }
    }
}
