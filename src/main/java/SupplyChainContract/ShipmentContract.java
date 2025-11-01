package SupplyChainContract;

import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;

import java.time.Instant;

@Contract(name = "ShipmentContract",
        info = @Info(
                title = "Supplychain contract",
                description = "A Sample Supplychain chaincode example",
                version = "0.0.1-SNAPSHOT"))
@Default
public class ShipmentContract implements ContractInterface {
    // Add shipment-related chaincode logic here
    private final Genson genson = new Genson();
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void createShipment(Context ctx, String shipmentId, String productId, String origin, String destination, String carrier, Integer quantity) {
        String shipmentKey = "SHIPMENT_" + shipmentId;
        String existingShipmentJSON = ctx.getStub().getStringState(shipmentKey);
        if (existingShipmentJSON != null && !existingShipmentJSON.isEmpty()) {
            throw new ChaincodeException("Shipment with ID " + shipmentId + " already exists.", "SHIPMENT_ALREADY_EXISTS");
        }

        String productKey = "PRODUCT_" + productId;
        String productJSON = ctx.getStub().getStringState(productKey);
        if (productJSON == null || productJSON.isEmpty()) {
            throw new ChaincodeException("Product not found: " + productId, "PRODUCT_NOT_FOUND");
        }
        Product product = genson.deserialize(productJSON, Product.class);
        if (product.getQuantity() < quantity) {
            throw new ChaincodeException("Insufficient product quantity for shipment.", "INSUFFICIENT_QUANTITY");
        }
        product.setQuantity(product.getQuantity() - quantity);
        ctx.getStub().putStringState(productKey, genson.serialize(product));

        Shipment shipment = new Shipment(shipmentId, productId, origin, destination, "CREATED", carrier, quantity);
        ctx.getStub().putStringState(shipmentKey, genson.serialize(shipment));

        // Audit log for shipment creation
        String logId = "AUDIT_" + ctx.getStub().getTxId();
        String user = ctx.getClientIdentity().getId();
        String timestamp = Instant.ofEpochSecond(ctx.getStub().getTxTimestamp().getEpochSecond()).toString();
        AuditLog log = new AuditLog(logId, "CREATE_SHIPMENT", productId, quantity, user, timestamp, shipmentId);
        ctx.getStub().putStringState(logId, genson.serialize(log));
    }


    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void updateShipmentStatus(Context ctx, String shipmentId, String status) {
        String shipmentKey = "SHIPMENT_" + shipmentId;
        String shipmentJSON = ctx.getStub().getStringState(shipmentKey);
        if (shipmentJSON == null || shipmentJSON.isEmpty()) {
            throw new ChaincodeException("Shipment not found: " + shipmentId, "SHIPMENT_NOT_FOUND");
        }
        Shipment shipment = Util.fromJSONString(shipmentJSON, Shipment.class);
        shipment.setStatus(status);
        // Audit log
        String logId = "AUDIT_" + ctx.getStub().getTxId();
        String user = ctx.getClientIdentity().getId();
        String timestamp = Instant.ofEpochSecond(ctx.getStub().getTxTimestamp().getEpochSecond()).toString();
        AuditLog log = new AuditLog(logId, "UPDATE_SHIPMENT", shipment.getProductId(), shipment.getQuantity(),
                user, timestamp, shipmentId);
        ctx.getStub().putStringState(logId, genson.serialize(log));

        ctx.getStub().putStringState(shipmentKey, Util.toJSONString(shipment));
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Shipment getShipment(Context ctx, String shipmentId) {
        String shipmentKey = "SHIPMENT_" + shipmentId;
        String shipmentJSON = ctx.getStub().getStringState(shipmentKey);
        if (shipmentJSON == null || shipmentJSON.isEmpty()) {
            throw new ChaincodeException("Shipment not found: " + shipmentId, "SHIPMENT_NOT_FOUND");
        }
        return genson.deserialize(shipmentJSON, Shipment.class);
    }
    /*
    new method for getShipment based on productId
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public AuditLog[] getAuditLogsByShipmentId(Context ctx, String shipmentId) {
        java.util.List<AuditLog> logs = new java.util.ArrayList<>();
        final String startKey = "AUDIT_";
        final String endKey = "AUDIT_~";
        var results = ctx.getStub().getStateByRange(startKey, endKey);
        for (var kv : results) {
            String logJSON = kv.getStringValue();
            if (logJSON != null && !logJSON.isEmpty()) {
                AuditLog log = genson.deserialize(logJSON, AuditLog.class);
                if (shipmentId.equals(log.getShipmentId())) {
                    logs.add(log);
                }
            }
        }
        try { results.close(); } catch (Exception e) { /* ignore */ }
        return logs.toArray(new AuditLog[0]);
    }

    // New helper method to place an order (creates a shipment) given productId and quantity
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Shipment placeOrder(Context ctx, String productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ChaincodeException("Quantity must be a positive integer.", "INVALID_QUANTITY");
        }

        String productKey = "PRODUCT_" + productId;
        String productJSON = ctx.getStub().getStringState(productKey);
        if (productJSON == null || productJSON.isEmpty()) {
            throw new ChaincodeException("Product not found: " + productId, "PRODUCT_NOT_FOUND");
        }

        Product product = genson.deserialize(productJSON, Product.class);
        if (product.getQuantity() < quantity) {
            throw new ChaincodeException("Insufficient product quantity for order.", "INSUFFICIENT_QUANTITY");
        }

        // Deduct the quantity from the product and persist
        product.setQuantity(product.getQuantity() - quantity);
        ctx.getStub().putStringState(productKey, genson.serialize(product));

        // Use the transaction id as the shipment id to ensure uniqueness
        String shipmentId = ctx.getStub().getTxId();
        String shipmentKey = "SHIPMENT_" + shipmentId;

        Shipment shipment = new Shipment(shipmentId, productId, "", "", "ORDERED", "", quantity);
        ctx.getStub().putStringState(shipmentKey, genson.serialize(shipment));

        // Audit log for placing order
        String logId = "AUDIT_" + ctx.getStub().getTxId();
        String user = ctx.getClientIdentity().getId();
        String timestamp = Instant.ofEpochSecond(ctx.getStub().getTxTimestamp().getEpochSecond()).toString();
        AuditLog log = new AuditLog(logId, "PLACE_ORDER", productId, quantity, user, timestamp, shipmentId);
        ctx.getStub().putStringState(logId, genson.serialize(log));

        return shipment;
    }

}
