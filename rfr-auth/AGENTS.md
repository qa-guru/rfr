# rfr-auth

Подробности — `README.md`.

Страницы login / register / error — Thymeleaf, не JSON API. Регистрация пишет только `rangiffler-auth`; профиль в API появляется при первом `Query.user`.

JWKS генерируется при старте: после рестарта старые JWT недействительны. OAuth-сессии и ключи в БД не хранятся.

`/oauth2/revoke` для SPA — только через `PublicClientRevocationAuthenticationConverter` / `Provider`; стандартный SAS отвечает `401 invalid_client`. Access token — для `rfr-api` (`aud: rangiffler-api`); `id_token` — фронту (`id_token_hint` при logout).
