# Repository Guidelines

## Project Structure & Module Organization
This repository combines a Spring Boot backend, Solidity contracts, and two Vite frontends. Core backend code lives in `src/main/java/com/zhm/springboot/userservice`, organized by domain modules such as `nft/`, `market/`, `wallet/`, `fantoken/`, `admin/`, and `blockchain/`. Shared configuration and infrastructure code sits in `config/`, `common/`, `filter/`, and `interceptor/`. Runtime configuration and MyBatis mapper XML files are in `src/main/resources`. Smart contracts are stored in `contracts/`, deployment scripts in `scripts/`, the user-facing web app in `frontend/`, and the admin UI in `admin-web/`.

## Build, Test, and Development Commands
- `mvn spring-boot:run` — start the backend locally.
- `mvn clean test` — compile and run Spring Boot tests.
- `mvn clean package` — build the backend JAR in `target/`.
- `npm run compile` — compile Hardhat contracts from the repository root.
- `npm test` — run contract tests under Hardhat.
- `npm run deploy:local` — deploy contracts to the local Hardhat network.
- `cd frontend && npm run dev` — start the main frontend.
- `cd frontend && npm run build` — create the production frontend bundle.
- `cd admin-web && npm run dev` — start the admin console.

## Coding Style & Naming Conventions
Use 4 spaces for Java and 2 spaces for JavaScript/React. Follow standard Spring naming: `*Controller`, `*Service`, `*ServiceImpl`, `*Mapper`, `*DTO`, and `*VO`. Keep package names lowercase and class names PascalCase. React components should use PascalCase filenames such as `App.jsx`; hooks and helper functions should use camelCase. Run `cd frontend && npm run lint` before submitting frontend changes.

## Testing Guidelines
Backend tests should live in `src/test/java` and mirror the production package structure. Contract tests belong in `test/` and should use descriptive names such as `NFTAsset.test.js`. Add tests for new controller endpoints, service logic, and contract behavior whenever features change. Prefer focused unit tests first, then integration checks for database, Redis, or blockchain interactions.

## Commit & Pull Request Guidelines
This snapshot is not a Git checkout, so no local history is available. Use concise, imperative commit messages, preferably with conventional prefixes like `feat:`, `fix:`, `refactor:`, and `docs:`. Pull requests should include a summary, affected modules, setup or migration notes, linked issues, and screenshots for `frontend/` or `admin-web/` UI changes.

## Security & Configuration Tips
Never commit real secrets from `.env`, wallet keys, or service credentials. Keep environment-specific settings in local config files, and update `.env.example` when new variables are required. Validate blockchain, Redis, MySQL, Nacos, RocketMQ, and MinIO settings before running deployment scripts.
