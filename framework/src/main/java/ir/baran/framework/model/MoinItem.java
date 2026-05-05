package ir.baran.framework.model;

/**
 * Model class representing a single item from moin table
 * Used for dictionary/reference entries
 */
public class MoinItem {
    
    private int id;
    private String title;
    private String des;
    private boolean isFavorite;

    public MoinItem() {
        this.id = 0;
        this.title = "";
        this.des = "";
        this.isFavorite = false;
    }

    public MoinItem(int id, String title, String des) {
        this.id = id;
        this.title = title != null ? title : "";
        this.des = des != null ? des : "";
        this.isFavorite = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null ? title : "";
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des != null ? des : "";
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @Override
    public String toString() {
        return "MoinItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", des='" + (des.length() > 50 ? des.substring(0, 50) + "..." : des) + '\'' +
                ", isFavorite=" + isFavorite +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoinItem moinItem = (MoinItem) o;
        return id == moinItem.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
