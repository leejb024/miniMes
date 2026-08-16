<template>
  <div class="app-shell" :class="{ collapsed }">
    <aside class="sidebar">
      <router-link class="logo" :to="defaultPath">
        <span class="logo-mark">mini</span>
        <span v-if="!collapsed" class="logo-title">MES</span>
      </router-link>

      <nav class="menu">
        <SidebarMenu
          v-for="item in menus"
          :key="item.id"
          :item="item"
        />
      </nav>
    </aside>

    <div class="main">
      <header class="navbar">
        <div class="navbar-left">
          <button type="button" class="hamburger" @click="collapsed = !collapsed" aria-label="메뉴 접기">
            <span />
            <span />
            <span />
          </button>
          <div class="breadcrumb">
            <span>miniMES</span>
            <template v-for="(crumb, index) in breadcrumbs" :key="crumb">
              <span class="sep">/</span>
              <strong v-if="index === breadcrumbs.length - 1">{{ crumb }}</strong>
              <span v-else>{{ crumb }}</span>
            </template>
          </div>
        </div>
        <div class="navbar-right">
          <span class="user-status">{{ user?.userNm || user?.userId }} 접속 중</span>
          <button type="button" class="ghost" @click="onLogout">로그아웃</button>
        </div>
      </header>
      <section class="app-main">
        <router-view />
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, provide, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchMe } from '@/api/auth'
import { clearSession, getToken, getUser, setSession } from '@/auth/session'
import { defaultPath, findOpenIdsByPath, menus } from '@/layout/menus'
import SidebarMenu from '@/layout/SidebarMenu.vue'

const route = useRoute()
const router = useRouter()
const user = ref(getUser())
const collapsed = ref(false)
const openedMenuIds = reactive({})

provide('openedMenuIds', openedMenuIds)

const breadcrumbs = computed(() => route.meta.breadcrumbs || [])

watch(
  () => route.path,
  (path) => {
    const ids = findOpenIdsByPath(path) || []
    ids.forEach((id) => {
      openedMenuIds[id] = true
    })
  },
  { immediate: true }
)

onMounted(async () => {
  try {
    const { data } = await fetchMe()
    user.value = data
    setSession(getToken(), data)
  } catch {
    clearSession()
    await router.replace({ name: 'login' })
  }
})

async function onLogout() {
  clearSession()
  await router.replace({ name: 'login' })
}
</script>
