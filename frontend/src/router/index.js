import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/auth/session'
import { defaultPath, leafMenus } from '@/layout/menus'
import AppLayout from '@/layout/AppLayout.vue'
import LoginView from '@/views/LoginView.vue'
import BomView from '@/views/master/BomView.vue'
import DepartmentView from '@/views/master/DepartmentView.vue'
import ItemView from '@/views/master/ItemView.vue'
import PartnerView from '@/views/master/PartnerView.vue'
import PlaceholderView from '@/views/PlaceholderView.vue'
import ProcessView from '@/views/master/ProcessView.vue'
import WarehouseView from '@/views/master/WarehouseView.vue'
import WorkcenterView from '@/views/master/WorkcenterView.vue'
import WorkOrderView from '@/views/production/WorkOrderView.vue'
import ProductionResultView from '@/views/production/ProductionResultView.vue'

const viewMap = {
  '/master/common/partner': PartnerView,
  '/master/common/warehouse': WarehouseView,
  '/master/common/department': DepartmentView,
  '/master/item/item': ItemView,
  '/master/process/process': ProcessView,
  '/master/process/workcenter': WorkcenterView,
  '/master/process/bom': BomView,
  '/production/management/work-order': WorkOrderView,
  '/production/management/result': ProductionResultView
}

const children = leafMenus.map((item) => ({
  path: item.path.replace(/^\//, ''),
  name: item.path.slice(1).replace(/\//g, '-'),
  component: viewMap[item.path] || PlaceholderView,
  meta: {
    title: item.title,
    breadcrumbs: item.breadcrumbs
  }
}))

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: LoginView, meta: { public: true } },
    {
      path: '/',
      component: AppLayout,
      redirect: defaultPath,
      children
    }
  ]
})

router.beforeEach((to) => {
  const token = getToken()
  if (!to.meta.public && !token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && token) {
    return { path: defaultPath }
  }
  return true
})

export default router
