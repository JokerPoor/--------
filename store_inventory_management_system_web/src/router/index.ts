import { createRouter, createWebHistory } from "vue-router";
import auth from "../services/auth";

const routes = [
  { path: "/", redirect: "/dashboard" },
  { path: "/login", component: () => import("../pages/auth/LoginPage.vue") },
  {
    path: "/register",
    component: () => import("../pages/auth/RegisterPage.vue"),
  },
  { path: "/dashboard", component: () => import("../pages/dashboard/DashboardPage.vue") },
  { path: "/users", component: () => import("../pages/users/UsersPage.vue") },
  { path: "/roles", component: () => import("../pages/roles/RolesPage.vue") },
  {
    path: "/permissions",
    component: () => import("../pages/permissions/PermissionsPage.vue"),
  },
  { path: "/pages", component: () => import("../pages/pages/PagesPage.vue") },
  { path: "/supplier", component: () => import("../pages/users/SupplierPage.vue") },
  { path: "/customer", component: () => import("../pages/users/CustomerPage.vue") },
  { path: "/supplier/products", component: () => import("../pages/supplier/MyProductsPage.vue") },
  { path: "/supplier/orders", component: () => import("../pages/supplier/SupplierOrderPage.vue") },
  { path: "/customer/shopping", component: () => import("../pages/customer/ShoppingPage.vue") },
  { path: "/ai/sales-stats", component: () => import("../pages/ai/SalesStatsPage.vue") },

  // Inventory
  { path: "/inventory/transfer-logs", component: () => import("../pages/inventory/TransferLogPage.vue") },

  // Amount
  { path: "/amount/orders", component: () => import("../pages/amount/AmountOrderPage.vue") },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, from, next) => {
  console.log('[Router] Navigating from', from.path, 'to', to.path)
  
  if (to.path.startsWith("/login") || to.path.startsWith("/register")) {
    next();
    return;
  }
  try {
    const justInited = await auth.ensureInited(router);
    if (justInited) {
      console.log('[Router] Just initialized, replacing route to', to.path)
      next({ ...to, replace: true });
      return;
    }
    
    // 如果路由匹配不到（404），尝试跳转到首页或第一个菜单
    if (to.matched.length === 0) {
      console.warn('[Router] No route matched for', to.path)
      const menuPaths = auth
        .getMenuPages()
        .filter((p) => !!p.path)
        .map((p) => (p.path.startsWith("/") ? p.path : `/${p.path}`));
      if (menuPaths.length > 0) {
         console.log('[Router] Redirecting to first menu:', menuPaths[0])
         next(menuPaths[0]);
         return;
      }
    } else {
      console.log('[Router] Route matched:', to.matched.map(m => m.path))
    }
    next();
  } catch (err) {
    console.error('[Router] Error in beforeEach:', err)
    next("/login");
  }
});

export default router;
