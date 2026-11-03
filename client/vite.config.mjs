import { defineConfig } from 'vite';

export default defineConfig({
  root: 'mmorts',
  cacheDir: '../node_modules/.vite',
  server: {
    host: '0.0.0.0', port: 5173, strictPort: true,
    watch: { usePolling: true },
    proxy: {
      '/ws/': { target: 'ws://server:8015', ws: true, rewrite: () => '/' },
    },
  },
  plugins: [{
    name: 'legacy-client-reload',
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        if (req.url?.split('?')[0] === '/') req.url = '/mmorts.html';
        next();
      });
    },
    handleHotUpdate({ server }) {
      // Классические скрипты делят глобальные переменные: перезагружаем страницу.
      server.ws.send({ type: 'full-reload' });
      return [];
    },
  }],
});
