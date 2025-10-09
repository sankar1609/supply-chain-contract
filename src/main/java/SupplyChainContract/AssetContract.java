package SupplyChainContract;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import com.owlike.genson.Genson;
import org.hyperledger.fabric.shim.ChaincodeException;
import java.time.Instant;

@Contract(name = "AssetContract",
        info = @Info(
                title = "Supplychain contract",
                description = "A Sample Supplychain chaincode example",
                version = "0.0.1-SNAPSHOT"))

public class AssetContract implements ContractInterface {
    private final Genson genson = new Genson();

    @Transaction()
    public void initLedger(Context ctx) {
        // Initialize ledger with sample products
        Product product = new Product("P001", "Laptop", "Electronics", 10);
        String productKey = "PRODUCT_" + product.getId();
        ctx.getStub().putStringState(productKey, genson.serialize(product));
    }

    @Transaction()
    public Product createProduct(Context ctx, String id, String name,
                                 String category, Integer quantity) {
        String productKey = "PRODUCT_" + id;
        String existingProductJSON = ctx.getStub().getStringState(productKey);
        if (existingProductJSON != null && !existingProductJSON.isEmpty()) {
            throw new ChaincodeException("Product with ID " + id + " already exists.");
        }
        Product product = new Product(id, name, category, quantity);
        ctx.getStub().putStringState(productKey, genson.serialize(product));

        // Audit log
        String logId = "AUDIT_" + ctx.getStub().getTxId();
        String user = ctx.getClientIdentity().getId();
        String timestamp = Instant.ofEpochSecond(ctx.getStub().getTxTimestamp().getEpochSecond()).toString();
        AuditLog log = new AuditLog(logId, "CREATE_PRODUCT", id, quantity, user, timestamp, null);
        ctx.getStub().putStringState(logId, genson.serialize(log));

        return product;
    }

    @Transaction()
    public Product readProduct(Context ctx, String id) {
        String productKey = "PRODUCT_" + id;
        String productJSON = ctx.getStub().getStringState(productKey);
        if (productJSON == null || productJSON.isEmpty()) {
            throw new ChaincodeException("Product not found: " + id);
        }
        return genson.deserialize(productJSON, Product.class);
    }

    @Transaction()
    public Product updateProductQuantity(Context ctx, String id, Integer addQuantity) {
        if (addQuantity < 0) {
            throw new ChaincodeException("Cannot add negative quantity to product.");
        }
        Product product = readProduct(ctx, id);
        product.setQuantity(product.getQuantity() + addQuantity);
        String productKey = "PRODUCT_" + id;
        ctx.getStub().putStringState(productKey, genson.serialize(product));

        // Audit log
        String logId = "AUDIT_" + ctx.getStub().getTxId();
        String user = ctx.getClientIdentity().getId();
        String timestamp = Instant.ofEpochSecond(ctx.getStub().getTxTimestamp().getEpochSecond()).toString();
        AuditLog log = new AuditLog(logId, "UPDATE_QUANTITY", id, addQuantity, user, timestamp, null);
        ctx.getStub().putStringState(logId, genson.serialize(log));

        return product;
    }

    @Transaction()
    public void deleteProduct(Context ctx, String id) {
        String productKey = "PRODUCT_" + id;
        String productJSON = ctx.getStub().getStringState(productKey);
        if (productJSON == null || productJSON.isEmpty()) {
            throw new ChaincodeException("Product not found: " + id);
        }
        // Audit log
        String logId = "AUDIT_" + ctx.getStub().getTxId();
        String user = ctx.getClientIdentity().getId();
        String timestamp = Instant.ofEpochSecond(ctx.getStub().getTxTimestamp().getEpochSecond()).toString();
        AuditLog log = new AuditLog(logId, "DELETE_PRODUCT", id, null, user, timestamp, null);
        ctx.getStub().putStringState(logId, genson.serialize(log));

        ctx.getStub().delState(productKey);
    }

    @Transaction()
    public AuditLog[] getAuditLogsByProductId(Context ctx, String productId) {
        java.util.List<AuditLog> logs = new java.util.ArrayList<>();
        // Use getStateByRange to iterate over all keys
        final String startKey = "AUDIT_";
        final String endKey = "AUDIT_~";
        var results = ctx.getStub().getStateByRange(startKey, endKey);
        for (var kv : results) {
            String logJSON = kv.getStringValue();
            if (logJSON != null && !logJSON.isEmpty()) {
                try {
                    AuditLog log = genson.deserialize(logJSON, AuditLog.class);
                    if (log != null && log.getProductId().equals(productId)) {
                        logs.add(log);
                    }
                } catch (Exception e) {
                    // Skip invalid JSON
                    throw new ChaincodeException("Cannot deserialize log: " + logJSON, e);

                }
            }
        }

        try { results.close(); } catch (Exception e) { /* ignore */ }
        if (logs.isEmpty()) {
            throw new ChaincodeException("No audit logs found for product ID: " + productId);
        }
        return logs.toArray(new AuditLog[0]);
    }

}
