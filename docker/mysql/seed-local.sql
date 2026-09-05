-- Usuario del ambiente LOCAL, para el certificado de docker/certs/.
--
-- Se aplica DESPUES del primer arranque, no en la inicializacion del contenedor: las tablas
-- las crea Hibernate al levantar la aplicacion con el perfil `local` (hbm2ddl.auto=update),
-- asi que en el init del contenedor todavia no existen.
--
-- Tampoco es un ApplicationRunner ni un @Component: AuthenticationService carga usuarios y
-- emisores UNA SOLA VEZ en su constructor, asi que un seeder de codigo correria tarde y el
-- usuario no estaria en el mapa. Es dato, no codigo — y asi no hay riesgo de que un seeder
-- viaje a produccion.
--
-- Aplicar con:  make dev-seed
USE ekonomi_primary;

-- El emisor se busca por el CN del emisor del certificado; `field` dice que RDN del sujeto
-- lleva el nombre de usuario. Ver AuthenticationService.loadUserDetails.
--
-- Dos emisores, y no es redundancia:
--   · `Ekonomi Dev CA`      — el `CN=dev` que generan los scripts. Lo usan curl y el proxy
--                             de Vite, que no pueden depender de un certificado que vence
--                             en 14 dias.
--   · `Julatec CA Clientes` — el certificado de persona que ya esta en el llavero del Mac,
--                             emitido por la CA interna y renovado solo por thot. Es el que
--                             ofrece el navegador.
--
-- 🔴 Esto vale para el ambiente LOCAL y solo para el. En produccion la CA interna NO puede
-- compartir truststore con la firma digital: AuthenticationService resuelve el emisor
-- comparando el CN como string, sin mirar huella ni serial, asi que quien tenga la llave de
-- una CA de ese truststore puede firmar un intermedio con CN=CA SINPE - PERSONA FISICA y
-- entrar como cualquier cedula. Aca no hay ninguna CA nacional en el truststore y la base es
-- desechable, por eso conviven.
INSERT INTO issuer (name, field) VALUES ('Ekonomi Dev CA', 'CN')
  ON DUPLICATE KEY UPDATE field = VALUES(field);
INSERT INTO issuer (name, field) VALUES ('Julatec CA Clientes', 'CN')
  ON DUPLICATE KEY UPDATE field = VALUES(field);

INSERT INTO user (username, displayName, email)
  VALUES ('dev', 'Desarrollo local', 'dev@localhost')
  ON DUPLICATE KEY UPDATE displayName = VALUES(displayName);

-- Las dos identidades del mismo usuario local. El `value` es el CN del SUJETO, que es lo
-- que dice `field='CN'` del emisor correspondiente.
DELETE FROM user_ids WHERE user_username = 'dev';
INSERT INTO user_ids (user_username, issuer, value) VALUES
  ('dev', 'Ekonomi Dev CA',      'dev'),
  ('dev', 'Julatec CA Clientes', '5-0359-0732');

-- Con method security activo, un usuario sin roles queda con 403 en /.
DELETE FROM user_roles WHERE user_username = 'dev';
INSERT INTO user_roles (user_username, roles) VALUES ('dev', 'ROLE_ADMIN'), ('dev', 'ROLE_USER');

-- Los tenants que puede abrir. La cookie `tenant` se valida contra esta lista.
DELETE FROM user_datasources WHERE user_username = 'dev';
INSERT INTO user_datasources (user_username, datasources) VALUES ('dev', 'julatec'), ('dev', 'tribuconta');

SELECT 'usuario dev listo' AS estado,
       (SELECT COUNT(*) FROM user_ids WHERE user_username = 'dev') AS certificados,
       (SELECT COUNT(*) FROM user_roles WHERE user_username = 'dev') AS roles,
       (SELECT COUNT(*) FROM user_datasources WHERE user_username = 'dev') AS tenants;

-- La columna del XML: Hibernate la crea como tinytext (255 bytes) y un comprobante real son
-- ~29 KB, asi que el INSERT falla con el error 1406 en vez de truncar. La anotacion ya lleva
-- `length = Integer.MAX_VALUE`, pero `hbm2ddl.auto=update` NO altera el tipo de una columna
-- que ya existe: solo crea las que faltan. Sobre un esquema ya creado hay que ensancharla a
-- mano, y por eso vive aca y no en la anotacion sola.
USE ekonomi_julatec;
ALTER TABLE factura              MODIFY COLUMN document longtext;
ALTER TABLE factura_compra       MODIFY COLUMN document longtext;
ALTER TABLE factura_exportacion  MODIFY COLUMN document longtext;
ALTER TABLE nota_credito         MODIFY COLUMN document longtext;
ALTER TABLE nota_debito          MODIFY COLUMN document longtext;
