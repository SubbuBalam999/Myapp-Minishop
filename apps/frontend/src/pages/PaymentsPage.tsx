import axios from "axios";
import { type FormEvent, useEffect, useMemo, useState } from "react";
import {
  createPayment,
  getOrders,
  getPayments,
  getUsers,
  refundPayment,
  type OrderRecord,
  type PaymentRecord,
  type User,
} from "../services/api";

const currencyFormatter = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});

function PaymentsPage() {
  const [orders, setOrders] = useState<OrderRecord[]>([]);
  const [payments, setPayments] = useState<PaymentRecord[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [selectedOrderId, setSelectedOrderId] = useState("");
  const [paymentMethod, setPaymentMethod] = useState("CARD");
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [refundingId, setRefundingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();

    async function loadPaymentData() {
      try {
        setIsLoading(true);
        setError(null);
        const [orderData, paymentData, userData] = await Promise.all([
          getOrders(controller.signal),
          getPayments(controller.signal),
          getUsers(controller.signal),
        ]);
        setOrders(orderData);
        setPayments(paymentData);
        setUsers(userData);
      } catch (requestError) {
        if (!axios.isCancel(requestError)) {
          setError("Unable to load payments through the API Gateway.");
        }
      } finally {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      }
    }

    void loadPaymentData();
    return () => controller.abort();
  }, []);

  const usersById = useMemo(
    () => new Map(users.map((user) => [user.id, user])),
    [users],
  );

  const selectedOrder = useMemo(
    () => orders.find((order) => order.id === Number(selectedOrderId)),
    [orders, selectedOrderId],
  );

  async function handleCreatePayment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedOrder) {
      setError("Select an order before creating a payment.");
      return;
    }

    try {
      setIsSubmitting(true);
      setError(null);
      setSuccess(null);
      const payment = await createPayment({
        orderId: selectedOrder.id,
        userId: selectedOrder.userId,
        amount: selectedOrder.totalAmount,
        paymentMethod,
      });
      setPayments((current) => [payment, ...current]);
      setSuccess(
        `Payment #${payment.id} completed successfully for order #${payment.orderId}.`,
      );
    } catch {
      setError("Unable to create the simulated payment.");
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleRefund(payment: PaymentRecord) {
    try {
      setRefundingId(payment.id);
      setError(null);
      setSuccess(null);
      const refunded = await refundPayment(payment.id);
      setPayments((current) =>
        current.map((item) => (item.id === refunded.id ? refunded : item)),
      );
      setSuccess(`Payment #${refunded.id} was refunded.`);
    } catch {
      setError(`Unable to refund payment #${payment.id}.`);
    } finally {
      setRefundingId(null);
    }
  }

  return (
    <section>
      <div className="page-heading">
        <div>
          <div className="eyebrow">Simulated checkout</div>
          <h1>Payments</h1>
        </div>
        {!isLoading && !error && (
          <span className="result-count">
            {payments.length} {payments.length === 1 ? "payment" : "payments"}
          </span>
        )}
      </div>

      {error && (
        <div className="status-panel status-error payment-message" role="alert">
          {error}
        </div>
      )}

      {success && (
        <div className="status-panel status-success payment-message">
          {success}
        </div>
      )}

      <div className="payment-layout">
        <aside className="payment-form-card">
          <h2>Create payment</h2>
          <p>
            Select an order. MiniShop will simulate a successful payment for
            its full total.
          </p>

          <form onSubmit={handleCreatePayment}>
            <label>
              Order
              <select
                value={selectedOrderId}
                onChange={(event) => setSelectedOrderId(event.target.value)}
                disabled={isLoading || orders.length === 0}
              >
                <option value="">Select an order</option>
                {orders.map((order) => (
                  <option key={order.id} value={order.id}>
                    Order #{order.id} -{" "}
                    {currencyFormatter.format(order.totalAmount)}
                  </option>
                ))}
              </select>
            </label>

            <label>
              Payment method
              <select
                value={paymentMethod}
                onChange={(event) => setPaymentMethod(event.target.value)}
              >
                <option value="CARD">Card</option>
                <option value="PAYPAL">PayPal</option>
                <option value="BANK_TRANSFER">Bank transfer</option>
              </select>
            </label>

            {selectedOrder && (
              <div className="payment-order-summary">
                <span>
                  {usersById.get(selectedOrder.userId)?.name ??
                    `User #${selectedOrder.userId}`}
                </span>
                <strong>
                  {currencyFormatter.format(selectedOrder.totalAmount)}
                </strong>
              </div>
            )}

            <button
              className="button button-primary"
              type="submit"
              disabled={!selectedOrder || isSubmitting}
            >
              {isSubmitting ? "Processing..." : "Create payment"}
            </button>
          </form>

          {!isLoading && orders.length === 0 && (
            <p className="payment-empty-note">
              Create an order before recording a payment.
            </p>
          )}
        </aside>

        <div className="payment-content">
          {isLoading && <div className="status-panel">Loading payments...</div>}

          {!isLoading && !error && payments.length === 0 && (
            <div className="status-panel">
              No payments have been recorded yet.
            </div>
          )}

          {!isLoading && !error && payments.length > 0 && (
            <div className="payments-list">
              {payments.map((payment) => (
                <article className="payment-card" key={payment.id}>
                  <div>
                    <span className="product-id">
                      Payment #{payment.id} for order #{payment.orderId}
                    </span>
                    <h2>
                      {usersById.get(payment.userId)?.name ??
                        `User #${payment.userId}`}
                    </h2>
                    <p>
                      {payment.paymentMethod.replaceAll("_", " ")} ·{" "}
                      {new Date(payment.createdAt).toLocaleString()}
                    </p>
                  </div>

                  <div className="payment-card-summary">
                    <span
                      className={`payment-status payment-status-${payment.status.toLowerCase()}`}
                    >
                      {payment.status}
                    </span>
                    <strong>{currencyFormatter.format(payment.amount)}</strong>
                    {payment.status === "SUCCESS" && (
                      <button
                        className="cart-remove"
                        type="button"
                        disabled={refundingId === payment.id}
                        onClick={() => void handleRefund(payment)}
                      >
                        {refundingId === payment.id
                          ? "Refunding..."
                          : "Refund"}
                      </button>
                    )}
                  </div>
                </article>
              ))}
            </div>
          )}
        </div>
      </div>
    </section>
  );
}

export default PaymentsPage;
