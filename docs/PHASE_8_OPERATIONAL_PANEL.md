# Fase 8 — Painel operacional

Este corte da issue #21 evolui o painel existente sem alterar as invariantes financeiras.

## Entregas deste corte

- busca por ID, categoria e motivo;
- filtro adicional por categoria;
- indicadores de pedidos pendentes, pagamentos em reconciliação e pagamentos PROCESSING há mais de 10 minutos;
- trilha global dos 100 eventos mais recentes disponível apenas ao AUDITOR;
- manutenção das ações existentes por papel para ANALYST, APPROVER e ADMIN;
- nenhum endpoint ou botão para forçar um auxílio para PAID.

## Segurança

A liquidação continua exclusiva do webhook autenticado do provedor sandbox. O painel apenas inicia pagamentos elegíveis e exibe estado operacional.

## Próximos critérios da #21

- E2E dedicado para busca, indicadores e AUDITOR;
- ação operacional segura para reconciliação usando o fluxo de serviço já existente;
- refinamento responsivo e acessibilidade;
- feedback de ações destrutivas/irreversíveis mais explícito.
