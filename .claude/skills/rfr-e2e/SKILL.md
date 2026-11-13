---
name: rfr-e2e
description: Создаёт и меняет код модуля rfr-e2e, кроме Page Objects. Применять к тестам, фикстурам, TestConfig, JUnit-расширениям и SQL ресурсов. Страницы и компоненты UI — skill rfr-page-objects.
---

# Модуль rfr-e2e

E2E: JUnit 6, Playwright Java (`@UsePlaywright`), Allure 3, `JdbcClient` для фикстур. Source set `src/test`, пакет `io.student.rangiffler`. Общий контекст и стенд — skill `rfr`. Page Objects — skill `rfr-page-objects`.

## Настройки

`TestConfig` читает `-Drfr.<ключ>=…` → `RFR_<КЛЮЧ>` → default. Gradle передаёт все `-Drfr.*` в тестовую JVM. Адреса и БД в тестах/фикстурах не хардкодить.

| Ключ | По умолчанию |
|---|---|
| `rfr.frontUrl` | `http://localhost:3001` |
| `rfr.authUrl` | `http://localhost:9001` |
| `rfr.apiUrl` | `http://localhost:8081` |
| `rfr.dbUrl` | `jdbc:mysql://localhost:3306` (схему добавляет код) |
| `rfr.dbUser` / `rfr.dbPassword` | `root` / `secret` |
| `rfr.browser` / `rfr.headless` | `chromium` / `true` |

## Пакеты

Новые классы — в существующие пакеты (`rfr-e2e/README.md`, «Архитектура»).

| Пакет | Что лежит |
|---|---|
| `config` | `TestConfig` — единственная точка чтения настроек |
| `jupiter` | JUnit: `annotation` (`@User`), `extension` (`UserExtension`), позже `condition`, `probe` |
| `data` | `TestUser`, `UserFixture`; `db` — `Databases`, `Jdbc` |
| `utils` | статические утилиты без состояния: `RandomDataUtils` |
| `ui` | только страницы и компоненты — skill `rfr-page-objects` |
| `test` | только тесты, по фичам: `auth`, `profile`, позже `people` |
| `assertions` | проверки, которые не являются методами страницы |

Границы: в `jupiter` нет SQL и локаторов; в `data` нет JUnit; в `test` нет локаторов и SQL. Состояние теста — в Store расширения, не в `ThreadLocal`. Данные — фикстурами (`@User` → `UserExtension` → `UserFixture`), без широких `DELETE`/`TRUNCATE`.

## Команды

```bash
./gradlew :rfr-e2e:installPlaywright
./gradlew e2e                                          # все тесты модуля
./gradlew :rfr-e2e:test --tests '*RegistrationTest'
./gradlew :rfr-e2e:test --tests '*MainPageTest'
./gradlew :rfr-e2e:allureReport
```

`installPlaywright` ставит Chromium один раз. `e2e` — корневая таска, сейчас гоняет `:rfr-e2e:test` целиком. Отчёт: `rfr-e2e/build/reports/allure-report/allureReport/index.html`. Проход через Playwright MCP запуском теста не считается.