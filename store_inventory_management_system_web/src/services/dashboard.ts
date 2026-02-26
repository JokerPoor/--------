import http from './http'

export interface AdminStats {
  totalUsers: number
  activeUsers: number
  todayOrders: number
  totalRevenue: number
  userTrend: { date: string; newUsers: number; activeUsers: number }[]
  roleDistribution: { roleName: string; count: number }[]
  operationTrend: { month: string; count: number }[]
  moduleAccess: { moduleName: string; count: number }[]
}

export interface StoreStats {
  totalProducts: number
  pendingOrders: number
  warningProducts: number
  inventoryWarning: { status: string; count: number }[]
  salesTrend: { month: string; amount: number; quantity: number }[]
  purchaseStatus: { status: string; count: number }[]
  topProducts: { productName: string; sales: number }[]
}

export interface SupplierStats {
  pendingOrders: number
  monthlyIncome: number
  totalProducts: number
  orderStatus: { status: string; count: number }[]
  amountTrend: { month: string; amount: number }[]
  productRanking: { productName: string; sales: number }[]
  monthlyIncomeTrend: { month: string; amount: number }[]
}

export interface CustomerStats {
  totalOrders: number
  monthlySpend: number
  orderStatus: { status: string; count: number }[]
  spendTrend: { month: string; amount: number }[]
  categoryStats: { category: string; count: number }[]
  monthlySpendTrend: { month: string; amount: number }[]
}

export async function getAdminStats(): Promise<AdminStats> {
  const res = await http.get('/dashboard/admin/stats')
  return res.data
}

export async function getStoreStats(): Promise<StoreStats> {
  const res = await http.get('/dashboard/store/stats')
  return res.data
}

export async function getSupplierStats(): Promise<SupplierStats> {
  const res = await http.get('/dashboard/supplier/stats')
  return res.data
}

export async function getCustomerStats(): Promise<CustomerStats> {
  const res = await http.get('/dashboard/customer/stats')
  return res.data
}
