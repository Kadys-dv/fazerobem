# LGPD — baseline para piloto controlado

## Finalidades e bases a validar juridicamente
- Cadastro e autenticação: execução do regulamento/contrato associativo e segurança.
- KYC e prevenção a fraude: legítimo interesse/obrigação aplicável, conforme parecer jurídico.
- Dados de pedidos de auxílio: execução das regras comunitárias e proteção contra fraude.
- Consentimento: usado apenas quando consentimento for realmente a base adequada; versionado e revogável.

## Minimização
CPF é mantido criptografado e também como hash + últimos 4 dígitos. Endereço completo é criptografado; respostas comuns usam versão redigida. Logs não devem conter CPF, endereço, documento ou segredo MFA.

## Direitos do titular
O produto deve suportar confirmação de tratamento, acesso, correção, portabilidade quando aplicável, informação sobre compartilhamento e eliminação quando legalmente possível. Solicitações devem gerar evento auditável.

## Retenção
Documentos de auxílio possuem expiração configurável e job auditável de remoção. Prazos definitivos precisam ser definidos com jurídico/contabilidade antes de produção.

## RIPD inicial
Riscos principais: exposição de documentos, abuso de privilégio interno, fraude de identidade, vazamento de backups e correlação indevida de dados. Controles: AES-256-GCM, ABAC por finalidade, MFA privilegiado, trilha de auditoria, retenção, segregação de funções e resposta a incidentes.
