<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchMe, login } from '@/api/auth'
import { fetchHealth } from '@/api/health'
import { ApiError, requireData } from '@/api/types'

const health = ref('正在请求后端...')
const username = ref('')
const password = ref('')
const currentUser = ref('')
const errorMessage = ref('')
const fieldErrors = ref<Record<string, string>>({})

onMounted(async () => {
  try {
    const response = await fetchHealth()
    health.value = requireData(response.data).status
  } catch (error) {
    health.value = error instanceof Error ? error.message : '请求失败'
  }
})

function showError(error: unknown, fallback: string) {
  fieldErrors.value = {}
  if (error instanceof ApiError) {
    errorMessage.value = error.message
    fieldErrors.value = Object.fromEntries(error.fields.map((item) => [item.field, item.message]))
    return
  }
  errorMessage.value = error instanceof Error ? error.message : fallback
}

async function submit() {
  errorMessage.value = ''
  fieldErrors.value = {}
  try {
    const pair = requireData((await login(username.value, password.value)).data)
    sessionStorage.setItem('token', pair.token)
    sessionStorage.setItem('refreshToken', pair.refreshToken)
    const me = requireData((await fetchMe()).data)
    currentUser.value = me.username
  } catch (error) {
    currentUser.value = ''
    showError(error, '登录失败')
  }
}

async function reloadMe() {
  errorMessage.value = ''
  fieldErrors.value = {}
  try {
    const me = requireData((await fetchMe()).data)
    currentUser.value = me.username
  } catch (error) {
    currentUser.value = ''
    showError(error, '获取失败')
  }
}
</script>

<template>
  <main>
    <h1>后端状态</h1>
    <p>{{ health }}</p>
    <form @submit.prevent="submit">
      <div>
        <input v-model="username" placeholder="用户名" />
        <p v-if="fieldErrors.username">{{ fieldErrors.username }}</p>
      </div>
      <div>
        <input v-model="password" type="password" placeholder="密码" />
        <p v-if="fieldErrors.password">{{ fieldErrors.password }}</p>
      </div>
      <button type="submit">登录</button>
      <button type="button" @click="reloadMe">重新获取当前用户</button>
    </form>
    <p v-if="currentUser">当前用户：{{ currentUser }}</p>
    <p v-if="errorMessage">{{ errorMessage }}</p>
  </main>
</template>
