import axios from "axios";
import { useEffect, useState } from "react";
import { getProducts, type Product } from "../services/api";

const currencyFormatter = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});

function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();

    async function loadProducts() {
      try {
        setIsLoading(true);
        setError(null);
        const data = await getProducts(controller.signal);
        setProducts(data);
      } catch (requestError) {
        if (!axios.isCancel(requestError)) {
          setError(
            "Unable to load products. Check that the API Gateway is running.",
          );
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      }
    }

    void loadProducts();

    return () => controller.abort();
  }, []);

  return (
    <section>
      <div className="page-heading">
        <div>
          <div className="eyebrow">Catalog</div>
          <h1>Products</h1>
        </div>
        {!isLoading && !error && (
          <span className="result-count">
            {products.length} {products.length === 1 ? "product" : "products"}
          </span>
        )}
      </div>

      {isLoading && <div className="status-panel">Loading products...</div>}

      {error && (
        <div className="status-panel status-error" role="alert">
          {error}
        </div>
      )}

      {!isLoading && !error && products.length === 0 && (
        <div className="status-panel">No products are available yet.</div>
      )}

      {!isLoading && !error && products.length > 0 && (
        <div className="product-grid">
          {products.map((product) => (
            <article className="product-card" key={product.id}>
              <div className="product-card-top">
                <span className="product-id">Product #{product.id}</span>
                <span
                  className={
                    product.stockQuantity > 0
                      ? "stock-badge"
                      : "stock-badge out-of-stock"
                  }
                >
                  {product.stockQuantity > 0
                    ? `${product.stockQuantity} in stock`
                    : "Out of stock"}
                </span>
              </div>
              <h2>{product.name}</h2>
              <p>
                {product.description || "No description is available for this product."}
              </p>
              <div className="product-price">
                {currencyFormatter.format(product.price)}
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default ProductsPage;
