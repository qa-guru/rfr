```
e2e/
├── src/test/java/io/student/rococo|rangiffler/
│   ├── config/
│   │   ├── Config.java         
│   │   └── LocalConfig.java         
│   ├── data/
│   │   ├── entity/ 
│   │   │   └──                                # Entity классы
│   │   ├── repository/
│   │   │   └── UserRepository.java           
│   │   │   └── MuseumRepository.java          # Для Rococo
│   │   └── mapper/extractor/jpa/tpl     
│   │       └──                                # Классы для работы с БД
│   ├── model/                                 # Только Rococo, в Rangiffler используем кодогенерацию 
│   ├── jupiter/                       
│   │   ├── annotation/                   
│   │   │   ├── User.java 
│   │   │   ├── Museum.java                   # Только Rococo
│   │   └── meta/                     
│   │   │   └── WebTest.java
│   │   └── extension/                    
│   │   │    ├── UserExtension.java
│   │   │    ├── MuseumExtension.java          # Только Rococo
│   │   │    └── BrowserExtension.java
│   ├── service/                        
│   │   ├── MuseumClient.java
│   │   ├── UsersClient.java
│   │   │   └── impl/                         # Реализации сервисов через репозитории БД
│   │   │       ├── MuseumDbClient.java       # Только Rococo
│   │   │       └── UsersDbClient.java
```