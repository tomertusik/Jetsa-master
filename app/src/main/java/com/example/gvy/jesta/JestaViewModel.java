package com.example.gvy.jesta;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

/**
 * Created by Tomer on 08/06/2018.
 */

public class JestaViewModel extends AndroidViewModel {

    private Model model;
    private LiveData<List<Jesta>> jestas;
    private LiveData<List<Jesta>> myJestas;

    public JestaViewModel(Application application) {
        super(application);
        model = Model.instance;
        jestas = model.getAllJestas();
        myJestas = model.getMyJestas();
    }

    public LiveData<List<Jesta>> getAllJestas() { return jestas; }

    public LiveData<List<Jesta>> getMyJestas() {
        return myJestas;
    }
}
