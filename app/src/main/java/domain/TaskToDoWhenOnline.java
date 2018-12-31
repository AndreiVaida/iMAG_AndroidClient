package domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class TaskToDoWhenOnline {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    private Integer id;

    private Integer productId;

    private boolean isInWishlist;

    public TaskToDoWhenOnline(Integer productId, boolean isInWishlist) {
        this.productId = productId;
        this.isInWishlist = isInWishlist;
    }

    @NonNull
    public Integer getId() {
        return id;
    }
    public void setId(@NonNull Integer id) {
        this.id = id;
    }

    public Integer getProductId() {
        return productId;
    }
    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public boolean isInWishlist() {
        return isInWishlist;
    }
    public void setInWishlist(boolean inWishlist) {
        isInWishlist = inWishlist;
    }
}
