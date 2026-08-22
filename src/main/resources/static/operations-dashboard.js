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

const baseLoadCases=loadCases;
loadCases=async function(){await baseLoadCases();await loadOperationalSummary();};

function initOpsDashboard(){
  injectDashboardControls();
  const timer=setInterval(()=>{
    if(state.me){clearInterval(timer);loadOperationalSummary();loadAuditTrail();}
  },100);
}
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',initOpsDashboard,{once:true});else initOpsDashboard();
