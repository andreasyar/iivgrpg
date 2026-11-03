import { cp, mkdir, readdir, readFile, rm } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import { join } from 'node:path';
import { Script } from 'node:vm';

const client = fileURLToPath(new URL('../', import.meta.url));
const source = join(client, 'mmorts');
const output = join(client, 'dist');

const skipped = new Set(['sprites']);
const isAsset = (name) => /\.(png|jpe?g|gif|bmp|ico|swf)$/i.test(name);

// Проверяем классические скрипты без преобразования в модули.
for (const name of await readdir(source)) {
  if (name.endsWith('.js')) new Script(await readFile(join(source, name), 'utf8'), { filename: name });
}
const html = await readFile(join(source, 'mmorts.html'), 'utf8');
for (const match of html.matchAll(/<script\b[^>]*>([\s\S]*?)<\/script>/gi)) {
  new Script(match[1], { filename: 'mmorts.html' });
}

// Сам каталог dist может быть точкой монтирования Docker volume.
await mkdir(output, { recursive: true });
for (const name of await readdir(output)) await rm(join(output, name), { recursive: true, force: true });
for (const name of await readdir(source)) {
  // Локальные спрайты не входят в дистрибутив: их подключают отдельно.
  if (skipped.has(name) || isAsset(name)) continue;
  await cp(join(source, name), join(output, name), { recursive: true });
}
await cp(join(source, 'mmorts.html'), join(output, 'index.html'));
console.log('Клиент проверен и скопирован в dist/. Локальные спрайты исключены.');
