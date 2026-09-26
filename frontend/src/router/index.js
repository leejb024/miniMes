import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/auth/session'
import { defaultPath, leafMenus } from '@/layout/menus'
import AppLayout from '@/layout/AppLayout.vue'
import LoginView from '@/views/LoginView.vue'
import BomView from '@/views/master/BomView.vue'
import ItemView from '@/views/master/ItemView.vue'
import PartnerView from '@/views/master/PartnerView.vue'
import PlaceholderView from '@/views/PlaceholderView.vue'
import ProcessView from '@/views/master/ProcessView.vue'
import WarehouseView from '@/views/master/WarehouseView.vue'
import PurchaseInboundView from '@/views/purchase/PurchaseInboundView.vue'
import PurchaseReturnView from '@/views/purchase/PurchaseReturnView.vue'
import MaterialHandoverView from '@/views/inventory/MaterialHandoverView.vue'
import MaterialReceiveView from '@/views/inventory/MaterialReceiveView.vue'
import StockCloseView from '@/views/inventory/StockCloseView.vue'
import StockInquiryView from '@/views/inventory/StockInquiryView.vue'
import WorkOrderView from '@/views/production/WorkOrderView.vue'
import ProductionResultView from '@/views/production/ProductionResultView.vue'

const viewMap = {
  '/master/common/partner': PartnerView,
  '/master/common/warehouse': WarehouseView,
  '/master/item/item': ItemView,
  '/master/process/process': ProcessView,
  '/master/process/bom': BomView,
  '/purchase/management/inbound': PurchaseInboundView,
  '/purchase/management/return': PurchaseReturnView,
  '/inventory/management/stock': StockInquiryView,
  '/inventory/management/close': StockCloseView,
  '/inventory/management/handover': MaterialHandoverView,
  '/inventory/management/receive': MaterialReceiveView,
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
    { path: '/purchase/management/order', redirect: '/purchase/management/inbound' },
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
