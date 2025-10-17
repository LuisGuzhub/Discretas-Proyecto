# Calculadora de Álgebra Booleana  
**Autor:** Luis Guzmán – ESPOL  

Este proyecto consiste en una calculadora de lógica proposicional desarrollada en Java utilizando Swing.  
Su propósito principal es servir como herramienta académica para el análisis de expresiones lógicas mediante la generación de tablas de verdad, la comprobación de equivalencia lógica y la simplificación algebraica en forma normal disyuntiva (DNF).

La aplicación no requiere librerías externas y ha sido implementada con un enfoque educativo, manteniendo claridad en la lógica interna y legibilidad en el código.

---

<p align="center">
  <img 
    src="https://github.com/user-attachments/assets/f4877b92-d438-4ba4-ad6f-59e6ceb7a42a"
    alt="Interfaz de la Calculadora"
    width="720">
</p>

---

## 1. Objetivos del proyecto

- Facilitar el estudio del álgebra booleana y la lógica proposicional.
- Implementar un parser propio que interprete expresiones con distintos operadores.
- Permitir la verificación práctica de tautologías, contradicciones y equivalencias.
- Mostrar procesos de simplificación aplicando leyes lógicas paso a paso.

---

## 2. Características principales

- Generación automática de **tablas de verdad** (detección de variables A–Z).
- Verificación de **equivalencia lógica** entre dos proposiciones.
- **Simplificación algebraica** con registro de transformaciones (De Morgan, Idempotencia, Absorción, Consenso).
- Interfaz gráfica con resaltado de discrepancias.
- Soporte para diferentes formas de escritura (`AND`, `OR`, `~`, `->`, `<->`, etc.).

---

## 3. Requisitos y ejecución

**Requisito:** Java 8 o superior

```bash
# Compilar
javac BooleanCalculator.java

# Ejecutar
java BooleanCalculator
```
## 4. Sintaxis y operadores permitidos
```
| Operador | Alternativas | Descripción          |            |
| :------: | :----------- | :------------------- | ---------- |
|    `~`   | `!`, `NOT`   | Negación             |            |
|    `&`   | `AND`        | Conjunción           |            |
|     `    | `            | `OR`                 | Disyunción |
|    `^`   | `XOR`        | Disyunción exclusiva |            |
|    `>`   | `->`         | Implicación          |            |
|    `=`   | `<->`, `IFF` | Bicondicional        |            |
```
Constantes: 1, 0, true, false, T, F
## 5. Ejemplos de uso
```
| Expresión             | Clasificación       |              |
| --------------------- | ------------------- | ------------ |
| `A                    | ~A`                 | Tautología   |
| `A & ~A`              | Contradicción       |              |
| `A > B` vs `~A        | B`                  | Equivalentes |
| `(A & B) \| (A & ~B)` | Se simplifica a `A` |              |
```
## 6. Estructura del proyecto
```
BooleanCalculator
 ├─ Interfaz gráfica (Swing)
 ├─ Generación de tabla y eventos
 └─ Módulo de equivalencias y simplificación

BooleanExpression
 ├─ Parser de expresiones
 ├─ Evaluador con precedencia
 └─ Manejo de variables y constantes

AlgebraicSimplifier
 ├─ Conversión a NNF (De Morgan)
 ├─ Distributiva / Idempotencia
 └─ Absorción y Consenso (DNF)

```
## 7. Posibles mejoras
- Exportación de tablas (CSV / PDF)
- Integración de mapas de Karnaugh
- Algoritmo de minimización (Quine–McCluskey)
- Modo oscuro y configuración visual

## 8. Uso académico
Este proyecto puede emplearse en asignaturas relacionadas con:
- Lógica y Computabilidad
- Matemáticas Discretas
- Diseño de Circuitos Digitales
- Fundamentos de Programación
Está desarrollado con fines educativos, priorizando la comprensión del funcionamiento interno sobre la dependencia de bibliotecas externas.

<p align="center">
  <strong>Autor:</strong><br>
  Luis Guzmán<br>
  Escuela Superior Politécnica del Litoral (ESPOL)
</p>
