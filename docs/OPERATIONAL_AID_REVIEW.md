# Painel operacional de auxílio

O painel `/operations.html` é destinado aos papéis `ANALYST`, `APPROVER`, `ADMIN` e `AUDITOR`.

## Responsabilidades por papel

- `ANALYST`: registra parecer e conclui a triagem antifraude.
- `APPROVER`: registra aprovação ou rejeição. Duas aprovações devem ser feitas por usuários distintos.
- `ADMIN`: consulta operacional para suporte e governança, sem assumir ações de analista/aprovador.
- `AUDITOR`: consulta operacional somente leitura.

## Visão do caso

A API `GET /api/v1/operations/aid-requests/{id}` agrega, para usuários operacionais autorizados:

- pedido de auxílio;
- resultado atual de elegibilidade;
- resumos seguros dos documentos;
- pareceres de análise;
- triagem antifraude;
- aprovações registradas.

O endpoint não expõe `storageKey`, SHA-256 do arquivo ou conteúdo do documento. O conteúdo continua disponível apenas pelo endpoint protegido já existente, que aplica autorização por papel.

## Regras preservadas

O painel não altera as regras do domínio. As ações continuam delegadas aos serviços existentes com `@PreAuthorize`, validação de elegibilidade, histórico imutável de antifraude e exigência de dois aprovadores distintos.

A integração permanece em sandbox e não movimenta dinheiro real.
