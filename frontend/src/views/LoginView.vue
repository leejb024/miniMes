<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <span class="brand-mark">mini</span>
        <span class="brand-name">MES</span>
      </div>
      <p class="subtitle">제조 실행 시스템</p>

      <form class="login-form" @submit.prevent="onSubmit">
        <label>
          아이디
          <input
            v-model.trim="userId"
            type="text"
            autocomplete="username"
            placeholder="아이디를 입력하세요"
          />
        </label>
        <label>
          비밀번호
          <input
            v-model="password"
            type="password"
            autocomplete="current-password"
            placeholder="비밀번호를 입력하세요"
            @keyup.enter="onSubmit"
          />
        </label>

        <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

        <button type="submit" :disabled="loading">
          {{ loading ? '로그인 중...' : '로그인' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { setSession } from '@/auth/session'

const router = useRouter()
const route = useRoute()

const userId = ref('admin')
const password = ref('admin123')
const loading = ref(false)
const errorMessage = ref('')

async function onSubmit() {
  if (!userId.value || !password.value) {
    errorMessage.value = '아이디와 비밀번호를 입력하세요.'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const { data } = await login(userId.value, password.value)
    setSession(data.token, {
      userSeq: data.userSeq,
      userId: data.userId,
      userNm: data.userNm
    })
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.replace(redirect)
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '로그인에 실패했습니다.'
  } finally {
    loading.value = false
  }
}
</script>
