package dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import domain.Product;

@Dao
public interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(final Product product);

    @Query("SELECT * FROM Product")
    List<Product> getAll();

    @Query("SELECT * FROM Product WHERE id = :id")
    Product getById(final Integer id);

    @Delete
    void delete(final Product product);

    @Query("DELETE FROM Product")
    void deleteAll();

    @Query("DELETE FROM Product WHERE isInWishlist = 0")
    void deleteAllNotInWishlist();

    @Query("SELECT * FROM Product WHERE isInWishlist = 1")
    List<Product> getAllInWishlist();
}
