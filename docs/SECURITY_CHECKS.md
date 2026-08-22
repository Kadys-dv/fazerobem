# Checks de segurança

No CI, `mvn verify` executa testes e gera SBOM CycloneDX. Pull requests executam Dependency Review. Para um repositório público/organizacional, habilite também CodeQL e secret scanning nas configurações do GitHub.

Antes de um piloto: revisar dependências críticas, executar SAST, procurar segredos no histórico Git, testar restore de backup, validar rotação de chaves e executar testes concorrentes contra PostgreSQL real.
