import assert from 'node:assert/strict';
import { runInNewContext } from 'node:vm';

const clientUrl = process.env.CLIENT_URL || 'http://localhost:5173/';
const serverUrl = process.env.SERVER_URL || new URL('/ws/', clientUrl).href.replace(/^http/, 'ws');
async function get(path) {
  const response = await fetch(new URL(path, clientUrl), { signal: AbortSignal.timeout(10000) });
  assert.equal(response.status, 200, path);
  return response.text();
}
const html = await get('/');
assert.match(html, /MMORPG 1/);
for (const match of html.matchAll(/<script\b[^>]*\bsrc="([^"]+)"/gi)) {
  const script = await get(match[1]);
  assert.doesNotMatch(script, /^<!DOCTYPE/i, match[1]);
}
const world = { MAPS: [] };
for (const name of ['objects', 'groundtiles', 'map_1', 'map_2']) {
  runInNewContext(await get(`cache/${name}.js`), world);
}
assert.equal(world.MAPS[1].tiles.length, 30);
assert.ok(world.MAPS[1].objects.length > 0);
assert.equal(world.MAPOBJECTS[1].tiles[0][0].acc, 0);
console.log('HTTP: клиент MMORPG, скрипты, определения объектов и две карты доступны.');

const sockets = [];
function player(login) {
  const socket = new WebSocket(serverUrl);
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
    const deadline = Date.now() + 10000;
    while (Date.now() < deadline) {
      if (failure) throw failure;
      const message = messages.find(message => message[0] === type && predicate(message[1]));
      if (message) return message[1];
      await new Promise(resolve => setTimeout(resolve, 25));
    }
    throw new Error(`Нет ${type} для ${login}`);
  }
  return {
    socket, send, waitFor,
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
  // Повторяемые имена также проверяют вход в уже созданные аккаунты.
  const first = player('SmokeOne');
  const one = await first.enter();
  const second = player('SmokeTwo');
  const two = await second.enter();
  assert.notEqual(one.id, two.id);
  await first.waitFor('UNITSTATUS', value => value.unit.id === two.id);
  const list = await second.waitFor('UNITSLIST');
  assert.ok(list.units.some(value => value.unit.id === one.id));
  first.send('PING');
  await first.waitFor('PONG');
  first.send('NEWCHAT', { message: 'v1 smoke' });
  await second.waitFor('CHAT', value => value.message === 'v1 smoke');
  first.send('MOVETO', { x: 5, y: 1 });
  const move = await second.waitFor('MOVE', value => value.id === one.id);
  assert.deepEqual({ x: move.way.at(-1).x, y: move.way.at(-1).y }, { x: 5, y: 1 });
  for (const cell of move.way) {
    assert.ok(!(cell.x === 3 && cell.y >= 1 && cell.y <= 4), 'Путь пересекает дерево');
  }
  // Проверяем исполнение пути сервером: новый наблюдатель получает итоговую позицию.
  await new Promise(resolve => setTimeout(resolve, Math.ceil(move.way.length / 3 * 1000) + 500));
  const observer = player('SmokeWatch');
  await observer.enter();
  const snapshot = await observer.waitFor('UNITSLIST');
  const arrived = snapshot.units.find(value => value.unit.id === one.id).unit;
  assert.equal(arrived.x, 5);
  assert.equal(arrived.y, 1);
  first.socket.close();
  await second.waitFor('DELUNIT', value => value.id === one.id);
  console.log('WebSocket: два персонажа, взаимная видимость, PING, чат, движение с обходом деревьев и выход.');
} finally {
  for (const socket of sockets) socket.close();
}
