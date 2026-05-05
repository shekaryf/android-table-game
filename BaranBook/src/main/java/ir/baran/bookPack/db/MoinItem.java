package ir.baran.bookPack.db;

/**
 * Model class for Moin dictionary items
 * Represents a single entry from the info.sqlite database
 */
public class MoinItem {
    private int id;
    private String title;
    private String des;

    public MoinItem() {
    }

    public MoinItem(int id, String title, String des) {
        this.id = id;
        this.title = title != null ? title : "";
        this.des = des != null ? des : "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title != null ? title : "";
    }

    public String getDes() {
        return des != null ? des : "";
    }

    public void setDes(String des) {
        this.des = des != null ? des : "";
    }

    @Override
    public String toString() {
        return "MoinItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", des='" + (des != null && des.length() > 50 ? des.substring(0, 50) + "..." : des) + '\'' +
                '}';
    }
}
