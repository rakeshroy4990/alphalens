import axios from 'axios';
import { resolveApiBaseUrl } from './apiBaseUrl';

const baseURL = resolveApiBaseUrl(import.meta.env.VITE_API_BASE_URL);

export const apiClient = axios.create({
  baseURL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
});
