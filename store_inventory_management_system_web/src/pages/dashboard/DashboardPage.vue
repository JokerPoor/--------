<template>
  <div class="dashboard-container p-6 space-y-6" v-loading="loading">
    <!-- Welcome Banner -->
    <div class="welcome-banner bg-gradient-to-r from-blue-500 to-purple-600 text-white p-6 rounded-lg shadow-lg">
      <h1 class="text-3xl font-bold">
        欢迎回来，{{ user?.userName || user?.userAccount }} 👋
      </h1>
      <p class="mt-2 opacity-90">{{ roleName }} | {{ today }}</p>
    </div>

    <!-- Weather & Quick Stats -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <!-- Weather Card -->
      <el-card shadow="hover" class="stat-card" v-if="weather && weather.length > 0">
        <template #header>
          <div class="flex justify-between items-center">
            <span class="font-semibold">{{ currentCityName }}</span>
            <el-dropdown @command="handleCityChange" trigger="click">
              <span class="text-xs text-blue-500 cursor-pointer">[切换]</span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="city in cities" :key="city.id" :command="city">
                    {{ city.name }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
        <div class="text-center">
          <div class="text-2xl font-bold text-blue-600">{{ weather[0].low }}°C - {{ weather[0].high }}°C</div>
          <div class="text-sm text-gray-500 mt-1">{{ weather[0].text_day }}</div>
        </div>
      </el-card>

      <!-- Quick Stats Cards -->
      <el-card shadow="hover" class="stat-card" v-for="stat in quickStats" :key="stat.title">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-gray-500 text-sm">{{ stat.title }}</div>
            <div class="text-2xl font-bold mt-1" :class="stat.color">{{ stat.value }}</div>
          </div>
          <el-icon :size="40" :class="stat.color">
            <component :is="stat.icon" />
          </el-icon>
        </div>
      </el-card>
    </div>

    <!-- Role-specific Dashboard Content -->
    <!-- 超级管理员 Dashboard -->
    <template v-if="isAdmin">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <el-card shadow="hover">
          <template #header><div class="font-bold">用户统计</div></template>
          <div ref="userStatsChart" style="height: 300px"></div>
        </el-card>
        
        <el-card shadow="hover">
          <template #header><div class="font-bold">角色分布</div></template>
          <div ref="roleDistChart" style="height: 300px"></div>
        </el-card>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <el-card shadow="hover">
          <template #header><div class="font-bold">系统操作日志趋势</div></template>
          <div ref="operationTrendChart" style="height: 300px"></div>
        </el-card>
        
        <el-card shadow="hover">
          <template #header><div class="font-bold">模块访问统计</div></template>
          <div ref="moduleAccessChart" style="height: 300px"></div>
        </el-card>
      </div>
    </template>

    <!-- 门店管理员 Dashboard -->
    <template v-else-if="isStoreAdmin">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <el-card shadow="hover">
          <template #header><div class="font-bold">库存预警</div></template>
          <div ref="inventoryWarningChart" style="height: 300px"></div>
        </el-card>
        
        <el-card shadow="hover">
          <template #header><div class="font-bold">销售趋势</div></template>
          <div ref="salesTrendChart" style="height: 300px"></div>
        </el-card>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <el-card shadow="hover">
          <template #header><div class="font-bold">采购订单状态</div></template>
          <div ref="purchaseStatusChart" style="height: 300px"></div>
        </el-card>
        
        <el-card shadow="hover">
          <template #header><div class="font-bold">热销商品 TOP 10</div></template>
          <div ref="topProductsChart" style="height: 300px"></div>
        </el-card>
      </div>
    </template>

    <!-- 供应商 Dashboard -->
    <template v-else-if="isSupplier">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <el-card shadow="hover">
          <template #header><div class="font-bold">我的订单统计</div></template>
          <div ref="supplierOrderChart" style="height: 300px"></div>
        </el-card>
        
        <el-card shadow="hover">
          <template #header><div class="font-bold">订单金额趋势</div></template>
          <div ref="supplierAmountChart" style="height: 300px"></div>
        </el-card>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <el-card shadow="hover">
          <template #header><div class="font-bold">商品销售排行</div></template>
          <div ref="supplierProductChart" style="height: 300px"></div>
        </el-card>
        
        <el-card shadow="hover">
          <template #header><div class="font-bold">月度收入统计</div></template>
          <div ref="supplierIncomeChart" style="height: 300px"></div>
        </el-card>
      </div>
    </template>

    <!-- 客户 Dashboard -->
    <template v-else-if="isCustomer">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <el-card shadow="hover">
          <template #header><div class="font-bold">我的订单状态</div></template>
          <div ref="customerOrderChart" style="height: 300px"></div>
        </el-card>
        
        <el-card shadow="hover">
          <template #header><div class="font-bold">消费趋势</div></template>
          <div ref="customerSpendChart" style="height: 300px"></div>
        </el-card>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <el-card shadow="hover">
          <template #header><div class="font-bold">购买商品分类</div></template>
          <div ref="customerCategoryChart" style="height: 300px"></div>
        </el-card>
        
        <el-card shadow="hover">
          <template #header><div class="font-bold">月度消费统计</div></template>
          <div ref="customerMonthlyChart" style="height: 300px"></div>
        </el-card>
      </div>
    </template>

    <!-- Quick Actions -->
    <el-card shadow="hover">
      <template #header><div class="font-bold">快捷操作</div></template>
      <div class="flex flex-wrap gap-3">
        <el-button type="primary" @click="router.push('/users')" v-if="isAdmin">
          <el-icon><User /></el-icon> 用户管理
        </el-button>
        <el-button type="success" @click="router.push('/inventory')" v-if="isStoreAdmin">
          <el-icon><Goods /></el-icon> 库存管理
        </el-button>
        <el-button type="warning" @click="router.push('/purchase/order')" v-if="isStoreAdmin">
          <el-icon><ShoppingCart /></el-icon> 采购订单
        </el-button>
        <el-button type="info" @click="router.push('/supplier/orders')" v-if="isSupplier">
          <el-icon><List /></el-icon> 我的订单
        </el-button>
        <el-button type="primary" @click="router.push('/customer/shopping')" v-if="isCustomer">
          <el-icon><ShoppingBag /></el-icon> 购物
        </el-button>
        <el-button @click="showInfo">
          <el-icon><InfoFilled /></el-icon> 个人信息
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { User, Goods, ShoppingCart, List, ShoppingBag, InfoFilled, TrendCharts, DataAnalysis, Wallet } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import auth from '../../services/auth'
import http from '../../services/http'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getAdminStats, getStoreStats, getSupplierStats, getCustomerStats } from '../../services/dashboard'
import type { AdminStats, StoreStats, SupplierStats, CustomerStats } from '../../services/dashboard'

const router = useRouter()
const user = computed(() => auth.state.user)
const today = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long'
})

// Weather
const weather = ref<any[]>([])
const currentCityName = ref('北京朝阳')
const cities = [
  { name: '北京朝阳', id: '110105' },
  { name: '北京海淀', id: '110108' },
  { name: '上海浦东', id: '310115' },
  { name: '广州天河', id: '440106' },
  { name: '深圳南山', id: '440305' },
  { name: '杭州西湖', id: '330106' },
  { name: '成都武侯', id: '510107' },
  { name: '武汉武昌', id: '420106' }
]

async function fetchWeather(districtId: string) {
  try {
    const res = await http.get('/weather', { params: { districtId } })
    if (res.data) weather.value = res.data
  } catch (e) {
    console.error('Failed to fetch weather:', e)
  }
}

function handleCityChange(city: { name: string; id: string }) {
  currentCityName.value = city.name
  localStorage.setItem('weather_city', JSON.stringify(city))
  fetchWeather(city.id)
}

// Role Detection
const isAdmin = computed(() => {
  if (!user.value) return false
  if (user.value.userAccount === 'admin' || user.value.id === 1) return true
  return user.value.roles?.some(r => r.roleName === '超级管理员')
})

const isStoreAdmin = computed(() => {
  if (!user.value) return false
  return user.value.roles?.some(r => r.roleName === '门店管理员')
})

const isSupplier = computed(() => {
  if (!user.value) return false
  return user.value.roles?.some(r => r.roleName === '供应商')
})

const isCustomer = computed(() => {
  if (!user.value) return false
  return user.value.roles?.some(r => r.roleName === '客户')
})

const roleName = computed(() => {
  if (!user.value) return '未知'
  if (user.value.userAccount === 'admin' || user.value.id === 1) return '超级管理员'
  return user.value.roles?.map(r => r.roleName).join(' / ') || '普通用户'
})

// Quick Stats
const quickStats = computed(() => {
  if (isAdmin.value && adminData.value) {
    return [
      { title: '总用户数', value: adminData.value.totalUsers.toString(), icon: User, color: 'text-blue-500' },
      { title: '今日订单', value: adminData.value.todayOrders.toString(), icon: ShoppingCart, color: 'text-green-500' },
      { title: '总收入', value: `¥${(adminData.value.totalRevenue / 10000).toFixed(1)}K`, icon: Wallet, color: 'text-orange-500' }
    ]
  } else if (isStoreAdmin.value && storeData.value) {
    return [
      { title: '库存商品', value: storeData.value.totalProducts.toString(), icon: Goods, color: 'text-blue-500' },
      { title: '待处理订单', value: storeData.value.pendingOrders.toString(), icon: ShoppingCart, color: 'text-orange-500' },
      { title: '预警商品', value: storeData.value.warningProducts.toString(), icon: TrendCharts, color: 'text-red-500' }
    ]
  } else if (isSupplier.value && supplierData.value) {
    return [
      { title: '待发货订单', value: supplierData.value.pendingOrders.toString(), icon: ShoppingCart, color: 'text-orange-500' },
      { title: '本月收入', value: `¥${(supplierData.value.monthlyIncome / 1000).toFixed(1)}K`, icon: Wallet, color: 'text-green-500' },
      { title: '商品数量', value: supplierData.value.totalProducts.toString(), icon: Goods, color: 'text-blue-500' }
    ]
  } else if (isCustomer.value && customerData.value) {
    return [
      { title: '我的订单', value: customerData.value.totalOrders.toString(), icon: ShoppingCart, color: 'text-blue-500' },
      { title: '本月消费', value: `¥${(customerData.value.monthlySpend / 1000).toFixed(1)}K`, icon: Wallet, color: 'text-orange-500' }
    ]
  }
  
  // Default fallback
  return [
    { title: '加载中...', value: '-', icon: User, color: 'text-gray-400' },
    { title: '加载中...', value: '-', icon: ShoppingCart, color: 'text-gray-400' }
  ]
})

// Chart Refs
const userStatsChart = ref<HTMLElement>()
const roleDistChart = ref<HTMLElement>()
const operationTrendChart = ref<HTMLElement>()
const moduleAccessChart = ref<HTMLElement>()
const inventoryWarningChart = ref<HTMLElement>()
const salesTrendChart = ref<HTMLElement>()
const purchaseStatusChart = ref<HTMLElement>()
const topProductsChart = ref<HTMLElement>()
const supplierOrderChart = ref<HTMLElement>()
const supplierAmountChart = ref<HTMLElement>()
const supplierProductChart = ref<HTMLElement>()
const supplierIncomeChart = ref<HTMLElement>()
const customerOrderChart = ref<HTMLElement>()
const customerSpendChart = ref<HTMLElement>()
const customerCategoryChart = ref<HTMLElement>()
const customerMonthlyChart = ref<HTMLElement>()

const chartInstances: ECharts[] = []

// Data refs
const adminData = ref<AdminStats | null>(null)
const storeData = ref<StoreStats | null>(null)
const supplierData = ref<SupplierStats | null>(null)
const customerData = ref<CustomerStats | null>(null)
const loading = ref(false)

// Fetch dashboard data based on role
async function fetchDashboardData() {
  loading.value = true
  try {
    if (isAdmin.value) {
      adminData.value = await getAdminStats()
    } else if (isStoreAdmin.value) {
      storeData.value = await getStoreStats()
    } else if (isSupplier.value) {
      supplierData.value = await getSupplierStats()
    } else if (isCustomer.value) {
      customerData.value = await getCustomerStats()
    }
  } catch (error) {
    console.error('Failed to fetch dashboard data:', error)
    ElMessage.error('获取仪表盘数据失败')
  } finally {
    loading.value = false
  }
}

// Initialize Charts for Admin
function initAdminCharts() {
  if (!adminData.value) return
  // User Stats Chart
  if (userStatsChart.value) {
    const chart = echarts.init(userStatsChart.value)
    const dates = adminData.value.userTrend.map(t => t.date)
    const newUsers = adminData.value.userTrend.map(t => t.newUsers)
    const activeUsers = adminData.value.userTrend.map(t => t.activeUsers)
    
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['新增用户', '活跃用户'] },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value' },
      series: [
        { name: '新增用户', type: 'line', data: newUsers, smooth: true },
        { name: '活跃用户', type: 'line', data: activeUsers, smooth: true }
      ]
    })
    chartInstances.push(chart)
  }

  // Role Distribution Chart
  if (roleDistChart.value) {
    const chart = echarts.init(roleDistChart.value)
    const roleData = adminData.value.roleDistribution.map(r => ({
      value: r.count,
      name: r.roleName
    }))
    
    chart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        name: '角色分布',
        type: 'pie',
        radius: '50%',
        data: roleData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
    chartInstances.push(chart)
  }

  // Operation Trend Chart
  if (operationTrendChart.value) {
    const chart = echarts.init(operationTrendChart.value)
    const months = adminData.value.operationTrend.map(t => t.month)
    const counts = adminData.value.operationTrend.map(t => t.count)
    
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: months },
      yAxis: { type: 'value' },
      series: [{
        name: '操作次数',
        type: 'bar',
        data: counts,
        itemStyle: { color: '#5470c6' }
      }]
    })
    chartInstances.push(chart)
  }

  // Module Access Chart
  if (moduleAccessChart.value) {
    const chart = echarts.init(moduleAccessChart.value)
    const modules = adminData.value.moduleAccess.map(m => m.moduleName)
    const counts = adminData.value.moduleAccess.map(m => m.count)
    
    chart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value' },
      yAxis: {
        type: 'category',
        data: modules
      },
      series: [{
        name: '访问次数',
        type: 'bar',
        data: counts,
        itemStyle: { color: '#91cc75' }
      }]
    })
    chartInstances.push(chart)
  }
}

// Initialize Charts for Store Admin
function initStoreAdminCharts() {
  if (!storeData.value) return
  
  // Inventory Warning Chart
  if (inventoryWarningChart.value) {
    const chart = echarts.init(inventoryWarningChart.value)
    const inventoryData = storeData.value.inventoryWarning.map(w => ({
      value: w.count,
      name: w.status
    }))
    
    chart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        name: '库存状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        data: inventoryData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
    chartInstances.push(chart)
  }

  // Sales Trend Chart
  if (salesTrendChart.value) {
    const chart = echarts.init(salesTrendChart.value)
    const months = storeData.value.salesTrend.map(t => t.month)
    const amounts = storeData.value.salesTrend.map(t => t.amount)
    const quantities = storeData.value.salesTrend.map(t => t.quantity)
    
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['销售额', '销售量'] },
      xAxis: { type: 'category', data: months },
      yAxis: [
        { type: 'value', name: '销售额(万元)' },
        { type: 'value', name: '销售量(件)' }
      ],
      series: [
        {
          name: '销售额',
          type: 'bar',
          data: amounts,
          itemStyle: { color: '#5470c6' }
        },
        {
          name: '销售量',
          type: 'line',
          yAxisIndex: 1,
          data: quantities,
          smooth: true
        }
      ]
    })
    chartInstances.push(chart)
  }

  // Purchase Status Chart
  if (purchaseStatusChart.value) {
    const chart = echarts.init(purchaseStatusChart.value)
    const purchaseData = storeData.value.purchaseStatus.map(p => ({
      value: p.count,
      name: p.status
    }))
    
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        name: '订单状态',
        type: 'pie',
        radius: '50%',
        data: purchaseData
      }]
    })
    chartInstances.push(chart)
  }

  // Top Products Chart
  if (topProductsChart.value) {
    const chart = echarts.init(topProductsChart.value)
    const products = storeData.value.topProducts.map(p => p.productName)
    const sales = storeData.value.topProducts.map(p => p.sales)
    
    chart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value' },
      yAxis: {
        type: 'category',
        data: products
      },
      series: [{
        name: '销售量',
        type: 'bar',
        data: sales,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#83bff6' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }]
    })
    chartInstances.push(chart)
  }
}


// Initialize Charts for Supplier
function initSupplierCharts() {
  if (!supplierData.value) return
  
  // Supplier Order Chart
  if (supplierOrderChart.value) {
    const chart = echarts.init(supplierOrderChart.value)
    const orderData = supplierData.value.orderStatus.map(o => ({
      value: o.count,
      name: o.status
    }))
    
    chart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        name: '订单状态',
        type: 'pie',
        radius: '50%',
        data: orderData
      }]
    })
    chartInstances.push(chart)
  }

  // Supplier Amount Chart
  if (supplierAmountChart.value) {
    const chart = echarts.init(supplierAmountChart.value)
    const months = supplierData.value.amountTrend.map(t => t.month)
    const amounts = supplierData.value.amountTrend.map(t => t.amount)
    
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: months },
      yAxis: { type: 'value', name: '金额(元)' },
      series: [{
        name: '订单金额',
        type: 'line',
        data: amounts,
        smooth: true,
        areaStyle: { opacity: 0.3 },
        itemStyle: { color: '#ee6666' }
      }]
    })
    chartInstances.push(chart)
  }

  // Supplier Product Chart
  if (supplierProductChart.value) {
    const chart = echarts.init(supplierProductChart.value)
    const products = supplierData.value.productRanking.map(p => p.productName)
    const sales = supplierData.value.productRanking.map(p => p.sales)
    
    chart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value' },
      yAxis: {
        type: 'category',
        data: products
      },
      series: [{
        name: '销售量',
        type: 'bar',
        data: sales,
        itemStyle: { color: '#fac858' }
      }]
    })
    chartInstances.push(chart)
  }

  // Supplier Income Chart
  if (supplierIncomeChart.value) {
    const chart = echarts.init(supplierIncomeChart.value)
    const months = supplierData.value.monthlyIncomeTrend.map(m => m.month)
    const incomes = supplierData.value.monthlyIncomeTrend.map(m => m.amount)
    
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: months },
      yAxis: { type: 'value', name: '收入(元)' },
      series: [{
        name: '月度收入',
        type: 'bar',
        data: incomes,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }]
    })
    chartInstances.push(chart)
  }
}

// Initialize Charts for Customer
function initCustomerCharts() {
  if (!customerData.value) return
  
  // Customer Order Chart
  if (customerOrderChart.value) {
    const chart = echarts.init(customerOrderChart.value)
    const orderData = customerData.value.orderStatus.map(o => ({
      value: o.count,
      name: o.status
    }))
    
    chart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        name: '订单状态',
        type: 'pie',
        radius: '50%',
        data: orderData
      }]
    })
    chartInstances.push(chart)
  }

  // Customer Spend Chart
  if (customerSpendChart.value) {
    const chart = echarts.init(customerSpendChart.value)
    const months = customerData.value.spendTrend.map(t => t.month)
    const amounts = customerData.value.spendTrend.map(t => t.amount)
    
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: months },
      yAxis: { type: 'value', name: '消费(元)' },
      series: [{
        name: '月度消费',
        type: 'line',
        data: amounts,
        smooth: true,
        areaStyle: { opacity: 0.3 },
        itemStyle: { color: '#73c0de' }
      }]
    })
    chartInstances.push(chart)
  }

  // Customer Category Chart
  if (customerCategoryChart.value) {
    const chart = echarts.init(customerCategoryChart.value)
    const categoryData = customerData.value.categoryStats.map(c => ({
      value: c.count,
      name: c.category
    }))
    
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        name: '商品分类',
        type: 'pie',
        radius: ['40%', '70%'],
        data: categoryData
      }]
    })
    chartInstances.push(chart)
  }

  // Customer Monthly Chart
  if (customerMonthlyChart.value) {
    const chart = echarts.init(customerMonthlyChart.value)
    const months = customerData.value.monthlySpendTrend.map(m => m.month)
    const amounts = customerData.value.monthlySpendTrend.map(m => m.amount)
    
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: months },
      yAxis: { type: 'value', name: '消费(元)' },
      series: [{
        name: '月度消费',
        type: 'bar',
        data: amounts,
        itemStyle: { color: '#91cc75' }
      }]
    })
    chartInstances.push(chart)
  }
}

function showInfo() {
  ElMessageBox.alert(`
    <p><strong>账号：</strong>${user.value?.userAccount}</p>
    <p><strong>昵称：</strong>${user.value?.userName}</p>
    <p><strong>手机：</strong>${user.value?.phone || '未设置'}</p>
    <p><strong>邮箱：</strong>${user.value?.email || '未设置'}</p>
  `, '个人信息', { dangerouslyUseHTMLString: true })
}

onMounted(async () => {
  // Load weather
  const savedCity = localStorage.getItem('weather_city')
  if (savedCity) {
    try {
      const city = JSON.parse(savedCity)
      currentCityName.value = city.name
      fetchWeather(city.id)
    } catch {
      fetchWeather('110105')
    }
  } else {
    fetchWeather('110105')
  }

  // Fetch dashboard data
  await fetchDashboardData()

  // Initialize charts based on role after data is loaded
  setTimeout(() => {
    if (isAdmin.value) {
      initAdminCharts()
    } else if (isStoreAdmin.value) {
      initStoreAdminCharts()
    } else if (isSupplier.value) {
      initSupplierCharts()
    } else if (isCustomer.value) {
      initCustomerCharts()
    }
  }, 100)
})

onBeforeUnmount(() => {
  chartInstances.forEach(chart => chart.dispose())
})
</script>

<style scoped lang="scss">
.dashboard-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.stat-card {
  transition: all 0.3s;
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.welcome-banner {
  animation: fadeInDown 0.6s ease-out;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
