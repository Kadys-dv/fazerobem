# ADR-0006 — Sandbox antes de dinheiro real

**Status:** Accepted  
**Data:** 2026-08-21

## Contexto

Software que controla recursos reais envolve risco técnico, jurídico, regulatório, contábil, operacional e reputacional. Uma suíte verde não é evidência suficiente para assumir custódia ou movimentação financeira.

## Decisão

Todas as fases atuais permanecem sandbox. Nenhuma feature pode habilitar PIX, cartão, custódia ou pagamento real apenas por configuração casual.

O gate mínimo para discutir piloto real inclui:

- pentest independente;
- revisão jurídica, regulatória, contábil e LGPD;
- provider financeiro autorizado/homologado;
- TLS/domínio e WebAuthn reais;
- KMS/secret manager e rotação de chaves;
- backup/restore/disaster recovery comprovados;
- observabilidade, alertas e resposta a incidentes;
- reconciliação externa e processos operacionais formais.

## Consequências

- evolução é mais lenta, porém riscos são encontrados sem afetar recursos reais;
- demos e testes precisam identificar claramente dados e provider como sandbox;
- qualquer integração real será tratada como nova fase arquitetural, não como simples troca de URL.

## Revisão

Este ADR só pode ser sucedido após aprovação explícita do gate, atualização do threat model e documentação do novo provider/fluxo regulado.
