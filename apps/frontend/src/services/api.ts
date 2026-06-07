import axios from "axios";

export interface User {
  id: number;
  name: string;
  email: string;
  createdAt: string;
}

export interface Product {
  id: number;
  name: string;
  description: string | null;
  price: number;
  stockQuantity: number;
  createdAt: string;
}

export interface CartItem {
  id: number;
  userId: number;
  productId: number;
  quantity: number;
  createdAt: string;
}

export interface AddCartItemRequest {
  userId: number;
  productId: number;
  quantity: number;
}

export type OrderStatus = "CREATED" | "PAID" | "CANCELLED";

export interface OrderItem {
  id: number;
  orderId: number;
  productId: number;
  productName: string;
  quantity: number;
  price: number;
}

export interface OrderRecord {
  id: number;
  userId: number;
  totalAmount: number;
  status: OrderStatus;
  createdAt: string;
  items: OrderItem[];
}

export interface CreateOrderRequest {
  userId: number;
  items: Array<{
    productId: number;
    productName: string;
    quantity: number;
    price: number;
  }>;
}

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  timeout: 10000,
  headers: {
    Accept: "application/json",
  },
});

export async function getUsers(signal?: AbortSignal): Promise<User[]> {
  const response = await api.get<User[]>("/api/users", { signal });
  return response.data;
}

export async function getProducts(signal?: AbortSignal): Promise<Product[]> {
  const response = await api.get<Product[]>("/api/products", { signal });
  return response.data;
}

export async function getCart(
  userId: number,
  signal?: AbortSignal,
): Promise<CartItem[]> {
  const response = await api.get<CartItem[]>(`/api/cart/${userId}`, { signal });
  return response.data;
}

export async function addCartItem(
  item: AddCartItemRequest,
): Promise<CartItem> {
  const response = await api.post<CartItem>("/api/cart/items", item);
  return response.data;
}

export async function deleteCartItem(id: number): Promise<void> {
  await api.delete(`/api/cart/items/${id}`);
}

export async function clearCart(userId: number): Promise<void> {
  await api.delete(`/api/cart/${userId}`);
}

export async function getOrders(
  signal?: AbortSignal,
): Promise<OrderRecord[]> {
  const response = await api.get<OrderRecord[]>("/api/orders", { signal });
  return response.data;
}

export async function getOrdersForUser(
  userId: number,
  signal?: AbortSignal,
): Promise<OrderRecord[]> {
  const response = await api.get<OrderRecord[]>(`/api/orders/user/${userId}`, {
    signal,
  });
  return response.data;
}

export async function createOrder(
  order: CreateOrderRequest,
): Promise<OrderRecord> {
  const response = await api.post<OrderRecord>("/api/orders", order);
  return response.data;
}

export default api;
