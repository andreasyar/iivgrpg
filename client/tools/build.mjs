import { access, cp, mkdir, readdir, readFile, rm } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import { join } from 'node:path';
import { Script } from 'node:vm';

const source = fileURLToPath(new URL('../', import.meta.url));
const output = join(source, 'dist');
const html = await readFile(join(source, 'test_001.html'), 'utf8');
const scripts = [...html.matchAll(/<script\b[^>]*\bsrc="([^"]+)"/gi)].map(match => match[1]);
// Проверяем классические скрипты без преобразования в модули.
for (const name of scripts) {
  new Script(await readFile(join(source, name), 'utf8'), { filename: name });
}
for (const match of html.matchAll(/<script\b[^>]*>([\s\S]*?)<\/script>/gi)) {
  new Script(match[1], { filename: 'test_001.html' });
}
await mkdir(output, { recursive: true });
for (const name of await readdir(output)) await rm(join(output, name), { recursive: true, force: true });
for (const name of scripts) await cp(join(source, name), join(output, name));
for (const name of await readdir(source)) {
  if (/\.(png|jpg|jpeg|gif|svg)$/i.test(name)) await cp(join(source, name), join(output, name));
}
await cp(join(source, 'test_001.html'), join(output, 'index.html'));
// Кэш мира создаёт сервер из БД; при сборке дистрибутива его может ещё не быть.
const cacheDir = join(source, 'cache');
try {
  await access(cacheDir);
  for (const name of ['objects.js', 'groundtiles.js', 'map_1.js', 'map_2.js']) {
    new Script(await readFile(join(cacheDir, name), 'utf8'), { filename: name });
  }
  await cp(cacheDir, join(output, 'cache'), { recursive: true });
  console.log('Клиент MMORPG 1 проверен и собран в dist/ вместе с кэшем мира и спрайтами.');
} catch (error) {
  if (error && error.code !== 'ENOENT') throw error;
  console.log('Клиент MMORPG 1 проверен и собран в dist/ со спрайтами; кэш мира появится при запуске сервера.');
}
