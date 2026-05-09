import { ref, onMounted, onUnmounted } from 'vue'

export const vSlide = {
  mounted(el, binding) {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('slide-in')
            observer.unobserve(entry.target)
          }
        })
      },
      {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
      }
    )
    
    el.classList.add('slide-hidden')
    observer.observe(el)
    
    // Store observer for cleanup
    el._slideObserver = observer
  },
  unmounted(el) {
    if (el._slideObserver) {
      el._slideObserver.disconnect()
    }
  }
}

// 添加对应的CSS样式
const slideStyles = `
.slide-hidden {
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-in {
  opacity: 1;
  transform: translateY(0);
}
`

// 动态注入样式
export function injectSlideStyles() {
  if (!document.getElementById('slide-styles')) {
    const style = document.createElement('style')
    style.id = 'slide-styles'
    style.textContent = slideStyles
    document.head.appendChild(style)
  }
}
