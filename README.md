# GPT Programming Assistant

REST API приложение на Spring Boot с интеграцией OpenAI и RAG (Retrieval-Augmented Generation) для ответов на вопросы по программированию.

## Технологии

- **Java 21**
- **Spring Boot 3.5**
- **Spring AI 1.1.2** — интеграция с AI моделями
- **ChromaDB** — векторная база данных для RAG

## Архитектура

```
src/main/java/io/mkalugin/gpt/
├── config/
│   └── ChromaConfig.java         # Конфигурация ChromaDB и VectorStore
├── controller/
│   ├── ChatController.java       # REST API для чата с GPT
│   └── RagController.java        # REST API для RAG запросов
├── service/
│   ├── ChatService.java          # Сервис общения с OpenAI
│   ├── DocumentService.java      # Загрузка документов в ChromaDB
│   └── RagService.java           # RAG: поиск + генерация ответа
├── dto/
│   ├── ChatRequest.java          # Запрос для чата
│   ├── ChatResponse.java         # Ответ от чата
│   ├── RagRequest.java           # Запрос для RAG
│   └── LoadDocumentsResponse.java
└── GptApplication.java           # Точка входа
```

## Как работает RAG

1. **Загрузка документов** — текстовые файлы из `resources/documents/` разбиваются на чанки и сохраняются в ChromaDB с векторными embeddings
2. **Поиск** — при запросе ищутся наиболее похожие чанки по семантическому сходству (top-5)
3. **Генерация** — найденный контекст передаётся в GPT-4o вместе с вопросом для формирования ответа

## Запуск

### 1. Настройка API ключа

Создайте файл `.env` в корне проекта:

```
OPENAI_API_KEY=sk-proj-your-api-key
```

### 2. Запуск ChromaDB

```bash
docker-compose up -d
```

### 3. Запуск приложения

```bash
./mvnw spring-boot:run
```

Приложение запустится на `http://localhost:8080`

## API Endpoints

### Простой чат с GPT

```bash
POST /api/chat
Content-Type: application/json

{
  "message": "Привет! Как дела?"
}
```

### RAG: Загрузка документов

```bash
POST /api/rag/load
```

Загружает все `.txt` файлы из `resources/documents/` в ChromaDB.

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

# Задать вопрос по Swift
curl -X POST http://localhost:8080/api/rag/query \
  -H "Content-Type: application/json" \
  -d '{"question": "Что такое опционалы в Swift?"}'

# Простой чат
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Напиши Hello World на Python"}'
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

## Добавление новых документов

1. Добавьте `.txt` файл в `src/main/resources/documents/`
2. Перезапустите приложение или вызовите `POST /api/rag/load`

Документы поддерживают Markdown разметку для лучшей структуризации.
