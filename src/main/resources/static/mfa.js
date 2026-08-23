const $=id=>document.getElementById(id);
let csrfToken=null;

async function json(path,options={}){
  const method=(options.method||'GET').toUpperCase();
  const headers=new Headers(options.headers||{});
  if(!['GET','HEAD','OPTIONS'].includes(method)){
    if(!csrfToken) csrfToken=await fetch('/api/v1/auth/csrf',{credentials:'same-origin'}).then(r=>r.json());
    headers.set(csrfToken.headerName,csrfToken.token);
  }
  if(options.body && !(options.body instanceof FormData)) headers.set('Content-Type','application/json');
  const response=await fetch(path,{...options,method,headers,credentials:'same-origin'});
  const text=await response.text();
  let body=null;
  if(text){try{body=JSON.parse(text)}catch{body=text}}
  if(!response.ok){
    const message=typeof body==='object'?(body.message||body.error||`Erro ${response.status}`):(body||`Erro ${response.status}`);
    throw new Error(message);
  }
  return body;
}

function message(text,error=false){
  const el=$('message');
  el.textContent=text;
  el.classList.toggle('error',error);
}

function normalizeCode(value){return String(value||'').replace(/\D/g,'').slice(0,6)}
function returnToOperations(){location.replace('/operations.html')}

async function init(){
  try{
    const me=await json('/api/v1/auth/me');
    const allowed=['ANALYST','APPROVER','ADMIN','AUDITOR'];
    if(!me?.authenticated||!allowed.includes(me.role)){location.replace('/');return;}
    const status=await json('/api/v1/mfa/status');
    $('loadingBox').classList.add('hidden');
    if(status.enabled){$('verifyBox').classList.remove('hidden');}
    else{$('enrollBox').classList.remove('hidden');}
  }catch(error){
    $('loadingBox').classList.add('hidden');
    message(error.message,true);
  }
}

$('startEnroll').addEventListener('click',async()=>{
  try{
    $('startEnroll').disabled=true;
    message('Gerando chave de autenticação...');
    const data=await json('/api/v1/mfa/enroll',{method:'POST'});
    $('secretValue').textContent=data.secret;
    $('secretBox').classList.remove('hidden');
    $('confirmForm').classList.remove('hidden');
    message('Adicione a chave no seu aplicativo autenticador e informe o primeiro código gerado.');
    $('confirmCode').focus();
  }catch(error){
    $('startEnroll').disabled=false;
    message(error.message,true);
  }
});

$('confirmForm').addEventListener('submit',async event=>{
  event.preventDefault();
  const code=normalizeCode($('confirmCode').value);
  if(code.length!==6)return message('Informe os 6 dígitos do aplicativo autenticador.',true);
  try{
    message('Confirmando código...');
    await json('/api/v1/mfa/confirm',{method:'POST',body:JSON.stringify({code})});
    message('MFA ativado. Abrindo o painel operacional...');
    setTimeout(returnToOperations,500);
  }catch(error){message(error.message,true)}
});

$('verifyForm').addEventListener('submit',async event=>{
  event.preventDefault();
  const code=normalizeCode($('verifyCode').value);
  if(code.length!==6)return message('Informe os 6 dígitos do aplicativo autenticador.',true);
  try{
    message('Verificando código...');
    await json('/api/v1/mfa/verify',{method:'POST',body:JSON.stringify({code})});
    message('Verificação concluída. Abrindo o painel operacional...');
    setTimeout(returnToOperations,350);
  }catch(error){message(error.message,true)}
});

document.addEventListener('DOMContentLoaded',init,{once:true});
