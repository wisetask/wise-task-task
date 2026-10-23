# wise-task-task

Микросервис задач и решений. Хранит задачи двух типов, каталоги задач и решения студентов; при проверке решения координирует graph-сервис и плагины.

## Технологии

- Java 25, Spring Boot 4.1.x, gRPC-сервер
- Spring Data JPA + PostgreSQL (база `wise_task_task`)

## Домен

Задача бывает двух типов (`task_type`):

- **GRAPH** (`task_graph`) — задача на конструирование графа. Условие — список `PluginInfo`: плагин + ожидаемое значение + текст ошибки. Правила (`rule`) задают допустимые операции с графом (красить/двигать/удалять/редактировать).
- **IMPLEMENTATION** (`task_implementation`) — задача на написание кода, проверяется плагином `plugin_id`.

Каталоги (`catalog`) группируют задачи и шарятся студентам (`catalog_student_ids`).

Решение (`solution`) — граф (`solution_graph`) или код (`solution_implementation`); хранит флаг `is_correct` и результаты плагинов.

Основные таблицы: `task`, `task_graph`, `task_implementation`, `task_catalog`, `catalog`, `catalog_student_ids`, `solution`, `solution_graph`, `solution_implementation`. Схема создаётся init-скриптами в `wise-task-gateway/docker/postgres/init-scripts`.

## Проверка решения (SolveTask)

1. Решение-граф сохраняется в graph-сервис (`createGraph`).
2. Для каждого условия задачи вызывается `CheckPluginSolution` в plugin-сервисе (для рукописных ответов — `HandwrittenAnswer`).
3. Ответ плагина сверяется с ожиданием (`=`, `>`, `<`, `>=`, `<=` для характеристик; «Выполняется/Не выполняется» для свойств).
4. Итог (`is_correct` + результаты по каждому плагину) сохраняется в БД.

Задачи IMPLEMENTATION проверяются компиляцией и прогоном кода через plugin-сервис (`CheckPluginImplementation`).

## gRPC API (порт 6568)

Proto: `src/main/resources/proto/task_grpc.proto`, пакет `ru.leti.wise.task.task`.

| Метод | Назначение |
|---|---|
| `GetTask` / `GetAllTask` / `GetTasksByIds` | Получение задач (фильтры + пагинация) |
| `CreateTask` / `UpdateTask` / `DeleteTask` | Управление задачами |
| `SolveTask` | Отправить решение на проверку |
| `GetTaskSolution` / `GetAllTaskSolutions` | Решения (фильтр по автору/задаче/корректности) |
| `CreateCatalog` / `UpdateCatalog` / `DeleteCatalog` / `GetCatalog` / `GetAllCatalogs` | Каталоги |

Фильтры `GetAllTask`: `name`, `description`, `category` (like, без регистра), `taskType`, `authorId`, `isPublic`.

## Зависимости

- **wise-task-graph** (`GRAPH_APP_NAME:6565`) — сохранение графов решений, получение графов задач.
- **wise-task-plugin** (`PLUGIN_APP_NAME:6566`) — проверка условий.

## Конфигурация

| Переменная | Назначение |
|---|---|
| `TASK_GRPC_PORT` | Порт gRPC-сервера (6568) |
| `TASK_DB`, `TASK_DB_USER`, `TASK_DB_PASSWORD` | Подключение к PostgreSQL |
| `GRAPH_APP_NAME` / `GRAPH_GRPC_PORT`, `PLUGIN_APP_NAME` / `PLUGIN_GRPC_PORT` | Адреса соседних сервисов |

## Запуск

Через общий docker-стенд gateway:

```bash
cd ../wise-task-gateway && docker compose up -d --build wise-task-task
```

Локально: `./gradlew bootRun` (нужны PostgreSQL с базой `wise_task_task` и поднятые graph/plugin).
