import { createSSRApp } from "vue";
import App from "./App.vue";
import uviewPlus from 'uview-plus'

// 引入我们刚刚封装的请求工具
import { request } from './utils/request.js'

export function createApp() {
	const app = createSSRApp(App);
	app.use(uviewPlus)
	
	// 将 request 挂载到全局，方便在各个页面使用
	app.config.globalProperties.$request = request
	
	return {
		app,
	};
}
