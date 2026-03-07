-- Borrar la tabla si ya existe para evitar errores al reiniciar
DROP TABLE IF EXISTS mesas;

-- Creación de la tabla con los mismos campos de la Entidad
CREATE TABLE mesas (
    id UUID PRIMARY KEY,
    numero INT NOT NULL UNIQUE,
    capacidad INT NOT NULL,
    estado VARCHAR(20) NOT NULL
);