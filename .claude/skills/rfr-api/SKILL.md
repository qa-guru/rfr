---
name: rfr-api
description: Создаёт и меняет код модуля rfr-api — GraphQL backend Rangiffler (OAuth Resource Server, пользователи, дружба, страны, mock ленты/фото). Применять к схеме query.graphqls, контроллерам, сервисам, JPA, Flyway, security и GraphQlExceptionResolver. Auth-сервер, React-фронт и E2E — вне ответственности этого skill.
---

# Модуль rfr-api

GraphQL API: Spring Boot 4.1.1, Spring for GraphQL, DGS Codegen, JWT Resource Server, JPA, Flyway, MySQL (`rangiffler-api`). Порт `8081`, `POST /graphql`. Пакет `io.student.rangiffler`. Общий контекст и стенд — skill `rfr`.

## Контракт

1. Схема — единственный источник контракта: `src/main/resources/graphql/query.graphqls`. Java-модели (`User`, `Photo`, inputs…) генерирует DGS Codegen в `io.student.rangiffler.model.types` (`./gradlew :rfr-api:generateJava`). Не пиши эти типы руками и не коммить `build/generated`.
2. `UserConnection` / `PhotoConnection` мапятся на `Page<...>`, скаляр `Date` — на `LocalDate`.
3. Реально на БД: `Query.user` / `users` / `countries`, `Mutation.user` / `friendship`, поля дружбы у `User`. Профиль создаётся при первом `Query.user` со страной `ru`. UUID в `rangiffler-api` и `rangiffler-auth` независимы; связь только по `username` (`sub` JWT).
4. Mock (ничего не сохраняют): `Query.feed` / `Feed.photos` / `Feed.stat` / `Photo.likes`, `Mutation.photo` / `deletePhoto`. Данные — `resources/mock/query_feed.json` и `query_feed_with_friends.json`. Успешный ответ mock-мутации не доказывает персистентность. Моки не убирай без явного запроса заменить их реализацией.
5. Таблицы `photo`, `like`, `photo_like`, `statistic` в миграции уже есть, но код их не использует — реализация фото/лайков/статистики отдельная задача диплома; не смешивай её с правками пользователей/дружбы без явного запроса.
6. Security (`RangifflerApiConfiguration`): JWT по JWKS `rfr-auth` (`issuer-uri`), обязательный audience `rangiffler-api`. В Bearer только access token; `id_token` (aud = `client`) — 401. Claim `authorities` → `GrantedAuthority` без префикса. Query — `@PreAuthorize("hasAuthority('read')")`, mutation — `hasAuthority('write')`. Username текущего пользователя — `@AuthenticationPrincipal Jwt` → `sub`.
7. Ошибки: HTTP GraphQL часто 200; смотри `errors`. `GraphQlExceptionResolver`: `ResourceNotFoundException` → `NOT_FOUND`, `FriendshipActionException` / `IllegalArgumentException` → `BAD_REQUEST`, иначе `INTERNAL_ERROR` без внутренних деталей. Невалидный Bearer — HTTP 401 до GraphQL.
8. Дружба — одна строка на пару (`FriendshipEntity` + `FriendshipStatus`). Действия `FriendshipInput.action`: `ADD` → `PENDING` / ответ `INVITATION_SENT`; `ACCEPT`/`REJECT` — только адресат; `DELETE` — любая сторона. Нельзя действие на себя; повторная заявка — ошибка.
9. `Mutation.user`: `null`/пропущенное поле = «не менять». Аватар — data URL (байты через util `StringAsBytes` / `BytesAsString`).
10. CORS — `CorsCustomizer` под `rangiffler-front.base-uri` (`http://localhost:3001`). GraphiQL включён, но GET требует аутентификации.
11. Без комментариев и Javadoc. Подробности и примеры операций — `rfr-api/README.md`.

## Пакеты

| Пакет / путь | Что лежит |
|---|---|
| `config` | Resource Server, audience, authorities converter, CORS; `GraphQlExceptionResolver` |
| `controller` | GraphQL query/mutation controllers (реальные и `*Mock*`) |
| `data/entity`, `projection`, `repository` | Country/User/Friendship, `UserWithStatus` |
| `exception` | `ResourceNotFoundException`, `FriendshipActionException` |
| `service/api` + `impl` | `UserService`, `CountryService` |
| `service/mock` | `FeedMockData` |
| `util` | Bytes/String, pagination/sort для GraphQL |
| `resources/graphql` | `query.graphqls` |
| `resources/db/migration/rangiffler-api` | Flyway `V1__schema_init.sql` |
| `resources/mock` | JSON ленты |

## Проверка результата

```bash
curl -s -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access_token>" \
  -d '{"query":"{ user { id username location { code } } }"}'
```

Стенд — skill `rfr`. `access_token` из localStorage фронта после логина, не `id_token`. Модели: `./gradlew :rfr-api:generateJava`.
