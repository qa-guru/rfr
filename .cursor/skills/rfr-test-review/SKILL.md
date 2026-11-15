---
name: rfr-test-review
description: Ревьюирует тестовый код rfr-e2e на владение ресурсами теста, декларативную регистрацию extension уровня класса (BeforeAll, AfterAll), глобальную регистрацию SuiteExtension через ServiceLoader, безопасность extension для любого теста и программную регистрацию extension. Применять к diff'ам в jupiter/ и data/, новым предусловиям @User и декораторам UserFixture, реализациям SuiteExtension, разбору падений с подозрением на утечку данных или общее состояние.
---

# Ревью владения ресурсами rfr-e2e

Ресурс — всё, что тест получает и обязан вернуть (строки в БД, `Page`, `BrowserContext`); для каждого ресурса найти владельца, момент получения, место хранения, момент освобождения.

## Правила
**1. Владелец — дерево декораторов.** В Store лежит только корень `UserFixture` из `UserRequest.fixture()` (`UserWithRelationsFixture` на каждый ненулевой счётчик → `SingleUserFixture`); Store закрывает его как `AutoCloseable`. Каждый декоратор владеет своим `origin` и `partners`. Поля `UserExtension`, статика и `ThreadLocal` ресурсом не владеют.

**2. Регистрация до частичной подготовки.** `UserExtension.beforeEach` делает `put` в Store до `create()`; `create()` не атомарен (`AuthAccount`, `Profile`, партнёры, `Friendship`), поэтому `close()` обязан работать после падения на любом шаге.

**3. Симметрия create/close.** Декоратор удаляет ровно то, что создал, адресно по `username`, в обратном порядке (свои строки → `partners` → `origin`) через `TearDown`, чтобы шаги были независимы и ошибки не терялись. Без широких `DELETE`/`TRUNCATE`.

**4. Новое предусловие — новый декоратор.** Композиция в `UserRequest.fixture()`, а не флаг в `BaseUserFixture` и не ветка в `UserExtension`; extension знает только `UserRequest` и `UserFixture`.

**5. Один `@User` — один параметр `TestUser`.** Один ключ Store `"user-" + index`; связанные пользователи — внутри `TestUser` (`friends()`, `incomeInvitations()`, `outcomeInvitations()`) и дерева фикстур, не отдельными параметрами.

**6. Браузерными ресурсами владеет `@UsePlaywright`.** `Page` и `BrowserContext` не создаются и не закрываются в тестах и extension'ах (`@UiTest` → `BrowserOptions`).

**7. Слои.** В `jupiter` нет SQL и локаторов; в `data` нет JUnit, SQL только в `data/db`, `db` не зависит от `data`; в `test` нет локаторов и SQL.

**8. Каждый вывод подтверждён.** Проба в `jupiter/probe/UserExtensionTest` (`EngineTestKit`, `exists()` из `db`, цели `Observed` и `FailingCase`) или конкретная строка кода; без найденной причины изменений не предлагать.

**9. Пул — аренда слота.** Пользователи, которых нельзя создать, ждут в одной на JVM очереди. Взятый слот лежит в экземпляре `StaticUserFixture` в Store, не в статике и не в поле extension. `UsersQueueExtension.beforeEach` делает `put` до `create()`. Пустая очередь: `create()` ждёт с таймаутом и падает, слот при этом не взят. `close()` возвращает только взятого и не удаляет строки в БД. Проба — `UsersQueueExtensionTest`.

**10. Регистрация уровня класса.** Extension уровня класса (BeforeAll, AfterAll) декларативно может быть зарегистрирован только над классом или статическим полем. Если обнаружено другое место регистрации, это должно считаться ошибкой.

**11. Безопасен для любого теста.** Extension не предполагает, что он зарегистрирован над конкретным тестовым методом, и готов к регистрации над классом или глобально. Перед созданием тестовых данных он проверяет сигнатуру текущего тестового метода; нет подходящего параметра — данных не создаёт.

**12. Без программной регистрации.** Программную регистрацию extension через `@RegisterExtension` не использовать. Исключение — extension, который необходимо настраивать через конструктор или билдер.

**13. SuiteExtension — только ServiceLoader.** Реализации `SuiteExtension` обязаны быть зарегистрированы глобально через ServiceLoader, иначе это сломает семантику. Класс указывают в `rfr-e2e/src/test/resources/META-INF/services/org.junit.jupiter.api.extension.Extension`, autodetection включён в `rfr-e2e/src/test/resources/junit-platform.properties` (`junit.jupiter.extensions.autodetection.enabled=true`). `@ExtendWith`, составная аннотация, статическое поле и `@RegisterExtension` на тестовом классе — ошибка: `beforeSuite` сдвинется на первый класс с аннотацией, а не на начало прогона. Исключение — пробы в `jupiter/probe`, которые гоняют extension через `EngineTestKit`. Проба семантики — `SuiteExtensionTest`.

## Формат ответа

- Для каждого ресурса — владелец, получение, хранение, освобождение.   
- Для каждого замечания — файл и строка, сценарий сбоя, как воспроизвести (какая проба или какой падающий шаг `create()`). 
- Три списка: «подтверждено запуском», «по чтению кода», «не проверено». 
- Если дефектов нет — так и сказать и перечислить ограничения решения.
