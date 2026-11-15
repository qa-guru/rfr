---
name: rfr-test-data
description: Правила тестовых данных rfr-e2e для @User, UserRequest, Relation и фикстур пользователей. Применять при изменении предусловий @User, связей дружбы, уникальности, профиля API и декораторов UserFixture в пакете data.
---

# Тестовые данные rfr-e2e

**1. Уникальность.** Каждый пользователь, включая партнёров, — `TestUser.unique()` (`rfr_` и случайный username). Общих логинов, литералов и одних строк на несколько тестов нет.

**2. Владелец — дерево декораторов.** Корень из `UserRequest.fixture()` (`UserWithRelationsFixture` на ненулевой счётчик → `SingleUserFixture`) владеет своим `origin` и `partners`; Store закрывает корень. Поля extension, статика и `ThreadLocal` данными не владеют.

**3. Новое предусловие — новый декоратор.** Его добавляют композицией в `UserRequest.fixture()`. Не флаг в фикстуре и не ветка в `UserExtension`: extension знает только `UserRequest` и `UserFixture`.

**4. Новая связь — значение `Relation`.** Достаточно статуса `Friendship`, направления requester/addressee и списка `TestUser`. `UserRequest.fixture()` оборачивает фикстуру в `UserWithRelationsFixture` для каждого ненулевого счётчика.

**5. SQL только в объектах строк `data/db`.** `AuthAccount`, `Profile`, `Friendship`, `Friendships`, `Invitations` вызывают SQL. Фикстуры, `jupiter` и тесты ограничиваются `create`, `delete` и `exists`.

**6. Профиль API создаётся явно.** Регистрация пишет только `rangiffler-auth`. `SingleUserFixture` сам вызывает `Profile.create()` и не ждёт первый `Query.user`.

**7. Пул заранее заведённых пользователей — только там, где создать нельзя.** Обычного пользователя создаёт фикстура. Общий пул допустим, лишь если путь создания закрыт.
