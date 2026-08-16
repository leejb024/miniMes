<template>
  <div class="menu-node" :class="'level-' + level">
    <template v-if="item.children?.length">
      <button type="button" class="menu-toggle" @click="toggle">
        <span class="menu-label">{{ item.title }}</span>
        <span class="caret" :class="{ closed: !opened }">⌃</span>
      </button>
      <div v-show="opened" class="menu-children">
        <SidebarMenu
          v-for="child in item.children"
          :key="child.id || child.path"
          :item="child"
          :level="level + 1"
        />
      </div>
    </template>
    <router-link v-else :to="item.path" class="menu-item">
      {{ item.title }}
    </router-link>
  </div>
</template>

<script setup>
import { computed, inject } from 'vue'

defineOptions({ name: 'SidebarMenu' })

const props = defineProps({
  item: { type: Object, required: true },
  level: { type: Number, default: 0 }
})

const openedIds = inject('openedMenuIds')
const menuId = computed(() => props.item.id || props.item.path)
const opened = computed(() => !!openedIds[menuId.value])

function toggle() {
  openedIds[menuId.value] = !openedIds[menuId.value]
}
</script>
