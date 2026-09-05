Please refactor `README.md` file taking into account the following:

- `src/main/resources/application.yaml`
- and to have possibility to run it locally as one standalone microservice to check functionality
so, please provide set http files for different cases.

As a result, we will have:
- the refactored `README.md` file:
1) structured existing sections.
2) + "how to run locally and test functionality" - it should be complete guide like:
-what standalone docker compose should be run - see the list in `/Users/Marina_Pimenova/ti-2026/ti-gateway-api/docker`
-set of http files under `/Users/Marina_Pimenova/ti-2026/ti-ai-orchestrator-api/http` folder to cover the main scenarios of this microservice.

Please take into account to avoid authentication - the controllers are marked by `@RequestMapping("/rest/v1")`.
