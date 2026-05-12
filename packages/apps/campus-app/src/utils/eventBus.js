import mitt from 'mitt';

const emitter = mitt();

export const eventBus = {
  $on: emitter.on,
  $off: emitter.off,
  $emit: emitter.emit,
};

// 事件类型
export const EVENTS = {
  NOTIFICATION_REFRESH: 'notification:refresh',
  ITEM_PUBLISHED: 'item:published',
  ORDER_CREATED: 'order:created',
};
