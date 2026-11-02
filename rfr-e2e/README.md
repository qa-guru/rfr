# rfr-e2e

E2E-тесты Rangiffler: JUnit 6.1.3, Playwright Java 1.63 (`@UsePlaywright`), Allure 3, `JdbcClient` для фикстур.

## Запуск

```bash
./gradlew :rfr-e2e:installPlaywright                      # один раз: браузер Chromium
./gradlew e2e                                             # все тесты rfr-e2e
./gradlew :rfr-e2e:test --tests '*RegistrationTest'
./gradlew :rfr-e2e:test --tests '*RegistrationTest.shouldAuthorizeAfterRegistration'
./gradlew :rfr-e2e:allureReport                           # rfr-e2e/build/reports/allure-report/allureReport/index.html
```

Стенд (см. корневой `AGENTS.md` / README): MySQL `:3306`, `rfr-auth :9001`, `rfr-api :8081`, фронт `:3001`. `:rfr-e2e:test` всегда выполняется (`outputs.upToDateWhen { false }`).

## Конфигурация

`TestConfig` в тестовой JVM читает `-Drfr.<ключ>`, иначе `RFR_<КЛЮЧ>`, иначе значение по умолчанию. Gradle передаёт только явно заданные `-Drfr.*` и не подставляет localhost, чтобы не перекрывать `RFR_*`.

| Ключ | По умолчанию |
|---|---|
| `rfr.frontUrl` | `http://localhost:3001` |
| `rfr.authUrl` | `http://localhost:9001` |
| `rfr.apiUrl` | `http://localhost:8081` |
| `rfr.dbUrl` | `jdbc:mysql://localhost:3306` (схема добавляется кодом) |
| `rfr.dbUser` / `rfr.dbPassword` | `root` / `secret` |
| `rfr.browser` / `rfr.headless` | `chromium` / `true` |

## Архитектура

Один source set `src/test`, пакет `io.student.rangiffler`:

| Пакет   | Что лежит |
|---------|---|
| `config` | `TestConfig` — единственная точка чтения настроек |
| `jupiter` | всё про JUnit: `annotation` (`@User`), `extension` (`UserExtension`), позже `condition`, `probe` |
| `data`  | тестовые данные и их владельцы: `TestUser`, `UserFixture`; `db` — `Databases`, `Jdbc` |
| `utils` | статические утилиты без состояния: `RandomDataUtils` — случайные username/password; сюда же будущие генераторы (описания фото и т.п.) |
| `ui`   | `page` — Page Objects, `component` — общие части страниц |
| `test`  | только тесты, по фичам: `auth`, `profile`, `web`, `fake` |

Правила: в `jupiter` нет SQL и локаторов; в `data` нет JUnit; в `test` нет локаторов и SQL; `ThreadLocal` для состояния теста не используется — состояние живёт в Store расширения.


## Агент

Page Objects пишет Cursor AI по skill `rfr-page-objects`;   
UI в браузере подтверждают через MCP Cursor (`cursor-ide-browser`).   
Доказательство корректности теста — запуск Gradle-задачи (`./gradlew e2e` или `:rfr-e2e:test`).
