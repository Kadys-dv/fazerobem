const state={me:null,policies:[],installPrompt:null};
const $=id=>document.getElementById(id);
const brl=value=>new Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'}).format(Number(value||0));
const labels={FOOD:'Alimentação',HEALTH:'Saúde',HOUSING:'Moradia',UTILITIES:'Contas essenciais',EMPLOYMENT:'Emprego e renda',EDUCATION:'Educação',GENERAL_EMERGENCY:'Emergência geral'};
const statusLabels={PENDING:'Em análise',APPROVED:'Aprovado',REJECTED:'Não aprovado',PAID:'Auxílio pago'};
const escapeHtml=value=>String(value??'').replace(/[&<>'"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c]));

function toast(message){const el=$('toast');el.textContent=message;el.classList.remove('hidden');clearTimeout(toast.timer);toast.timer=setTimeout(()=>el.classList.add('hidden'),3500)}
function parseMoney(value){const normalized=String(value).trim().replace(/\./g,'').replace(',','.');const number=Number(normalized);if(!Number.isFinite(number)||number<=0)throw new Error('Informe um valor válido.');return number.toFixed(2)}
async function jsonOrError(response){if(response.ok){if(response.status===204)return null;const text=await response.text();return text?JSON.parse(text):null}let message=`Erro ${response.status}`;try{const data=await response.json();message=data.message||data.error||message}catch{}throw new Error(message)}
async function csrf(){return fetch('/api/v1/auth/csrf',{credentials:'same-origin'}).then(jsonOrError)}
async function api(path,options={}){const method=(options.method||'GET').toUpperCase();const headers=new Headers(options.headers||{});if(!['GET','HEAD','OPTIONS'].includes(method)){const token=await csrf();headers.set(token.headerName,token.token)}const response=await fetch(path,{credentials:'same-origin',...options,method,headers});return jsonOrError(response)}

function openAuth(mode='login'){$('authDialog').showModal();switchAuth(mode);$('authMessage').textContent=''}
function switchAuth(mode){const login=mode==='login';$('loginForm').classList.toggle('hidden',!login);$('registerForm').classList.toggle('hidden',login);$('loginTab').classList.toggle('active',login);$('registerTab').classList.toggle('active',!login)}

async function loadSession(){try{state.me=await api('/api/v1/auth/me');}catch{state.me={authenticated:false}}const logged=state.me?.authenticated===true;$('authBtn').classList.toggle('hidden',logged);$('logoutBtn').classList.toggle('hidden',!logged);$('heroAuthBtn').textContent=logged?'Ir para minha área':'Participar da comunidade';if(logged&&state.me.role==='MEMBER'){$('memberArea').classList.remove('hidden');$('memberRole').textContent=state.me.role;$('welcomeTitle').textContent='Minha comunidade';await loadMine()}else{$('memberArea').classList.add('hidden')}}

async function loadPublic(){try{const [t,policies,ledger]=await Promise.all([api('/api/v1/transparency'),api('/api/v1/aid-policies'),api('/api/v1/transparency/ledger')]);state.policies=policies;$('balance').textContent=brl(t.balance);$('members').textContent=t.activeMembers;$('requests').textContent=t.totalAidRequests;$('paid').textContent=t.paidAidRequests;renderPolicies();renderLedger(ledger);renderCategoryOptions()}catch(error){toast('Não foi possível atualizar a transparência: '+error.message)}}
function renderPolicies(){$('policies').innerHTML=state.policies.map(p=>`<div class="policy-card"><strong>${escapeHtml(labels[p.category]||p.category)}</strong><div class="chips"><span class="chip">Teto ${brl(p.maxAmount)}</span><span class="chip">Carência ${p.waitingDays} dias</span><span class="chip">Intervalo ${p.cooldownDays} dias</span><span class="chip">Documento ${p.documentRequired?'obrigatório':'opcional'}</span></div></div>`).join('')||'<div class="empty-state">Nenhuma regra disponível.</div>'}
function renderLedger(entries){$('ledger').innerHTML=entries.length?entries.slice(0,12).map(x=>`<div class="ledger-row"><div><strong>${escapeHtml(x.description||x.type)}</strong><strong class="${Number(x.amount)>=0?'positive':'negative'}">${brl(x.amount)}</strong></div><small>${new Date(x.createdAt).toLocaleString('pt-BR')} · ${escapeHtml(x.type)}</small></div>`).join(''):'<div class="empty-state">Nenhuma movimentação publicada.</div>'}
function renderCategoryOptions(){const select=$('aidCategory');if(!select)return;select.innerHTML='<option value="">Selecione</option>'+state.policies.map(p=>`<option value="${p.category}">${escapeHtml(labels[p.category]||p.category)}</option>`).join('');updateSelectedPolicy()}
function updateSelectedPolicy(){const p=state.policies.find(x=>x.category===$('aidCategory').value);$('selectedPolicy').innerHTML=p?`<strong>${escapeHtml(labels[p.category]||p.category)}</strong><br>Teto: ${brl(p.maxAmount)} · carência: ${p.waitingDays} dias · intervalo: ${p.cooldownDays} dias · documento: ${p.documentRequired?'obrigatório':'opcional'}.`:'Selecione uma categoria para visualizar as regras.'}

async function loadMine(){if(!state.me?.memberId)return;try{const requests=await api('/api/v1/aid-requests/mine');$('myRequests').classList.toggle('empty-state',requests.length===0);$('myRequests').innerHTML=requests.length?requests.map(r=>`<div class="request-row"><div><strong>${escapeHtml(labels[r.category]||r.category)}</strong><span class="status-pill">${escapeHtml(statusLabels[r.status]||r.status)}</span></div><small>${brl(r.amount)} · ${new Date(r.createdAt).toLocaleDateString('pt-BR')}</small><small>${escapeHtml(r.reason)}</small></div>`).join(''):'Nenhum pedido enviado ainda.'}catch(error){toast('Não foi possível carregar seus pedidos: '+error.message)}}

async function login(email,password){const token=await csrf();const body=new URLSearchParams({username:email,password,[token.parameterName]:token.token});await fetch('/login',{method:'POST',credentials:'same-origin',headers:{'Content-Type':'application/x-www-form-urlencoded'},body});await loadSession();if(!state.me?.authenticated)throw new Error('E-mail ou senha inválidos.');$('authDialog').close();toast('Você entrou na comunidade.')}
async function logout(){const token=await csrf();const body=new URLSearchParams({[token.parameterName]:token.token});await fetch('/logout',{method:'POST',credentials:'same-origin',headers:{'Content-Type':'application/x-www-form-urlencoded'},body});state.me=null;await loadSession();toast('Sessão encerrada.')}

$('authBtn').addEventListener('click',()=>openAuth('login'));
$('heroAuthBtn').addEventListener('click',()=>{if(state.me?.authenticated)$('memberArea').scrollIntoView({behavior:'smooth'});else openAuth('register')});
$('closeAuth').addEventListener('click',()=>$('authDialog').close());
$('loginTab').addEventListener('click',()=>switchAuth('login'));
$('registerTab').addEventListener('click',()=>switchAuth('register'));
$('aidCategory').addEventListener('change',updateSelectedPolicy);
$('refreshPublic').addEventListener('click',loadPublic);
$('refreshMine').addEventListener('click',loadMine);
$('logoutBtn').addEventListener('click',()=>logout().catch(e=>toast(e.message)));

$('loginForm').addEventListener('submit',async event=>{event.preventDefault();$('authMessage').classList.remove('error');$('authMessage').textContent='Entrando…';try{await login($('loginEmail').value.trim(),$('loginPassword').value);$('authMessage').textContent=''}catch(error){$('authMessage').classList.add('error');$('authMessage').textContent=error.message}});
$('registerForm').addEventListener('submit',async event=>{event.preventDefault();$('authMessage').classList.remove('error');$('authMessage').textContent='Criando conta…';try{const email=$('registerEmail').value.trim();const password=$('registerPassword').value;await api('/api/v1/auth/register',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({name:$('registerName').value.trim(),email,password})});await login(email,password);toast('Conta criada com sucesso.')}catch(error){$('authMessage').classList.add('error');$('authMessage').textContent=error.message}});
$('contributionForm').addEventListener('submit',async event=>{event.preventDefault();try{if(!$('contributionConsent').checked)throw new Error('Confirme que compreendeu a natureza da contribuição.');const amount=parseMoney($('contributionAmount').value);await api('/api/v1/contributions',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({memberId:state.me.memberId,amount})});event.target.reset();toast('Contribuição sandbox registrada.');await loadPublic()}catch(error){toast(error.message)}});
$('aidForm').addEventListener('submit',async event=>{event.preventDefault();try{const payload={memberId:state.me.memberId,amount:parseMoney($('aidAmount').value),category:$('aidCategory').value,reason:$('aidReason').value.trim(),emergency:$('aidEmergency').checked};if(!payload.category)throw new Error('Selecione uma categoria.');await api('/api/v1/aid-requests',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)});event.target.reset();renderCategoryOptions();toast('Pedido enviado para análise.');await Promise.all([loadMine(),loadPublic()])}catch(error){toast(error.message)}});

window.addEventListener('beforeinstallprompt',event=>{event.preventDefault();state.installPrompt=event;$('installBtn').classList.remove('hidden')});
$('installBtn').addEventListener('click',async()=>{if(!state.installPrompt)return;state.installPrompt.prompt();await state.installPrompt.userChoice;state.installPrompt=null;$('installBtn').classList.add('hidden')});
if('serviceWorker'in navigator)window.addEventListener('load',()=>navigator.serviceWorker.register('/sw.js').catch(()=>{}));

Promise.all([loadPublic(),loadSession()]);
