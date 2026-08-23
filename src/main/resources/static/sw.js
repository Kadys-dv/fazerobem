const CACHE='fazerobem-static-v5';
const STATIC=['/','/index.html','/app.css','/app.js','/member-onboarding.js','/terms.html','/privacy.html','/community-rules.html','/manifest.webmanifest','/icon.svg'];

self.addEventListener('install',event=>{
  event.waitUntil(caches.open(CACHE).then(cache=>cache.addAll(STATIC)));
  self.skipWaiting();
});

self.addEventListener('activate',event=>{
  event.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(k=>k!==CACHE).map(k=>caches.delete(k)))));
  self.clients.claim();
});

self.addEventListener('fetch',event=>{
  const request=event.request;
  if(request.method!=='GET')return;
  const url=new URL(request.url);
  if(url.origin!==self.location.origin)return;
  if(url.pathname.startsWith('/api/')||url.pathname.startsWith('/login')||url.pathname.startsWith('/logout')||url.pathname.startsWith('/webauthn/'))return;
  event.respondWith(caches.match(request).then(cached=>cached||fetch(request).then(response=>{
    if(response.ok&&STATIC.includes(url.pathname)){
      const clone=response.clone();
      caches.open(CACHE).then(cache=>cache.put(request,clone));
    }
    return response;
  })));
});
