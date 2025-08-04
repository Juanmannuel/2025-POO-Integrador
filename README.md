# 🎭 Sistema de Gestión de Eventos Culturales

**Trabajo Integrador - Programación Orientada a Objetos I**  
**Grupo 8 - 2025**

## 📋 Descripción del Proyecto

Sistema integral para la gestión de eventos culturales de un municipio, desarrollado con JavaFX y aplicando principios de Programación Orientada a Objetos. El sistema permite administrar diferentes tipos de eventos (ferias, exposiciones, conciertos, talleres, ciclos de cine), gestionar personas con múltiples roles, y manejar inscripciones de participantes con validaciones de negocio.

## 🚀 Tecnologías Utilizadas

- **Java 21** - Lenguaje de programación
- **JavaFX 21.0.1** - Framework para interfaz gráfica
- **Maven 3.9.11** - Gestión de dependencias y construcción
- **FXML** - Declaración de interfaces
- **CSS** - Estilos personalizados

## 🏗️ Arquitectura del Sistema

### Modelo de Datos
El sistema implementa un **modelo rico** con las siguientes características:

#### 📦 Clases Principales
- **`Evento` (Abstracta)**: Base para todos los tipos de eventos
  - `Taller`: Eventos con cupo máximo e instructor
  - `Concierto`: Eventos con artistas y tipo de entrada
  - `Exposicion`: Eventos con curador y tipo de arte
  - `Feria`: Eventos con stands y modalidad (techada/aire libre)
  - `CicloCine`: Eventos con películas programadas

- **`Persona`**: Entidad con validaciones de negocio (DNI, email)
- **`RolEvento`**: Relación entre personas y eventos con roles específicos

#### 🔧 Interfaces y Contratos
- **`IEventoConCupo`**: Para eventos con límite de participantes
- **`IEventoConInscripcion`**: Para eventos que requieren inscripción previa

#### 📊 Enumeraciones
- `EstadoEvento`: PLANIFICACION, CONFIRMADO, CANCELADO, EJECUCION, FINALIZADO
- `TipoEvento`: TALLER, CONCIERTO, EXPOSICION, FERIA, CICLO_CINE
- `TipoRol`: ORGANIZADOR, ARTISTA, CURADOR, INSTRUCTOR, PARTICIPANTE
- `Modalidad`: PRESENCIAL, VIRTUAL
- `TipoEntrada`: GRATUITA, PAGA

### Patrones de Diseño Implementados
- **MVC (Model-View-Controller)**: Separación de responsabilidades
- **Template Method**: En la clase abstracta `Evento`
- **Strategy**: Para diferentes tipos de eventos
- **Observer**: En los filtros de las pantallas

## 🖥️ Funcionalidades por Pantalla

### 👥 Gestión de Personas
**Ubicación**: `Menú → Personas`

**Funcionalidades Implementadas:**
- ✅ **Visualización tabular** de todas las personas registradas
- ✅ **Filtros en tiempo real**:
  - Por rol (Organizador, Artista, Curador, Instructor, Participante)
  - Por nombre (búsqueda parcial)
  - Por DNI
- ✅ **Modal de gestión** con formulario completo:
  - Campos: DNI, Nombre, Apellido, Teléfono, Email, Rol
  - Validaciones automáticas (formato email, longitud DNI)
  - Capitalización automática de nombres
- ✅ **Operaciones CRUD**:
  - Agregar nueva persona
  - Modificar datos existentes
  - Eliminar persona
- ✅ **Paginación** para grandes volúmenes de datos

**Validaciones de Negocio:**
- DNI entre 7-10 caracteres
- Email con formato válido
- Nombres capitalizados automáticamente

### 🎪 Gestión de Eventos
**Ubicación**: `Menú → Eventos`

**Funcionalidades Implementadas:**
- ✅ **Formulario dinámico** que se adapta según el tipo de evento
- ✅ **Campos base**: Nombre, Descripción, Fecha Inicio, Fecha Fin
- ✅ **Secciones específicas** por tipo:
  - **Taller**: Cupo máximo, modalidad, instructor
  - **Concierto**: Artistas, tipo de entrada
  - **Exposición**: Tipo de arte, curador
  - **Feria**: Cantidad de stands, ambiente
  - **Ciclo de Cine**: Lista de películas
- ✅ **Estados de evento** con transiciones controladas
- ✅ **Asignación de responsables** por evento

**Validaciones de Negocio:**
- No confirmar eventos con fecha pasada
- Control de transiciones de estado
- Validación de datos específicos por tipo

### 🎟️ Gestión de Participantes
**Ubicación**: `Menú → Participantes`

**Funcionalidades Implementadas:**
- ✅ **Vista consolidada** de todas las inscripciones
- ✅ **Filtros avanzados**:
  - Por evento específico
  - Por nombre del participante
  - Por DNI del participante
- ✅ **Información de cupo** en tiempo real
- ✅ **Modal de inscripción inteligente**:
  - Selección de evento (solo confirmados)
  - Selección de participante
  - Preview automático de información
  - Validación de cupo disponible
- ✅ **Modal de detalles completos**:
  - Información del evento y participante
  - Datos específicos del tipo de evento
  - Estado actual de la inscripción
- ✅ **Operaciones principales**:
  - Inscribir participante
  - Des-inscribir con confirmación
  - Ver detalles de participación
- ✅ **Validaciones estrictas**:
  - Solo eventos en estado CONFIRMADO
  - Respeto de cupos máximos
  - Prevención de inscripciones duplicadas
  - Control de estados de eventos

**Reglas de Negocio Específicas:**
- **Talleres**: Cupo limitado, requiere instructor
- **Conciertos**: Sin límite de cupo, múltiples artistas
- **Eventos Finalizados**: No permiten nuevas inscripciones
- **Eventos Cancelados**: Des-inscripción automática

### 🏠 Pantalla Principal
**Funcionalidades:**
- ✅ **Menú lateral** de navegación
- ✅ **Fecha actual** formateada
- ✅ **Contenido dinámico** según la sección seleccionada
- ✅ **Diseño responsive** y profesional

## 🚀 Instrucciones de Ejecución

### Prerrequisitos
- **Java 21** o superior
- **Maven 3.6** o superior
- Sistema operativo: Windows, macOS, o Linux

### Ejecutar la Aplicación

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/usuario/2025-POO-Integrador.git
   cd 2025-POO-Integrador/proyecto_integrador
   ```

2. **Ejecutar con Maven**:
   ```bash
   mvn javafx:run
   ```

3. **Compilar solamente**:
   ```bash
   mvn compile
   ```
