import {defineConfig} from 'vite';
export default defineConfig({server:{proxy:{'/api':'http://localhost:8090'}},build:{rollupOptions:{output:{manualChunks:{charts:['recharts'],react:['react','react-dom','react-router-dom']}}}}});

