package dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import domain.TaskToDoWhenOnline;

@Dao
public interface TaskToDoWhenOnlineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(final TaskToDoWhenOnline taskToDoWhenOnline);

    @Query("SELECT * FROM TaskToDoWhenOnline")
    List<TaskToDoWhenOnline> getAll();

    @Delete
    void delete(final TaskToDoWhenOnline taskToDoWhenOnline);

}
