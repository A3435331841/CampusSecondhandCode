import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { copyFileSync, mkdirSync, existsSync, readdirSync } from 'fs'
import { resolve } from 'path'

function copyCustomTabBar() {
  return {
    name: 'copy-custom-tab-bar',
    writeBundle() {
      const src = resolve(__dirname, 'src/wxcomponents/custom-tab-bar')
      const dest = resolve(__dirname, 'dist/dev/mp-weixin/custom-tab-bar')
      if (!existsSync(src)) return
      mkdirSync(dest, { recursive: true })
      for (const f of readdirSync(src)) {
        copyFileSync(resolve(src, f), resolve(dest, f))
      }
    }
  }
}

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    uni(),
    copyCustomTabBar(),
  ],
  css: {
    preprocessorOptions: {
      scss: {
        silenceDeprecations: ['legacy-js-api', 'import']
      }
    }
  }
})
