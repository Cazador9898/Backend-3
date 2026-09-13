CREATE TABLE IF NOT EXISTS transacciones_procesadas (
    id BIGINT PRIMARY KEY,
    fecha DATE NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    anomalia BOOLEAN NOT NULL,
    observacion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS intereses_procesados (
    cuenta_id BIGINT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    saldo_inicial DECIMAL(15,2) NOT NULL,
    edad INT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    tasa DECIMAL(10,4) NOT NULL,
    interes_calculado DECIMAL(15,2) NOT NULL,
    saldo_final DECIMAL(15,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS movimientos_anuales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cuenta_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    transaccion VARCHAR(30) NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS estados_cuenta_anuales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cuenta_id BIGINT NOT NULL,
    anio INT NOT NULL,
    total_depositos DECIMAL(15,2) NOT NULL,
    total_retiros_compras DECIMAL(15,2) NOT NULL,
    saldo_neto DECIMAL(15,2) NOT NULL,
    cantidad_movimientos INT NOT NULL,
    UNIQUE KEY uk_cuenta_anio (cuenta_id, anio)
);

CREATE TABLE IF NOT EXISTS resumen_transacciones_diarias (
    fecha DATE PRIMARY KEY,
    total_transacciones INT NOT NULL,
    cantidad_anomalias INT NOT NULL,
    monto_total DECIMAL(15,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS atm_operaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cuenta_id BIGINT NOT NULL,
    atm_id VARCHAR(50) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    saldo_resultante DECIMAL(15,2) NOT NULL,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
