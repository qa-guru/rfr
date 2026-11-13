---
name: rfr-page-objects
description: Создаёт и меняет Page Objects и компоненты UI Rangiffler на Playwright Java в rfr-e2e (пакеты ui/page и ui/component). Применять к страницам, локаторам, действиям и проверкам UI. Фикстуры, SQL и JUnit extensions — вне ответственности страниц.
---

# Page Objects Rangiffler

Страницы — `rfr-e2e/src/test/java/io/student/rangiffler/ui/page`, общие части — `ui/component`, адреса — `config/TestConfig`.
Доступные имена и роли элементов подтверждай в браузере через Playwright MCP (`http://localhost:3001`), не угадывай по исходникам фронта.
Общий контекст и стенд — skill `rfr`. Тесты/фикстуры — skill `rfr-e2e`.

## Контракт

1. Класс `final`, поля `private final`: `Page`, `Locator`, компоненты. Исключение — `public static final String URL` с базой из `TestConfig`; он есть у каждой страницы, даже если на неё попадают только переходом.
2. Конструктор принимает только `Page` и только присваивает поля: без переходов, кликов, проверок, ожиданий и SQL.
3. Композиция вместо наследования: общие части (`CredentialsForm`, `HeaderComponent`) — компоненты с тем же `Page`, базовых классов нет.
4. Каждый публичный метод страницы и компонента — с `@Step`, и каждый параметр попадает в текст шага: `@Step("Заполнить форму входа: {username} / {password}")`.
5. Переход возвращает объект следующей страницы, действие и проверка на текущей — `this`.
6. Одно бизнес-действие — один метод, имя которого называет форму: `fillLoginForm(username, password)`, `fillRegistrationForm(...)`. Без безликих `fillForm`/`fill`/`submit` и цепочек `fillUsername().fillPassword()`; отдельный метод на поле — только если тесту нужно именно поле (валидация).
7. Действие отдельно от проверки: `fill…`/`click…` ничего не проверяют; `checkThat…` только проверяют через `PlaywrightAssertions.assertThat(locator)` и ничего не кликают. Без `locator.isVisible()` в JUnit-проверках и ручных ожиданий.
8. Без `checkThatOpened`, `checkPageLoaded`, `isLoaded`, `waitUntilLoaded`: `click`/`fill` ждут элемент, web-first assertions ждут состояние. URL и видимость страницы проверяем, только когда переход и есть предмет теста.
9. Локаторы: `getByRole` с точным `name`, `getByLabel`, `getByPlaceholder`, `getByText` с точным совпадением, `getByTestId`. Без `first()`/`nth()`, MUI-классов и XPath; неуникальный элемент сужай родителем: `page.getByRole(AriaRole.BANNER).getByRole(…)`.
10. Проверка не должна быть тавтологией локатора: `assertThat(heading("Travels map")).hasText("Travels map")` ничего не доказывает. Ожидаемое значение приходит из теста (`checkThatUsername(username)`).
11. Вход только через UI; `access_token`/`id_token` в `localStorage` не подставлять (до занятия 13).
12. Без комментариев и Javadoc: смысл передают имена методов и текст `@Step`.

## Проверка результата

Прогон `./gradlew :rfr-e2e:test --tests '<Тест>'` — единственное подтверждение. Проход сценария через MCP запуском теста не считается.
В отчёте разделяй: что подтверждено в браузере через MCP, что взято из исходников.

## Практика занятия 01

### MUI Select: option фильтруется по вложенному IMG
Поле вроде Location — MUI Select с ролью combobox. Доступное имя пункта бывает составным (`France France`), поэтому `getByRole(OPTION)` с `setName(country).setExact(true)` пункт не находит. Действие выбора страны:
- клик по `page.getByRole(AriaRole.COMBOBOX, … setName("Location").setExact(true))`;
- клик по `page.getByRole(AriaRole.OPTION).filter(… setHas(page.getByRole(AriaRole.IMG, … setName(country).setExact(true))))`.

Список опций рисуется отдельным оверлеем, поэтому локатор `option` берётся у `page`, а не у локатора закрытого combobox. Точное имя страны задаётся у вложенного `IMG`, а не у самого `option`. В тест передаётся одно и то же название страны. Проверка читает этот же флаг внутри combobox: `location.getByRole(IMG, … setName(country).setExact(true))`.

### Ожидание ответа мутации — внутри действия
`clickSave()` кликает Save внутри `page.waitForResponse(...)`: URL содержит `/graphql`, тело запроса содержит `UpdateUser`, ответ успешный. Тест вызывает `clickSave()` и сразу `checkThat...`. В тесте нет своего `waitForResponse` и нет `waitForTimeout`. Аватар после сохранения — `img`, чей `src` равен data URL загруженного файла. Ожидаемая строка приходит из теста в `checkThatAvatarSaved(avatarSrc)`.
