# Памятка: локальный стенд и git bundle

Для агента полный контекст проекта — skill `rfr` (`.claude/skills/rfr/SKILL.md`).
Ниже — краткая шпаргалка для человека (Windows / PowerShell).

## Запуск стенда

Требования: JDK 25, Node.js 22+, Docker Desktop (демон запущен).

```bash
docker compose up -d          # MySQL :3306, root/secret; дождись healthy
./gradlew runAll              # auth :9001, api :8081
cd rfr-gql-client && npm ci && npm run dev   # :3001
./gradlew e2e                 # все тесты rfr-e2e
```

## Ошибка: порт 3306 занят (`docker compose up -d`)

Если Docker пишет что-то вроде:

```text
ports are not available: ... bind: Only one usage of each socket address ... 3306
```

на хосте уже слушает локальный MySQL (`mysqld.exe`). Освободите порт в **PowerShell** (не Git Bash — там `/FI` ломается).

```powershell
# из корня репо (пример пути)
cd C:\Users\<user>\Projects\qa_guru_2026_advanced\rfr

# кто держит 3306
netstat -ano | findstr :3306
tasklist /FI "PID eq <PID_из_netstat>"

# остановить службу MySQL (часто MySQL80)
Get-Service *mysql*
net stop MySQL80
# или: Stop-Service MySQL80

# порт свободен?
netstat -ano | findstr :3306

docker compose up -d
```

Если `net stop` не находит службу — возьмите имя из `Get-Service *mysql*` и остановите его, либо `Stop-Process -Id <PID> -Force`.

## Остановка

В терминале, где крутится процесс:

- `./gradlew runAll` → **Ctrl+C** (auth :9001 и api :8081)
- `npm run dev` → **Ctrl+C** (фронт :3001)

Если порты остались заняты (из корня репо):

```bash
bash ports.sh                 # kill процессов на 9001, 8081, 3001
```

MySQL:

```bash
docker compose down           # контейнер rfr-mysql; volume mysql-volume сохраняется
```

## Работа с `.bundle` (материалы занятия)

`.bundle` — это **git-архив ветки**, не проект. В IDEA его не открывают через File → Open: содержимое подтягивают в клон `rfr` командами `git fetch` / `git switch`.

### Куда положить файл

Удобно рядом с клоном, например:

```text
C:\Users\<user>\Projects\qa_guru_2026_advanced\
  rfr\                 ← клон репозитория
  rfr-l03.bundle       ← бандл из материалов (буква l = lesson, не цифра 1)
```

Проверка, что файл на месте (**PowerShell**):

```powershell
cd C:\Users\<user>\Projects\qa_guru_2026_advanced
Get-Item .\rfr-l03.bundle
```

Если `не существует` — бандл в другой папке или ещё не скачан. Поиск:

```powershell
Get-ChildItem -Path C:\Users\<user> -Filter *.bundle -Recurse -ErrorAction SilentlyContinue
```

### Пути: PowerShell vs Git Bash

В **Git Bash** путь `C:\Users\...` ломается (`\` — escape). Используйте:

- PowerShell с обычным Windows-путём, или
- прямые слэши: `"C:/Users/<user>/Projects/qa_guru_2026_advanced/rfr-l03.bundle"`

### Список веток внутри бандла

Из корня клона `rfr`:

```powershell
cd C:\Users\<user>\Projects\qa_guru_2026_advanced\rfr
git bundle list-heads ..\rfr-l03.bundle
```

Пример вывода:

```text
10be86b0efabee3c39b30bbd202af740b50b33c0 refs/heads/v3/l03-junit-live
```

Имя ветки здесь — `v3/l03-junit-live` (без префикса `refs/heads/`).

### Подтянуть ветку и переключиться

```powershell
git fetch ..\rfr-l03.bundle v3/l03-junit-live:v3/l03-junit-live
git switch v3/l03-junit-live
```

Отдельная рабочая ветка от кода занятия:

```powershell
git switch -c hw/03-junit-live v3/l03-junit-live
```

После `switch` IDEA обычно сама показывает новую ветку (VCS → Git → Branches).

### Типичные ошибки

| Симптом | Причина | Что сделать |
|---|---|---|
| `could not open '...bundle'` / `не существует` | Нет файла или неверное имя | Проверить имя (`rfr-l03`, не `rfr-103`) и путь через `Get-Item` |
| `C:UsersuserProjects...` без слэшей | Git Bash съел `\` | PowerShell или путь с `/` |
| `need a repository to verify a bundle` | `git bundle verify` вызван вне клона | Сначала `cd` в `rfr`, затем `git bundle verify ..\rfr-l03.bundle` |
