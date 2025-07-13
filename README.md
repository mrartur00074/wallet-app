# Тестовое задание

## Тестовые данные

При старте автоматически создаются 2 тестовых кошелька:

| UUID                                  | Начальный баланс |
|---------------------------------------|------------------|
| `11111111-1111-1111-1111-111111111111`| 1000             |
| `22222222-2222-2222-2222-222222222222`| 100              |

Автогенерация: /src/main/java/com.example.walletapp.config.DataInitializer

## Конфигурация

### Основные файлы

1. **`application.properties`** (основные настройки):

2. **`docker-compose.yml`**:

### Liquibase миграции

Файл инициализации (`db/changelog/db.changelog-master.xml`):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.6.xsd">

    <include file="db/changelog/changes/001-initial-schema.yaml"/>
    <include file="db/changelog/changes/002-add-constraints.yaml"/>
</databaseChangeLog>
```

Содержание `001-initial-schema.yaml`:
```sql
databaseChangeLog:
  - changeSet:
      id: 001-create-wallets-table
      author: mrartur0074
      changes:
        - createTable:
            tableName: wallets
            columns:
              - column:
                  name: id
                  type: UUID
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: balance
                  type: BIGINT
                  constraints:
                    nullable: false
                  defaultValue: 0
              - column:
                  name: version
                  type: BIGINT
                  constraints:
                    nullable: false
                  defaultValue: 0
```

Содержание `002-add-constraints.yaml`:
```sql
databaseChangeLog:
  - changeSet:
      id: add-balance-constraint
      author: mrartur0074
      changes:
        - sql:
            sql: ALTER TABLE wallets ADD CONSTRAINT ck_wallet_balance_non_negative CHECK (balance >= 0)
```

## API Endpoints

| Метод | Путь                   | Описание          |
|-------|------------------------|-------------------|
| GET   | /api/v1/wallets/{UUID} | Получить кошелёк  |
| POST  | /api/v1/wallet         | Выполнить перевод |

## Запуск

Собрать и запустить:
```bash
docker-compose up --build
```

## Логирование

Логи выводятся в консоль и в файл логов. Формат:
```
2025-07-13 12:00:00 [http-nio-8080-exec-1] INFO  c.e.w.WalletController - Получен запрос баланса
``` 