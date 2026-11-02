# rfr-e2e

Page Objects — skill `rfr-page-objects`. Подробности — `README.md`.

Пакеты: `config`, `jupiter`, `data`, `api`, `ui`, `assertions`, `test`. Новые классы — в существующие пакеты.

В Java, Gradle, SQL и properties модуля нет комментариев и Javadoc.   
Вход в тестах только через UI; токены в `localStorage` не подставлять.   
Успех — JUnit через Gradle, не проход сценария в MCP.   
Данные — фикстуры (`@User` → `UserExtension` → `UserFixture`), без широких `DELETE` / `TRUNCATE`.
