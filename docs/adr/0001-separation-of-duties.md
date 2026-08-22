# ADR-0001 — Separação de funções

**Status:** Accepted  
**Data:** 2026-08-21

## Contexto

Um fundo comunitário não deve permitir que uma única conta crie evidência, aprove e inicie a saída financeira do mesmo auxílio. Comprometimento de conta, erro humano e abuso interno teriam impacto alto.

## Decisão

Separar capacidades críticas por papel:

- `MEMBER`: solicita auxílio e envia documentos;
- `ANALYST`: registra parecer e antifraude;
- `APPROVER`: aprova/rejeita; duas aprovações precisam de usuários distintos;
- `ADMIN`: pode iniciar pagamento aprovado, mas não substitui as aprovações;
- `AUDITOR`: leitura e verificação, sem mutação financeira.

Quem participou como aprovador do caso não pode iniciar o pagamento do mesmo auxílio.

## Consequências

### Positivas
- reduz poder concentrado;
- melhora rastreabilidade;
- aumenta resistência a conta única comprometida;
- facilita auditoria e revisão de incidentes.

### Negativas
- aumenta complexidade operacional;
- exige pelo menos dois aprovadores disponíveis;
- não elimina conluio entre múltiplos operadores.

## Revisão

Qualquer redução da separação de funções exige novo threat model e novo ADR. Para produção, controles organizacionais e revisão periódica de acessos devem complementar o software.
