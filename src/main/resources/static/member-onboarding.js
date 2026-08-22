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

async function memberOnboardingStatus(){
  if(!state.me?.authenticated||state.me.role!=='MEMBER')return {complete:true,missing:[],required:[],version:null};
  return api('/api/v1/member/onboarding');
}

async function ensureMemberOnboarding(){
  if(!state.me?.authenticated||state.me.role!=='MEMBER')return true;
  try{
    const status=await memberOnboardingStatus();
    state.memberOnboarded=status.complete===true;
    state.memberMissingConsents=status.missing||[];
    state.memberPolicyVersion=status.version;
    state.memberRequiredConsents=status.required||[];
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
    const status=await memberOnboardingStatus();
    const completed=await api('/api/v1/member/onboarding',{
      method:'POST',
      headers:{'Content-Type':'application/json'},
      body:JSON.stringify({version:status.version,accepted:status.required})
    });
    state.memberOnboarded=completed.complete===true;
    state.memberMissingConsents=completed.missing||[];
    if(!state.memberOnboarded)throw new Error('O primeiro acesso ainda possui documentos pendentes.');
    $('onboardingDialog').close();
    $('onboardingMessage').textContent='';
    toast('Primeiro acesso concluído.');
  }catch(error){$('onboardingMessage').textContent=error.message}
});

document.addEventListener('member-session-ready',()=>{
  if(state.me?.authenticated&&state.me.role==='MEMBER'){
    void Promise.all([ensureMemberOnboarding(),loadMemberContributions()]);
  }
});

async function bootstrapMemberFeatures(){
  for(let i=0;i<100&&state.me===null;i++)await new Promise(resolve=>setTimeout(resolve,50));
  if(state.me?.authenticated&&state.me.role==='MEMBER'){
    document.dispatchEvent(new Event('member-session-ready'));
  }
}

void bootstrapMemberFeatures();
