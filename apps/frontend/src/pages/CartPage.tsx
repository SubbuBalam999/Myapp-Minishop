import axios from "axios";
import { useEffect, useMemo, useState, type FormEvent } from "react";
import {
  addCartItem,
  clearCart,
  createOrder,
  deleteCartItem,
  getCart,
  getProducts,
  getUsers,
  type CartItem,
  type Product,
  type User,
} from "../services/api";

const currencyFormatter = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});

function CartPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [items, setItems] = useState<CartItem[]>([]);
  const [selectedUserId, setSelectedUserId] = useState("");
  const [selectedProductId, setSelectedProductId] = useState("");
  const [quantity, setQuantity] = useState(1);
  const [isLoadingOptions, setIsLoadingOptions] = useState(true);
  const [isLoadingCart, setIsLoadingCart] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();

    async function loadOptions() {
      try {
        setError(null);
        const [userData, productData] = await Promise.all([
          getUsers(controller.signal),
          getProducts(controller.signal),
        ]);
        setUsers(userData);
        setProducts(productData);

        if (userData.length > 0) {
          setSelectedUserId(String(userData[0].id));
        }
        if (productData.length > 0) {
          setSelectedProductId(String(productData[0].id));
        }
      } catch (requestError) {
        if (!axios.isCancel(requestError)) {
          setError("Unable to load cart options through the API Gateway.");
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoadingOptions(false);
        }
      }
    }

    void loadOptions();
    return () => controller.abort();
  }, []);

  useEffect(() => {
    if (!selectedUserId) {
      setItems([]);
      return;
    }

    const controller = new AbortController();

    async function loadCart() {
      try {
        setIsLoadingCart(true);
        setError(null);
        setItems(await getCart(Number(selectedUserId), controller.signal));
      } catch (requestError) {
        if (!axios.isCancel(requestError)) {
          setError("Unable to load this user's cart through the API Gateway.");
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoadingCart(false);
        }
      }
    }

    void loadCart();
    return () => controller.abort();
  }, [selectedUserId]);

  const productsById = useMemo(
    () => new Map(products.map((product) => [product.id, product])),
    [products],
  );

  const itemCount = items.reduce((total, item) => total + item.quantity, 0);
  const estimatedTotal = items.reduce((total, item) => {
    const product = productsById.get(item.productId);
    return total + (product?.price ?? 0) * item.quantity;
  }, 0);

  async function handleAddItem(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedUserId || !selectedProductId || quantity < 1) {
      return;
    }

    try {
      setIsSaving(true);
      setError(null);
      setSuccess(null);
      await addCartItem({
        userId: Number(selectedUserId),
        productId: Number(selectedProductId),
        quantity,
      });
      setItems(await getCart(Number(selectedUserId)));
      setQuantity(1);
    } catch {
      setError("Unable to add the item through the API Gateway.");
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDeleteItem(id: number) {
    try {
      setIsSaving(true);
      setError(null);
      setSuccess(null);
      await deleteCartItem(id);
      setItems((currentItems) =>
        currentItems.filter((item) => item.id !== id),
      );
    } catch {
      setError("Unable to remove the cart item through the API Gateway.");
    } finally {
      setIsSaving(false);
    }
  }

  async function handleClearCart() {
    if (!selectedUserId) {
      return;
    }

    try {
      setIsSaving(true);
      setError(null);
      setSuccess(null);
      await clearCart(Number(selectedUserId));
      setItems([]);
    } catch {
      setError("Unable to clear the cart through the API Gateway.");
    } finally {
      setIsSaving(false);
    }
  }

  async function handleCreateOrder() {
    if (!selectedUserId || items.length === 0) {
      return;
    }

    const orderItems = items.flatMap((item) => {
      const product = productsById.get(item.productId);
      return product
        ? [
            {
              productId: item.productId,
              productName: product.name,
              quantity: item.quantity,
              price: product.price,
            },
          ]
        : [];
    });

    if (orderItems.length !== items.length) {
      setError("Every cart item needs a matching product before checkout.");
      return;
    }

    try {
      setIsSaving(true);
      setError(null);
      setSuccess(null);
      const order = await createOrder({
        userId: Number(selectedUserId),
        items: orderItems,
      });

      try {
        await clearCart(Number(selectedUserId));
        setItems([]);
        setSuccess(`Order #${order.id} was created successfully.`);
      } catch {
        setSuccess(`Order #${order.id} was created.`);
        setError("The order was saved, but the cart could not be cleared.");
      }
    } catch {
      setError("Unable to create the order through the API Gateway.");
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <section>
      <div className="page-heading">
        <div>
          <div className="eyebrow">Shopping session</div>
          <h1>Cart</h1>
        </div>
        <span className="result-count">
          {itemCount} {itemCount === 1 ? "item" : "items"}
        </span>
      </div>

      {error && (
        <div className="status-panel status-error cart-error" role="alert">
          {error}
        </div>
      )}

      {success && (
        <div className="status-panel status-success cart-error" role="status">
          {success}
        </div>
      )}

      <div className="cart-layout">
        <aside className="cart-controls">
          <label>
            Shopper
            <select
              value={selectedUserId}
              onChange={(event) => setSelectedUserId(event.target.value)}
              disabled={isLoadingOptions || users.length === 0}
            >
              {users.length === 0 && <option value="">No users available</option>}
              {users.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.name}
                </option>
              ))}
            </select>
          </label>

          <form onSubmit={handleAddItem}>
            <label>
              Product
              <select
                value={selectedProductId}
                onChange={(event) => setSelectedProductId(event.target.value)}
                disabled={isLoadingOptions || products.length === 0}
              >
                {products.length === 0 && (
                  <option value="">No products available</option>
                )}
                {products.map((product) => (
                  <option key={product.id} value={product.id}>
                    {product.name} - {currencyFormatter.format(product.price)}
                  </option>
                ))}
              </select>
            </label>

            <label>
              Quantity
              <input
                type="number"
                min="1"
                value={quantity}
                onChange={(event) => setQuantity(Number(event.target.value))}
              />
            </label>

            <button
              className="button button-primary"
              type="submit"
              disabled={
                isSaving ||
                !selectedUserId ||
                !selectedProductId ||
                quantity < 1
              }
            >
              Add to cart
            </button>
          </form>
        </aside>

        <div className="cart-content">
          {isLoadingCart && (
            <div className="status-panel">Loading cart items...</div>
          )}

          {!isLoadingCart && items.length === 0 && (
            <div className="status-panel">
              This cart is empty. Choose a product to get started.
            </div>
          )}

          {!isLoadingCart && items.length > 0 && (
            <>
              <div className="cart-items">
                {items.map((item) => {
                  const product = productsById.get(item.productId);
                  return (
                    <article className="cart-item" key={item.id}>
                      <div>
                        <span className="product-id">Item #{item.id}</span>
                        <h2>{product?.name ?? `Product #${item.productId}`}</h2>
                        <p>
                          Quantity {item.quantity}
                          {product &&
                            ` at ${currencyFormatter.format(product.price)} each`}
                        </p>
                      </div>
                      <button
                        className="cart-remove"
                        type="button"
                        disabled={isSaving}
                        onClick={() => void handleDeleteItem(item.id)}
                      >
                        Remove
                      </button>
                    </article>
                  );
                })}
              </div>

              <div className="cart-summary">
                <div>
                  <span>Estimated total</span>
                  <strong>{currencyFormatter.format(estimatedTotal)}</strong>
                </div>
                <div className="cart-summary-actions">
                  <button
                    className="button button-secondary"
                    type="button"
                    disabled={isSaving}
                    onClick={() => void handleClearCart()}
                  >
                    Clear cart
                  </button>
                  <button
                    className="button button-primary"
                    type="button"
                    disabled={isSaving}
                    onClick={() => void handleCreateOrder()}
                  >
                    Create order
                  </button>
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </section>
  );
}

export default CartPage;
