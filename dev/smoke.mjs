import assert from 'node:assert/strict';

const clientUrl = process.env.CLIENT_URL || 'http://localhost:5173/';
const serverUrl = process.env.SERVER_URL || new URL('/ws/', clientUrl).href.replace(/^http/, 'ws');
const response = await fetch(clientUrl, { signal: AbortSignal.timeout(10000) });
assert.equal(response.status, 200);
const html = await response.text();
assert.match(html, /MMORTS test client/);
for (const match of html.matchAll(/<script\b[^>]*\bsrc="([^"]+)"/gi)) {
  const script = await fetch(new URL(match[1], clientUrl), { signal: AbortSignal.timeout(10000) });
  assert.equal(script.status, 200, `Отсутствует скрипт: ${match[1]}`);
  assert.doesNotMatch(script.headers.get('content-type') || '', /text\/html/);
}
console.log('HTTP: страница клиента и все скрипты доступны.');

const sockets = [];
try {
  const players = await Promise.all([1, 2].map(number => new Promise((resolve, reject) => {
    const socket = new WebSocket(serverUrl);
    sockets.push(socket);
    const units = new Map();
    let clientId;
    let confirms = 0;
    let done = false;
    const timer = setTimeout(() => finish(new Error('Нет подтверждения игрового состояния')), 15000);
    function finish(error) {
      if (done) return;
      done = true;
      clearTimeout(timer);
      if (error) reject(error);
      else resolve(clientId);
    }
    function apply(actions) {
      for (const [type, value] of actions) {
        if (type === 'NEWUNIT') units.set(value.id, value);
        if (type === 'DELUNIT') units.delete(value);
      }
    }
    function report(step) {
      socket.send(JSON.stringify([['STATE', step, [...units.values()].map(unit => ['UNIT', unit]), []]]));
    }
    socket.addEventListener('open', () => {
      socket.send(JSON.stringify([['LOGIN', { login: `Smoke${number}`, password: 'test' }]]));
    });
    socket.addEventListener('error', () => finish(new Error('Ошибка WebSocket')));
    socket.addEventListener('close', () => finish(new Error('Сервер закрыл соединение')));
    socket.addEventListener('message', ({ data }) => {
      try {
        const [type, value, state, actions] = JSON.parse(data);
        if (type === 'WELCOME') {
          clientId = value;
          apply(actions);
          assert.equal([...units.values()].filter(unit => unit.ownerId === clientId).length, 2);
          report(state + 1);
        } else if (type === 'CONFIRM') {
          apply(state);
          confirms++;
          if (confirms >= 3 && units.size >= 4) finish();
          report(value + 1);
        } else if (type === 'ERROR') {
          finish(new Error(`Ошибка игры: ${JSON.stringify(value)}`));
        }
      } catch (error) { finish(error); }
    });
  })));
  assert.notEqual(players[0], players[1]);
  console.log('WebSocket через прокси: два игрока вошли и подтвердили несколько шагов как минимум с четырьмя юнитами.');
} finally {
  for (const socket of sockets) socket.close();
}
