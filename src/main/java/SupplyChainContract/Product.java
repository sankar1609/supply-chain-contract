package SupplyChainContract;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;


@DataType
public class Product {
    @Property
    private String id;
    @Property
    private String name;
    @Property
    private String category;
    @Property
    private Integer quantity;

    public Product() {}

    public Product(@JsonProperty("id") final String id,
                   @JsonProperty("name") final String name,
                   @JsonProperty("category") final String category,
                   @JsonProperty("quantity") final Integer quantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}



