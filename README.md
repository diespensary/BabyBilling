# Nexign Bootcamp 2025

## Технологический стек

* OpenJDK 17
* Gradle
* Spring (Boot, Security, Data, Web, Cloud, AMQP)

* RabbitMQ
* PostgreSQL
* H2 Database
* Liquibase
* Docker

---

## Структура проекта

Проект состоит из следующих микросервисов:

* **CDR**: Генерация и отправка CDR-записей.
* **BRT**: Хранение и обработка данных абонентов и звонков.
* **HRS**: Тарификация звонков.
* **CRM**: Управление взаимоотношениями с клиентами (абоненты, менеджеры).

---

## Схема взаимодействия сервисов

1. **CDR → BRT**: Передача CDR-файлов (пакетами по 10 записей).
2. **BRT → HRS**: Отправка данных для тарификации.
3. **HRS → BRT**: Возврат тарификационных данных для списания.
4. **CRM ↔ BRT/HRS**: Взаимодействие по REST API.

---

## Особенности реализации

* Параллельная генерация звонков и асинхронная отправка данных в BRT.
* Автоматическое разделение звонков, начинающихся до и заканчивающихся после полуночи.
* Поддержка разных типов тарифов (классика и помесячный).

---

## Схема базы данных

* **CDR:** Таблицы `subscriber`, `cdr_record`.
* **BRT:** Таблицы `subscriber`, `cdr_data`, `balance_history`.
* **HRS:** Таблица `tariff_plan`.
* **CRM:** Таблицы для абонентов и ролей.

---

## Swagger UI

* CDR: `http://localhost:8080/swagger-ui/index.html`
* BRT: `http://localhost:8081/swagger-ui/index.html`
* HRS: `http://localhost:8082/swagger-ui/index.html`
* CRM: `http://localhost:8083/swagger-ui/index.html`

---

## Запуск сервисов

### С помощью Docker Compose

```bash
docker compose up --build
```

---

## Данные для авторизации

* RabbitMQ:

  * URL: `http://localhost:15672`
  * Username: `user`
  * Password: `password`

* PostgreSQL:

  * Username: `user`
  * Password: `password`
 
---

## Авторизация API CRM

* Менеджер: `admin:admin`
* Абонент: `{msisdn}:""` (пустой пароль)

Примеры вызовов:

* API менеджера: `http://localhost:8083/manager/**`
* API абонента: `http://localhost:8083/subscriber/**`

---

## Подключение к базам данных

* **BRT:** `jdbc:postgresql://127.0.0.1:5430/brt_db`
* **HRS:** `jdbc:postgresql://127.0.0.1:5440/hrs_db`
* **CRM:** `jdbc:postgresql://127.0.0.1:5450/crm_db`
