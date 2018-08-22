package com.example.gvy.jesta;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by Tomer on 01/06/2018.
 */

@Database(entities = {Jesta.class}, version = 1)
public abstract class JestasRoomDataBase extends RoomDatabase {

    private static JestasRoomDataBase instance;

    public abstract JestaDao jestaDao();

    public static JestasRoomDataBase getDatabase() {
        if (instance == null) {
            instance = Room.databaseBuilder(JestaApp.getContext(), JestasRoomDataBase.class, "jestass_db").build();
        }
        return instance;
    }
}
