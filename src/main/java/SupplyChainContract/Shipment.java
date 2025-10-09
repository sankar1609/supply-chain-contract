package SupplyChainContract;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class Shipment {
    @Property
    private String shipmentId;
    @Property
    private String productId;
    @Property
    private String origin;
    @Property
    private String destination;
    @Property
    private String status;
    @Property
    private String carrier;
    @Property
    private Integer quantity;

    public Shipment(String shipmentId, String productId, String origin, String destination, String status, String carrier, Integer quantity) {
        this.shipmentId = shipmentId;
        this.productId = productId;
        this.origin = origin;
        this.destination = destination;
        this.status = status;
        this.carrier = carrier;
        this.quantity = quantity;
    }

    public Shipment() {
        // No-arg constructor for deserialization
    }

    // Getters and setters
    public String getShipmentId() { return shipmentId; }
    public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
