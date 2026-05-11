INSERT INTO mesas (id, nombre, capacidad, zona, estado, activo) VALUES
(CAST(RANDOM_UUID() AS VARCHAR(36)), 'Mesa 1', 4, 'Terraza principal', 'LIBRE', true),
(CAST(RANDOM_UUID() AS VARCHAR(36)), 'Mesa 2', 2, 'Interior', 'OCUPADA', true),
(CAST(RANDOM_UUID() AS VARCHAR(36)), 'Mesa 3', 6, 'VIP', 'OCUPADA', true),
(CAST(RANDOM_UUID() AS VARCHAR(36)), 'Mesa 4', 4, 'Interior', 'INACTIVA', false),
(CAST(RANDOM_UUID() AS VARCHAR(36)), 'Mesa 5', 8, 'Terraza secundaria', 'POR_PAGAR', true),
(CAST(RANDOM_UUID() AS VARCHAR(36)), 'Mesa 6', 4, 'Barra', 'LIBRE', true);