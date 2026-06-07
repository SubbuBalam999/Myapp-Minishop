import axios from "axios";
import { useEffect, useMemo, useState } from "react";
import {
  getOrders,
  getOrdersForUser,
  getUsers,
  type OrderRecord,
  type User,
} from "../services/api";

const currencyFormatter = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});

function OrdersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [orders, setOrders] = useState<OrderRecord[]>([]);
  const [selectedUserId, setSelectedUserId] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();

    async function loadUsers() {
      try {
        setUsers(await getUsers(controller.signal));
      } catch (requestError) {
        if (!axios.isCancel(requestError)) {
          setError("Unable to load shoppers through the API Gateway.");
        }
      }
    }

    void loadUsers();
    return () => controller.abort();
  }, []);

  useEffect(() => {
    const controller = new AbortController();

    async function loadOrders() {
      try {
        setIsLoading(true);
        setError(null);
        const data = selectedUserId
          ? await getOrdersForUser(Number(selectedUserId), controller.signal)
          : await getOrders(controller.signal);
        setOrders(data);
      } catch (requestError) {
        if (!axios.isCancel(requestError)) {
          setError("Unable to load order history through the API Gateway.");
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      }
    }

    void loadOrders();
    return () => controller.abort();
  }, [selectedUserId]);

  const usersById = useMemo(
    () => new Map(users.map((user) => [user.id, user])),
    [users],
  );

  return (
    <section>
      <div className="page-heading">
        <div>
          <div className="eyebrow">Purchase history</div>
          <h1>Orders</h1>
        </div>
        {!isLoading && !error && (
          <span className="result-count">
            {orders.length} {orders.length === 1 ? "order" : "orders"}
          </span>
        )}
      </div>

      <div className="orders-toolbar">
        <label>
          Shopper
          <select
            value={selectedUserId}
            onChange={(event) => setSelectedUserId(event.target.value)}
          >
            <option value="">All shoppers</option>
            {users.map((user) => (
              <option key={user.id} value={user.id}>
                {user.name}
              </option>
            ))}
          </select>
        </label>
      </div>

      {isLoading && <div className="status-panel">Loading orders...</div>}

      {error && (
        <div className="status-panel status-error" role="alert">
          {error}
        </div>
      )}

      {!isLoading && !error && orders.length === 0 && (
        <div className="status-panel">
          No orders have been created for this selection.
        </div>
      )}

      {!isLoading && !error && orders.length > 0 && (
        <div className="orders-list">
          {orders.map((order) => (
            <article className="order-card" key={order.id}>
              <div className="order-card-header">
                <div>
                  <span className="product-id">Order #{order.id}</span>
                  <h2>
                    {usersById.get(order.userId)?.name ??
                      `User #${order.userId}`}
                  </h2>
                  <p>{new Date(order.createdAt).toLocaleString()}</p>
                </div>
                <div className="order-card-summary">
                  <span
                    className={`order-status order-status-${order.status.toLowerCase()}`}
                  >
                    {order.status}
                  </span>
                  <strong>
                    {currencyFormatter.format(order.totalAmount)}
                  </strong>
                </div>
              </div>

              <div className="order-items">
                {order.items.map((item) => (
                  <div className="order-item-row" key={item.id}>
                    <div>
                      <strong>{item.productName}</strong>
                      <span>Product #{item.productId}</span>
                    </div>
                    <span>
                      {item.quantity} x {currencyFormatter.format(item.price)}
                    </span>
                  </div>
                ))}
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default OrdersPage;
