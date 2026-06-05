# Data Pipeline Engine
ELT-сервис для пакетной обработки финансовых транзакций в стиле телеком-оператора. Читает грязные CSV-логи, валидирует их в многопоточном режиме и загружает чистые данные в PostgreSQL.

## Технологии
- Java 21
- Spring Boot 4.0
- PostgresSQL 18
- JdbcTemplate + batchUpdate
- Flyway (автоматические миграции)
- ExecutorService (многопоточность)
- Swagger UI

## Архитектура ELT-паплайна

### Как работает пайплайн
**Шаг 1 - Чтение файла**
BufferedReader читает CSV файл построчно, не загружая весь файл в память сразу.

**Шаг 2 - Многопоточная валидация**
10000 строк делятся на 4 части. Каждый поток из пула ExecutorService валидирует свою часть параллельно:
- Сумма отрицательная -> статус INVALID
- Текст вместо цифр -> статус INVALID 
- Всё корректно -> статус VALID

**Шаг 3 - Пакетная вставка в staging таблицу**
Все строки (и VALID и INVALID) сохраняются в `raw_transactions_import` через `jdbcTemplate.batchUpdate()` пачками по 500 строк.

**Шаг 4 - Фильтрация и загрузка чистых данных**
Только VALID строки конвертируются из String в правильные типы (Long, BigDecimal, LocalTime) и сохраняются в `clean_transactions`.

## Таблицы

|
 Таблица
|
 Описание
|
|
---
|
|
 `raw_transactions_import`
|
 Буферная staging таблица - все строки из CSV включая невалидные со статусом и описанием ошибки
|
|
 `clean_transactions`
|
 Финальная таблица - только валидные транзакции с правильными типами данных
|
|
 `Flyway_schema_history`
|
 Служебная таблица Flyway - история миграций
|

## Ключевые технические решения

**почему JdbcTemplate, а не Hibernate?**
Привс вставке 10000 строк Hibernate создает 10000 отдельных SQL запросов и загружает память сущностями. JdbcTemplate через `batchUpdate()` отправляет данные пачками по 500 строк - это в 20-30 раз быстрее.

**ППочему мнопоточность?**
Валидация в один поток загружает только одно ядро процессора. ExecutorService с пулом 4 потоков распараллеливает обработку и использует все ядра одновременно.

**Почему staging таблица?**
Мы не пишем сразу в финальную таблицу. Сначала все данные попадают в буфер `raw-transactions_import`, где фиксируются ошибки валидации. Только после этого чистые данные переливаются в `clean_transactions`. Это стандартный паттерн дата-приложении. 

**Почему Flyway?**
Flyway автоматически создает таблицы при запуске приложения и отслеживает историю миграции. Не нужно вручную запускать SQL скрипты в pgAdmin.

## Запуск проекта

### 1. Создать базу данных
```sql
CREATE DATABASE data_pipeline_db;
```
### 2. Настроить подключение
В `aplication.yml`:
```yaml
spring:
    datasource:
        url: jdbc:postgresql://localhost:5432/data_pipeline_db
        username: postgres
        password: ваш_пароль
```

### 3. Запускать приложение
Flyway автоматически создаст все таблицы при старте.

### 4. Открыть Swagger UI
http://localhost:8080/swagger-ui/index.html

### 5. Запустить пайплайн
POST /api/pipeline/run?filePath=src/main/resources/data/transactions_dump.csv

## Проверка результатов в pgAdmin
```sql
-- Количество всех загруженных строк
SELECT COUNT(*) FROM raw_transactions_import;

-- Статистика по статусам валидации
SELECT import_status, COUNT(*)
FROM raw_transactions_import
GROUP BY import_status;

-- Просмотр невалидных строк с причиной ошибки
SELECT * FROM raw_transactions_import
WHERE import_status = 'INVALID';

-- Финальные чистые данные
SELECT COUNT(*) FROM clean_transactions;
SELECT * FROM clean_transactions;
```