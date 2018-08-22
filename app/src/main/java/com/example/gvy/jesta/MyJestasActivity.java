package com.example.gvy.jesta;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.example.gvy.jetsa.R;
import com.example.gvy.jetsa.databinding.MyJestasBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Tomer on 10/08/2018.
 */

public class MyJestasActivity extends AppCompatActivity {

    private MyJestasBinding binding;
    private AddJestaFragment addJestaFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.my_jestas);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Loading.startLoading(this);
        EventBus.getDefault().register(this);
        binding.addJesta.setOnClickListener(new AddJestaListener());
        binding.back.setOnClickListener(new BackListener());
        initMyJestas();

    }

    private void initMyJestas() {
        binding.reycler.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        binding.reycler.setLayoutManager(new LinearLayoutManager(MyJestasActivity.this) {
            @Override
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
        JestaViewModel viewModel = ViewModelProviders.of(this).get(JestaViewModel.class);
        viewModel.getMyJestas().observe(this, new Observer<List<Jesta>>() {
            @Override
            public void onChanged(@Nullable final List<Jesta> jestas) {
                // Update the cached copy of the words in the adapter.
                if (jestas != null) {
                    if (!jestas.isEmpty()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JestasAdapter adapter = new JestasAdapter(jestas, MyJestasActivity.this, new JestaClickListener(), null);
                                binding.reycler.setAdapter(adapter);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.reycler.setAdapter(null);
                            }
                        });
                    }
                }
                Loading.stopLoading();
            }
        });
    }

    private class JestaClickListener implements JestasAdapter.JestasAdapterListener {

        @Override
        public void onJestaClick(Jesta jesta, Drawable image) {
            JestaOpenFragment fragment = JestaOpenFragment.newInstance(jesta, image, true, new OnDeleteListener());
            getSupportFragmentManager().beginTransaction().add(binding.mainFrame.getId(), fragment).
                    addToBackStack("Expand").commit();
        }

        @Override
        public void setJestasList(List<Jesta> jestas) {
        }
    }

    private class AddJestaListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            addJestaFragment = new AddJestaFragment();
            getSupportFragmentManager().beginTransaction().addToBackStack("").add(R.id.mainFrame,addJestaFragment).commit();
        }
    }

    private class OnDeleteListener implements JestaOpenFragment.ExpandDeleteListener {
        @Override
        public void onDelete() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Loading.startLoading(MyJestasActivity.this);
                }
            });
            Model.instance.getMyJestas(new DoneListener());
        }

        @Override
        public void stopLoading() {
        }

        @Override
        public void startLoading() {
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStackImmediate();
        else super.onBackPressed();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAddJestaEvent(AddJestaEvent event) {
        getSupportFragmentManager().beginTransaction().remove(addJestaFragment).commit();
        Model.instance.getMyJestas(new DoneListener());
    }

    private class DoneListener implements Model.JestasDoneListener {
        @Override
        public void onJestasLoaded(final List<Jesta> jestas) {
            if (jestas != null) {
                if (!jestas.isEmpty()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JestasAdapter adapter = new JestasAdapter(jestas, MyJestasActivity.this, new JestaClickListener(), null);
                            binding.reycler.setAdapter(adapter);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.reycler.setAdapter(null);
                        }
                    });
                }
            }
            Loading.stopLoading();
        }
    }

    private class BackListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
           finish();
        }
    }
}
