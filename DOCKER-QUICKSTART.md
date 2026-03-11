# Docker Quick Start Guide

## 🚀 Get Started in 3 Steps

### 1. Initial Setup
```bash
make setup
```
This will:
- Create `.env` file with default configuration
- Start MySQL, MailHog, and Adminer services
- Initialize multi-tenant databases

### 2. Build Project
```bash
make build
```

### 3. Run Application
```bash
make run-local
```

Your application will start with the `docker` profile, connecting to the containerized services.

---

## 📋 Quick Commands

| Command | Description |
|---------|-------------|
| `make up` | Start all Docker services |
| `make down` | Stop all services |
| `make logs` | View service logs |
| `make status` | Check service status |
| `make info` | Show connection details |

## 🔧 Development Commands

| Command | Description |
|---------|-------------|
| `make build` | Build project (skip tests) |
| `make test` | Run all tests |
| `make run-local` | Run app with Docker services |
| `make dev` | Setup + Build + Run |

## 🗄️ Database Commands

| Command | Description |
|---------|-------------|
| `make db-shell` | Open MySQL CLI |
| `make db-backup` | Backup all databases |
| `make db-reset` | Reset databases (⚠️ destroys data) |

## 🌐 Service URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| **Adminer** | http://localhost:8080 | User: `ekonomi`<br>Pass: `ekonomi_password` |
| **MailHog** | http://localhost:8025 | No auth required |
| **MySQL** | localhost:3306 | User: `ekonomi`<br>Pass: `ekonomi_password` |

## 💾 Databases

The setup creates three databases:

1. **ekonomi_primary** - Main security/auth database
2. **ekonomi_julatec** - Julatec tenant database
3. **ekonomi_tribuconta** - Tribuconta tenant database

## 🔌 JDBC Connection Strings

```properties
# Primary
jdbc:mysql://localhost:3306/ekonomi_primary?useSSL=false&serverTimezone=America/Costa_Rica

# Julatec
jdbc:mysql://localhost:3306/ekonomi_julatec?useSSL=false&serverTimezone=America/Costa_Rica

# Tribuconta
jdbc:mysql://localhost:3306/ekonomi_tribuconta?useSSL=false&serverTimezone=America/Costa_Rica
```

## 📧 Email Testing

All emails sent by the application are captured by MailHog:

1. Application sends email → MailHog SMTP (port 1025)
2. View emails → http://localhost:8025

## 🔍 Troubleshooting

### Services won't start?
```bash
# Check what's using the ports
lsof -i :3306
lsof -i :8080

# Or change ports in .env
vim .env
```

### Database connection refused?
```bash
# Check MySQL is healthy
make status

# Restart MySQL
make mysql-restart
```

### Need to start fresh?
```bash
# Reset everything (destroys data!)
make clean
make setup
```

## 📚 More Help

- Full documentation: [DOCKER.md](DOCKER.md)
- All commands: `make help`
- Connection info: `make info`

## ⚡ Pro Tips

1. **Keep services running**: Leave Docker services running in the background
   ```bash
   make up
   # Work with your IDE
   ```

2. **Quick iteration**: Hot reload is enabled in the application
   ```bash
   make run-local
   # Edit code, changes auto-reload
   ```

3. **Multiple terminals**:
   - Terminal 1: `make logs` (watch logs)
   - Terminal 2: Development work
   - Terminal 3: Database queries

4. **Use Adminer**: Better than command line for exploring data
   ```bash
   make adminer  # Opens in browser
   ```

5. **Database backups**: Backup before major changes
   ```bash
   make db-backup
   ```

## ⚙️ Configuration

Edit `.env` to customize:
- Database credentials
- Service ports
- Application settings

See `.env.example` for all available options.

---

**Need more help?** See [DOCKER.md](DOCKER.md) for detailed documentation.
