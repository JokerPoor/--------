import { createRouter, createWebHistory } from "vue-router";
import auth from "../services/auth";

const routes = [
  { path: "/", redirect: "/users" },
  { path: "/login", component: () => import("../pages/auth/LoginPage.vue") },
  {
    path: "/register",
    component: () => import("../pages/auth/RegisterPage.vue"),
  },
  { path: "/users", component: () => import("../pages/users/UsersPage.vue") },
  { path: "/roles", component: () => import("../pages/roles/RolesPage.vue") },
  {
    path: "/permissions",
    component: () => import("../pages/permissions/PermissionsPage.vue"),
  },
  { path: "/pages", component: () => import("../pages/pages/PagesPage.vue") },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to) => {
  if (to.path.startsWith("/login") || to.path.startsWith("/register"))
    return true;
  try {
    await auth.ensureInited(router);
    const menuPaths = auth
      .getMenuPages()
      .filter((p) => !!p.path)
      .map((p) => (p.path.startsWith("/") ? p.path : `/${p.path}`));
    if (menuPaths.length > 0 && !menuPaths.includes(to.path)) return menuPaths[0];
    return true;
  } catch {
    return "/login";
  }
});

export default router;
