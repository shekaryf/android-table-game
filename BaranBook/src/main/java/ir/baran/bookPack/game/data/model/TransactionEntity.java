package ir.baran.bookPack.game.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "Transactions")
public class TransactionEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Integer id;

    @ColumnInfo(name = "sku_id")
    @NonNull
    private String skuId;

    @ColumnInfo(name = "purchase_token")
    @NonNull
    private String purchaseToken;

    @ColumnInfo(name = "purchase_date")
    private long purchaseDate;

    @ColumnInfo(name = "amount")
    private int amount;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getSkuId() { return skuId; }
    public void setSkuId(String skuId) { this.skuId = skuId; }
    public String getPurchaseToken() { return purchaseToken; }
    public void setPurchaseToken(String purchaseToken) { this.purchaseToken = purchaseToken; }
    public long getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(long purchaseDate) { this.purchaseDate = purchaseDate; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
}
