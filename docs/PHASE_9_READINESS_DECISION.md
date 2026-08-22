# Fase 9 — Production Readiness Gate

## Decisão

**GO para iniciar a Fase 9 como trabalho de Production Readiness em ambiente sandbox/staging.**

**NO-GO para dinheiro real, custódia, PIX real ou qualquer liquidação financeira de produção.**

Esta decisão separa explicitamente duas coisas diferentes: o sistema já possui evidência suficiente para avançar tecnicamente da Fase 8 para atividades de readiness, mas ainda não possui as validações externas e a infraestrutura de produção necessárias para movimentar recursos reais.

## Evidências aprovadas da Fase 8

### Piloto sandbox

O gate `pilot-simulation` cobre 50 membros fictícios, contribuições sandbox, 20 pedidos de auxílio, documentos, análise, antifraude, dupla aprovação, separação de funções, três liquidações completas, idempotência, tentativa de replay e validação das invariantes financeiras diretamente no PostgreSQL.

### Carga e concorrência

O gate `load-concurrency` cobre concorrência em ledger, auditoria, Redis e criação de tentativa financeira, incluindo 80 appends concorrentes no ledger, 80 na auditoria, 600 operações Redis e 20 racers para o mesmo auxílio. O gate valida ausência de forks, links órfãos e múltiplas tentativas financeiras ativas.

### Segurança e staging

A baseline mantém `security` e `staging-config` como gates obrigatórios. O staging permanece fail-closed para configurações sensíveis e a aplicação continua sem integração financeira real.

### Chaos engineering e disaster recovery

O gate `chaos-dr` valida falhas controladas, restart de dependências e restauração, com verificação posterior das invariantes financeiras.

### Operação humana

Os PRs #27 e #28 concluíram o painel operacional da Fase 8 para ANALYST, APPROVER, ADMIN e AUDITOR. A segregação de funções é preservada e não existe ação de interface capaz de forçar `PAID`. Itens em `RECONCILIATION_REQUIRED` permitem apenas nota operacional auditada pelo ADMIN; o estado financeiro continua dependente de confirmação externa autenticada.

### Observabilidade, incidentes e documentação

A Fase 8 possui baseline documentada para observabilidade/alertas, incident response, threat model, chaos/DR, LGPD, security checks e pentest readiness.

## Gate técnico para entrada na Fase 9

| Critério | Estado | Evidência |
| --- | --- | --- |
| Fase 8 verde em CI | APROVADO | workflows obrigatórios da baseline |
| Piloto reproduzível sem violação de invariantes | APROVADO | `pilot-simulation` |
| Carga e concorrência | APROVADO | `load-concurrency` |
| Staging endurecido | APROVADO | `staging-config` / PR #17 |
| Backup, restore e chaos/DR | APROVADO | `chaos-dr` / PR #16 |
| Observabilidade e incident response | APROVADO PARA READINESS | documentação e controles da Fase 8 |
| Documentação da Fase 8 | APROVADO | PR #26 |
| Painel operacional e segregação de funções | APROVADO | PRs #27 e #28 |

Resultado do gate técnico: **APROVADO PARA INICIAR PRODUCTION READINESS**.

## Bloqueadores absolutos para dinheiro real

Os itens abaixo permanecem **NO-GO** e não podem ser tratados como opcionais:

1. **Pentest independente** executado por terceiro qualificado, com achados críticos/altos corrigidos ou formalmente tratados.
2. **Revisão jurídica, contábil e regulatória** do modelo de ajuda mútua, fluxos financeiros, termos, responsabilidades e enquadramento aplicável.
3. **LGPD formal**: bases legais, retenção, direitos dos titulares, operadores/suboperadores, registro das operações e processo de incidente validados juridicamente.
4. **Secrets/KMS de produção**: nenhum segredo operacional de produção deve depender de configuração de desenvolvimento, arquivo local ou valor default.
5. **TLS, domínio e WebAuthn reais**, incluindo configuração correta de RP ID/origins e ciclo operacional de certificados.
6. **Provedor financeiro autorizado e contrato real**, com autenticação forte, idempotência, webhooks assinados, timeout/retry documentados e sandbox homologado antes da produção.
7. **Reconciliação externa**, com fonte independente do estado interno, rotina operacional, tratamento de divergências e evidência auditável.
8. **Monitoramento externo e alertas de produção**, com responsáveis, escalonamento e cobertura de disponibilidade, pagamentos, filas, banco, Redis e segurança.
9. **Backup/restore de infraestrutura real** testado no ambiente alvo, não apenas no sandbox de CI.
10. **Runbook de go-live e rollback**, incluindo critérios objetivos para interromper pagamentos e voltar ao modo seguro.

Enquanto qualquer item acima estiver pendente, a decisão permanece: **NO-GO PARA DINHEIRO REAL**.

## Escopo autorizado da Fase 9

A Fase 9 pode trabalhar em:

- preparação de infraestrutura de produção sem habilitar dinheiro real;
- KMS/secrets, TLS, domínio e WebAuthn reais;
- homologação sandbox de provedor financeiro;
- reconciliação externa em modo de teste;
- monitoramento externo;
- exercícios de incident response e restore no ambiente alvo;
- correções resultantes de pentest;
- formalização de documentação jurídica/LGPD com profissionais responsáveis;
- checklist de go-live, rollback e critérios de aprovação final.

## Escopo não autorizado

A Fase 9 **não autoriza**:

- receber contribuições reais;
- custodiar dinheiro de membros;
- executar PIX ou transferência real;
- marcar manualmente um auxílio como `PAID`;
- substituir confirmação do provedor por ação administrativa;
- remover dupla aprovação, antifraude, idempotência ou auditoria para acelerar go-live;
- apresentar o sistema como financeiramente pronto antes da conclusão dos gates externos.

## Critério para o futuro GO de produção

Uma decisão futura de **GO PARA PRODUÇÃO COM DINHEIRO REAL** deverá ser registrada separadamente e somente poderá ocorrer quando todos os bloqueadores externos estiverem concluídos, as evidências estiverem anexadas/referenciadas e um ensaio final de go-live/rollback no ambiente alvo tiver sido aprovado.

Até essa decisão existir, o sistema permanece **sandbox/staging only**.