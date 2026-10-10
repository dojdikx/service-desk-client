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

## Текущее состояние

Каркас приложения: тема макета (`#2563EB`), Hilt, нижняя навигация **Тикеты / Диалоги / Аналитика / Ещё** с заглушками экранов.

Макеты: папка [design/](design/).
