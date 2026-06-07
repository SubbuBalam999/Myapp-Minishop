import axios from "axios";
import { useEffect, useMemo, useState } from "react";
import {
  getInventory,
  getProducts,
  type InventoryRecord,
  type Product,
} from "../services/api";

function InventoryPage() {
  const [inventory, setInventory] = useState<InventoryRecord[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();

    async function loadInventory() {
      try {
        setIsLoading(true);
        setError(null);
        const [inventoryData, productData] = await Promise.all([
          getInventory(controller.signal),
          getProducts(controller.signal),
        ]);
        setInventory(inventoryData);
        setProducts(productData);
      } catch (requestError) {
        if (!axios.isCancel(requestError)) {
          setError("Unable to load inventory through the API Gateway.");
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      }
    }

    void loadInventory();
    return () => controller.abort();
  }, []);

  const productsById = useMemo(
    () => new Map(products.map((product) => [product.id, product])),
    [products],
  );

  return (
    <section>
      <div className="page-heading">
        <div>
          <div className="eyebrow">Stock control</div>
          <h1>Inventory</h1>
        </div>
        {!isLoading && !error && (
          <span className="result-count">
            {inventory.length}{" "}
            {inventory.length === 1 ? "product tracked" : "products tracked"}
          </span>
        )}
      </div>

      {isLoading && <div className="status-panel">Loading inventory...</div>}

      {error && (
        <div className="status-panel status-error" role="alert">
          {error}
        </div>
      )}

      {!isLoading && !error && inventory.length === 0 && (
        <div className="status-panel">
          No product inventory has been registered yet.
        </div>
      )}

      {!isLoading && !error && inventory.length > 0 && (
        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Product ID</th>
                <th>Product</th>
                <th>Available</th>
                <th>Reserved</th>
                <th>Total tracked</th>
                <th>Updated</th>
              </tr>
            </thead>
            <tbody>
              {inventory.map((record) => {
                const product = productsById.get(record.productId);
                return (
                  <tr key={record.id}>
                    <td className="strong-cell">#{record.productId}</td>
                    <td>{product?.name ?? "Unknown product"}</td>
                    <td>
                      <span
                        className={
                          record.availableQuantity > 0
                            ? "inventory-quantity"
                            : "inventory-quantity inventory-empty"
                        }
                      >
                        {record.availableQuantity}
                      </span>
                    </td>
                    <td>{record.reservedQuantity}</td>
                    <td>
                      {record.availableQuantity + record.reservedQuantity}
                    </td>
                    <td className="muted-cell">
                      {new Date(record.updatedAt).toLocaleString()}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

export default InventoryPage;
