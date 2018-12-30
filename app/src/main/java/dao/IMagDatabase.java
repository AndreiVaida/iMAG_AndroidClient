package dao;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import domain.Product;

@Database(entities = {Product.class}, version = 1, exportSchema = false)
public abstract class IMagDatabase extends RoomDatabase {
    public abstract ProductDao productDao();
}
