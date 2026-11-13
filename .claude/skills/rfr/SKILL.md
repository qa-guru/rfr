---
name: rfr
description: Общий контекст учебного проекта Rangiffler (монорепозиторий). Применять при ориентации в структуре, запуске стенда, портах, OAuth/токенах между сервисами и выборе сервисного skill. Код конкретного модуля — skills rfr-auth, rfr-api, rfr-gql-client, rfr-e2e; Page Objects — rfr-page-objects.
---

# Проект Rangiffler

Учебный монорепозиторий курса Java Advanced 3.0 (путешествия, дружба, лента фото). Регистрация, вход, профиль и дружба — настоящие. Фото, лайки, лента и статистика пока из JSON-моков `rfr-api`.

## Сервисы и skills

| Модуль | Роль | Порт | Skill |
|---|---|---|---|
| `rfr-auth` | Spring Authorization Server, схема `rangiffler-auth` | 9001 | `rfr-auth` |
| `rfr-api` | GraphQL Resource Server `/graphql`, схема `rangiffler-api` | 8081 | `rfr-api` |
| `rfr-gql-client` | React + Apollo SPA | 3001 | `rfr-gql-client` |
| `rfr-e2e` | E2E: JUnit 6, Playwright Java, Allure 3 | — | `rfr-e2e` |
| Page Objects в `rfr-e2e` | UI pages/components | — | `rfr-page-objects` |

## Стек

- JDK 25, Gradle 9.7.1 (wrapper в корне); бэкенды — Spring Boot 4.1.1
- MySQL 8.4 (`root` / `secret`, порт 3306), схемы создаёт приложение
- Фронт: Node 22+, React 19, Vite 8, Apollo Client 4, MUI 9 — вне Gradle (`npm ci` / `npm run dev`)
- Auth: OAuth 2.1 Authorization Code + PKCE, публичный клиент `client`

## Запуск стенда

Требования: JDK 25, Node.js 22+, Docker Desktop (демон должен быть запущен). Если остался контейнер от `docker run` — `docker rm -f rfr-mysql` (volume `mysql-volume` сохранится).

```bash
docker compose up -d --wait                    # MySQL, ждём healthy
./gradlew runAll                               # rfr-auth :9001 и rfr-api :8081
(cd rfr-gql-client && npm ci && npm run dev)   # http://localhost:3001
./gradlew :rfr-e2e:installPlaywright           # браузер для тестов, один раз
./gradlew e2e                                  # всегда гоняет тесты (не UP-TO-DATE)
```

Кратко для человека — также `pamiatka.md` (стенд, порт 3306, git bundle). По отдельности бэкенды: `./gradlew :rfr-auth:bootRun` и `./gradlew :rfr-api:bootRun`.

| URL | Что |
|---|---|
| http://localhost:9001 | auth (OIDC: `/.well-known/openid-configuration`) |
| http://localhost:8081/graphql | api (GraphiQL: `/graphiql`) |
| http://localhost:3001 | фронт |

Остановка: Ctrl+C в терминалах `runAll` / `npm run dev`; при занятых портах — `bash ports.sh` (9001/8081/3001); MySQL — `docker compose down` (данные в volume `mysql-volume`).

## Сквозные правила

1. В Bearer к `rfr-api` только **access token** (`aud` = `rangiffler-api`). `id_token` (`aud` = `client`) — для фронта и `id_token_hint` при logout, не для API.
2. UUID пользователя в `rangiffler-auth` и `rangiffler-api` независимы; связь только по `username` (`sub` JWT). API-профиль создаётся при первом авторизованном `Query.user`.
3. Моки ленты/фото в `rfr-api` ничего не сохраняют; успешный UI после mock-мутации не доказывает персистентность. Моки не убирай без явного запроса.
4. Корневой пакет Java: `io.student.rangiffler`. Без лишних комментариев и Javadoc — смысл в именах.
5. Подробности модуля — в его skill и `*/README.md`; не копируй контракт другого сервиса в свой skill.
6. Файлы не удаляй (`rm` / Delete). Вместо удаления переименуй в backup: `mv path/to/File.ext path/to/File.ext.bak`. Не затирай существующий `.bak` без явного запроса.

## Agent hooks (compile on edit)

После правок агента в контекст возвращается вывод компиляции/проверки (скрипт `.cursor/hooks/compile-on-edit.sh`):

| Изменение | Команда |
|---|---|
| `rfr-auth/**/*.java` | `./gradlew :rfr-auth:compileJava` |
| `rfr-api/**/*.java` или GraphQL schema | `./gradlew :rfr-api:compileJava` |
| `rfr-e2e/**/*.java` | `./gradlew :rfr-e2e:compileTestJava` |
| `rfr-gql-client/src/**/*.{ts,tsx,js,jsx,css}` | `npm run lint` + `tsc --noEmit` |

- Cursor: `.cursor/hooks.json` → `postToolUse` (`Write` / `StrReplace`) → `additional_context`
