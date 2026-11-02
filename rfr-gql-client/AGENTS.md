# rfr-gql-client

Подробности — `README.md`. Не плодить новые корни в `src/`, пока фича не выпадает из `api`, `hooks`, `pages`, `components`.

В `rfr-api` уходит только `access_token`. `id_token` — лишь `id_token_hint` при logout. `GetUser` создаёт API-профиль при первом входе.

Лента, фото и лайки на бэкенде mock: snackbar и кэш Apollo не доказывают сохранение.
