# GPT Programming Assistant

`REST API` приложение на Spring Boot с интеграцией `OpenAI` и `RAG` (`Retrieval-Augmented Generation`) для ответов на вопросы по программированию.

<div align="center">
    <img src="images/image_for_readme.png" width="512" height="512">
</div>

## Технологии

- **Java 21** <img src="images/java_icon.png" height=30 width=30> - основной язык программирования
- **Spring Boot 3.5** <img src="images/spring_icon.png" height=30 width=30> - фрэймворк для создания веб-сервиса
- **Spring AI 1.1.2**  <img src="images/spring_ai.png" height=30 width=40> — интеграция с AI моделями
- **ChromaDB** <img src="images/chroma_db.png" height=30 width=30> — векторная база данных для RAG
- **Spring Actuator** <img src="images/actuator_icon.svg" height=30 width=30> — мониторинг и health checks
- **Caffeine Cache** <img src="images/caffeine_icon.png" height=30 width=40> — кэширование RAG запросов
- **Bucket4j** <img src="images/bcfj_icon.png" height=30 width=30> — rate limiting
- **Micrometer/Prometheus** <img src="images/micrometer_icon.png" height=30 width=30> <img src="images/prometheus_icon.png" height=30 width=30> — экспорт метрик

## Архитектура

```
src/main/java/io/mkalugin/gpt/
├── client/
│   └── ChromaDbClient.java         # Клиент для запросов к ChromaDB
├── config/
│   ├── AppConfig.java              # Конфигурация ChatMemory и Swagger
│   ├── CacheConfig.java            # Конфигурация Caffeine cache
│   ├── ChromaConfig.java           # Конфигурация ChromaDB и VectorStore
│   └── SecurityConfig.java         # Конфигурация API Key и Rate Limiting
├── controller/
│   ├── ChatController.java         # Контроллер REST API для чата с GPT
│   └── RagController.java          # Контроллер REST API для RAG запросов
├── dto/
│   ├── ChatRequest.java            # Запрос для чата
│   ├── ChatResponse.java           # Ответ от чата
│   ├── RagRequest.java             # Запрос для RAG
│   ├── LoadDocumentsResponse.java  # Ответ загрузки документов
│   ├── DocumentInfo.java           # Информация о документе
│   ├── DocumentListResponse.java   # Список документов из ChromaDB
│   └── chroma/
│       ├── ChromaCollection.java   # DTO коллекции ChromaDB
│       └── ChromaGetResponse.java  # DTO ответа получения документов
├── exception/
│   ├── ErrorResponse.java          # DTO ответа об ошибке
│   ├── GlobalExceptionHandler.java # Глобальный обработчик исключений
│   └── JailbreakAttemptException.java # Исключение при jailbreak
├── filter/
│   ├── ApiKeyAuthFilter.java       # Фильтр аутентификации по API ключу
│   └── RateLimitFilter.java        # Фильтр ограничения частоты запросов
├── service/
│   ├── ChatService.java            # Сервис общения с OpenAI
│   ├── DocumentService.java        # Загрузка документов в ChromaDB
│   ├── InputValidationService.java # Валидация ввода и защита от jailbreak
│   └── RagService.java             # RAG: поиск + генерация ответа
├── utils/
│   ├── Constants.java              # Константы приложения
│   ├── JailbreakPatterns.java      # Паттерны для обнаружения jailbreak
│   └── SystemPrompts.java          # Системные промпты для AI
└── GptApplication.java             # Точка входа
```

## Как работает RAG

1. **Загрузка документов** — текстовые файлы из `resources/documents/` разбиваются на чанки и сохраняются в ChromaDB с векторными embeddings
2. **Поиск** — при запросе ищутся наиболее похожие чанки по семантическому сходству (top-5)
3. **Генерация** — найденный контекст передаётся в `GPT-4o` вместе с вопросом для формирования ответа

## Запуск

### 1. Настройка API ключа

Создайте файл `.env` в корне проекта:

```
# OpenAI API Key (обязательно)
OPENAI_API_KEY=sk-proj-your-api-key

# API Key аутентификация (опционально)
API_KEY_ENABLED=false
API_KEY=your-secret-api-key

# Rate Limiting (опционально)
RATE_LIMIT_RPM=60

# Token Limits (опционально)
MAX_TOKENS=2048

# Jailbreak Protection (опционально)
JAILBREAK_PROTECTION=true
MAX_INPUT_LENGTH=10000
```

### 2. Запуск ChromaDB

```bash
docker-compose up -d
```

### 3. Запуск приложения

**С помощью Maven:**
```bash
./mvnw spring-boot:run
```

**С помощью Makefile:**
```bash
make deploy    # Полное развертывание
make run       # Только запуск
make help      # Справка по командам
```

**С помощью скриптов:**
```bash
# Linux/macOS
./scripts/deploy.sh deploy

# Windows
scripts\deploy.bat deploy
```

Приложение запустится на `http://localhost:8080`
Swagger UI доступен на `http://localhost:8080/swagger-ui.html`

## API Endpoints

### Простой чат с GPT

```bash
POST /api/chat
Content-Type: application/json

{
  "message": "Привет! Напиши сортировку пузьком на Python и объясни как она работает.",
  "conversationId": null
}
```

**Ответ:**
```json
{
  "response": "Конечно! Вот сортировка пузырьком...",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000"
}
```

Для продолжения диалога передайте `conversationId` из предыдущего ответа.

### RAG: Загрузка документов

```bash
POST /api/rag/load
```

Загружает все `.txt` файлы из `resources/documents/` в ChromaDB.

### RAG: Список документов

```bash
GET /api/rag/documents?limit=100
```

Возвращает список документов из ChromaDB с метаданными.

**Ответ:**
```json
{
  "documents": [
    {
      "id": "doc-123-abc",
      "content": "Текст документа (до 500 символов)...",
      "metadata": {"source": "documents/swift.txt"}
    }
  ],
  "totalCount": 42,
  "collectionName": "documents"
}
```

### RAG: Вопрос по документам

```bash
POST /api/rag/query
Content-Type: application/json

{
  "question": "Как объявить переменную в Swift?"
}
```

## Примеры запросов

```bash
# Загрузить документы
curl -X POST http://localhost:8080/api/rag/load

# Получить список документов
curl http://localhost:8080/api/rag/documents?limit=50

# Задать вопрос по Swift
curl -X POST http://localhost:8080/api/rag/query \
  -H "Content-Type: application/json" \
  -d '{"question": "Что такое опционалы в Swift?"}'

# Простой чат (новый разговор)
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Напиши Hello World на Python"}'

# Продолжение разговора (с памятью)
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "А теперь на Java", "conversationId": "ваш-conversation-id"}'
```

## Конфигурация

`application.yaml`:

| Параметр | Описание | По умолчанию |
|----------|----------|--------------|
| `spring.ai.openai.api-key` | API ключ OpenAI | `${OPENAI_API_KEY}` |
| `spring.ai.openai.chat.options.model` | Модель GPT | `gpt-4o` |
| `spring.ai.openai.chat.options.temperature` | Креативность ответов | `0.7` |
| `spring.ai.vectorstore.chroma.client.base-url` | URL ChromaDB | `http://localhost:8000` |
| `spring.ai.vectorstore.chroma.collection-name` | Имя коллекции | `documents` |
| `app.security.enabled` | Включить API Key аутентификацию | `false` |
| `app.security.api-key` | API ключ для аутентификации | `${API_KEY}` |
| `app.rate-limit.requests-per-minute` | Лимит запросов в минуту | `60` |
| `spring.ai.openai.chat.options.max-tokens` | Максимум токенов в ответе | `2048` |
| `app.moderation.jailbreak-protection` | Защита от jailbreak атак | `true` |
| `app.moderation.max-input-length` | Максимальная длина ввода | `10000` |

## Добавление новых документов

1. Добавьте `.txt` файл в `src/main/resources/documents/`
2. Перезапустите приложение или вызовите `POST /api/rag/load`

## Безопасность

### API Key аутентификация

Для включения аутентификации установите в `.env`:

```
API_KEY_ENABLED=true
API_KEY=your-secret-key
```

Затем добавляйте заголовок `X-API-Key` к запросам:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-secret-key" \
  -d '{"message": "Hello"}'
```

### Rate Limiting

По умолчанию: 60 запросов в минуту на IP-адрес. Настраивается через `RATE_LIMIT_RPM`.

### Jailbreak Protection

Защита от попыток обхода системных инструкций модели:

- **Фильтрация паттернов** — блокировка известных jailbreak техник (DAN, ignore instructions, и т.д.)
- **Защищённые системные промпты** — инструкции модели не игнорировать ограничения
- **Лимит токенов** — ограничение длины ответов для контроля затрат

Настройка в `.env`:
```
JAILBREAK_PROTECTION=true    # Включить защиту (по умолчанию: true)
MAX_INPUT_LENGTH=10000       # Максимальная длина сообщения
MAX_TOKENS=2048              # Максимум токенов в ответе
```

При попытке jailbreak возвращается ошибка 400:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Potential jailbreak attempt detected. This incident has been logged."
}
```

Заголовки ответа:
- `X-Rate-Limit-Remaining` — оставшееся количество запросов
- `Retry-After` — время до сброса лимита (при превышении)

## Мониторинг

| Endpoint | Описание |
|----------|----------|
| `/actuator/health` | Состояние приложения |
| `/actuator/info` | Информация о приложении |
| `/actuator/metrics` | Метрики JVM и приложения |
| `/actuator/prometheus` | Метрики в формате Prometheus |

```bash
# Проверка здоровья
curl http://localhost:8080/actuator/health

# Метрики Prometheus
curl http://localhost:8080/actuator/prometheus
```

## Кэширование

RAG запросы кэшируются с помощью Caffeine:
- **Максимум:** 500 записей
- **TTL:** 10 минут
- Одинаковые вопросы возвращают кэшированный ответ

## Особенности

1. Документы поддерживают `Markdown` разметку для лучшей структуризации
2. Приложение поддерживает сохранение контекста разговора между запросами
3. Кэширование RAG запросов для быстрых повторных ответов
4. Rate Limiting для защиты от злоупотреблений
5. Prometheus метрики для мониторинга

## Ссылки

1. [Документация Spring AI](https://docs.spring.io/spring-ai/reference/index.html)
2. [Кэширование Caffeine](https://blog.devops.dev/easy-to-use-caffeine-cache-1-3db5861f6f39)

## Author - Maxim Kalugin
