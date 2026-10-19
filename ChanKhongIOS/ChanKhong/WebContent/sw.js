const CACHE_NAME = 'ttc-vacuum-pwa-v8';
const APP_SHELL = [
  './',
  './index.html',
  './manifest.webmanifest',
  './apple-touch-icon.png',
  './icon-192.png',
  './icon-512.png',
  './tc-vacuum-icon.png'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => cache.addAll(APP_SHELL))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(
        keys.filter((key) => key.startsWith('ttc-vacuum-pwa-') && key !== CACHE_NAME)
          .map((key) => caches.delete(key))
      ))
      .then(() => self.clients.claim())
  );
});

function pdfRangeResponse(response, rangeHeader) {
  return response.blob().then((blob) => {
    const match = /^bytes=(\d*)-(\d*)$/.exec(rangeHeader || '');
    if (!match) return response;

    const size = blob.size;
    const suffixLength = match[1] === '' ? Number(match[2]) : null;
    const start = suffixLength === null ? Number(match[1]) : Math.max(0, size - suffixLength);
    const requestedEnd = match[1] === '' || match[2] === '' ? size - 1 : Number(match[2]);
    if (!Number.isFinite(start) || start < 0 || start >= size || requestedEnd < start) {
      return new Response(null, {
        status: 416,
        headers: { 'Content-Range': `bytes */${size}`, 'Accept-Ranges': 'bytes' }
      });
    }

    const end = Math.min(requestedEnd, size - 1);
    return new Response(blob.slice(start, end + 1), {
      status: 206,
      headers: {
        'Accept-Ranges': 'bytes',
        'Content-Length': String(end - start + 1),
        'Content-Range': `bytes ${start}-${end}/${size}`,
        'Content-Type': response.headers.get('Content-Type') || 'application/pdf'
      }
    });
  });
}

async function handlePdf(request) {
  const cache = await caches.open(CACHE_NAME);
  const cached = await cache.match(request.url);
  const range = request.headers.get('Range');
  if (cached) return range ? pdfRangeResponse(cached, range) : cached;

  try {
    const fullRequest = range
      ? new Request(request.url, { credentials: 'same-origin' })
      : request;
    const response = await fetch(fullRequest);
    if (response.ok && response.status === 200) {
      try { await cache.put(request.url, response.clone()); } catch (_) { /* Keep online reading available if storage is full. */ }
      return range ? pdfRangeResponse(response, range) : response;
    }
    return response;
  } catch (_) {
    return new Response('This PDF has not been cached yet. Open it once while online, then try again offline.', {
      status: 503,
      headers: { 'Content-Type': 'text/plain; charset=utf-8' }
    });
  }
}

self.addEventListener('fetch', (event) => {
  const request = event.request;
  if (request.method !== 'GET') return;

  const url = new URL(request.url);
  if (url.origin !== self.location.origin) return;

  if (url.pathname.toLowerCase().endsWith('.pdf')) {
    event.respondWith(handlePdf(request));
    return;
  }

  event.respondWith((async () => {
    const cache = await caches.open(CACHE_NAME);
    const cached = await cache.match(request);
    if (cached) return cached;
    try {
      const response = await fetch(request);
      if (response.ok && response.type === 'basic') {
        try { await cache.put(request, response.clone()); } catch (_) { /* The network response still works. */ }
      }
      return response;
    } catch (_) {
      return cache.match('./index.html') || new Response('You are offline.', {
        status: 503,
        headers: { 'Content-Type': 'text/plain; charset=utf-8' }
      });
    }
  })());
});
