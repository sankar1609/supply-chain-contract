package SupplyChainContract;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class AuditLog {
    @Property
    private String logId;
    @Property
    private String action;
    @Property
    private String productId;
    @Property
    private Integer quantity;
    @Property
    private String user;
    @Property
    private String timestamp;
    @Property
    private String shipmentId;

    public AuditLog(String logId, String action, String productId, Integer quantity, String user, String timestamp, String shipmentId) {
        this.logId = logId;
        this.action = action;
        this.productId = productId;
        this.quantity = quantity;
        this.user = user;
        this.timestamp = timestamp;
        this.shipmentId = shipmentId;
    }

    public AuditLog() {}
    public void setLogId(String logId) { this.logId = logId; }
    public void setAction(String action) { this.action = action; }
    public void setProductId(String productId) { this.productId = productId; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getLogId() { return logId; }
    public String getAction() { return action; }
    public String getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }
    public String getUser() { return user; }
    public String getTimestamp() { return timestamp; }
    public String getShipmentId() { return shipmentId; }
    public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }
}
