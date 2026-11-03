// Запускать в отдельном контейнере Node 24, не в runtime-образах.
import assert from 'node:assert/strict';

const base = process.env.CLIENT_URL || 'http://web';
const response = await fetch(base);
assert.equal(response.status, 200);
const html = await response.text();
assert.match(html, /MMORTS test client/);
assert.match(html, /location\.host \+ '\/ws\/'/);
assert.doesNotMatch(html, /@vite\/client/);
for (const match of html.matchAll(/<script\b[^>]*\bsrc="([^"]+)"/gi)) {
  const script = await fetch(new URL(match[1], base));
  assert.equal(script.status, 200, match[1]);
  assert.doesNotMatch(script.headers.get('content-type') || '', /text\/html/);
}
assert.equal((await fetch(new URL('/sprites/grass.png', base))).status, 404);
assert.equal((await fetch(new URL('/healthz', base))).status, 200);

const sockets = [];
try {
  // Держим оба соединения открытыми: два игрока через Nginx.
  await Promise.all([1, 2].map(player => new Promise((resolve, reject) => {
    const url = new URL('/ws/', base);
    url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:';
    const socket = new WebSocket(url);
    sockets.push(socket);
    const timer = setTimeout(() => finish(new Error(`Игрок ${player}: нет WELCOME`)), 15000);
    function finish(error) {
      clearTimeout(timer);
      if (error) reject(error);
      else resolve();
    }
    socket.addEventListener('open', () => {
      socket.send(JSON.stringify([['LOGIN', {
        login: `Release${Date.now()}P${player}`,
        password: 'test',
      }]]));
    });
    socket.addEventListener('error', () => finish(new Error(`Игрок ${player}: ошибка WebSocket`)));
    socket.addEventListener('message', ({ data }) => {
      try {
        const [type] = JSON.parse(data);
        if (type === 'WELCOME') finish();
      } catch (error) {
        finish(error);
      }
    });
  })));
  assert.ok(sockets.every(socket => socket.readyState === WebSocket.OPEN));
  console.log('Release OK: статический клиент, без встроенных спрайтов, два игрока через /ws/.');
} finally {
  for (const socket of sockets) socket.close();
}
