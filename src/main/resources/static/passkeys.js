const out=document.querySelector('#out');
const registerButton=document.querySelector('#register');

const b64uToBytes=s=>Uint8Array.from(atob(s.replace(/-/g,'+').replace(/_/g,'/').padEnd(Math.ceil(s.length/4)*4,'=')),c=>c.charCodeAt(0));
const bytesToB64u=b=>btoa(String.fromCharCode(...new Uint8Array(b))).replace(/\+/g,'-').replace(/\//g,'_').replace(/=+$/,'');

function decodeCreation(o){
  o.challenge=b64uToBytes(o.challenge);
  o.user.id=b64uToBytes(o.user.id);
  if(o.excludeCredentials)o.excludeCredentials=o.excludeCredentials.map(c=>({...c,id:b64uToBytes(c.id)}));
  return o;
}

function serialize(c){
  return {
    id:c.id,
    rawId:bytesToB64u(c.rawId),
    type:c.type,
    authenticatorAttachment:c.authenticatorAttachment,
    response:{
      attestationObject:bytesToB64u(c.response.attestationObject),
      clientDataJSON:bytesToB64u(c.response.clientDataJSON),
      transports:c.response.getTransports?c.response.getTransports():[]
    },
    clientExtensionResults:c.getClientExtensionResults()
  };
}

async function getCsrf(){
  const r=await fetch('/api/v1/auth/csrf',{credentials:'same-origin'});
  if(!r.ok)throw new Error(`Falha ao obter CSRF (${r.status})`);
  return r.json();
}

registerButton.addEventListener('click',async()=>{
  registerButton.disabled=true;
  out.textContent='Preparando cadastro da passkey...';
  try{
    if(!window.PublicKeyCredential||!navigator.credentials)throw new Error('Este navegador não oferece suporte a passkeys/WebAuthn.');
    const csrf=await getCsrf();
    let r=await fetch('/webauthn/register/options',{
      method:'POST',
      credentials:'same-origin',
      headers:{[csrf.headerName]:csrf.token}
    });
    if(!r.ok)throw new Error(await r.text());
    const opts=decodeCreation(await r.json());
    out.textContent='Confirme no Windows Hello ou no autenticador do seu dispositivo.';
    const credential=await navigator.credentials.create({publicKey:opts});
    const body={publicKey:{credential:serialize(credential),label:document.querySelector('#label').value.trim()||'Meu dispositivo'}};
    r=await fetch('/webauthn/register',{
      method:'POST',
      credentials:'same-origin',
      headers:{'Content-Type':'application/json',[csrf.headerName]:csrf.token},
      body:JSON.stringify(body)
    });
    if(!r.ok)throw new Error(await r.text());
    out.textContent='Passkey cadastrada com sucesso. Agora saia e entre usando a passkey para validar o MFA.';
  }catch(e){
    out.textContent='Erro: '+(e?.message||String(e));
  }finally{
    registerButton.disabled=false;
  }
});
