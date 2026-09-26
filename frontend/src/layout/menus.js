export const menus = [
  {
    id: 'master',
    title: '마스터데이터',
    children: [
      {
        id: 'master-common',
        title: '공통',
        children: [
          { path: '/master/common/partner', title: '거래처관리' },
          { path: '/master/common/warehouse', title: '창고관리' }
        ]
      },
      {
        id: 'master-item',
        title: '품목',
        children: [
          { path: '/master/item/item', title: '품목관리' }
        ]
      },
      {
        id: 'master-process',
        title: '공정',
        children: [
          { path: '/master/process/process', title: '공정관리' },
          { path: '/master/process/bom', title: 'BOM 관리' }
        ]
      }
    ]
  },
  {
    id: 'purchase',
    title: '구매',
    children: [
      {
        id: 'purchase-management',
        title: '구매관리',
        children: [
          { path: '/purchase/management/inbound', title: '입고등록' },
          { path: '/purchase/management/return', title: '반품관리' }
        ]
      }
    ]
  },
  {
    id: 'inventory',
    title: '재고',
    children: [
      {
        id: 'inventory-management',
        title: '재고관리',
        children: [
          { path: '/inventory/management/stock', title: '재고조회' },
          { path: '/inventory/management/close', title: '재고 마감관리' },
          { path: '/inventory/management/handover', title: '자재인계' },
          { path: '/inventory/management/receive', title: '자재 인수' }
        ]
      }
    ]
  },
  {
    id: 'production',
    title: '생산',
    children: [
      {
        id: 'production-management',
        title: '생산관리',
        children: [
          { path: '/production/management/work-order', title: '작업지시' },
          { path: '/production/management/result', title: '생산실적' }
        ]
      }
    ]
  }
]

export function collectLeafMenus(nodes = menus, parents = []) {
  return nodes.flatMap((node) => {
    const trail = [...parents, node.title]
    if (node.children?.length) {
      return collectLeafMenus(node.children, trail)
    }
    return node.path ? [{ ...node, breadcrumbs: trail }] : []
  })
}

export const leafMenus = collectLeafMenus()
export const defaultPath = leafMenus[0].path

export function findOpenIdsByPath(path, nodes = menus, trail = []) {
  for (const node of nodes) {
    const next = node.id ? [...trail, node.id] : trail
    if (node.path === path) {
      return next
    }
    if (node.children?.length) {
      const found = findOpenIdsByPath(path, node.children, next)
      if (found) {
        return found
      }
    }
  }
  return null
}
