package domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Product {
    @NonNull
    @PrimaryKey
    private Integer id;

    private String name;

    private Integer price;

    private String details;

    private String image;

    private boolean isInWishlist;


    public Product(@NonNull Integer id, String name, Integer price, String details, String image) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.details = details;
        this.image = image;
        isInWishlist = false;
    }

    @NonNull
    public Integer getId() {
        return id;
    }
    public void setId(@NonNull Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }
    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getDetails() {
        return details;
    }
    public void setDetails(String details) {
        this.details = details;
    }

    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }

    public boolean isInWishlist() {
        return isInWishlist;
    }
    public void setInWishlist(boolean inWishlist) {
        isInWishlist = inWishlist;
    }
}
