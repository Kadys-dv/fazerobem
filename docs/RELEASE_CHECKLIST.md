# Release Readiness Checklist

Use este checklist para qualquer tag/release do Fazer o Bem.

## Código e testes
- [ ] `mvn clean verify` verde.
- [ ] Testcontainers PostgreSQL/Redis verdes.
- [ ] E2E Playwright do membro, governança e settlement verde.
- [ ] E2E WebAuthn verde.
- [ ] `frontend-syntax` verde.

## Segurança
- [ ] Gitleaks sem achados.
- [ ] CodeQL verde.
- [ ] SBOM CycloneDX gerada.
- [ ] Dependency review sem bloqueios.
- [ ] Segredos de CI efêmeros; nenhum segredo de produção em código.
- [ ] HMAC, replay protection e janela temporal testados.

## Integridade financeira sandbox
- [ ] Idempotência validada.
- [ ] `SETTLED` gera um único `AID_PAYMENT`.
- [ ] `FAILED` não debita o ledger.
- [ ] `RECONCILIATION_REQUIRED` não debita o ledger.
- [ ] Saldo insuficiente bloqueia antes do provider.
- [ ] Ledger e auditoria mantêm integridade.

## Documentação
- [ ] README representa o estado real.
- [ ] CHANGELOG atualizado.
- [ ] Release notes atualizadas.
- [ ] Arquitetura e threat model revisados.
- [ ] Limitações conhecidas documentadas.

## Gate para dinheiro real
Para qualquer versão que pretenda sair do sandbox, todos os itens abaixo tornam-se obrigatórios:
- [ ] pentest independente;
- [ ] revisão jurídica/regulatória/contábil;
- [ ] avaliação LGPD/DPIA quando aplicável;
- [ ] KMS/secret manager real;
- [ ] TLS e domínio real;
- [ ] backup/restore e disaster recovery testados;
- [ ] provedor financeiro autorizado/homologado;
- [ ] observabilidade, alertas e resposta a incidentes;
- [ ] reconciliação externa comprovada.
