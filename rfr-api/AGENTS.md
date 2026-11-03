# rfr-api

Не править классы в `build/generated` (их пишет DGS `generateJava`). Подробности — `README.md`.

Моки не сохраняют данные: `Query.feed`, `Mutation.photo` / `deletePhoto`, JSON в `src/main/resources/mock/`. Таблицы `photo`, `like`, `photo_like`, `statistic` в миграции есть — не использовать и не заменять моки без явной просьбы.

UUID в `rangiffler-api` и `rangiffler-auth` независимы, связь только по `username`. Первый `Query.user` создаёт профиль со страной `ru`. В GraphQL — access token, не `id_token`. Ошибки домена идут в `errors` при HTTP 200.
