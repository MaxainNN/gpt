.PHONY: help build clean test run docker-up docker-down deploy package

MVN = ./mvnw
DOCKER_COMPOSE = docker-compose

help:
	@echo "GPT Programming Assistant - commands:"
	@echo ""
	@echo "  make build       - Build without tests"
	@echo "  make test        - Run tests"
	@echo "  make clean       - Clean target"
	@echo "  make package     - Create jar"
	@echo "  make run         - Run application"
	@echo "  make docker-up   - Run Docker containers (ChromaDB)"
	@echo "  make docker-down - Stop Docker containers"
	@echo "  make deploy      - Deploy"
	@echo "  make health      - Healthcheck"
	@echo "  make metrics     - Metrics"
	@echo ""

build:
	$(MVN) compile -DskipTests

test:
	$(MVN) test

clean:
	$(MVN) clean

package:
	$(MVN) package -DskipTests

run:
	$(MVN) spring-boot:run

docker-up:
	$(DOCKER_COMPOSE) up -d

docker-down:
	$(DOCKER_COMPOSE) down

deploy: docker-up build run

health:
	@curl -s http://localhost:8080/actuator/health | python -m json.tool 2>/dev/null || curl -s http://localhost:8080/actuator/health

metrics:
	@curl -s http://localhost:8080/actuator/prometheus

restart: docker-down clean deploy

logs:
	$(DOCKER_COMPOSE) logs -f
