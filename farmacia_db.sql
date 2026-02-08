-- CONFIGURACIÓN INICIAL
SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

-- 1. CREAR TABLA CATEGORIAS
CREATE TABLE public.categorias (
    id_categoria SERIAL PRIMARY KEY,
    nombre character varying(100)
);

-- 2. CREAR TABLA CORTES DE CAJA
CREATE TABLE public.cortes_caja (
    id_corte SERIAL PRIMARY KEY,
    fecha_inicio timestamp without time zone,
    fecha_fin timestamp without time zone,
    total_ventas numeric(10,2)
);

-- 3. CREAR TABLA MEDICAMENTOS
CREATE TABLE public.medicamentos (
    id_medicamento SERIAL PRIMARY KEY,
    nombre character varying(150),
    id_categoria integer NOT NULL,
    precio numeric(10,2),
    stock integer,
    stock_minimo integer,
    fecha_vencimiento date
);

-- 4. CREAR TABLA VENTAS
CREATE TABLE public.ventas (
    id_venta SERIAL PRIMARY KEY,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    total numeric(10,2),
    id_corte integer NOT NULL
);

-- 5. CREAR TABLA DETALLE VENTA
CREATE TABLE public.detalle_venta (
    id_detalle SERIAL PRIMARY KEY,
    id_venta integer NOT NULL,
    id_medicamento integer NOT NULL,
    cantidad integer,
    precio_unitario numeric(10,2),
    subtotal numeric(10,2)
);

-- 6. CREAR TABLA MOVIMIENTOS INVENTARIO
CREATE TABLE public.movimientos_inventario (
    id_movimiento SERIAL PRIMARY KEY,
    id_medicamento integer NOT NULL,
    tipo character varying(10),
    cantidad integer,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    motivo character varying(255)
);

-- 7. AGREGAR LAS RELACIONES (FOREIGN KEYS)

-- Categorías en Medicamentos
ALTER TABLE ONLY public.medicamentos
    ADD CONSTRAINT fk_medicamento_categoria FOREIGN KEY (id_categoria) REFERENCES public.categorias(id_categoria);

-- Cortes en Ventas
ALTER TABLE ONLY public.ventas
    ADD CONSTRAINT fk_venta_corte FOREIGN KEY (id_corte) REFERENCES public.cortes_caja(id_corte);

-- Ventas en Detalle
ALTER TABLE ONLY public.detalle_venta
    ADD CONSTRAINT fk_detalle_venta FOREIGN KEY (id_venta) REFERENCES public.ventas(id_venta);

-- Medicamentos en Detalle
ALTER TABLE ONLY public.detalle_venta
    ADD CONSTRAINT fk_detalle_medicamento FOREIGN KEY (id_medicamento) REFERENCES public.medicamentos(id_medicamento);

-- Medicamentos en Movimientos
ALTER TABLE ONLY public.movimientos_inventario
    ADD CONSTRAINT fk_movimiento_medicamento FOREIGN KEY (id_medicamento) REFERENCES public.medicamentos(id_medicamento);