# EcoRent North – Sistema de gestión de alquileres

Sistema full‑stack para la gestión de alquiler de equipos de EcoRent Norte S.L.  
Permite administrar clientes, catálogo de equipos, contratos de alquiler, devoluciones, pagos e informes, a través de una API REST en Spring Boot y un frontend en React.

---

## Tabla de contenidos

1. [Arquitectura del proyecto](#arquitectura-del-proyecto)  
2. [Tecnologías principales](#tecnologías-principales)  
3. [Requisitos previos](#requisitos-previos)  
4. [Instalación y puesta en marcha rápida](#instalación-y-puesta-en-marcha-rápida)  
   - [Backend (Spring Boot)](#backend-spring-boot)  
   - [Frontend (React + Vite)](#frontend-react--vite)  
5. [Configuración técnica](#configuración-técnica)  
   - [Base de datos H2](#base-de-datos-h2)  
   - [Seguridad y CORS](#seguridad-y-cors)  
   - [Documentación de la API (Swagger)](#documentación-de-la-api-swagger)  
6. [Estructura del proyecto](#estructura-del-proyecto)  
7. [Casos de uso funcionales](#casos-de-uso-funcionales)  
   - [Gestión de clientes](#gestión-de-clientes)  
   - [Gestión de equipos](#gestión-de-equipos)  
   - [Gestión de alquileres](#gestión-de-alquileres)  
   - [Gestión de pagos](#gestión-de-pagos)  
   - [Informes y dashboard](#informes-y-dashboard)  
8. [Scripts útiles](#scripts-útiles)  
9. [Despliegue y notas para producción](#despliegue-y-notas-para-producción)  
10. [Resolución de problemas comunes](#resolución-de-problemas-comunes)  
11. [Licencia](#licencia)

---

## Arquitectura del proyecto

El sistema está dividido en dos partes claramente separadas:

- **Backend** (`src/main/java/com/ecorent/gestionalquileres`):
  - API REST con Spring Boot 3 (Java 21).
  - Capa de persistencia con Spring Data JPA.
  - Base de datos **H2** en modo fichero (`./data/ecorentdb`).
  - Validación de datos con Bean Validation.
  - Documentación de la API con **springdoc-openapi** (Swagger UI).
  - Configuración de seguridad y CORS con Spring Security.

- **Frontend** (`frontend/`):
  - SPA en **React** (create-vite) con Vite como bundler.
  - Consumo de la API vía **Axios**, usando la ruta base `/api`.
  - Proxy de desarrollo de Vite hacia el backend (`http://localhost:8080`).
  - Vistas específicas para:
    - Dashboard
    - Clientes
    - Equipos
    - Alquileres
    - Pagos
    - Informes

La comunicación entre frontend y backend se hace siempre a través de `/api`, que en desarrollo es redirigido por Vite al backend de Spring Boot.

---

## Tecnologías principales

**Backend**

- Java **21**
- Spring Boot **3.2.5**
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - spring-boot-starter-validation
  - spring-boot-starter-security
- Base de datos **H2** (modo fichero)
- SpringDoc OpenAPI (`springdoc-openapi-starter-webmvc-ui`)
- Lombok
- Maven (con **maven wrapper** incluido: `mvnw`, `mvnw.cmd`)

**Frontend**

- React **19**
- Vite **7**
- Axios
- ESLint + reglas básicas para React

---

## Requisitos previos

Asegúrate de tener instalado:

- **Java JDK 21**  
  Comprobar versión:
  ```bash
  java -version
  ```
- **Maven 3.9+** (opcional si usas el wrapper `mvnw` del proyecto)  
  Comprobar versión:
  ```bash
  mvn -version
  ```
- **Node.js 18+** y **npm**  
  Comprobar versión:
  ```bash
  node -v
  npm -v
  ```

Opcional:

- Git, si clonas el repositorio desde un remoto.
- Un IDE para Java (IntelliJ / VSCode / Eclipse) y otro para el frontend si se desea.

---

## Instalación y puesta en marcha rápida

Clona o descarga este repositorio y sitúate en la carpeta raíz:

```bash
cd gestionalquileres
```

### Backend (Spring Boot)

Desde la raíz del proyecto:

#### Usando Maven Wrapper (recomendado, multiplataforma)

- **Windows**:
  ```bash
  mvnw.cmd spring-boot:run
  ```

- **Linux / macOS**:
  ```bash
  ./mvnw spring-boot:run
  ```

#### Usando Maven instalado en el sistema

```bash
mvn spring-boot:run
```

Por defecto, el backend arrancará en:

- `http://localhost:8080`

### Frontend (React + Vite)

En otra terminal, desde la raíz del proyecto:

```bash
cd frontend
npm install
npm run dev
```

Por defecto, Vite arrancará en:

- `http://localhost:5173`

El archivo `vite.config.js` ya está configurado para **proxificar** todas las peticiones a `/api` hacia `http://localhost:8080`, por lo que solo necesitas tener **ambos servidores** levantados:

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5173`

---

## Configuración técnica

### Base de datos H2

La configuración de la base de datos se encuentra en `src/main/resources/application.properties`:

```properties
server.port=8080

spring.datasource.url=jdbc:h2:file:./data/ecorentdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

Puntos clave:

- **Base de datos en fichero**: `./data/ecorentdb` (se crea automáticamente).
- **Usuario H2** por defecto:
  - user: `sa`
  - password: *(vacío)*
- **H2 Console** activada en desarrollo:
  - URL: `http://localhost:8080/h2-console`
  - JDBC URL (en el formulario): `jdbc:h2:file:./data/ecorentdb`

### Seguridad y CORS

La configuración básica de seguridad está en  
`src/main/java/com/ecorent/gestionalquileres/config/SecurityConfig.java`.

Aspectos importantes:

- **CSRF desactivado** para simplificar en desarrollo.
- **H2 Console** permitida sin autenticación:
  ```java
  .requestMatchers("/h2-console/**").permitAll()
  .anyRequest().permitAll()
  ```
- **Todas las peticiones** están actualmente permitidas (`permitAll`), es decir, **no hay autenticación de usuarios** todavía.
- CORS configurado para permitir peticiones desde el frontend en desarrollo:
  ```java
  configuration.setAllowedOrigins(List.of("http://localhost:5173"));
  configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
  configuration.setAllowedHeaders(List.of("*"));
  ```

### Documentación de la API (Swagger)

Se utiliza **springdoc-openapi** para generar documentación de la API REST.

Una vez levantado el backend, la UI interactiva suele estar disponible en:

- `http://localhost:8080/swagger-ui.html`  
  (redirige a `/swagger-ui/index.html`)

Desde ahí se pueden explorar endpoints y probar peticiones directamente.

---

## Estructura del proyecto

Resumen de las carpetas más relevantes:

```text
gestionalquileres/
├── pom.xml                      # Configuración Maven del backend
├── mvnw / mvnw.cmd              # Maven Wrapper
├── src/
│   └── main/
│       ├── java/com/ecorent/gestionalquileres/
│       │   ├── GestionalquileresApplication.java   # Clase main de Spring Boot
│       │   ├── config/                             # Configuración (seguridad, CORS, etc.)
│       │   ├── controller/                         # Controladores REST (Clientes, Equipos, Alquileres, Pagos, Informes)
│       │   ├── dto/                                # DTOs de entrada/salida
│       │   ├── entity/                             # Entidades JPA (Client, Equipment, Rental, Payment...)
│       │   ├── exception/                          # Gestión de errores de negocio y globales
│       │   ├── repository/                         # Repositorios JPA
│       │   └── service/                            # Lógica de negocio
│       └── resources/
│           ├── application.properties              # Configuración de Spring Boot
│           ├── static/                             # Recursos estáticos (si se usan)
│           └── templates/                          # Vistas (no utilizadas en este frontend React)
│
├── frontend/
│   ├── package.json              # Dependencias y scripts del frontend
│   ├── vite.config.js            # Configuración de Vite + proxy /api
│   └── src/
│       ├── main.jsx              # Punto de entrada de React
│       ├── App.jsx               # Layout principal y navegación entre vistas
│       ├── api/axiosConfig.js    # Configuración de Axios (baseURL: /api)
│       ├── components/
│       │   ├── layout/           # Header, Sidebar, Card, etc.
│       │   ├── dashboard/        # Dashboard.jsx
│       │   ├── client/           # ClientView.jsx
│       │   ├── equipment/        # EquipmentView.jsx
│       │   ├── rental/           # RentalView.jsx
│       │   ├── payment/          # PaymentSection.jsx
│       │   └── report/           # ReportsView.jsx
│       └── styles/               # global.css, App.css, etc.
└── data/
    └── ecorentdb.mv.db           # Fichero de la base de datos H2 (creado en runtime)
```

---

## Casos de uso funcionales

A nivel de negocio, EcoRent North permite:

### Gestión de clientes

Módulo asociado a `ClientView` en el frontend y a `ClientController` en el backend.

Casos de uso típicos:

- **Crear cliente**: alta de nuevos clientes con sus datos básicos de contacto.
- **Listar clientes**: visualizar todos los clientes registrados.
- **Editar cliente**: actualizar la información de un cliente existente.
- **Eliminar cliente**: dar de baja clientes que ya no se utilizan.
- **Consulta de detalle**: ver la ficha individual de un cliente (opcional según implementación concreta).

Estos casos de uso sientan la base para asociar clientes a contratos de alquiler.

### Gestión de equipos

Módulo asociado a `EquipmentView` y `EquipmentController`.

Casos de uso típicos:

- **Registrar equipo**: alta de nuevos equipos/maquinaria disponibles para alquilar.
- **Listar equipos**: ver el inventario completo.
- **Actualizar equipo**: modificar datos como descripción, precio de alquiler, etc.
- **Cambiar estado**: gestionar el estado del equipo (por ejemplo, disponible, alquilado, fuera de servicio, etc.).
- **Desactivar / eliminar equipo**: retirar equipos que ya no estén operativos.

El estado de los equipos es clave para determinar qué se puede alquilar en cada momento.

### Gestión de alquileres

Módulo asociado a `RentalView` y `RentalController`.

Casos de uso típicos:

- **Crear un nuevo alquiler**:
  - Seleccionar cliente.
  - Seleccionar uno o varios equipos.
  - Definir fechas de inicio/fin previstas.
  - Calcular importe estimado según tarifas.
- **Listar alquileres activos**:
  - Ver qué equipos están actualmente alquilados y por quién.
- **Gestionar devoluciones**:
  - Registrar la devolución efectiva del equipo.
  - Actualizar el estado del equipo a disponible.
  - Generar información de retorno (`RentalReturnResponse`).
- **Histórico de alquileres**:
  - Consultar alquileres finalizados, totales facturados, etc.

### Gestión de pagos

Módulo asociado a `PaymentSection` y `PaymentController`.

Casos de uso típicos:

- **Registrar un pago**:
  - Asociar el pago a un alquiler o a una factura concreta.
  - Registrar importe, fecha, método de pago, etc.
- **Listar pagos**:
  - Ver todos los pagos registrados.
- **Consultar pagos por alquiler/cliente**:
  - Comprender qué está cobrado y qué pendiente.

### Informes y dashboard

Módulos asociados a `ReportsView`, `Dashboard` y `ReportController`.

Apoyados en DTOs como:

- `IncomeReportResponse` – para informes de ingresos (por período, por ejemplo).
- `TopClientResponse` – para identificar los mejores clientes.
- `TopEquipmentResponse` – para ver qué equipos se alquilan más.

Casos de uso típicos:

- **Dashboard**:
  - Ver métricas clave al entrar en la aplicación (nº de clientes, equipos disponibles, alquileres activos, etc., según implementación).
  - Acceso rápido a las principales vistas (clientes, equipos, alquileres, informes, pagos).

- **Informes**:
  - **Informe de ingresos**: ingresos totales en un rango de fechas.
  - **Top clientes**: clientes con mayor volumen de negocio.
  - **Top equipos**: equipos más demandados.
  - Otros informes derivados según necesidades futuras.

---

## Scripts útiles

### Backend

Desde la raíz del proyecto:

- Ejecutar la aplicación:
  ```bash
  ./mvnw spring-boot:run          # Linux / macOS
  mvnw.cmd spring-boot:run        # Windows
  ```
- Ejecutar tests:
  ```bash
  ./mvnw test
  ```
- Generar artefacto `jar`:
  ```bash
  ./mvnw clean package
  ```

*(Sustituye `./mvnw` por `mvn` si prefieres usar Maven instalado en el sistema.)*

### Frontend

Desde la carpeta `frontend/`:

- Instalar dependencias:
  ```bash
  npm install
  ```
- Arrancar entorno de desarrollo:
  ```bash
  npm run dev
  ```
- Build de producción:
  ```bash
  npm run build
  ```
- Previsualizar build:
  ```bash
  npm run preview
  ```
- Linter:
  ```bash
  npm run lint
  ```

---

## Despliegue y notas para producción

Actualmente la configuración está orientada a **desarrollo**:

- Base de datos **H2 en fichero**.
- Seguridad muy laxa (`permitAll()` para todas las peticiones).
- CORS solo preparado para `http://localhost:5173`.
- Frontend servido por Vite en modo dev.

Para un entorno de producción se recomienda:

1. Cambiar H2 por una base de datos de producción (PostgreSQL, MySQL, etc.).
2. Configurar usuarios/roles reales en Spring Security.
3. Restringir CORS a los dominios de frontend oficiales.
4. Servir el frontend como estático (por ejemplo, empaquetando el build de React dentro del mismo Spring Boot o en un servidor web aparte).
5. Definir perfiles de Spring (`application-dev.properties`, `application-prod.properties`, ...).

---

## Resolución de problemas comunes

- **El puerto 8080 ya está en uso**  
  Otro servicio está usando ese puerto. Cambia el puerto en `application.properties`:
  ```properties
  server.port=8081
  ```
  y actualiza el proxy de Vite en `frontend/vite.config.js`:
  ```js
  proxy: {
    '/api': {
      target: 'http://localhost:8081',
      changeOrigin: true
    }
  }
  ```

- **El frontend no puede conectar con la API**  
  Verifica:
  - Que el backend está levantado en el puerto correcto.
  - Que en `frontend/src/api/axiosConfig.js` la `baseURL` sea `/api`.
  - Que el proxy de Vite apunta al mismo host/puerto que el backend.

- **No puedo acceder a la consola H2**  
  Asegúrate:
  - De que el backend está arrancado.
  - De entrar en `http://localhost:8080/h2-console`.
  - De usar el JDBC URL `jdbc:h2:file:./data/ecorentdb` y usuario `sa` sin contraseña (por defecto).

---

## Licencia

La licencia de este proyecto no está definida explícitamente en el repositorio.  
