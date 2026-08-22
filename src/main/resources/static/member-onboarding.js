const MEMBER_POLICY_VERSION='2026-08-v1';
const MEMBER_REQUIRED_CONSENTS=['TERMS','PRIVACY_POLICY','COMMUNITY_RULES'];

async function loadMemberContributions(){
  if(!state.me?.memberId||!$('myContributions'))return;
  try{
    const entries=await api('/api/v1/member/contributions');
    const total=entries.reduce((sum,x)=>sum+Number(x.amount||0),0);
    $('myContributionTotal').textContent=brl(total);
    $('myContributions').classList.toggle('empty-state',entries.length===0);
    $('myContributions').innerHTML=entries.length
      ?entries.slice(0,12).map(x=>`<div class="ledger-row"><div><strong>Contribuição voluntária</strong><strong class="positive">${brl(x.amount)}</strong></div><small>${new Date(x.createdAt).toLocaleString('pt-BR')} · hash ${escapeHtml(String(x.entryHash||'').slice(0,12))}…</small></div>`).join('')
      :'Você ainda não registrou contribuições.';
  }catch(error){toast('Não foi possível carregar suas contribuições: '+error.message)}
}

async function memberMissingConsents(){
  if(!state.me?.memberId)return [];
  const records=await api(`/api/v1/privacy/consents/${state.me.memberId}`);
  const accepted=new Set(records.filter(r=>r.accepted===true&&r.documentVersion===MEMBER_POLICY_VERSION).map(r=>r.consentType));
  return MEMBER_REQUIRED_CONSENTS.filter(type=>!accepted.has(type));
}

async function ensureMemberOnboarding(){
  if(!state.me?.authenticated||state.me.role!=='MEMBER')return true;
  try{
    const missing=await memberMissingConsents();
    state.memberOnboarded=missing.length===0;
    state.memberMissingConsents=missing;
    if(state.memberOnboarded){
      if($('onboardingDialog')?.open)$('onboardingDialog').close();
      return true;
    }
    if(!$('onboardingDialog').open)$('onboardingDialog').showModal();
    return false;
  }catch(error){
    toast('Não foi possível verificar os documentos vigentes: '+error.message);
    return false;
  }
}

async function requireMemberOnboarding(event){
  if(state.memberOnboarded)return true;
  if(event){event.preventDefault();event.stopImmediatePropagation()}
  await ensureMemberOnboarding();
  toast('Conclua o primeiro acesso antes de usar esta função.');
  return false;
}

for(const formId of ['contributionForm','aidForm','documentForm']){
  $(formId)?.addEventListener('submit',event=>{
    if(!state.memberOnboarded){void requireMemberOnboarding(event)}
  },true);
}

$('onboardingDialog')?.addEventListener('cancel',event=>{if(!state.memberOnboarded)event.preventDefault()});
$('onboardingForm')?.addEventListener('submit',async event=>{
  event.preventDefault();
  if(!$('acceptTerms').checked||!$('acceptPrivacy').checked||!$('acceptRules').checked)return;
  $('onboardingMessage').textContent='Registrando seus aceites…';
  try{
    const missing=await memberMissingConsents();
    for(const type of missing){
      await api('/api/v1/privacy/consents',{
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({type,version:MEMBER_POLICY_VERSION,accepted:true})
      });
    }
    await ensureMemberOnboarding();
    $('onboardingMessage').textContent='';
    toast('Primeiro acesso concluído.');
  }catch(error){$('onboardingMessage').textContent=error.message}
});

document.addEventListener('member-session-ready',()=>{
  if(state.me?.authenticated&&state.me.role==='MEMBER'){
    void Promise.all([ensureMemberOnboarding(),loadMemberContributions()]);
  }
});

const originalLoadSession=loadSession;
loadSession=async function(){
  await originalLoadSession();
  document.dispatchEvent(new Event('member-session-ready'));
};

const originalLoadMine=loadMine;
loadMine=async function(){
  await originalLoadMine();
  await loadMemberContributions();
};

if(state.me?.authenticated&&state.me.role==='MEMBER'){
  document.dispatchEvent(new Event('member-session-ready'));
}
