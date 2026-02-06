# Rangiffler

  Приветствую тебя, мой дорогой студент!
Если ты это читаешь - то ты собираешься сделать первый шаг в написании диплома QA.GURU Advanced.

  Это один из двух вариантов дипломной работы - второй расположен [тут, называется Rococo](https://github.com/qa-guru/rococo)
Проекты отличаются как по своей механике, так и технологиям (Rococo использует классический REST на frontend,
тогда как Rangiffler использует GraphQL). Следует сказать, что Rangiffler может отказаться немного сложнее именно из-за GraphQL, но,
в качестве компенсации за сложность, даст тебе больше интересного опыта.
Выбор за тобой!

  Далее я опишу основные направления работы, но помни, что этот диплом - не шаблонная работа, а место
для творчества - прояви себя!

  Кстати, Rangiffler - произошло от названия северных оленей - Rangifer. Мы выбрали именно такое
название для этого проекта - потому, что он про путешествия, а северный олень - рекордсмен по
преодолеваемым расстояниям на суше. Путешествуй, be like Rangiffler! (это девиз этого проекта)

# Что будет являться готовым дипломом?

  Тут все просто, диплом глобально требует от тебя реализовать три вещи:

- Реализовать бэкенд на микросервисах (Spring boot, но если вдруг есть желание использовать что-то другое - мы не против)
- Реализовать полноценное покрытие тестами микросервисов и frontend (если будут какие-то
  unit-тесты - это большой плюс!)
- Красиво оформить репозиторий на гихабе, что бы любой, кто зайдет на твою страничку, смог понять,
  как все запустить, как прогнать тесты. Удели внимание этому пункту. Если я не смогу все запустить по твоему README - диплом останется без проверки

# Текущая структура проекта

```
rfr/
├── rfr-auth/              # OAuth 2.0 Authorization Server (готов)
├── rfr-api/               # GraphQL API Backend (базовая реализация)
│   ├── Mock контроллеры для фотографий
│   ├── Реальные контроллеры для пользователей и стран
│   └── Flyway миграции БД
├── rfr-gql-client/        # React + TypeScript Frontend (готов)
└── rfr-e2e/               # E2E тесты (заготовка)
```

**Порты:**
- rfr-auth: 9001
- rfr-api: 8081
- rfr-gql-client: 3001

**Технологии:**
- Backend: Spring Boot 4.0.2, Spring for GraphQL, Spring Security, JPA, Flyway
- Frontend: React 18, TypeScript, Apollo Client, Material-UI
- Auth: Spring Authorization Server
- БД: MySQL 8

# С чего начать?

Мы подготовили для тебя полностью рабочий frontend, минимально работающий сервис auth, а также базовую реализацию rfr-api с mock контроллерами.
Так как данный проект использует GraphQL, а frontend уже написан под конкретный API, то в проекте есть файл
`schema.graphqls` - он используется бэкендом rfr-api, куда прилетают все запросы с фронта.

Т.к. механика проекта сложнее, чем в Niffler и в Rococo, а именно, подразумевает хранение фоток, лайков, статистики, дружбы с другими юзерами и т.д.,
я добавил в проект схему базы данных - `V1__schema_init.sql`. 
Обрати внимание, что он написан так, как будто весь бэкенд Rangiffler - один монолитный сервис с монолитной же базой данных. В то время как диплом подразумевает микросервисную архитектуру,
и каждый из сервисов будет использовать 1-2 таблицы из этого скрипта. Но, на первом этапе, вы можете создать "монолитную" базу как есть этим скриптом,
и уже потом думать, как ее разбивать на сервисы.

В проекте уже есть минимальная реализация rfr-api с mock контроллерами для фотографий. Это позволяет сразу увидеть механику проекта Rangiffler.
Mock контроллеры возвращают статические данные из JSON файлов (`resources/mock/`), что позволяет протестировать frontend без полной реализации бэкенда.
Важно понимать, что несмотря на наличие моков mutation запросов (например, удаление фото), никакого реального удаления не произойдет, и при обновлении страницы 
будут возвращены те же данные из JSON файлов.

У тебя также есть проект Niffler, который будет выступать образцом для подражания в разработке микросервисов.
Тестовое покрытие niffler, которого мы с тобой добились на настоящий момент, однако, является достаточно слабым - учтите это при написании тестов на Rangiffler - это,
все-таки, диплом для SDET / Senior QA Automation и падать в грязь лицом с десятком тестов на весь сервис
точно не стоит. Итак, приступим!

#### 1. Запусти зависимости (БД):

```bash
bash localenv.sh
```

Скрипт запустит MySQL в Docker контейнере.

#### 2. Запусти rfr-auth:

```bash
cd rfr-auth
../gradlew bootRun
```

Или запусти класс с методом main руками. Auth будет доступен на порту 9001: http://localhost:9001

#### 3. Запусти rfr-api:

```bash
cd rfr-api
../gradlew bootRun
```

rfr-api стартанет на порту 8081: http://localhost:8081/graphql

GraphiQL интерфейс доступен по адресу: http://localhost:8081/graphiql

#### 4. Обнови зависимости и запускай фронт:

```bash
cd rfr-gql-client
npm i
npm run dev
```

Фронт стартанет в твоем браузере на порту 3001: http://localhost:3001/

#### 5. Проверь работоспособность

Кнопка "Войти" работает через сервис auth. После успешной авторизации ты попадешь на главную страницу Rangiffler.

Mock контроллеры в rfr-api будут возвращать статические данные из JSON файлов для ленты фотографий.
Реальная работа с пользователями и странами уже реализована через базу данных.


# Что дальше?

#### 1. В первую очередь, необходимо подумать над сервисами - какие тебе понадобятся.

  Например, можно предложить вот такую структуру сервисов:

<img src="services.png" width="600">

  ВАЖНО! Картинка - не догма, а лишь один из вариантов для примера. 
Например, для хранения статистики можно отдельный сервис сделать.
Взаимодействие между gateway и всеми остальными сервисами можно сделать с помощью
REST, gRPC или SOAP. Я бы посоветовал отдать предпочтение gRPC.

#### 2. Изучи текущую реализацию rfr-api

В проекте уже есть базовая монолитная реализация rfr-api. Что уже реализовано:

**Готово:**
- SecurityConfig с OAuth 2.0 Resource Server
- GraphQL схема (`schema.graphqls`)
- Модели для GraphQL типов (User, Country, Feed, Photo, Stat, Likes)
- Mock контроллеры для фотографий (FeedMockQueryController, PhotoMockMutationController)
- Реальные контроллеры для пользователей и стран (UserQueryController, UserMutationController, CountryQueryController)
- Сервисный слой (UserService, CountryService)
- JPA entities и repositories
- Flyway миграции для БД
- Mock данные в JSON файлах (`resources/mock/`)

**Что нужно доработать:**
- Заменить mock контроллеры фотографий на реальную реализацию с БД
- Добавить entity и repository для фотографий
- Реализовать сервис для работы с фотографиями
- Реализовать функционал лайков
- Реализовать статистику по странам

Все важные подсказки ниже, в разделе "Особенности реализации backend"

#### 3. Как только у вас появилось уже 2 сервиса, есть смысл подумать о докеризации

  Чем раньше у ваc получится запустить в докере фронт и все бэкенды, тем проще будет дальше.
На самом деле, докеризация не является строго обязательным требованием, но если вы хотите в будущем
задеплоить свой сервис на прод, прикрутить CI/CD, без этого никак не обойдется.

  Я советую использовать плагин jib - как в niffler, для бэкендов, и самописный dockerfile для фронта.
Фронтенд использует React, докеризация там работает ровно так же, как и в Niffler.

#### 4. Выбрать протоколо взаимодействия между сервисами

  В поставляемом фронтенде используется [GraphQL](https://graphql.org/). А вот взаимодействие между микросервисами можно
делать как угодно! REST, gRPC, SOAP. Делай проект я, однозначно взял бы gRPC - не писать руками кучу
model-классов, получить перформанс и простое написание тестов. Стоит сказать, что здесь не
понадобятся streaming rpc, и все ограничится простыми унарными запросами. Однако если вы хотите
использовать REST или SOAP - мы не будем возражать.

#### 5. Реализовать микросервисный бэкенд

  Это место где, внезапно, СОВА НАРИСОВАНА!
На самом деле, концептуально и технически каждый сервис будет похож на что-то из niffler, поэтому
главное внимательность и аккуратность. Любые отхождения от niffler возможны - ты можешь захотеть
использовать, например, NoSQL базы или по другому организовать конфигурацию / структуру проекта -
никаких ограничений, лишь бы сервис выполнял свое прямое назначение

##### Особенности реализации backend

###### Connection-mипы данных для GraphQL, пагинация

  В отличие от Niffler, в поставляемом файле `query.graphqls` есть несколько используемых типов `type` - которые не описаны в этом файле!
Это типы `UserConnection` и `PhotoConnection`. Даже IDEA отобразит их красным: 

<img src="IDEA-error.png" width="600">

  **Однако, это не ошибка.** Дело в том, что типы с именем `{Typename}Connection` генерируются автоматически и представляют собой ни что иное,
как реализацию пагинации для GraphQL. То есть все типы `{Typename}Connection` можно упрошенно считать "Коробочкой, в которой есть список {Typename}
и механизмы навигации к следующей и предыдущей страницам". Поэтому, эти типы описывать в файле `query.graphqls` руками не нужно, обращать внимание
на "красноту" в IDEA тоже не нужно. Почитать о том, что они действительно генерируются автоматически, [можно тут](https://docs.spring.io/spring-graphql/reference/request-execution.html#execution.pagination)

  Как ты понял из вышесказанного пункта, Rangiffler действительно использует пагинацию для фоток и пользователей. Это значит, что вам надо концептуально понять, как решается две задачи:
    - Что вернуть из контроллеров в качестве ответа с типом `{Typename}Connection` - с учетом что мы его не описываем руками и классы для него не создаем
    - Как сделать запрос в БД с пагинацией, что бы было, что возвращать. Ответы будут ниже

###### Pageble контроллеры (дата-фетчеры) для GraphQL

  Пусть у нас есть тип User:
```graphql
type User {
    id: ID!
    username: String!
}
```
  и есть query с пагинацией на запрос всех юзеров:
```graphql
type Query {
  users(page:Int, size:Int, searchQuery:String): UserConnection
}
```
  Тогда создадим java-класс **только для типа User**, не создавая для UserConnection:
```java
public record UserGql(UUID id, String username) {}
```
  И опишем контроллер, возвращающий UserConnection. С точки зрения Spring-graphql типы `{Typename}Connection` не что иное, как `Slice<Typename>`
из пакета `org.springframework.data.domain`.
Таким образом в нашем примере контроллер для query `users` вернет `Slice<UserGql>`:
```java
  @QueryMapping
  public Slice<UserGql> users(@AuthenticationPrincipal Jwt principal,
                              @Argument int page,
                              @Argument int size,
                              @Argument @Nullable String searchQuery) {
  return userService.allUsers(
          principal.getClaim("sub"),
          PageRequest.of(page, size),
          searchQuery
  );
}
```
  Здесь первый аргумент - это просто сессия (как и в Niffler), `int page, int size` - два обязательных аргумента пагинации, они прилетят с фронта.
Третий аргумент `String searchQuery` - необязательный аргумент, который фронт отправляет при использовании поиска в таблицах.
Обратите внимение на конструкцию `PageRequest.of(page, size)` - она создает объект `Pageable` - и именно используя его мы можем получить `Slice<UserEntity>`
```java
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

  @Query("select u from UserEntity u where u.username <> :username" +
          " and (u.username like %:searchQuery% or u.firstname like %:searchQuery% or u.surname like %:searchQuery%)")
  Slice<UserEntity> findByUsernameNotAndSearchQuery(@Param("username") String username,
                                                    @Nonnull Pageable pageable,
                                                    @Param("searchQuery") String searchQuery);
}
```
  Тип `Slice` - это ровно то, что ожидает от вас получит фронт, вам лишь придется преобразовать его в `Slice<UserGql>`,
для этого надо воспользоваться методом `map()`, имеющимся в классе `Slice`.
Обратите внимание, что этот вариант метода требует обязательного `@Param("searchQuery") String searchQuery` - поэтому нужно
реализовать и второй метод в репозитории, без searchQuery. А логика, какой из них вызвать, будет на уровне сервиса, в зависимости от того,
придет в контроллер с фронта этот searchQuery или нет.

  Почитать про пагинацию в JPA Repository, дополнительно, тут: https://www.baeldung.com/spring-data-jpa-pagination-sorting

###### Pageble в JpaRepository

  Вы, вероятно, заметили аннотацию `@Query` над методом в примере, содержащую JPQL запрос. Это не спроста.
Дело в том, что единственный способ получить функционал пагинации - это доставать данные из БД **одним запросом**.

  Это значит, что если нам нужны допустим фотографии юзера с пагинацией, мы не можем сделать так:
```java
UserEntity user = findById(id);
return user.getPhotos();
```
  В этом коде _просто нет возможности использовать пагинацию._ Но что если нам нужно запросить фотографии юзера с пагинацией?

```java
  Slice<PhotoEntity> findByUser(@Nonnull UserEntity user,
                                @Nonnull Pageable pageable);

```
  Вот так уже сработает - тут всего один запрос, и поэтому он работает с `Pageable`.

  Я предлагаю вам свой вариант получения друзей и заявок на дружбу одним запросом:

```java
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

  Optional<UserEntity> findByUsername(@Nonnull String username);

  Slice<UserEntity> findByUsernameNot(@Nonnull String username,
                                      @Nonnull Pageable pageable);

  @Query("select u from UserEntity u where u.username <> :username" +
          " and (u.username like %:searchQuery% or u.firstname like %:searchQuery% or u.surname like %:searchQuery%)")
  Slice<UserEntity> findByUsernameNotAndSearchQuery(@Param("username") String username,
                                                    @Nonnull Pageable pageable,
                                                    @Param("searchQuery") String searchQuery);

  @Query("select u from UserEntity u join FriendshipEntity f on u = f.addressee" +
          " where f.status = data.io.student.rangiffler.FriendshipStatus.ACCEPTED and f.requester = :requester")
  Slice<UserEntity> findFriends(@Param("requester") UserEntity requester,
                                @Nonnull Pageable pageable);

  @Query("select u from UserEntity u join FriendshipEntity f on u = f.addressee" +
          " where f.status = data.io.student.rangiffler.FriendshipStatus.ACCEPTED and f.requester = :requester" +
          " and (u.username like %:searchQuery% or u.firstname like %:searchQuery% or u.surname like %:searchQuery%)")
  Slice<UserEntity> findFriends(@Param("requester") UserEntity requester,
                                @Nonnull Pageable pageable,
                                @Param("searchQuery") String searchQuery);

  @Query("select u from UserEntity u join FriendshipEntity f on u = f.addressee" +
          " where f.status = data.io.student.rangiffler.FriendshipStatus.ACCEPTED and f.requester = :requester")
  List<UserEntity> findFriends(@Param("requester") UserEntity requester);

  @Query("select u from UserEntity u join FriendshipEntity f on u = f.addressee" +
          " where f.status = data.io.student.rangiffler.FriendshipStatus.ACCEPTED and f.requester = :requester" +
          " and (u.username like %:searchQuery% or u.firstname like %:searchQuery% or u.surname like %:searchQuery%)")
  Slice<UserEntity> findFriends(@Param("requester") UserEntity requester,
                                @Param("searchQuery") String searchQuery);

  @Query("select u from UserEntity u join FriendshipEntity f on u = f.addressee" +
          " where f.status = data.io.student.rangiffler.FriendshipStatus.PENDING and f.requester = :requester")
  Slice<UserEntity> findOutcomeInvitations(@Param("requester") UserEntity requester,
                                           @Nonnull Pageable pageable);

  @Query("select u from UserEntity u join FriendshipEntity f on u = f.addressee" +
          " where f.status = data.io.student.rangiffler.FriendshipStatus.PENDING and f.requester = :requester" +
          " and (u.username like %:searchQuery% or u.firstname like %:searchQuery% or u.surname like %:searchQuery%)")
  Slice<UserEntity> findOutcomeInvitations(@Param("requester") UserEntity requester,
                                           @Nonnull Pageable pageable,
                                           @Param("searchQuery") String searchQuery);

  @Query("select u from UserEntity u join FriendshipEntity f on u = f.addressee" +
          " where f.status = data.io.student.rangiffler.FriendshipStatus.PENDING and f.requester = :requester")
  List<UserEntity> findOutcomeInvitations(@Param("requester") UserEntity requester);

  @Query("select u from UserEntity u join FriendshipEntity f on u = f.addressee" +
          " where f.status = data.io.student.rangiffler.FriendshipStatus.PENDING and f.requester = :requester" +
          " and (u.username like %:searchQuery% or u.firstname like %:searchQuery% or u.surname like %:searchQuery%)")
  List<UserEntity> findOutcomeInvitations(@Param("requester") UserEntity requester,
                                          @Param("searchQuery") String searchQuery);

  @Query("select u from UserEntity u join FriendshipEntity f on u = f.requester" +
          " where f.status = data.io.student.rangiffler.FriendshipStatus.PENDING and f.addressee = :addressee")
  Slice<UserEntity> findIncomeInvitations(@Param("addressee") UserEntity addressee,
                                          @Nonnull Pageable pageable);

  @Query("select u from UserEntity u join FriendshipEntity f on u = f.requester" +
          " where f.status = data.io.student.rangiffler.FriendshipStatus.PENDING and f.addressee = :addressee" +
          " and (u.username like %:searchQuery% or u.firstname like %:searchQuery% or u.surname like %:searchQuery%)")
  Slice<UserEntity> findIncomeInvitations(@Param("addressee") UserEntity addressee,
                                          @Nonnull Pageable pageable,
                                          @Param("searchQuery") String searchQuery);

  @Query("select u from UserEntity u join FriendshipEntity f on u = f.requester" +
          " where f.status = data.io.student.rangiffler.FriendshipStatus.PENDING and f.addressee = :addressee")
  List<UserEntity> findIncomeInvitations(@Param("addressee") UserEntity addressee);

  @Query("select u from UserEntity u join FriendshipEntity f on u = f.requester" +
          " where f.status = data.io.student.rangiffler.FriendshipStatus.PENDING and f.addressee = :addressee" +
          " and (u.username like %:searchQuery% or u.firstname like %:searchQuery% or u.surname like %:searchQuery%)")
  List<UserEntity> findIncomeInvitations(@Param("addressee") UserEntity addressee,
                                         @Param("searchQuery") String searchQuery);

}
```
  Обратите внимание, что вместо поля pending, здесь используется enum с двумя статусами `FriendshipStatus.PENDING`/`FriendshipStatus.ACCEPTED`.

###### EntityProjection в JpaRepository  

  В GQL схеме есть поле `isOwner: Boolean!` для того, что бы фотографии могли размечаться на свои / не свои. В идеале, делать это на уровне SQL (JPQL) запроса прямо в репозитории, для этого надо ввести промежуточный слой - интерфейс или класс, реализующий паттерн EntityProjection:
```java

public interface PhotoRepository extends JpaRepository<PhotoEntity, UUID> {

  interface FeedPhotoView {
    UUID getId();

    byte[] getPhoto();

    CountryEntity getCountry();

    String getDescription();

    java.util.Date getCreatedDate();

    boolean isOwner();
  }

  // только "свои" фото, если параметр user - текущий пользователь
  @Nonnull
  Slice<PhotoEntity> findByUserOrderByCreatedDateDesc(@Nonnull UserEntity user,
                                                      @Nonnull Pageable pageable);

  //  "свои" и "чужие" фото, если в листе List<UserEntity> users есть текущий пользователь и его друзья. В результате будут объекты интерфеса FeedPhotoView с правильным признаком isOwner
  @Nonnull
  @Query("select p.id as id, p.photo as photo, p.country as country, p.description as description, p.createdDate as createdDate, " +
      "case when p.user.username = :username then true else false end as isOwner " +
      "from PhotoEntity p where p.user in :users order by p.createdDate desc")
  Slice<FeedPhotoView> findFeedPhotos(@Param("users") @Nonnull List<UserEntity> users,
                                      @Param("username") @Nonnull String username,
                                      @Nonnull Pageable pageable);
}
  
  ```

###### Передача информации о пагинации по gRPC (для примера) между сервисами, возврат `Slice` из сервисов

  Тут все просто. Вам с фронта приходят `int page, int size` + не забыть про третий опциональный парметр - `searchQuery`. 
Тогда, к примеру, gRPC сообщение в сервис с пользователями будет таким:
```protobuf
message UsersRequest {
  string searchQuery = 1;
  int32 page = 2;
  int32 size = 3;
}

message UsersResponse {
  repeated User users = 1;
  boolean hasNext = 2;
}
```
  Тогда мы сможем вернуть на фронт созданный руками Slice
```java
            List<UserGql> userGqlList = response.getUsersList()
                    .stream()
                    .map(UserGql::fromGrpcMessage)
                    .toList();
            return new SliceImpl<>(userGqlList, PageRequest.of(page, size), response.hasNext());
```

  Здесь объект `PageRequest.of(page, size)` - это изначальные параметры int page, int size, а `response.hasNext()` - получаем
в самом микросервисе из объекта Slice, который вернет JpaRepository.

###### Security config

   Для локального тестирования вы можете открыть доступ к antMatcher("/graphiql/**"):
```java
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        corsCustomizer.corsCustomizer(http);

        http.authorizeHttpRequests(customizer ->
                customizer.requestMatchers(antMatcher("/graphiql/**"))
                        .permitAll()
                        .anyRequest()
                        .authenticated()
        ).oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
```

###### GraphQL контроллеры совместно с record

   Несмотря на то, что record - immutable тип без сеттеров, последние версии Spring-graphql корректно позволяют "наполнять" его данными с помощью
`@SchemaMapping` Такис образом если клиент запрашивает:
```json
     query user {
     user {
       id
       username
       friends(page: 0, size: 10) {
         edges {
           node {
             id
             username
           }
         }
         pageInfo {
           hasPreviousPage
           hasNextPage
         }
       }
     }
   }
```
  То бэкенд соберет ему ответ вот так: 
```java
  @QueryMapping
  public UserGql user(@AuthenticationPrincipal Jwt principal) {
    return userService.currentUser(principal.getClaim("sub")); // Здесь будет null в полe friends
  }
  
    @SchemaMapping(typeName = "User", field = "friends") // будет вызван автоматически, т.к. в запросе фронт попросил friends
    public Slice<UserGql> friends(UserGql user, @Argument int page, @Argument int size, @Argument @Nullable String searchQuery) {
      // получит на вход UserGql user и добавит внутрь него Slice<UserGql> с друзьями
      return userService.friends(
              user.username(),
              PageRequest.of(page, size),
              searchQuery
      );
    }
}
```
  Таким образом, ни при каких обстоятельствах, вызывать явно в своем коде методы, аннотированные как `@SchemaMapping` - не нужно!

###### Контроль доступа:

  Логика проекта подразумевает массу операций, таких как удаление фото, простановка лайков, рекдактирование и так далее.
В общем случае, с фронта уходит ID изменяемого объекта. Поэтому особое внимание необходимо уделить контролю доступа к объекту - не пытается 
ли пользователь отредактировать чужое фото, или поставить второй лайк под фото, которое уже лайкнул ранее.

#### 6. Подготовить структуру тестового "фреймворка", подумать о том какие прекондишены и как вы будете создавать

Здесь однозначно понадобится возможность API-логина и работы со всеми возможными preconditions проекта - фотками,
пользователями и т.д. Например, было бы хорошо иметь тесты примерно такого вида:
```java
@Test
@DisplayName("...")
@Tag("...")
@ApiLogin(user = @User(photos = @Photo(country = RUSSIA)))
void exampleTest(UserGql createdUser) { ... }

@Test
@DisplayName("...")
@Tag("...")
@ApiLogin(user = @TestUser(photos = @Photo(country = INDIA), partners = {
        @Partner(status = FRIEND, photos = @Photo(country = CANADA, imageClasspath = "cat.jpeg")),
        @Partner(status = INCOME_INVITATION, photos = @Photo(country = CANADA, imageClasspath = "dog.jpeg")),
        @Partner(status = OUTCOME_INVITATION, photos = @Photo(country = AUSTRALIA, imageClasspath = "fish.jpeg"))}))
void exampleTest2(UserGql createdUser) { ... }
```

#### 7. Реализовать достаточное, на твой взгляд, покрытие e-2-e тестами

  На наш взгляд, только основны позитивных сценариев тут не менее трех десятков.
А если не забыть про API-тесты (будь то REST или gRPC), то наберется еще столько же.

#### 8. Оформить все красиво!

  Да, тут еще раз намекну про важность ридми, важность нарисовать топологию (схему) твоих сервисов, важность скриншотиков и прочих красот.
Очень важно думать о том, что если чего-то не будет описано в README, то и проверить я это что-то не смогу.

<img src="rangiffler.png" width="800">
