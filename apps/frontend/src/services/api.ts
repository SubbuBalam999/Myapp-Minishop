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

export default api;
