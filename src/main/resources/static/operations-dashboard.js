const opsDashboard={search:'',category:'ALL',summary:null};

function injectDashboardControls(){
  const header=document.querySelector('.ops-header');
  if(!header||document.getElementById('opsSummary'))return;
  const wrap=document.createElement('div');
  wrap.innerHTML=`<section id="opsSummary" class="ops-summary" aria-label="Indicadores operacionais">
    <div><small>Pendentes</small><strong id="pendingIndicator">—</strong></div>
    <div><small>Reconciliação</small><strong id="reconciliationIndicator">—</strong></div>
    <div><small>Pagamentos presos</small><strong id="stuckIndicator">—</strong></div>
  </section>
  <div class="ops-extra-filters">
    <label>Buscar<input id="caseSearch" type="search" placeholder="Motivo, categoria ou ID" autocomplete="off"></label>
    <label>Categoria<select id="categoryFilter"><option value="ALL">Todas</option><option value="HEALTH">Saúde</option><option value="FOOD">Alimentação</option><option value="HOUSING">Moradia</option><option value="EDUCATION">Educação</option><option value="OTHER">Outros</option></select></label>
  </div>`;
  header.insertAdjacentElement('afterend',wrap);
  document.getElementById('caseSearch').addEventListener('input',e=>{opsDashboard.search=e.target.value.trim().toLowerCase();renderCases();});
  document.getElementById('categoryFilter').addEventListener('change',e=>{opsDashboard.category=e.target.value;renderCases();});
}

const baseRenderCases=renderCases;
renderCases=function(){
  const original=state.cases;
  const q=opsDashboard.search;
  state.cases=original.filter(x=>{
    const categoryOk=opsDashboard.category==='ALL'||x.category===opsDashboard.category;
    const haystack=`${x.id} ${x.category||''} ${x.reason||''}`.toLowerCase();
    return categoryOk&&(!q||haystack.includes(q));
  });
  try{baseRenderCases();}finally{state.cases=original;}
};

async function loadOperationalSummary(){
  try{
    const s=await json('/api/v1/operations/summary');
    opsDashboard.summary=s;
    document.getElementById('pendingIndicator').textContent=s.pending;
    document.getElementById('reconciliationIndicator').textContent=s.reconciliation;
    document.getElementById('stuckIndicator').textContent=s.stuckPayments;
  }catch(e){toast(e.message);}
}

async function loadAuditTrail(){
  if(state.me?.role!=='AUDITOR')return;
  let section=document.getElementById('auditorTrail');
  if(!section){
    section=document.createElement('article');
    section.id='auditorTrail';
    section.className='ops-card auditor-trail';
    section.innerHTML='<div class="panel-title"><strong>Trilha global de auditoria</strong><span>últimos 100</span></div><div id="auditList" class="timeline"></div>';
    document.querySelector('.ops-main').append(section);
  }
  try{
    const rows=await json('/api/v1/operations/audit-events');
    document.getElementById('auditList').innerHTML=rows.length?rows.map(x=>`<div class="timeline-item"><strong>${esc(x.action)} · ${esc(x.entityType)}</strong><small>${date(x.createdAt)} · ator ${esc(x.actorUserId||'sistema')}</small><div>${esc(x.metadata||'')}</div><small>hash ${esc((x.eventHash||'').slice(0,16))}…</small></div>`).join(''):'<p>Nenhum evento de auditoria.</p>';
  }catch(e){toast(e.message);}
}

function renderProviderRefreshControl(rows){
  const paymentCard=document.getElementById('paymentList')?.closest('.ops-card');
  if(!paymentCard)return;
  let control=document.getElementById('providerRefreshControl');
  if(!control){
    control=document.createElement('div');
    control.id='providerRefreshControl';
    control.className='reconciliation-form hidden';
    control.innerHTML='<button id="providerRefreshButton" class="secondary" type="button">Consultar status no provedor</button><small>Consulta somente o status da tentativa existente. Não cria uma nova transferência.</small>';
    document.getElementById('paymentList').insertAdjacentElement('afterend',control);
    document.getElementById('providerRefreshButton').addEventListener('click',refreshProviderStatus);
  }
  const candidate=rows.find(x=>['PROCESSING','RECONCILIATION_REQUIRED'].includes(x.status));
  control.dataset.paymentId=candidate?.id||'';
  control.classList.toggle('hidden',state.me?.role!=='ADMIN'||!candidate);
}

async function refreshProviderStatus(){
  const control=document.getElementById('providerRefreshControl');
  const paymentId=control?.dataset.paymentId;
  if(state.me?.role!=='ADMIN'||!paymentId)return;
  const button=document.getElementById('providerRefreshButton');
  button.disabled=true;
  try{
    const updated=await json(`/api/v1/payments/attempts/${encodeURIComponent(paymentId)}/refresh-provider`,{method:'POST'});
    await selectCase(state.selectedId,false);
    await loadOperationalSummary();
    toast(`Status consultado: ${paymentStatusLabel(updated.status)}`);
  }catch(e){toast(e.message);}finally{button.disabled=false;}
}

const baseLoadPayments=loadPayments;
loadPayments=async function(aidId,aidStatus){
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
  renderProviderRefreshControl(rows);
};

const baseLoadCases=loadCases;
loadCases=async function(){await baseLoadCases();await loadOperationalSummary();};

function initOpsDashboard(){
  injectDashboardControls();
  const timer=setInterval(()=>{
    if(state.me){clearInterval(timer);loadOperationalSummary();loadAuditTrail();}
  },100);
}
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',initOpsDashboard,{once:true});else initOpsDashboard();
