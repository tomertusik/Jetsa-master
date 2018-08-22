package com.example.gvy.jesta;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by Tomer on 01/06/2018.
 */

@Dao
public interface JestaDao {

    @Query("SELECT * FROM jesta")
    List<Jesta> getAllJestas();

    @Query("SELECT * FROM jesta WHERE user_id LIKE :ownerID")
    List<Jesta> getMyJestas(String ownerID);

    @Delete
    void delete(Jesta jesta);

    @Insert
    void insertAll(List<Jesta> jestas);

    @Query("DELETE FROM jesta")
    void deleteAll();
}
