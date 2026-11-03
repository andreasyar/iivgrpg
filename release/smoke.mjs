// Запускать в отдельном контейнере Node 24, не в runtime-образах.
import assert from 'node:assert/strict';
import { runInNewContext } from 'node:vm';

const base = process.env.CLIENT_URL || 'http://web';
const response = await fetch(base);
assert.equal(response.status, 200);
const html = await response.text();
assert.match(html, /MMORPG 1/);
assert.match(html, /location\.host \+ '\/ws\/'/);
assert.doesNotMatch(html, /@vite\/client/);
for (const match of html.matchAll(/<script\b[^>]*\bsrc="([^"]+)"/gi)) {
  const script = await fetch(new URL(match[1], base));
  assert.equal(script.status, 200, match[1]);
  assert.doesNotMatch(script.headers.get('content-type') || '', /text\/html/);
}
assert.equal((await fetch(new URL('/grass.png', base))).status, 200);
assert.equal((await fetch(new URL('/player.png', base))).status, 200);
assert.equal((await fetch(new URL('/healthz', base))).status, 200);

async function waitForCache() {
  const deadline = Date.now() + 60000;
  while (Date.now() < deadline) {
    const maps = await Promise.all(
      ['objects.js', 'groundtiles.js', 'map_1.js', 'map_2.js'].map(async name => {
        const result = await fetch(new URL(`/cache/${name}`, base));
        return result.status === 200 ? await result.text() : null;
      }),
    );
    if (maps.every(Boolean)) return maps;
    await new Promise(resolve => setTimeout(resolve, 500));
  }
  throw new Error('Кэш мира не появился');
}

const [objects, groundtiles, map1, map2] = await waitForCache();
const world = { MAPS: [] };
for (const source of [objects, groundtiles, map1, map2]) runInNewContext(source, world);
assert.equal(world.MAPS[1].tiles.length, 30);
assert.ok(world.MAPS[1].objects.length > 0);

const sockets = [];
function player(login) {
  const url = new URL('/ws/', base);
  url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:';
  const socket = new WebSocket(url);
  sockets.push(socket);
  const messages = [];
  let failure;
  socket.addEventListener('message', ({ data }) => {
    const message = JSON.parse(data);
    messages.push(message);
    if (['ERROR', 'WRONGPASS'].includes(message[0])) failure = new Error(data);
  });
  const send = (type, value) => socket.send(JSON.stringify(value === undefined ? [type] : [type, value]));
  async function waitFor(type, predicate = () => true) {
    const deadline = Date.now() + 15000;
    while (Date.now() < deadline) {
      if (failure) throw failure;
      const message = messages.find(message => message[0] === type && predicate(message[1]));
      if (message) return message[1];
      await new Promise(resolve => setTimeout(resolve, 25));
    }
    throw new Error(`Нет ${type} для ${login}`);
  }
  return {
    send, waitFor,
    async enter() {
      await new Promise((resolve, reject) => {
        socket.addEventListener('open', resolve, { once: true });
        socket.addEventListener('error', reject, { once: true });
      });
      send('LOGIN', { login, password: 'test' });
      return waitFor('HELLO');
    },
  };
}
try {
  // Логин исторически 3–16 символов: латинские буквы, цифры или `_`.
  const suffix = String(Date.now()).slice(-8);
  const first = player(`r${suffix}a`);
  const one = await first.enter();
  const second = player(`r${suffix}b`);
  const two = await second.enter();
  assert.notEqual(one.id, two.id);
  await first.waitFor('UNITSTATUS', value => value.unit.id === two.id);
  first.send('PING');
  await first.waitFor('PONG');
  first.send('NEWCHAT', { message: 'v1 release' });
  await second.waitFor('CHAT', value => value.message === 'v1 release');
  console.log('Release OK: статический клиент со спрайтами, кэш мира и два игрока через /ws/.');
} finally {
  for (const socket of sockets) socket.close();
}
