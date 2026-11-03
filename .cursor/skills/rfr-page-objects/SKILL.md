---
name: rfr-page-objects
description: Создаёт и меняет Page Objects и компоненты UI Rangiffler на Playwright Java в rfr-e2e (пакеты ui/page и ui/component). Применять к страницам, локаторам, действиям и проверкам UI. Фикстуры, SQL и JUnit extensions — вне ответственности страниц.
---

# Page Objects Rangiffler

Страницы — `rfr-e2e/src/test/java/io/student/rangiffler/ui/page`, общие части — `ui/component`, адреса — `config/TestConfig`.
Доступные имена и роли элементов подтверждай в браузере через Playwright MCP (`http://localhost:3001`), не угадывай по исходникам фронта.

## Контракт

1. Класс `final`, поля `private final`: `Page`, `Locator`, компоненты. Исключение — `public static final String URL` с базой из `TestConfig`; он есть у каждой страницы, даже если на неё попадают только переходом. `URL` инициализируется на уровне класса из `TestConfig`, не в конструкторе.
2. Конструктор принимает только `Page` и только присваивает поля: без переходов, кликов, проверок, ожиданий и SQL.
3. Композиция вместо наследования: страницы и компоненты не наследуются ни от какого `BasePage`/`BaseComponent`/абстрактного родителя. Базовых классов нет. Общие части (`CredentialsForm`, `HeaderComponent`) — самостоятельные `final`-классы, принимающие тот же `Page` и подключаемые полем. Компонент выделяй, если элемент повторяется на ≥2 страницах или образует смысловой блок с собственными действиями; одиночный локатор — поле страницы, не компонент.
4. Каждый публичный метод страницы и компонента — с `@Step`, и каждый параметр (кроме секретных) попадает в текст шага: `@Step("Fill login form: {username}")`. Текст шага — на английском, `@Step` обязателен и для действий, и для проверок. Секретные параметры (`password`, токены) в текст шага не подставляй — только несекретные.
5. Переход возвращает объект следующей страницы, действие и проверка на текущей — `this`.
6. Одно бизнес-действие — один метод, имя которого называет форму: `fillLoginForm(username, password)`, `fillRegistrationForm(...)`. Без безликих `fillForm`/`fill`/`submit` и цепочек `fillUsername().fillPassword()`; отдельный метод на поле — только если тесту нужно именно поле (валидация). Примеры имён: `clickSubmit()`, `openProfile()`, `checkThatErrorShown(message)`, `checkThatUsername(username)`.
7. Действие отдельно от проверки: `fill…`/`click…` ничего не проверяют; `checkThat…` только проверяют через `PlaywrightAssertions.assertThat(...)` и ничего не кликают. Допустимы обе формы ассерта — `assertThat(locator)` и `assertThat(page)`; `page`-ассерты — только когда предмет теста — переход (см. п. 8). Без `locator.isVisible()` в JUnit-проверках и ручных ожиданий.
8. Без `checkThatOpened`, `checkPageLoaded`, `isLoaded`, `waitUntilLoaded`: `click`/`fill` ждут элемент, web-first assertions ждут состояние. URL и видимость страницы проверяем, только когда переход и есть предмет теста.
9. Локаторы — в порядке приоритета: `getByTestId` → `getByRole` с точным `name` → `getByLabel`/`getByPlaceholder` → `getByText` с точным совпадением. Переходи на следующий уровень, только если предыдущий недоступен в разметке. Без `first()`/`nth()`, MUI-классов и XPath; неуникальный элемент сужай родителем: `page.getByRole(AriaRole.BANNER).getByRole(…)`.
10. Проверка не должна быть тавтологией локатора: `assertThat(heading("Travels map")).hasText("Travels map")` ничего не доказывает. Ожидаемое значение приходит из теста параметром метода (`checkThatUsername(username)`), а не литералом внутри Page Object.
11. Вход только через UI. Токены (`access_token`/`id_token`) в `localStorage` не подставлять — даже если это ускорило бы тест. (до занятия 13).
12. Без комментариев и Javadoc: смысл передают имена методов и текст `@Step`.
13. MUI `Select` — это `combobox`. Option не ищи по составному accessible name вроде «France France»: кликни `combobox`, затем `getByRole(OPTION)` с фильтром `setHas(getByRole(IMG, name страны, exact))`. Имя страны — параметр из теста.
14. Если сразу после клика тест делает `reload()` или уходит со страницы, клик обязан дождаться ответа сервера: `page.waitForResponse(..., button::click)`. Для профиля — POST `/graphql` с операцией `UpdateUser`. Это синхронизация действия, не `waitUntilLoaded` и не проверка в `checkThat…`.

## Частые ошибки   

| ❌ Неправильно | ✅ Правильно | Почему |
|---|---|---|
| `fillUsername().fillPassword().submit()` | `fillLoginForm(username, password)` | Одно бизнес-действие — один метод (п. 6) |
| `checkThatOpened()` / `checkPageLoaded()` / `waitUntilLoaded()` | ничего — переход возвращает следующую страницу | `click`/`fill` ждут элемент, ассерты ждут состояние (п. 8) |
| `assertThat(heading("Travels map")).hasText("Travels map")` | `checkThatHeading("Travels map")` | Ожидание — параметр из теста, не литерал страницы (п. 10) |
| `page.locator(".MuiButton-root").first().click()` | `page.getByTestId("submit").click()` или `page.getByRole(BUTTON, name("Войти")).click()` | Без MUI-классов, `first()`/`nth()` (п. 9) |
| клик внутри `checkThat…` | только `assertThat(...)`, без кликов в проверках | Действие отдельно от проверки (п. 7) |
| `class LoginPage extends BasePage` | `final class LoginPage` с полями-компонентами | Композиция вместо наследования (п. 3) |
| `@Step("Fill login form: {username} / {password}")` | `@Step("Fill login form: {username}")` | Пароль не интерполируется в шаг — утечка в отчёт (п. 4) |
| `getByRole(OPTION, name("France France"))` | `OPTION` + `setHas(IMG с точным именем страны)` | Accessible name option склеивает alt флага и текст (п. 13) |
| `save.click(); page.reload();` | `waitForResponse(..., save::click)` затем `reload()` | Иначе reload обгоняет мутацию `UpdateUser` (п. 14) |

## Проверка результата

Прогон `./gradlew :rfr-e2e:test --tests '<Тест>' --rerun-tasks` — единственное подтверждение. Проход сценария через MCP запуском теста не считается.
Если тест падает на локаторе — вернись к MCP, подтверди имя/роль в браузере, исправь локатор, перезапусти. Не ослабляй проверку, чтобы тест прошёл.
В отчёте разделяй: что подтверждено в браузере через MCP, что взято из исходников.