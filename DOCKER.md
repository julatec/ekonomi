# Ekonomi Docker Setup for Local Testing

This directory contains Docker Compose configuration for running Ekonomi's dependencies locally.

## Prerequisites

- Docker 20.10+
- Docker Compose 1.29+
- Maven 3.6+
- Java 11+

## Services

The Docker Compose setup includes:

1. **MySQL 8.0** - Multi-tenant database setup
   - Primary database: `ekonomi_primary`
   - Tenant databases: `ekonomi_julatec`, `ekonomi_tribuconta`
   - Port: 3306

2. **MailHog** - Email testing
   - SMTP server on port 1025
   - Web UI on http://localhost:8025

3. **Adminer** - Database management UI
   - Web UI on http://localhost:8080
   - System: MySQL
   - Server: mysql
   - Username: ekonomi
   - Password: ekonomi_password

## Quick Start

### 1. Initial Setup

```bash
# Copy environment file
cp .env.example .env

# Edit .env if you want to customize ports or credentials
vim .env

# Start all services
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f
```

### 2. Verify MySQL Setup

```bash
# Connect to MySQL
docker-compose exec mysql mysql -u ekonomi -pekonomi_password

# List databases
SHOW DATABASES;

# Expected output:
# - ekonomi_primary
# - ekonomi_julatec
# - ekonomi_tribuconta
```

### 3. Run Ekonomi Application

The application can run locally against the Docker services:

```bash
# Build the project
mvn clean install

# Run with Docker profile
cd webapp
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# Or run with IDE using profile: docker
```

### 4. Access Services

- **Application**: http://localhost:8080 (if running webapp)
- **Adminer**: http://localhost:8080 (database management)
- **MailHog**: http://localhost:8025 (email testing UI)

## Database Management

### Connect with MySQL Client

```bash
# From host machine
mysql -h 127.0.0.1 -P 3306 -u ekonomi -pekonomi_password ekonomi_primary

# From Docker container
docker-compose exec mysql mysql -u ekonomi -pekonomi_password ekonomi_primary
```

### Adminer Web UI

Navigate to http://localhost:8080 and use:
- **System**: MySQL
- **Server**: mysql
- **Username**: ekonomi
- **Password**: ekonomi_password
- **Database**: ekonomi_primary (or ekonomi_julatec, ekonomi_tribuconta)

### Backup Database

```bash
# Backup all databases
docker-compose exec mysql mysqldump -u root -proot_password --all-databases > backup.sql

# Backup specific database
docker-compose exec mysql mysqldump -u ekonomi -pekonomi_password ekonomi_julatec > ekonomi_julatec_backup.sql

# Restore database
docker-compose exec -T mysql mysql -u ekonomi -pekonomi_password ekonomi_julatec < ekonomi_julatec_backup.sql
```

## Email Testing with MailHog

MailHog captures all emails sent by the application:

1. Configure Spring Boot to use MailHog (already done in `application-docker.properties`)
2. Send emails from your application
3. View emails at http://localhost:8025

## Running Tests

```bash
# Run all tests
mvn clean test

# Run specific module tests
mvn clean test -pl storage
mvn clean test -pl tribunet-adapters

# Run with Docker profile (if tests need database)
mvn clean test -Dspring.profiles.active=docker
```

## Useful Commands

### Start services
```bash
docker-compose up -d
```

### Stop services
```bash
docker-compose stop
```

### Stop and remove containers
```bash
docker-compose down
```

### Remove containers and volumes (clean slate)
```bash
docker-compose down -v
```

### View logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f mysql
docker-compose logs -f mailhog
```

### Restart a service
```bash
docker-compose restart mysql
```

### Execute commands in containers
```bash
# MySQL shell
docker-compose exec mysql mysql -u root -proot_password

# Bash shell
docker-compose exec mysql bash
```

## Troubleshooting

### Port Already in Use

If you get port conflicts:

```bash
# Edit .env and change ports
MYSQL_PORT=3307
ADMINER_PORT=8081
MAILHOG_WEB_PORT=8026
```

### MySQL Connection Issues

```bash
# Check MySQL is healthy
docker-compose ps

# Check MySQL logs
docker-compose logs mysql

# Restart MySQL
docker-compose restart mysql

# Reset MySQL completely
docker-compose down -v
docker-compose up -d
```

### Database Not Initialized

If databases are missing:

```bash
# Check initialization logs
docker-compose logs mysql | grep -i init

# Manually run initialization
docker-compose exec mysql mysql -u root -proot_password < docker/mysql/init/01-init-databases.sql
```

### Clear All Data and Restart

```bash
# Stop and remove everything including volumes
docker-compose down -v

# Remove MySQL data volume
docker volume rm ekonomi-mysql-data

# Start fresh
docker-compose up -d
```

## Development Workflow

1. **Start Docker services**
   ```bash
   docker-compose up -d
   ```

2. **Run application with docker profile**
   ```bash
   cd webapp
   mvn spring-boot:run -Dspring-boot.run.profiles=docker
   ```

3. **Make code changes** - application will auto-reload (Spring DevTools)

4. **View emails** at http://localhost:8025

5. **Manage database** at http://localhost:8080 (Adminer)

6. **Stop services when done**
   ```bash
   docker-compose stop
   ```

## Configuration Files

- `docker-compose.yml` - Main Docker Compose configuration
- `.env` - Environment variables (create from .env.example)
- `docker/mysql/init/01-init-databases.sql` - Database initialization script
- `docker/mysql/conf.d/ekonomi.cnf` - MySQL configuration
- `webapp/src/main/resources/application-docker.properties` - Spring Boot Docker profile

## Multi-Tenant Testing

To test different tenants:

```sql
-- Connect to julatec tenant database
USE ekonomi_julatec;

-- Create test data
-- Your SQL here...

-- Connect to tribuconta tenant database
USE ekonomi_tribuconta;

-- Create test data
-- Your SQL here...
```

## CI/CD Integration

The Docker setup can be used in CI/CD pipelines:

```yaml
# Example GitHub Actions workflow
services:
  mysql:
    image: mysql:8.0
    env:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: ekonomi_primary
    options: >-
      --health-cmd "mysqladmin ping"
      --health-interval 10s
      --health-timeout 5s
      --health-retries 5
```

## Performance Tuning

For better local development performance, edit `docker/mysql/conf.d/ekonomi.cnf`:

```ini
innodb_buffer_pool_size = 512M  # Increase for better performance
max_connections = 500           # Increase if needed
```

Then restart MySQL:
```bash
docker-compose restart mysql
```

## Security Notes

⚠️ **Warning**: These credentials are for local development only. Never use these in production!

- Change all passwords in `.env`
- Use strong passwords in production
- Configure SSL/TLS for production databases
- Restrict network access in production

## Support

For issues or questions:
- Check logs: `docker-compose logs -f`
- GitHub Issues: https://github.com/julatec/ekonomi/issues
- Documentation: See project README.md
