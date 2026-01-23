#!/bin/bash

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

check_dependencies() {
    log_info "Checking dependencies..."

    if ! command -v java &> /dev/null; then
        log_error "Java is not installed. Please install JDK 21+"
        exit 1
    fi

    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 21 ]; then
        log_error "Java 21+ is required. Current version: $JAVA_VERSION"
        exit 1
    fi
    log_success "Java $JAVA_VERSION found"

    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed. Please install Docker"
        exit 1
    fi
    log_success "Docker found"

    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose is not installed"
        exit 1
    fi
    log_success "Docker Compose found"
}

check_env() {
    log_info "Checking environment configuration..."

    if [ ! -f "$PROJECT_DIR/.env" ]; then
        log_warning ".env file not found. Creating template..."
        cat > "$PROJECT_DIR/.env" << EOF
# OpenAI API Key (required)
OPENAI_API_KEY=sk-proj-your-api-key-here

# API Key Authentication (optional)
API_KEY_ENABLED=false
API_KEY=your-api-key-here

# Rate Limiting (optional)
RATE_LIMIT_RPM=60
EOF
        log_warning "Please edit .env file and add your OPENAI_API_KEY"
        exit 1
    fi

    if grep -q "sk-proj-your-api-key-here" "$PROJECT_DIR/.env"; then
        log_error "Please set your OPENAI_API_KEY in .env file"
        exit 1
    fi

    log_success "Environment configured"
}

start_docker() {
    log_info "Starting Docker containers..."
    cd "$PROJECT_DIR"
    docker-compose up -d

    log_info "Waiting for ChromaDB to be ready..."
    sleep 5

    if curl -s http://localhost:8000/api/v1/heartbeat > /dev/null 2>&1; then
        log_success "ChromaDB is running"
    else
        log_warning "ChromaDB may not be ready yet. Continuing..."
    fi
}

build_project() {
    log_info "Building project..."
    cd "$PROJECT_DIR"

    if [ -f "./mvnw" ]; then
        chmod +x ./mvnw
        ./mvnw clean package -DskipTests
    else
        mvn clean package -DskipTests
    fi

    log_success "Project built successfully"
}

start_app() {
    log_info "Starting application..."
    cd "$PROJECT_DIR"

    JAR_FILE=$(find target -name "*.jar" -type f | head -1)

    if [ -z "$JAR_FILE" ]; then
        log_error "JAR file not found. Run build first."
        exit 1
    fi

    log_info "Starting $JAR_FILE..."
    java -jar "$JAR_FILE" &
    APP_PID=$!

    log_info "Waiting for application to start..."
    sleep 10

    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        log_success "Application is running on http://localhost:8080"
        log_info "Swagger UI: http://localhost:8080/swagger-ui.html"
        log_info "Health: http://localhost:8080/actuator/health"
        log_info "Metrics: http://localhost:8080/actuator/prometheus"
    else
        log_warning "Application may still be starting..."
    fi

    echo ""
    log_info "Press Ctrl+C to stop the application"
    wait $APP_PID
}

stop_all() {
    log_info "Stopping all services..."
    cd "$PROJECT_DIR"
    pkill -f "gpt.*\.jar" || true
    docker-compose down
    log_success "All services stopped"
}

show_help() {
    echo "GPT Programming Assistant - Deployment Script"
    echo ""
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  deploy    - Full deployment (docker + build + run)"
    echo "  start     - Start application (assumes already built)"
    echo "  stop      - Stop all services"
    echo "  build     - Build project only"
    echo "  docker    - Start Docker containers only"
    echo "  status    - Check services status"
    echo "  help      - Show this help"
    echo ""
}

check_status() {
    log_info "Checking services status..."

    echo ""
    echo "Docker containers:"
    docker-compose ps 2>/dev/null || echo "Docker Compose not running"

    echo ""
    echo "Application health:"
    curl -s http://localhost:8080/actuator/health 2>/dev/null || echo "Application not running"
    echo ""
}

main() {
    case "${1:-deploy}" in
        deploy)
            check_dependencies
            check_env
            start_docker
            build_project
            start_app
            ;;
        start)
            check_env
            start_docker
            start_app
            ;;
        stop)
            stop_all
            ;;
        build)
            build_project
            ;;
        docker)
            start_docker
            ;;
        status)
            check_status
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            log_error "Unknown command: $1"
            show_help
            exit 1
            ;;
    esac
}

main "$@"
