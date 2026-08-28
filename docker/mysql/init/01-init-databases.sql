-- Ekonomi Multi-tenant Database Initialization
-- This script sets up the databases for the multi-tenant ekonomi application

-- Create databases for each tenant
CREATE DATABASE IF NOT EXISTS `ekonomi_primary` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `ekonomi_julatec` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `ekonomi_tribuconta` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- El usuario lo crea la propia imagen de MySQL a partir de MYSQL_USER y
-- MYSQL_PASSWORD del compose, antes de ejecutar este script. No se repite
-- acá para no dejar una contraseña literal en el repositorio.

-- Grant privileges
GRANT ALL PRIVILEGES ON `ekonomi_primary`.* TO 'ekonomi'@'%';
GRANT ALL PRIVILEGES ON `ekonomi_julatec`.* TO 'ekonomi'@'%';
GRANT ALL PRIVILEGES ON `ekonomi_tribuconta`.* TO 'ekonomi'@'%';

-- Apply privileges
FLUSH PRIVILEGES;

-- Log completion
SELECT 'Ekonomi databases initialized successfully' AS message;
