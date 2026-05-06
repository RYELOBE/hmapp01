import request from './request'

export const authAPI = {
  login(data) {
    return request.post('/auth/login', data)
  },
  register(data) {
    return request.post('/auth/register', data)
  },
  logout() {
    return request.post('/auth/logout')
  },
  getUserInfo() {
    return request.get('/auth/userinfo')
  }
}

export const productAPI = {
  list(params) {
    return request.get('/product/list', { params })
  },
  detail(id) {
    return request.get(`/product/${id}`)
  },
  publish(data) {
    return request.post('/product', data)
  },
  update(id, data) {
    return request.put(`/product/${id}`, data)
  },
  delete(id) {
    return request.delete(`/product/${id}`)
  },
  getCategories() {
    return request.get('/category/list')
  },
  getRecommend() {
    return request.get('/product/recommend')
  },
  search(params) {
    return request.get('/product/search', { params })
  }
}

export const orderAPI = {
  create(data) {
    return request.post('/order', data)
  },
  list(params) {
    return request.get('/order/list', { params })
  },
  detail(id) {
    return request.get(`/order/${id}`)
  },
  pay(id) {
    return request.put(`/order/${id}/pay`)
  },
  confirm(id) {
    return request.put(`/order/${id}/confirm`)
  },
  cancel(id) {
    return request.put(`/order/${id}/cancel`)
  }
}

export const cartAPI = {
  list() {
    return request.get('/cart/list')
  },
  add(data) {
    return request.post('/cart', data)
  },
  update(id, data) {
    return request.put(`/cart/${id}`, data)
  },
  delete(id) {
    return request.delete(`/cart/${id}`)
  }
}

export const userAPI = {
  profile() {
    return request.get('/user/profile')
  },
  updateProfile(data) {
    return request.put('/user/profile', data)
  },
  uploadAvatar(data) {
    return request.post('/user/avatar', data)
  },
  getFavorites() {
    return request.get('/user/favorites')
  },
  addFavorite(productId) {
    return request.post('/user/favorite', { productId })
  },
  removeFavorite(productId) {
    return request.delete(`/user/favorite/${productId}`)
  }
}

export const aiAPI = {
  chat(data) {
    return request.post('/ai/chat', data)
  },
  recommend(params) {
    return request.get('/ai/recommend', { params })
  }
}

export const adminAPI = {
  getUserList(params) {
    return request.get('/admin/user/list', { params })
  },
  getProductList(params) {
    return request.get('/admin/product/list', { params })
  },
  auditProduct(id, data) {
    return request.put(`/admin/product/${id}/audit`, data)
  },
  getOrderList(params) {
    return request.get('/admin/order/list', { params })
  },
  getStatistics() {
    return request.get('/admin/statistics')
  },
  getCategoryList() {
    return request.get('/admin/category/list')
  },
  addCategory(data) {
    return request.post('/admin/category', data)
  },
  updateCategory(id, data) {
    return request.put(`/admin/category/${id}`, data)
  },
  deleteCategory(id) {
    return request.delete(`/admin/category/${id}`)
  }
}
