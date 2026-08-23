const state={me:null,cases:[],selectedId:null,selectedMemberId:null,csrf:null,initialized:false};
const $=id=>document.getElementById(id);
const money=v=>new Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'}).format(Number(v||0));
const date=v=>v?new Intl.DateTimeFormat('pt-BR',{dateStyle:'short',timeStyle:'short'}).format(new Date(v)):'—';
const esc=v=>String(v??'').replace(/[&<>'"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c]));

async function json(url,options={}){
  const headers=new Headers(options.headers||{});
  if(options.body && !(options.body instanceof FormData)) headers.set('Content-Type','application/json');
  if(options.method && options.method!=='GET'){
    if(!state.csrf) state.csrf=await fetch('/api/v1/auth/csrf',{credentials:'same-origin'}).then(r=>r.json());
    headers.set(state.csrf.headerName,state.csrf.token);
  }
  const res=await fetch(url,{...options,headers,credentials:'same-origin'});
  if(!res.ok){let msg=`Erro ${res.status}`;try{const body=await res.json();msg=body.message||body.error||msg;}catch{}throw new Error(msg);}
  if(res.status===204)return null;
  return res.json();
}

function toast(message){const el=$('toast');if(!el)return;el.textContent=message;el.classList.remove('hidden');setTimeout(()=>el.classList.add('hidden'),3200);}
function statusLabel(s){return ({PENDING:'Em análise',APPROVED:'Aprovado',REJECTED:'Rejeitado',PAID:'Pago'})[s]||s;}
function paymentStatusLabel(s){return ({READY:'Pronto',PROCESSING:'Processando',SETTLED:'Liquidado',FAILED:'Falhou',RECONCILIATION_REQUIRED:'Reconciliação necessária'})[s]||s;}

async function init(){
  if(state.initialized)return;
  state.initialized=true;
  state.me=await json('/api/v1/auth/me');
  const allowed=['ANALYST','APPROVER','ADMIN','AUDITOR'];
  if(!state.me.authenticated||!allowed.includes(state.me.role)){location.href='/';return;}
  $('operatorIdentity').textContent=`${state.me.email} · ${state.me.role}`;
  $('analystActions').classList.toggle('hidden',state.me.role!=='ANALYST');
  $('approverActions').classList.toggle('hidden',state.me.role!=='APPROVER');
  $('paymentActions').classList.toggle('hidden',!['ADMIN','AUDITOR'].includes(state.me.role));
  bind();
  await loadCases();
}

function bind(){
  $('refreshButton').addEventListener('click',loadCases);
  $('statusFilter').addEventListener('change',renderCases);
  $('analysisForm').addEventListener('submit',submitAnalysis);
  $('fraudForm').addEventListener('submit',submitFraud);
  $('approvalForm').addEventListener('submit',submitApproval);
  $('rejectionForm').addEventListener('submit',submitRejection);
  $('pixDestinationForm').addEventListener('submit',submitPixDestination);
  $('paymentForm').addEventListener('submit',submitPayment);
  $('reconciliationForm').addEventListener('submit',submitReconciliationNote);
}

async function loadCases(){
  try{
    state.cases=await json('/api/v1/aid-requests');
    renderCases();
    if(state.selectedId && state.cases.some(x=>x.id===state.selectedId)) await selectCase(state.selectedId,false);
  }catch(e){toast(e.message);}
}

function renderCases(){
  const filter=$('statusFilter').value;
  const items=state.cases.filter(x=>filter==='ALL'||x.status===filter);
  $('caseCount').textContent=items.length;
  $('caseList').innerHTML=items.length?items.map(x=>`<button class="case-item ${x.id===state.selectedId?'active':''}" data-id="${esc(x.id)}"><span class="case-row"><strong>${money(x.amount)}</strong><span class="status-pill">${esc(statusLabel(x.status))}</span></span><small>${esc(x.category)} · ${date(x.createdAt)}</small><small>${esc((x.reason||'').slice(0,92))}</small></button>`).join(''):'<p>Nenhum pedido neste filtro.</p>';
  document.querySelectorAll('.case-item').forEach(b=>b.addEventListener('click',()=>selectCase(b.dataset.id)));
}

async function selectCase(id,rerender=true){
  try{
    state.selectedId=id;
    if(rerender)renderCases();
    const detail=await json(`/api/v1/operations/aid-requests/${id}`);
    renderDetail(detail);
    if(['ADMIN','AUDITOR'].includes(state.me.role)) await loadPayments(id,detail.request.status);
  }catch(e){toast(e.message);}
}

function renderDetail(d){
  const r=d.request,e=d.eligibility;
  state.selectedMemberId=r.memberId;
  $('caseWorkspace').classList.remove('empty-state');
  $('emptyCase').classList.add('hidden');
  $('caseContent').classList.remove('hidden');
  $('caseStatus').textContent=statusLabel(r.status);
  $('caseTitle').textContent=`Pedido ${r.category}`;
  $('caseMeta').textContent=`Criado em ${date(r.createdAt)} · membro ${r.memberId}`;
  $('caseAmount').textContent=money(r.amount);
  $('caseReason').textContent=r.reason;
  const blockers=e.blockers||[],warnings=e.warnings||[];
  $('eligibilitySummary').innerHTML=`<div class="${e.eligible?'eligibility-ok':'eligibility-blocked'}">${e.eligible?'Elegível pelos critérios atuais':'Bloqueado pelos critérios atuais'}</div>${blockers.length?`<ul class="eligibility-list">${blockers.map(x=>`<li>${esc(x)}</li>`).join('')}</ul>`:''}${warnings.length?`<ul class="eligibility-list">${warnings.map(x=>`<li>Atenção: ${esc(x)}</li>`).join('')}</ul>`:''}<small>Documentos: ${e.documentCount} · Antifraude: ${esc(e.fraudStatus||'PENDING')} · Aprovação exige 2 aprovadores distintos.</small>`;
  $('documentCount').textContent=d.documents.length;
  $('documentList').innerHTML=d.documents.length?d.documents.map(x=>`<div class="timeline-item"><strong>${esc(x.documentType)} · ${esc(x.fileName)}</strong><small>${esc(x.contentType)} · ${Math.ceil((x.sizeBytes||0)/1024)} KB · ${date(x.createdAt)}</small><a class="document-link" href="/api/v1/aid-documents/${encodeURIComponent(x.id)}/content" target="_blank" rel="noopener">Abrir documento</a></div>`).join(''):'<p>Nenhum documento anexado.</p>';
  $('historyList').innerHTML=historyHtml(d);
  const pending=r.status==='PENDING';
  if(state.me.role==='ANALYST'){
    document.querySelectorAll('#analystActions button').forEach(b=>b.disabled=!pending);
    $('fraudForm').classList.toggle('hidden',!!d.fraudScreening);
  }
  if(state.me.role==='APPROVER') document.querySelectorAll('#approverActions button').forEach(b=>b.disabled=!pending);
}

async function loadPayments(aidId,aidStatus){
  const rows=await json(`/api/v1/payments/${aidId}`);
  $('paymentCount').textContent=rows.length;
  $('paymentList').innerHTML=rows.length?rows.map(x=>`<div class="timeline-item"><strong>${esc(paymentStatusLabel(x.status))} · ${money(x.amount)}</strong><small>${date(x.updatedAt)} · ref ${esc(x.providerReference||'aguardando')}</small></div>`).join(''):'<p>Nenhuma tentativa de pagamento registrada.</p>';
  const active=rows.some(x=>['READY','PROCESSING','SETTLED','RECONCILIATION_REQUIRED'].includes(x.status));
  $('pixDestinationForm').classList.toggle('hidden',state.me.role!=='ADMIN');
  $('paymentForm').classList.toggle('hidden',state.me.role!=='ADMIN');
  $('initiatePaymentButton').disabled=state.me.role!=='ADMIN'||aidStatus!=='APPROVED'||active;
  const recon=rows.find(x=>x.status==='RECONCILIATION_REQUIRED');
  $('reconciliationForm').classList.toggle('hidden',state.me.role!=='ADMIN'||!recon);
  $('reconciliationPaymentId').value=recon?.id||'';
}

function historyHtml(d){
  const rows=[];
  rows.push({at:d.request.createdAt,title:'Pedido criado',text:`${d.request.category} · ${money(d.request.amount)}`});
  (d.analyses||[]).forEach(x=>rows.push({at:x.createdAt,title:'Parecer do analista',text:x.opinion}));
  if(d.fraudScreening) rows.push({at:d.fraudScreening.createdAt,title:`Antifraude: ${d.fraudScreening.status}`,text:`Risco ${d.fraudScreening.riskScore}/100 · ${d.fraudScreening.note}${d.fraudScreening.flags?' · '+d.fraudScreening.flags:''}`});
  (d.approvals||[]).forEach((x,i)=>rows.push({at:x.createdAt,title:`Aprovação ${i+1}`,text:x.note}));
  if(d.request.status==='APPROVED')rows.push({at:d.request.updatedAt||d.request.createdAt,title:'Dupla aprovação concluída',text:d.request.decisionReason||'Pedido aprovado.'});
  if(d.request.status==='PAID')rows.push({at:d.request.updatedAt||d.request.createdAt,title:'Auxílio pago',text:d.request.decisionReason||'Pagamento liquidado e registrado no ledger.'});
  if(d.request.status==='REJECTED')rows.push({at:d.request.updatedAt||d.request.createdAt,title:'Pedido rejeitado',text:d.request.decisionReason||'Rejeição registrada.'});
  return rows.sort((a,b)=>new Date(a.at)-new Date(b.at)).map(x=>`<div class="timeline-item"><strong>${esc(x.title)}</strong><small>${date(x.at)}</small><div>${esc(x.text)}</div></div>`).join('');
}

async function submitAnalysis(ev){ev.preventDefault();await act(`/api/v1/aid-requests/${state.selectedId}/analysis`,{opinion:$('analysisOpinion').value},'Parecer registrado');$('analysisForm').reset();}
async function submitFraud(ev){ev.preventDefault();await act(`/api/v1/aid-requests/${state.selectedId}/fraud-screening`,{status:$('fraudStatus').value,riskScore:Number($('riskScore').value),flags:$('fraudFlags').value,note:$('fraudNote').value},'Triagem antifraude concluída');$('fraudForm').reset();$('riskScore').value='0';}
async function submitApproval(ev){ev.preventDefault();await act(`/api/v1/aid-requests/${state.selectedId}/approve`,{note:$('approvalNote').value},'Aprovação registrada');$('approvalForm').reset();}
async function submitRejection(ev){ev.preventDefault();await act(`/api/v1/aid-requests/${state.selectedId}/reject`,{note:$('rejectionNote').value},'Rejeição registrada');$('rejectionForm').reset();}
async function submitPixDestination(ev){
  ev.preventDefault();
  if(state.me?.role!=='ADMIN'||!state.selectedMemberId)return;
  const input=$('pixKey');
  const raw=input.value.trim();
  if(!raw)return;
  try{
    const saved=await json(`/api/v1/admin/payment-destinations/${encodeURIComponent(state.selectedMemberId)}/pix`,{
      method:'PUT',
      body:JSON.stringify({keyType:$('pixKeyType').value,pixKey:raw})
    });
    input.value='';
    $('pixDestinationStatus').textContent=`Destino protegido: ${saved.masked} · ${saved.keyType}`;
    toast('Destino Pix protegido salvo');
  }catch(e){
    input.value='';
    toast(e.message);
  }
}
async function submitPayment(ev){
  ev.preventDefault();
  if(!state.selectedId)return;
  try{
    const key=`ops-${state.selectedId}`;
    await json(`/api/v1/payments/${state.selectedId}/initiate`,{method:'POST',headers:{'Idempotency-Key':key}});
    await loadCases();
    await selectCase(state.selectedId);
    toast('Pagamento sandbox iniciado');
  }catch(e){toast(e.message);}
}
async function submitReconciliationNote(ev){
  ev.preventDefault();
  const paymentId=$('reconciliationPaymentId').value;
  const note=$('reconciliationNote').value.trim();
  if(!paymentId||!note)return;
  try{
    await json(`/api/v1/payments/attempts/${encodeURIComponent(paymentId)}/reconciliation-note`,{method:'POST',body:JSON.stringify({note})});
    $('reconciliationNote').value='';
    await selectCase(state.selectedId,false);
    toast('Nota de reconciliação registrada; status mantido');
  }catch(e){toast(e.message);}
}
async function act(url,body,success){
  try{
    await json(url,{method:'POST',body:JSON.stringify(body)});
    await loadCases();
    await selectCase(state.selectedId);
    toast(success);
  }catch(e){toast(e.message);}
}

function bootstrap(){init().catch(e=>{state.initialized=false;toast(e.message);});}
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',bootstrap,{once:true});
else bootstrap();
