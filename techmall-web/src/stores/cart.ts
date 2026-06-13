import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface CartItem {
  productId: number
  name: string
  price: number
  imageUrl: string
  quantity: number
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>(JSON.parse(localStorage.getItem('cart') || '[]'))

  const totalCount = computed(() => items.value.reduce((s, i) => s + i.quantity, 0))
  const totalPrice = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0))

  function addItem(item: CartItem) {
    const exist = items.value.find(i => i.productId === item.productId)
    if (exist) { exist.quantity += item.quantity }
    else { items.value.push(item) }
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  function removeItem(productId: number) {
    items.value = items.value.filter(i => i.productId !== productId)
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  function updateQuantity(productId: number, qty: number) {
    const item = items.value.find(i => i.productId === productId)
    if (item) { item.quantity = Math.max(1, qty) }
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  function clear() {
    items.value = []
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  return { items, totalCount, totalPrice, addItem, removeItem, updateQuantity, clear }
})
