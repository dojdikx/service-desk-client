# service-desk-client

Android CRM для сотрудников поддержки Service desk (Jetpack Compose, Material 3).

## Требования

- Android Studio Ladybug или новее
- JDK 17
- Эмулятор или устройство API 24+

## Сборка

```bash
.\gradlew.bat :androidApp:assembleDebug
```

Установка на эмулятор:

```bash
.\gradlew.bat :androidApp:installDebug
```

## Сервер

По умолчанию клиент обращается к `http://10.0.2.2:8080` (localhost хоста с эмулятора Android).

Переопределение в `gradle.properties`:

```properties
SERVER_BASE_URL=http://10.0.2.2:8080
```

В манифесте включён `usesCleartextTraffic` для локальной разработки.

## Функциональность

- Вход по JWT, роли `admin` / `support_agent`
- **Заявки:** канбан по статусам с сервера, фильтры, поиск, ручное создание, карточка, назначение исполнителя (admin)
- **Диалоги и чат:** превью, история, вложения, доставка, повтор отправки, ответ с цитатой (долгое нажатие на сообщение), WebSocket
- **Аналитика** и **выгрузка заявок** в Excel
- **Сотрудники** (регистрация и список — только admin)
- **Ещё:** язык (RU / zh-CN), светлая / тёмная / системная тема, выход

Макеты: папка [design/](design/).
