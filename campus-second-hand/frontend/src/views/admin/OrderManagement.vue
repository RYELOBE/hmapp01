<template>
  <div class="management">
    <h2>订单管理</h2>

    <a-card class="search-card">
      <a-space wrap>
        <a-input
          v-model="searchOrderNo"
          placeholder="搜索订单号"
          allow-clear
          style="width: 200px"
          @press-enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix><icon-search /></template>
        </a-input>

        <a-select
          v-model="searchStatus"
          placeholder="选择状态"
          allow-clear
          style="width: 140px"
          @change="handleSearch"
        >
          <a-option value="">全部</a-option>
          <a-option value="pending_payment">待付款</a-option>
          <a-option value="pending_shipment">待发货</a-option>
          <a-option value="pending_receipt">待收货</a-option>
          <a-option value="completed">已完成</a-option>
          <a-option value="cancelled">已取消</a-option>
          <a-option value="refund_request">退款申请</a-option>
          <a-option value="refund_approved">已退款</a-option>
          <a-option value="refund_rejected">退款被拒</a-option>
        </a-select>

        <a-range-picker
          v-model="dateRange"
          style="width: 260px"
          @change="handleSearch"
        />

        <a-button type="primary" @click="handleSearch">
          <template #icon><icon-search /></template>
          搜索
        </a-button>
        <a-button @click="handleReset">重置</a-button>
      </a-space>
    </a-card>

    <a-card class="table-card">
      <a-table
        :columns="columns"
        :data="filteredData"
        :pagination="pagination"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #orderNo="{ record }">
          <a-link @click="viewDetail(record)">{{ record.orderNo }}</a-link>
        </template>

        <template #productInfo="{ record }">
          <div class="product-cell">
            <img
              :src="record.productImage"
              class="product-image"
              :alt="record.productName"
              @error="handleImageError"
            />
            <span class="product-name">{{ record.productName }}</span>
          </div>
        </template>

        <template #buyerInfo="{ record }">
          <div class="user-cell">
            <span>{{ record.buyerName }}</span>
            <span class="user-phone">{{ record.buyerPhone }}</span>
          </div>
        </template>

        <template #sellerInfo="{ record }">
          <div class="user-cell">
            <span>{{ record.sellerName }}</span>
            <span class="user-phone">{{ record.sellerPhone }}</span>
          </div>
        </template>

        <template #amount="{ record }">
          <span class="amount">¥{{ record.amount.toFixed(2) }}</span>
        </template>

        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>

        <template #orderTime="{ record }">
          {{ formatDate(record.orderTime) }}
        </template>

        <template #operations="{ record }">
          <a-space>
            <a-link @click="viewDetail(record)">查看详情</a-link>
            <a-link
              v-if="record.status === 'refund_request'"
              status="success"
              @click="handleApproveRefund(record)"
            >
              同意退款
            </a-link>
            <a-link
              v-if="record.status === 'refund_request'"
              status="danger"
              @click="handleRejectRefund(record)"
            >
              拒绝退款
            </a-link>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="detailVisible"
      title="订单详情"
      :width="700"
      :footer="null"
    >
      <div v-if="currentOrder" class="order-detail">
        <a-descriptions :column="2" bordered title="订单信息">
          <a-descriptions-item label="订单号">{{ currentOrder.orderNo }}</a-descriptions-item>
          <a-descriptions-item label="订单状态">
            <a-tag :color="getStatusColor(currentOrder.status)">
              {{ getStatusText(currentOrder.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="订单金额">
            <span class="amount">¥{{ currentOrder.amount.toFixed(2) }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="下单时间">{{ formatDate(currentOrder.orderTime) }}</a-descriptions-item>
          <a-descriptions-item label="支付方式">{{ currentOrder.paymentMethod }}</a-descriptions-item>
          <a-descriptions-item label="支付时间">{{ currentOrder.payTime ? formatDate(currentOrder.payTime) : '-' }}</a-descriptions-item>
        </a-descriptions>

        <a-descriptions :column="1" bordered title="商品信息" class="detail-section">
          <a-descriptions-item>
            <div class="product-detail-cell">
              <img
                :src="currentOrder.productImage"
                class="detail-product-image"
                @error="handleImageError"
              />
              <div class="product-info">
                <p><strong>{{ currentOrder.productName }}</strong></p>
                <p class="product-price">¥{{ currentOrder.productPrice.toFixed(2) }} × {{ currentOrder.quantity }}</p>
              </div>
            </div>
          </a-descriptions-item>
        </a-descriptions>

        <a-descriptions :column="2" bordered title="买家信息" class="detail-section">
          <a-descriptions-item label="买家">{{ currentOrder.buyerName }}</a-descriptions-item>
          <a-descriptions-item label="联系电话">{{ currentOrder.buyerPhone }}</a-descriptions-item>
          <a-descriptions-item label="收货地址" :span="2">{{ currentOrder.address }}</a-descriptions-item>
        </a-descriptions>

        <a-descriptions :column="2" bordered title="卖家信息" class="detail-section">
          <a-descriptions-item label="卖家">{{ currentOrder.sellerName }}</a-descriptions-item>
          <a-descriptions-item label="联系电话">{{ currentOrder.sellerPhone }}</a-descriptions-item>
        </a-descriptions>

        <div v-if="currentOrder.logistics" class="detail-section">
          <a-descriptions :column="2" bordered title="物流信息">
            <a-descriptions-item label="物流公司">{{ currentOrder.logistics.company }}</a-descriptions-item>
            <a-descriptions-item label="运单号">{{ currentOrder.logistics.trackingNo }}</a-descriptions-item>
            <a-descriptions-item label="发货时间" :span="2">{{ formatDate(currentOrder.logistics.shipTime) }}</a-descriptions-item>
          </a-descriptions>
        </div>

        <div v-if="currentOrder.refund" class="detail-section refund-section">
          <a-descriptions :column="1" bordered title="退款信息">
            <a-descriptions-item label="退款原因">{{ currentOrder.refund.reason }}</a-descriptions-item>
            <a-descriptions-item label="退款金额">
              <span class="amount">¥{{ currentOrder.refund.amount.toFixed(2) }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="申请时间">{{ formatDate(currentOrder.refund.applyTime) }}</a-descriptions-item>
            <a-descriptions-item v-if="currentOrder.refund.handleTime" label="处理时间">
              {{ formatDate(currentOrder.refund.handleTime) }}
            </a-descriptions-item>
            <a-descriptions-item v-if="currentOrder.refund.handleNote" label="处理备注" :span="2">
              {{ currentOrder.refund.handleNote }}
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <div class="detail-section">
          <h4>操作日志</h4>
          <a-timeline>
            <a-timeline-item
              v-for="(log, index) in currentOrder.operationLogs"
              :key="index"
              :color="getLogColor(log.action)"
            >
              <p><strong>{{ log.action }}</strong></p>
              <p>{{ formatDate(log.time) }}</p>
              <p v-if="log.operator">操作人：{{ log.operator }}</p>
            </a-timeline-item>
          </a-timeline>
        </div>
      </div>
    </a-modal>

    <a-modal
      v-model:visible="refundVisible"
      title="拒绝退款原因"
      @before-ok="submitRefundReject"
    >
      <a-form :model="refundForm" layout="vertical">
        <a-form-item label="拒绝原因" required>
          <a-textarea
            v-model="refundForm.reason"
            placeholder="请输入拒绝退款原因"
            :max-length="200"
            show-word-limit
            style="height: 100px"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Message } from '@arco-design/web-vue'

const searchOrderNo = ref('')
const searchStatus = ref('')
const dateRange = ref([])
const detailVisible = ref(false)
const refundVisible = ref(false)
const currentOrder = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)

const refundForm = reactive({
  reason: ''
})

const mockOrders = [
  { id: 1, orderNo: 'ORD20240315001', productName: 'iPhone 14 Pro Max 256G', productPrice: 6999, quantity: 1, productImage: 'https://picsum.photos/200?random=101', buyerName: '小明', buyerPhone: '13900139001', sellerName: '张三', sellerPhone: '13800138001', amount: 6999, status: 'pending_shipment', orderTime: '2024-03-15 10:30:00', payTime: '2024-03-15 10:35:00', paymentMethod: '支付宝', address: '北京市海淀区中关村大街1号', logistics: null, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-15 10:30:00' }, { action: '支付成功', time: '2024-03-15 10:35:00', operator: '系统' }] },
  { id: 2, orderNo: 'ORD20240314002', productName: '高等数学同济第七版', productPrice: 25, quantity: 1, productImage: 'https://picsum.photos/200?random=102', buyerName: '小红', buyerPhone: '13900139002', sellerName: '李四', sellerPhone: '13800138002', amount: 25, status: 'completed', orderTime: '2024-03-14 15:00:00', payTime: '2024-03-14 15:10:00', paymentMethod: '微信支付', address: '上海市浦东新区张江高科', logistics: { company: '顺丰速运', trackingNo: 'SF1234567890', shipTime: '2024-03-14 18:00:00' }, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-14 15:00:00' }, { action: '支付成功', time: '2024-03-14 15:10:00' }, { action: '卖家已发货', time: '2024-03-14 18:00:00', operator: '李四' }, { action: '确认收货', time: '2024-03-15 12:00:00', operator: '小红' }] },
  { id: 3, orderNo: 'ORD20240315003', productName: '小米手环8 NFC版', productPrice: 180, quantity: 1, productImage: 'https://picsum.photos/200?random=103', buyerName: '小刚', buyerPhone: '13900139003', sellerName: '王五', sellerPhone: '13800138003', amount: 180, status: 'refund_request', orderTime: '2024-03-15 09:30:00', payTime: '2024-03-15 09:35:00', paymentMethod: '支付宝', address: '广州市天河区五山路381号', logistics: { company: '圆通速递', trackingNo: 'YT9876543210', shipTime: '2024-03-15 14:00:00' }, refund: { reason: '商品有划痕，与描述不符', amount: 180, applyTime: '2024-03-16 10:00:00' }, operationLogs: [{ action: '订单创建', time: '2024-03-15 09:30:00' }, { action: '支付成功', time: '2024-03-15 09:35:00' }, { action: '卖家已发货', time: '2024-03-15 14:00:00' }, { action: '买家申请退款', time: '2024-03-16 10:00:00', operator: '小刚' }] },
  { id: 4, orderNo: 'ORD20240313004', productName: '床上书桌折叠桌', productPrice: 45, quantity: 1, productImage: 'https://picsum.photos/200?random=104', buyerName: '小丽', buyerPhone: '13900139004', sellerName: '赵六', sellerPhone: '13800138004', amount: 45, status: 'pending_payment', orderTime: '2024-03-13 20:00:00', payTime: null, paymentMethod: null, address: '杭州市西湖区玉泉路1号', logistics: null, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-13 20:00:00' }] },
  { id: 5, orderNo: 'ORD20240312005', productName: 'Nike Air Force 1 白', productPrice: 380, quantity: 1, productImage: 'https://picsum.photos/200?random=105', buyerName: '小华', buyerPhone: '13900139005', sellerName: '钱七', sellerPhone: '13800138005', amount: 380, status: 'cancelled', orderTime: '2024-03-12 11:30:00', payTime: null, paymentMethod: null, address: '武汉市洪山区珞喻路1037号', logistics: null, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-12 11:30:00' }, { action: '订单取消', time: '2024-03-12 12:00:00', operator: '买家' }] },
  { id: 6, orderNo: 'ORD20240315006', productName: '线性代数教材', productPrice: 18, quantity: 2, productImage: 'https://picsum.photos/200?random=106', buyerName: '小美', buyerPhone: '13900139006', sellerName: '孙八', sellerPhone: '13800138006', amount: 36, status: 'pending_shipment', orderTime: '2024-03-15 08:45:00', payTime: '2024-03-15 08:50:00', paymentMethod: '微信支付', address: '成都市武侯区科华北路99号', logistics: null, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-15 08:45:00' }, { action: '支付成功', time: '2024-03-15 08:50:00' }] },
  { id: 7, orderNo: 'ORD20240311007', productName: '雅思真题剑15-18', productPrice: 60, quantity: 1, productImage: 'https://picsum.photos/200?random=107', buyerName: '小强', buyerPhone: '13900139007', sellerName: '周九', sellerPhone: '13800138007', amount: 60, status: 'completed', orderTime: '2024-03-11 20:30:00', payTime: '2024-03-11 20:35:00', paymentMethod: '支付宝', address: '南京市鼓楼区汉口路22号', logistics: { company: '中通快递', trackingNo: 'ZT5555666677', shipTime: '2024-03-11 22:00:00' }, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-11 20:30:00' }, { action: '支付成功', time: '2024-03-11 20:35:00' }, { action: '卖家已发货', time: '2024-03-11 22:00:00' }, { action: '确认收货', time: '2024-03-13 16:00:00' }] },
  { id: 8, orderNo: 'ORD20240315008', productName: '电动牙刷', productPrice: 90, quantity: 1, productImage: 'https://picsum.photos/200?random=108', buyerName: '小雪', buyerPhone: '13900139008', sellerName: '吴十', sellerPhone: '13800138008', amount: 90, status: 'pending_receipt', orderTime: '2024-03-15 07:00:00', payTime: '2024-03-15 07:05:00', paymentMethod: '支付宝', address: '西安市雁塔区雁塔路南段99号', logistics: { company: '韵达快递', trackingNo: 'YD8888999900', shipTime: '2024-03-15 12:00:00' }, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-15 07:00:00' }, { action: '支付成功', time: '2024-03-15 07:05:00' }, { action: '卖家已发货', time: '2024-03-15 12:00:00' }] },
  { id: 9, orderNo: 'ORD20240310009', productName: 'MacBook Air M1', productPrice: 5500, quantity: 1, productImage: 'https://picsum.photos/200?random=109', buyerName: '小鹏', buyerPhone: '13900139009', sellerName: '张三', sellerPhone: '13800138001', amount: 5500, status: 'completed', orderTime: '2024-03-10 14:00:00', payTime: '2024-03-10 14:10:00', paymentMethod: '微信支付', address: '深圳市南山区科技园南区', logistics: { company: '顺丰速运', trackingNo: 'SF1111222233', shipTime: '2024-03-10 18:00:00' }, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-10 14:00:00' }, { action: '支付成功', time: '2024-03-10 14:10:00' }, { action: '卖家已发货', time: '2024-03-10 18:00:00' }, { action: '确认收货', time: '2024-03-12 09:00:00' }] },
  { id: 10, orderNo: 'ORD20240315010', productName: 'Adidas运动外套', productPrice: 150, quantity: 1, productImage: 'https://picsum.photos/200?random=110', buyerName: '小云', buyerPhone: '13900139010', sellerName: '李四', sellerPhone: '13800138002', amount: 150, status: 'pending_shipment', orderTime: '2024-03-15 06:00:00', payTime: '2024-03-15 06:05:00', paymentMethod: '微信支付', address: '苏州市姑苏区十梓街1号', logistics: null, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-15 06:00:00' }, { action: '支付成功', time: '2024-03-15 06:05:00' }] },
  { id: 11, orderNo: 'ORD20240315011', productName: '漫步者蓝牙耳机', productPrice: 120, quantity: 1, productImage: 'https://picsum.photos/200?random=111', buyerName: '小月', buyerPhone: '13900139011', sellerName: '王五', sellerPhone: '13800138003', amount: 120, status: 'refund_approved', orderTime: '2024-03-14 16:00:00', payTime: '2024-03-14 16:10:00', paymentMethod: '支付宝', address: '天津市南开区卫津路92号', logistics: { company: '申通快递', trackingNo: 'ST3333444455', shipTime: '2024-03-14 20:00:00' }, refund: { reason: '不想要了', amount: 120, applyTime: '2024-03-15 09:00:00', handleTime: '2024-03-15 10:30:00', handleNote: '已同意退款' }, operationLogs: [{ action: '订单创建', time: '2024-03-14 16:00:00' }, { action: '支付成功', time: '2024-03-14 16:10:00' }, { action: '卖家已发货', time: '2024-03-14 20:00:00' }, { action: '买家申请退款', time: '2024-03-15 09:00:00' }, { action: '退款已处理', time: '2024-03-15 10:30:00', operator: '管理员' }] },
  { id: 12, orderNo: 'ORD20240315012', productName: '卡西欧计算器', productPrice: 55, quantity: 1, productImage: 'https://picsum.photos/200?random=112', buyerName: '小晨', buyerPhone: '13900139012', sellerName: '周九', sellerPhone: '13800138007', amount: 55, status: 'pending_shipment', orderTime: '2024-03-15 05:30:00', payTime: '2024-03-15 05:35:00', paymentMethod: '支付宝', address: '长沙市岳麓区麓山南路1号', logistics: null, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-15 05:30:00' }, { action: '支付成功', time: '2024-03-15 05:35:00' }] },
  { id: 13, orderNo: 'ORD20240309013', productName: '考研政治资料全套', productPrice: 80, quantity: 1, productImage: 'https://picsum.photos/200?random=113', buyerName: '小燕', buyerPhone: '13900139013', sellerName: '吴十', sellerPhone: '13800138008', amount: 80, status: 'completed', orderTime: '2024-03-09 11:00:00', payTime: '2024-03-09 11:05:00', paymentMethod: '微信支付', address: '济南市历下区山大南路27号', logistics: { company: '百世快递', trackingNo: 'HT6666777788', shipTime: '2024-03-09 15:00:00' }, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-09 11:00:00' }, { action: '支付成功', time: '2024-03-09 11:05:00' }, { action: '卖家已发货', time: '2024-03-09 15:00:00' }, { action: '确认收货', time: '2024-03-11 14:00:00' }] },
  { id: 14, orderNo: 'ORD20240315014', productName: '宿舍小风扇', productPrice: 25, quantity: 2, productImage: 'https://picsum.photos/200?random=114', buyerName: '小琪', buyerPhone: '13900139014', sellerName: '张三', sellerPhone: '13800138001', amount: 50, status: 'pending_receipt', orderTime: '2024-03-15 04:00:00', payTime: '2024-03-15 04:05:00', paymentMethod: '支付宝', address: '合肥市蜀山区黄山路460号', logistics: { company: '天天快递', trackingNo: 'TT9999000011', shipTime: '2024-03-15 10:00:00' }, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-15 04:00:00' }, { action: '支付成功', time: '2024-03-15 04:05:00' }, { action: '卖家已发货', time: '2024-03-15 10:00:00' }] },
  { id: 15, orderNo: 'ORD20240315015', productName: 'Nike运动背包', productPrice: 120, quantity: 1, productImage: 'https://picsum.photos/200?random=115', buyerName: '小杰', buyerPhone: '13900139015', sellerName: '李四', sellerPhone: '13800138002', amount: 120, status: 'pending_shipment', orderTime: '2024-03-15 03:00:00', payTime: '2024-03-15 03:05:00', paymentMethod: '微信支付', address: '郑州市金水区文化路79号', logistics: null, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-15 03:00:00' }, { action: '支付成功', time: '2024-03-15 03:05:00' }] },
  { id: 16, orderNo: 'ORD20240308016', productName: 'iPad Air 5 64G', productPrice: 3200, quantity: 1, productImage: 'https://picsum.photos/200?random=116', buyerName: '小婷', buyerPhone: '13900139016', sellerName: '王五', sellerPhone: '13800138003', amount: 3200, status: 'completed', orderTime: '2024-03-08 13:00:00', payTime: '2024-03-08 13:10:00', paymentMethod: '支付宝', address: '重庆市沙坪坝区沙正街174号', logistics: { company: '顺丰速运', trackingNo: 'SF2222333344', shipTime: '2024-03-08 17:00:00' }, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-08 13:00:00' }, { action: '支付成功', time: '2024-03-08 13:10:00' }, { action: '卖家已发货', time: '2024-03-08 17:00:00' }, { action: '确认收货', time: '2024-03-10 15:00:00' }] },
  { id: 17, orderNo: 'ORD20240315017', productName: '大学物理教材上下册', productPrice: 35, quantity: 2, productImage: 'https://picsum.photos/200?random=117', buyerName: '小文', buyerPhone: '13900139017', sellerName: '赵六', sellerPhone: '13800138004', amount: 70, status: 'refund_rejected', orderTime: '2024-03-07 10:00:00', payTime: '2024-03-07 10:05:00', paymentMethod: '微信支付', address: '哈尔滨市南岗区西大直街92号', logistics: { company: '圆通速递', trackingNo: 'YT1111222233', shipTime: '2024-03-07 14:00:00' }, refund: { reason: '书本有污渍', amount: 70, applyTime: '2024-03-08 11:00:00', handleTime: '2024-03-08 14:00:00', handleNote: '该商品本来就是二手的，轻微污渍在描述范围内，拒绝退款' }, operationLogs: [{ action: '订单创建', time: '2024-03-07 10:00:00' }, { action: '支付成功', time: '2024-03-07 10:05:00' }, { action: '卖家已发货', time: '2024-03-07 14:00:00' }, { action: '确认收货', time: '2024-03-08 09:00:00' }, { action: '买家申请退款', time: '2024-03-08 11:00:00' }, { action: '退款申请被拒绝', time: '2024-03-08 14:00:00', operator: '管理员' }] },
  { id: 18, orderNo: 'ORD20240315018', productName: 'Switch游戏卡带', productPrice: 180, quantity: 1, productImage: 'https://picsum.photos/200?random=118', buyerName: '小波', buyerPhone: '13900139018', sellerName: '孙八', sellerPhone: '13800138006', amount: 180, status: 'pending_shipment', orderTime: '2024-03-15 02:00:00', payTime: '2024-03-15 02:05:00', paymentMethod: '支付宝', address: '南昌市东湖区八一大道555号', logistics: null, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-15 02:00:00' }, { action: '支付成功', time: '2024-03-15 02:05:00' }] },
  { id: 19, orderNo: 'ORD20240306019', productName: '宿舍电煮锅', productPrice: 45, quantity: 1, productImage: 'https://picsum.photos/200?random=119', buyerName: '小凤', buyerPhone: '13900139019', sellerName: '张三', sellerPhone: '13800138001', amount: 45, status: 'completed', orderTime: '2024-03-06 16:00:00', payTime: '2024-03-06 16:10:00', paymentMethod: '微信支付', address: '福州市鼓楼区工业路523号', logistics: { company: '中通快递', trackingNo: 'ZT4444555566', shipTime: '2024-03-06 19:00:00' }, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-06 16:00:00' }, { action: '支付成功', time: '2024-03-06 16:10:00' }, { action: '卖家已发货', time: '2024-03-06 19:00:00' }, { action: '确认收货', time: '2024-03-08 18:00:00' }] },
  { id: 20, orderNo: 'ORD20240315020', productName: '程序员穿搭T恤', productPrice: 30, quantity: 3, productImage: 'https://picsum.photos/200?random=120', buyerName: '小雷', buyerPhone: '13900139020', sellerName: '李四', sellerPhone: '13800138002', amount: 90, status: 'pending_receipt', orderTime: '2024-03-15 01:00:00', payTime: '2024-03-15 01:05:00', paymentMethod: '支付宝', address: '兰州市城关区天水路1号', logistics: { company: '韵达快递', trackingNo: 'YD5555666677', shipTime: '2024-03-15 08:00:00' }, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-15 01:00:00' }, { action: '支付成功', time: '2024-03-15 01:05:00' }, { action: '卖家已发货', time: '2024-03-15 08:00:00' }] },
  { id: 21, orderNo: 'ORD20240305021', productName: '索尼PS4游戏机', productPrice: 1200, quantity: 1, productImage: 'https://picsum.photos/200?random=121', buyerName: '小超', buyerPhone: '13900139021', sellerName: '王五', sellerPhone: '13800138003', amount: 1200, status: 'completed', orderTime: '2024-03-05 15:00:00', payTime: '2024-03-05 15:10:00', paymentMethod: '微信支付', address: '贵阳市云岩区中华北路101号', logistics: { company: '顺丰速运', trackingNo: 'SF6666777788', shipTime: '2024-03-05 19:00:00' }, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-05 15:00:00' }, { action: '支付成功', time: '2024-03-05 15:10:00' }, { action: '卖家已发货', time: '2024-03-05 19:00:00' }, { action: '确认收货', time: '2024-03-07 17:00:00' }] },
  { id: 22, orderNo: 'ORD20240315022', productName: '四六级真题全套', productPrice: 15, quantity: 2, productImage: 'https://picsum.photos/200?random=122', buyerName: '小梅', buyerPhone: '13900139022', sellerName: '赵六', sellerPhone: '13800138004', amount: 30, status: 'pending_shipment', orderTime: '2024-03-15 00:30:00', payTime: '2024-03-15 00:35:00', paymentMethod: '微信支付', address: '昆明市五华区一二一大街1号', logistics: null, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-15 00:30:00' }, { action: '支付成功', time: '2024-03-15 00:35:00' }] },
  { id: 23, orderNo: 'ORD20240304023', productName: '羽毛球拍套装', productPrice: 85, quantity: 1, productImage: 'https://picsum.photos/200?random=123', buyerName: '小龙', buyerPhone: '13900139023', sellerName: '吴十', sellerPhone: '13800138008', amount: 85, status: 'completed', orderTime: '2024-03-04 12:00:00', payTime: '2024-03-04 12:10:00', paymentMethod: '支付宝', address: '太原市小店区坞城路92号', logistics: { company: '申通快递', trackingNo: 'ST7777888899', shipTime: '2024-03-04 16:00:00' }, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-04 12:00:00' }, { action: '支付成功', time: '2024-03-04 12:10:00' }, { action: '卖家已发货', time: '2024-03-04 16:00:00' }, { action: '确认收货', time: '2024-03-06 11:00:00' }] },
  { id: 24, orderNo: 'ORD20240315024', productName: '瑜伽垫加厚', productPrice: 35, quantity: 1, productImage: 'https://picsum.photos/200?random=124', buyerName: '小丹', buyerPhone: '13900139024', sellerName: '钱七', sellerPhone: '13800138005', amount: 35, status: 'pending_payment', orderTime: '2024-03-15 11:30:00', payTime: null, paymentMethod: null, address: '石家庄市裕华区槐安东路136号', logistics: null, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-15 11:30:00' }] },
  { id: 25, orderNo: 'ORD20240303025', productName: '水乳套装护肤品', productPrice: 120, quantity: 1, productImage: 'https://picsum.photos/200?random=125', buyerName: '小珂', buyerPhone: '13900139025', sellerName: '孙八', sellerPhone: '13800138006', amount: 120, status: 'pending_receipt', orderTime: '2024-03-03 09:00:00', payTime: '2024-03-03 09:05:00', paymentMethod: '支付宝', address: '长春市朝阳区自由大路1号', logistics: { company: '圆通速递', trackingNo: 'YT8888999900', shipTime: '2024-03-03 14:00:00' }, refund: null, operationLogs: [{ action: '订单创建', time: '2024-03-03 09:00:00' }, { action: '支付成功', time: '2024-03-03 09:05:00' }, { action: '卖家已发货', time: '2024-03-03 14:00:00' }] }
]

const columns = [
  { title: '订单号', dataIndex: 'orderNo', width: 160, slotName: 'orderNo', align: 'center' },
  { title: '商品信息', dataIndex: 'productInfo', width: 220, slotName: 'productInfo' },
  { title: '买家', dataIndex: 'buyerInfo', width: 140, slotName: 'buyerInfo', align: 'center' },
  { title: '卖家', dataIndex: 'sellerInfo', width: 140, slotName: 'sellerInfo', align: 'center' },
  { title: '订单金额', dataIndex: 'amount', width: 100, slotName: 'amount', align: 'center' },
  { title: '订单状态', dataIndex: 'status', width: 110, slotName: 'status', align: 'center' },
  { title: '下单时间', dataIndex: 'orderTime', width: 160, slotName: 'orderTime', align: 'center' },
  { title: '操作', dataIndex: 'operations', width: 180, slotName: 'operations', align: 'center', fixed: 'right' }
]

const pagination = reactive({
  total: computed(() => filteredData.value.length),
  current: currentPage,
  pageSize: pageSize,
  showTotal: true,
  showPageSize: true,
  pageSizeOptions: [10, 20, 50]
})

const filteredData = computed(() => {
  let result = [...mockOrders]

  if (searchOrderNo.value) {
    result = result.filter(item =>
      item.orderNo.toLowerCase().includes(searchOrderNo.value.toLowerCase())
    )
  }

  if (searchStatus.value) {
    result = result.filter(item => item.status === searchStatus.value)
  }

  if (dateRange.value && dateRange.value.length === 2) {
    const startDate = new Date(dateRange.value[0]).getTime()
    const endDate = new Date(dateRange.value[1]).getTime() + 86399999
    result = result.filter(item => {
      const itemDate = new Date(item.orderTime).getTime()
      return itemDate >= startDate && itemDate <= endDate
    })
  }

  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return result.slice(start, end)
})

const statusColors = {
  pending_payment: 'orange',
  pending_shipment: 'arcoblue',
  pending_receipt: 'geekblue',
  completed: 'green',
  cancelled: 'gray',
  refund_request: 'red',
  refund_approved: 'green',
  refund_rejected: 'gray'
}

const statusTexts = {
  pending_payment: '待付款',
  pending_shipment: '待发货',
  pending_receipt: '待收货',
  completed: '已完成',
  cancelled: '已取消',
  refund_request: '退款申请',
  refund_approved: '已退款',
  refund_rejected: '退款被拒'
}

const getStatusColor = (status) => {
  return statusColors[status] || 'gray'
}

const getStatusText = (status) => {
  return statusTexts[status] || status
}

const getLogColor = (action) => {
  if (action.includes('成功') || action.includes('完成') || action.includes('已通过')) return 'green'
  if (action.includes('取消') || action.includes('拒绝') || action.includes('被拒')) return 'red'
  if (action.includes('申请')) return 'orange'
  return 'blue'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleImageError = (e) => {
  e.target.src = 'https://picsum.photos/200?random=placeholder'
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleReset = () => {
  searchOrderNo.value = ''
  searchStatus.value = ''
  dateRange.value = []
  currentPage.value = 1
}

const handlePageChange = (page) => {
  currentPage.value = page
}

const handlePageSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const viewDetail = (record) => {
  currentOrder.value = record
  detailVisible.value = true
}

const handleApproveRefund = (record) => {
  record.status = 'refund_approved'
  record.refund = record.refund || {}
  record.refund.handleTime = new Date().toLocaleString('zh-CN')
  record.refund.handleNote = '已同意退款'
  record.operationLogs.unshift({
    action: '退款已处理（同意）',
    time: new Date().toLocaleString('zh-CN'),
    operator: '管理员'
  })
  Message.success(`订单 ${record.orderNo} 退款申请已通过`)
}

const handleRejectRefund = (record) => {
  currentOrder.value = record
  refundForm.reason = ''
  refundVisible.value = true
}

const submitRefundReject = (done) => {
  if (!refundForm.reason.trim()) {
    Message.warning('请填写拒绝退款原因')
    done(false)
    return
  }

  const order = mockOrders.find(o => o.id === currentOrder.value.id)
  if (order) {
    order.status = 'refund_rejected'
    order.refund = order.refund || {}
    order.refund.handleTime = new Date().toLocaleString('zh-CN')
    order.refund.handleNote = refundForm.reason
    order.operationLogs.unshift({
      action: '退款申请被拒绝',
      time: new Date().toLocaleString('zh-CN'),
      operator: '管理员'
    })
  }

  Message.warning(`订单 ${currentOrder.value.orderNo} 退款申请已拒绝`)
  refundVisible.value = false
  done(true)
}
</script>

<style scoped>
.management {
  padding: 20px;
}

.management h2 {
  margin-bottom: 24px;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  background: #fff;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #e5e7eb;
  flex-shrink: 0;
}

.product-name {
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-phone {
  font-size: 12px;
  color: #999;
}

.amount {
  color: #ff4d4f;
  font-weight: 600;
}

.order-detail {
  padding: 10px 0;
}

.order-detail h4 {
  margin: 16px 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.detail-section {
  margin-top: 20px;
}

.product-detail-cell {
  display: flex;
  align-items: center;
  gap: 16px;
}

.detail-product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.product-info {
  flex: 1;
}

.product-info p {
  margin: 4px 0;
}

.product-price {
  color: #666;
  font-size: 13px;
}

.refund-section {
  background: #fff1f0;
  padding: 12px;
  border-radius: 8px;
}

:deep(.arco-table .arco-table-tr:hover .arco-table-td) {
  background-color: #f7f8fa;
}

:deep(.arco-tag) {
  border-radius: 4px;
}
</style>
