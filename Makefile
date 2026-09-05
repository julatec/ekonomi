.PHONY: help up down restart logs status clean build test run-docker run-local db-shell db-backup db-restore adminer mailhog

# Default target
.DEFAULT_GOAL := help

# Colors for output
COLOR_RESET = \033[0m
COLOR_BOLD = \033[1m
COLOR_GREEN = \033[32m
COLOR_YELLOW = \033[33m
COLOR_BLUE = \033[34m

help: ## Show this help message
	@echo "$(COLOR_BOLD)Ekonomi - Docker Development Commands$(COLOR_RESET)"
	@echo ""
	@echo "$(COLOR_GREEN)Available targets:$(COLOR_RESET)"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(COLOR_BLUE)%-20s$(COLOR_RESET) %s\n", $$1, $$2}'
	@echo ""
	@echo "$(COLOR_YELLOW)Example usage:$(COLOR_RESET)"
	@echo "  make up            # Start all services"
	@echo "  make logs          # View logs"
	@echo "  make run-local     # Run app locally with Docker services"
	@echo ""

# Docker Compose Commands
up: ## Start all Docker services
	@echo "$(COLOR_GREEN)Starting Docker services...$(COLOR_RESET)"
	docker-compose up -d
	@echo "$(COLOR_GREEN)Services started successfully!$(COLOR_RESET)"
	@echo "  - MySQL:   localhost:3306"
	@echo "  - Adminer: http://localhost:8080"
	@echo "  - MailHog: http://localhost:8025"

down: ## Stop and remove all Docker services
	@echo "$(COLOR_YELLOW)Stopping Docker services...$(COLOR_RESET)"
	docker-compose down

restart: ## Restart all Docker services
	@echo "$(COLOR_YELLOW)Restarting Docker services...$(COLOR_RESET)"
	docker-compose restart

logs: ## View logs from all services
	docker-compose logs -f

status: ## Show status of all services
	@echo "$(COLOR_BLUE)Service Status:$(COLOR_RESET)"
	@docker-compose ps

clean: ## Remove all containers, volumes, and images
	@echo "$(COLOR_YELLOW)Warning: This will remove all data!$(COLOR_RESET)"
	@read -p "Are you sure? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		docker-compose down -v --rmi local; \
		echo "$(COLOR_GREEN)Cleanup complete!$(COLOR_RESET)"; \
	else \
		echo "$(COLOR_YELLOW)Cleanup cancelled.$(COLOR_RESET)"; \
	fi

# Build and Test Commands
build: ## Build the Maven project
	@echo "$(COLOR_GREEN)Building project...$(COLOR_RESET)"
	mvn clean install -DskipTests

build-full: ## Build with tests
	@echo "$(COLOR_GREEN)Building project with tests...$(COLOR_RESET)"
	mvn clean install

test: ## Run all tests
	@echo "$(COLOR_GREEN)Running tests...$(COLOR_RESET)"
	mvn clean test

test-adapters: ## Run tribunet-adapters tests
	@echo "$(COLOR_GREEN)Running tribunet-adapters tests...$(COLOR_RESET)"
	mvn clean test -pl tribunet-adapters

test-storage: ## Run storage tests
	@echo "$(COLOR_GREEN)Running storage tests...$(COLOR_RESET)"
	mvn clean test -pl storage

# Application Commands
dev-certs: ## Generate the local dev CA, server cert and client .p12 into docker/certs/
	@echo "$(COLOR_GREEN)Generating local development certificates...$(COLOR_RESET)"
	./docker/certs/generar-certs-dev.sh

certs-thot: ## Fetch the server cert and CA chain from the internal CA on thot
	@echo "$(COLOR_GREEN)Fetching server certificate from the internal CA...$(COLOR_RESET)"
	./docker/certs/traer-certs-thot.sh

dev-seed: ## Seed the local database with the dev user matching docker/certs/
	@echo "$(COLOR_GREEN)Seeding local dev user...$(COLOR_RESET)"
	docker exec -i ekonomi-mysql mysql -uekonomi -pekonomi_password < docker/mysql/seed-local.sql

# El perfil es `local`, no `docker`: nunca existio un application-docker.properties, y por eso
# este target no podia funcionar. Ver webapp/src/main/resources/application-local.properties.
run-local: up ## Run application locally against the Docker MySQL (mTLS on :8443)
	@echo "$(COLOR_GREEN)Starting on https://ekonomi.promyse.home.julatec.name:8443/ (needs make dev-certs + the /etc/hosts line)$(COLOR_RESET)"
	cd webapp && mvn spring-boot:run -Dspring-boot.run.profiles=local

run-docker: ## Run application in Docker container
	@echo "$(COLOR_GREEN)Starting application in Docker...$(COLOR_RESET)"
	docker-compose up -d webapp

# Database Commands
db-shell: ## Open MySQL shell
	@echo "$(COLOR_BLUE)Connecting to MySQL...$(COLOR_RESET)"
	docker-compose exec mysql mysql -u ekonomi -pekonomi_password ekonomi_primary

db-root-shell: ## Open MySQL shell as root
	@echo "$(COLOR_BLUE)Connecting to MySQL as root...$(COLOR_RESET)"
	docker-compose exec mysql mysql -u root -proot_password

db-backup: ## Backup all databases
	@echo "$(COLOR_GREEN)Creating database backup...$(COLOR_RESET)"
	@mkdir -p backups
	@docker-compose exec -T mysql mysqldump -u root -proot_password --all-databases > backups/backup_$$(date +%Y%m%d_%H%M%S).sql
	@echo "$(COLOR_GREEN)Backup saved to backups/$(COLOR_RESET)"

db-restore: ## Restore database (usage: make db-restore FILE=backup.sql)
	@if [ -z "$(FILE)" ]; then \
		echo "$(COLOR_YELLOW)Usage: make db-restore FILE=backup.sql$(COLOR_RESET)"; \
		exit 1; \
	fi
	@echo "$(COLOR_YELLOW)Restoring database from $(FILE)...$(COLOR_RESET)"
	docker-compose exec -T mysql mysql -u root -proot_password < $(FILE)
	@echo "$(COLOR_GREEN)Database restored!$(COLOR_RESET)"

db-reset: ## Reset all databases (WARNING: destroys all data)
	@echo "$(COLOR_YELLOW)Warning: This will destroy all database data!$(COLOR_RESET)"
	@read -p "Are you sure? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		docker-compose down -v; \
		docker-compose up -d mysql; \
		echo "$(COLOR_GREEN)Databases reset!$(COLOR_RESET)"; \
	else \
		echo "$(COLOR_YELLOW)Reset cancelled.$(COLOR_RESET)"; \
	fi

# Service Access Commands
adminer: ## Open Adminer in browser
	@echo "$(COLOR_BLUE)Opening Adminer...$(COLOR_RESET)"
	@open http://localhost:8080 || xdg-open http://localhost:8080 || echo "Please open http://localhost:8080 in your browser"

mailhog: ## Open MailHog in browser
	@echo "$(COLOR_BLUE)Opening MailHog...$(COLOR_RESET)"
	@open http://localhost:8025 || xdg-open http://localhost:8025 || echo "Please open http://localhost:8025 in your browser"

# Maintenance Commands
mysql-logs: ## View MySQL logs
	docker-compose logs -f mysql

mysql-restart: ## Restart MySQL service
	@echo "$(COLOR_YELLOW)Restarting MySQL...$(COLOR_RESET)"
	docker-compose restart mysql

mysql-stop: ## Stop MySQL service
	docker-compose stop mysql

mysql-start: ## Start MySQL service
	docker-compose start mysql

# Setup Commands
setup: ## Initial setup (copy .env and start services)
	@if [ ! -f .env ]; then \
		echo "$(COLOR_GREEN)Creating .env file from .env.example...$(COLOR_RESET)"; \
		cp .env.example .env; \
		echo "$(COLOR_YELLOW)Please review and edit .env if needed$(COLOR_RESET)"; \
	else \
		echo "$(COLOR_YELLOW).env file already exists$(COLOR_RESET)"; \
	fi
	@echo "$(COLOR_GREEN)Starting services...$(COLOR_RESET)"
	@$(MAKE) up
	@echo ""
	@echo "$(COLOR_GREEN)Setup complete!$(COLOR_RESET)"
	@echo "$(COLOR_BLUE)Next steps:$(COLOR_RESET)"
	@echo "  1. Check service status: make status"
	@echo "  2. Build project: make build"
	@echo "  3. Run application: make run-local"

# Development workflow
dev: setup build run-local ## Complete development setup and run

# Information
info: ## Show connection information
	@echo "$(COLOR_BOLD)Ekonomi Docker Services$(COLOR_RESET)"
	@echo ""
	@echo "$(COLOR_GREEN)Database Connections:$(COLOR_RESET)"
	@echo "  Host:     localhost"
	@echo "  Port:     3306"
	@echo "  Username: ekonomi"
	@echo "  Password: ekonomi_password"
	@echo "  Databases:"
	@echo "    - ekonomi_primary"
	@echo "    - ekonomi_julatec"
	@echo "    - ekonomi_tribuconta"
	@echo ""
	@echo "$(COLOR_GREEN)Web Interfaces:$(COLOR_RESET)"
	@echo "  Adminer:  http://localhost:8080"
	@echo "  MailHog:  http://localhost:8025"
	@echo ""
	@echo "$(COLOR_GREEN)JDBC URLs:$(COLOR_RESET)"
	@echo "  Primary:    jdbc:mysql://localhost:3306/ekonomi_primary"
	@echo "  Julatec:    jdbc:mysql://localhost:3306/ekonomi_julatec"
	@echo "  Tribuconta: jdbc:mysql://localhost:3306/ekonomi_tribuconta"
