# 🗺️ MyWay - Aplicación de Navegación Inteligente

> **Aplicación móvil de navegación con IA, preferencias personalizadas, planificación de viajes y gestión inteligente de rutas, desarrollada en Kotlin con Jetpack Compose.**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com/)
[![License](https://img.shields.io/badge/License-Academic-yellow.svg)](LICENSE)

---

## 📌 Tabla de Contenidos
1. [Características Principales](#-características-principales)
2. [Vista Previa](#️-vista-previa)
3. [Requisitos del Sistema](#-requisitos-del-sistema)
4. [Guía de Instalación Paso a Paso](#-guía-de-instalación-paso-a-paso)
5. [Configuración de API Keys](#-configuración-de-api-keys-detallada)
6. [Configuración de Firebase](#-configuración-de-firebase)
7. [Estructura del Proyecto](#-estructura-del-proyecto)
8. [Funcionalidades Detalladas](#-funcionalidades-detalladas)
9. [Arquitectura](#-arquitectura)
10. [Solución de Problemas](#-solución-de-problemas)
11. [Contribución](#-cómo-contribuir)
12. [Autores](#-autores)
13. [Licencia](#-licencia)

---

## ⭐ Características Principales

### 🔐 **Módulo de Autenticación**
-  Registro de usuarios con validación de datos
-  Inicio de sesión seguro
-  Recuperación de contraseña por email
-  Persistencia de sesión automática
-  Validación en tiempo real de formularios

### 🎯 **Módulo de Preferencias Inteligentes**
-  Selección personalizada de medios de transporte (carro, moto, caminando)
-  Configuración de transporte preferido con estrella ⭐
-  Selección de paradas sugeridas (gasolinera, restaurante, tienda)
-  **Ruta más rápida** con análisis de tráfico en tiempo real
-  Sincronización automática en la nube con delay de 500ms
-  Almacenamiento en colección independiente de Firebase

### 🗺️ **Módulo de Navegación Avanzada**
-  Búsqueda inteligente con autocompletado de Google Places
-  Búsqueda por categorías predefinidas (restaurantes, gasolineras, hoteles, parques, supermercados)
-  Cálculo de rutas con múltiples opciones de transporte
-  Visualización de rutas coloreadas en mapa (verde, azul, rojo)
-  Integración dual: Google Places API + Firebase Custom Places
-  Gestión de favoritos sincronizada
-  Historial de lugares recientes (Room Database)

### 📅 **Módulo de Planificación de Viajes**
-  Creación de planes con múltiples destinos
-  Búsqueda de destinos con autocompletado integrado
-  Calendario interactivo con selección múltiple de fechas
-  Diseño moderno inspirado en aplicaciones de viajes
-  Validación completa de campos
-  Compatible con Android API 24+ (sin java.time)

### 👤 **Módulo de Perfil y Ajustes**
-  Visualización de información del usuario
-  Gestión de cuenta
-  Cierre de sesión seguro

---

## 🖼️ Vista Previa

### Flujo Principal de la Aplicación

```
📱 Login → 🏠 Home → 🔍 Buscar Lugar → 🗺️ Ver Rutas → 🚗 Navegar
                  ↓
            ⚙️ Preferencias → 💾 Sincronización Cloud
                  ↓
            📅 Crear Plan → 🗓️ Calendario → ✈️ Itinerario
```

---

## 💻 Requisitos del Sistema

### Herramientas de Desarrollo

| Herramienta | Versión  |
|-------------|----------------|
| **Android Studio** | Hedgehog (2023.1.1) | 
| **JDK** | 17 | 
| **Gradle** | 8.4 | 
| **Android SDK** | API 24 (Android 7.0) | 

### Dispositivo/Emulador

- **Sistema Operativo:** Android 7.0 (Nougat) o superior
- **RAM mínima:** 2 GB
- **Espacio disponible:** 100 MB
- **Permisos necesarios:**
  - 📍 Ubicación precisa (GPS)
  - 📍 Ubicación aproximada
  - 🌐 Internet

### Cuentas Necesarias

-  **Cuenta de Google** (para acceder a Google Cloud Console)
-  **Cuenta de Firebase** (proyecto configurado)

---

## 🚀 Guía de Instalación Paso a Paso

### Paso 1️⃣: Clonar el Repositorio

```bash
# Clona el repositorio
git clone https://github.com/TU_USUARIO/MyWay.git

# Entra al directorio del proyecto
cd MyWay
```

---

### Paso 2️⃣: Configurar Firebase

#### 2.1. Crear Proyecto en Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Click en **"Agregar proyecto"**
3. Nombre del proyecto: `MyWay` (o el que prefieras)
4. Acepta los términos y continúa
5. Habilita Google Analytics (opcional pero recomendado)
6. Click en **"Crear proyecto"**

#### 2.2. Agregar Aplicación Android

1. En la consola de Firebase, click en el ícono de Android
2. Ingresa el **Package name**: `com.example.myway`
3. Ingresa un nickname (opcional): `MyWay Android`
4. Deja el SHA-1 vacío por ahora (lo agregaremos después)
5. Click en **"Registrar app"**

#### 2.3. Descargar google-services.json

1. Descarga el archivo `google-services.json`
2. Colócalo en la carpeta `app/` del proyecto:
   ```
   MyWay/
   └── app/
       ├── build.gradle.kts
       └── google-services.json  ← Aquí
   ```

#### 2.4. Habilitar Servicios de Firebase

1. En Firebase Console, ve a **"Authentication"**
2. Click en **"Comenzar"**
3. Habilita el proveedor **"Correo electrónico/contraseña"**
4. Click en **"Guardar"**

5. Ve a **"Firestore Database"**
6. Click en **"Crear base de datos"**
7. Selecciona **"Comenzar en modo de prueba"** (cambiarás las reglas después)
8. Elige la ubicación más cercana (ej: `us-east1`)
9. Click en **"Habilitar"**

#### 2.5. Configurar Reglas de Firestore

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Preferencias de viaje (colección independiente)
    match /preferencias_viaje/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Lugares personalizados
    match /lugares/{placeId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
    
    // Planes de viaje
    match /planes_viaje/{planId} {
      allow read, write: if request.auth != null;
      
      match /lugares/{lugarId} {
        allow read, write: if request.auth != null;
      }
    }
    
    // Favoritos
    match /usuarios/{userId}/favoritos/{favId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

---

### Paso 3️⃣: Configurar Google Maps y Places API

## 🔥 Configuración de Firebase y Google Maps

La aplicación **MyWay** ya cuenta con toda la configuración necesaria para su correcto funcionamiento con **Firebase** y **Google Maps Platform**.

---

### 🧩 Integración con Firebase

El proyecto se encuentra completamente integrado con Firebase, vinculado al paquete  
`com.example.myway`.

- **Servicios habilitados:** Authentication, Firestore Database y Storage.  
- **Archivo `google-services.json`:** incluido en la carpeta `app/` del proyecto.  
- **Reglas de seguridad:** configuradas para acceso autenticado.  
- **Autenticación:** habilitada con el proveedor de correo electrónico/contraseña.  
- **Permisos de acceso:** el profesor cuenta con permisos de colaboración en Firebase, enviados previamente por correo institucional, lo que le permite ingresar directamente a la consola para verificar la autenticación, las colecciones y generar su propio **SHA-1** desde Android Studio en caso de ser necesario.

> 🔐 **Nota:** No es necesario crear nuevamente el proyecto en Firebase, ya que este se encuentra completamente vinculado a la aplicación entregada.

---

### 🗺️ Integración con Google Maps y Places API

La aplicación también está integrada con los servicios de **Google Maps**, incluyendo:

- **Maps SDK for Android**  
- **Places API**  
- **Directions API**  

Estos servicios se encuentran activos y configurados en el proyecto de **Google Cloud** asociado a la aplicación.

---

### 🔑 Configuración de la API Key (para el evaluador)

Por motivos de seguridad, la **API Key de Google Maps** no se encuentra dentro del repositorio público (GitHub), ya que es de uso privado.  
Sin embargo, se proporciona al evaluador la siguiente clave para permitir la ejecución completa del proyecto.

Para habilitar el mapa correctamente:

1. Abre el archivo `local.properties` (ubicado en la raíz del proyecto Android).  
2. Agrega la siguiente línea al final del archivo:

   ```properties
   MAPS_API_KEY=AIzaSyDQeDHEuDEajRDtKUyNafoay6LfcRe0oso


#### 3.4. Obtener SHA-1 Certificate Fingerprint

Abre una terminal y ejecuta:

**En Windows:**
```bash
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

**En macOS/Linux:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Busca la línea que dice `SHA1:` y copia el valor (ejemplo: `A1:B2:C3:...`)

#### 3.5. Restringir la API Key

1. En Google Cloud Console, click en la API Key que creaste
2. En **"Restricciones de aplicación"**, selecciona **"Aplicaciones de Android"**
3. Click en **"+ Agregar un elemento"**
4. **Nombre del paquete:** `com.example.myway`
5. **Huella digital del certificado SHA-1:** Pega el SHA-1 que copiaste
6. Click en **"Guardar"**

#### 3.6. Agregar SHA-1 a Firebase (Opcional pero Recomendado)

1. Ve a Firebase Console → Configuración del proyecto
2. Scroll hasta **"Tus apps"**
3. Click en la app Android
4. En **"Huellas digitales de certificados SHA"**
5. Click en **"Agregar huella digital"**
6. Pega el SHA-1
7. Click en **"Guardar"**

---

### Paso 4️⃣: Configurar API Keys en el Proyecto

#### 4.1. Crear archivo local.properties

En la **raíz del proyecto** (no en `app/`), crea o edita el archivo `local.properties`:

```properties
# Ubicación del Android SDK (se genera automáticamente)
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk

# ⚠️ IMPORTANTE: Agrega tu API Key de Google Maps aquí
MAPS_API_KEY=AIzaSyDQeDHEuDEajRDtKUyNafoay6LfcRe0oso
```

**⚠️ IMPORTANTE:**
- Reemplaza `TU_API_KEY_AQUI` con tu API Key real
- Este archivo **NO debe subirse a GitHub** (ya está en `.gitignore`)
- Si trabajas en equipo, cada desarrollador debe crear su propio `local.properties`

#### 4.2. Verificar que BuildConfig esté Configurado

El archivo `app/build.gradle.kts` debe tener:

```kotlin
android {
    // ...
    buildFeatures {
        buildConfig = true
    }
    
    defaultConfig {
        // ...
        
        // Leer API Key desde local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { localProperties.load(it) }
        }
        
        buildConfigField(
            "String",
            "MAPS_API_KEY",
            "\"${localProperties.getProperty("MAPS_API_KEY", "")}\""
        )
    }
}
```

---

### Paso 5️⃣: Abrir y Sincronizar el Proyecto

#### 5.1. Abrir en Android Studio

1. Abre **Android Studio**
2. Click en **"Open"** (o `File → Open`)
3. Navega hasta la carpeta `MyWay/`
4. Click en **"OK"**

#### 5.2. Sincronizar Gradle

Android Studio sincronizará automáticamente. Si no:

1. Click en **"File → Sync Project with Gradle Files"**
2. Espera a que termine (puede tomar varios minutos la primera vez)

Si hay errores, revisa:
- ✅ `google-services.json` está en `app/`
- ✅ `local.properties` tiene la `MAPS_API_KEY`
- ✅ Conexión a Internet activa

---

### Paso 6️⃣: Ejecutar la Aplicación

#### 6.1. Conectar Dispositivo o Iniciar Emulador

**Opción A: Dispositivo Físico**
1. Habilita **"Opciones de desarrollador"** en tu Android
2. Activa **"Depuración USB"**
3. Conecta el dispositivo por USB
4. Acepta el mensaje de confianza en el dispositivo

**Opción B: Emulador**
1. Click en **"Device Manager"** en Android Studio
2. Click en **"Create Virtual Device"**
3. Selecciona un dispositivo (ej: Pixel 6)
4. Selecciona una imagen del sistema (recomendado: API 34)
5. Click en **"Finish"**
6. Click en el botón ▶️ del emulador

#### 6.2. Compilar y Ejecutar

1. En Android Studio, selecciona tu dispositivo/emulador
2. Click en el botón **"Run"** ▶️ (o presiona `Shift + F10`)
3. Espera a que compile e instale
4. La app se abrirá automáticamente

**Desde Terminal (opcional):**
```bash
# Compilar
./gradlew assembleDebug

# Instalar en dispositivo conectado
./gradlew installDebug

# O todo junto
./gradlew clean assembleDebug installDebug
```

---

## 🔑 Configuración de API Keys Detallada

### ¿Por qué Necesitamos API Keys?

| API | Propósito | Costo |
|-----|-----------|-------|
| **Maps SDK for Android** | Mostrar mapas interactivos | Gratis hasta 28,000 cargas/mes |
| **Places API** | Búsqueda y autocompletado | Gratis hasta 1,000 solicitudes/mes |
| **Directions API** | Calcular rutas entre puntos | Gratis hasta 2,500 solicitudes/mes |

### Límites de Uso Gratuito

Google Cloud ofrece **$200 en créditos gratis por mes**, lo cual es más que suficiente para desarrollo y pruebas.

### Monitorear Uso de APIs

1. Ve a Google Cloud Console
2. Click en **"APIs y servicios" → "Panel"**
3. Verás gráficos de uso en tiempo real

---

## 🔥 Configuración de Firebase

### Colecciones de Firestore Utilizadas

```
📦 Firestore Database
├── 📁 preferencias_viaje/           # Colección independiente
│   └── 📄 {userId}                   # Documento por usuario
│       ├── userId: string
│       ├── transportesSeleccionados: array
│       ├── transportePreferido: string
│       ├── paradasSugeridas: array
│       ├── rutaMasRapida: boolean
│       └── fechaActualizacion: timestamp
│
├── 📁 lugares/                       # Lugares personalizados
│   └── 📄 {placeId}
│       ├── nombre: string
│       ├── latitude: number
│       ├── longitude: number
│       └── tipo: string
│
├── 📁 planes_viaje/                  # Planes de viaje
│   └── 📄 {planId}
│       ├── userId: string
│       ├── titulo: string
│       ├── destinos: array
│       ├── fechas: array
│       └── 📁 lugares/               # Subcolección
│           └── 📄 {lugarId}
│
└── 📁 usuarios/                      # Datos de usuarios
    └── 📄 {userId}
        └── 📁 favoritos/             # Subcolección de favoritos
            └── 📄 {favoritoId}
                ├── placeId: string
                ├── placeName: string
                └── timestamp: timestamp
```

### Reglas de Seguridad Recomendadas

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Solo usuarios autenticados pueden leer/escribir
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
    
    // Preferencias: solo el propietario
    match /preferencias_viaje/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Favoritos: solo el propietario
    match /usuarios/{userId}/favoritos/{favId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

---

## 📁 Estructura del Proyecto

```
MyWay/
├── 📱 app/
│   ├── 📂 src/main/
│   │   ├── 📂 java/com/example/myway/
│   │   │   │
│   │   │   ├── 📂 data/                      # Capa de datos
│   │   │   │   ├── 📂 dao/                   # Room DAOs
│   │   │   │   │   ├── FavoritePlaceDao.kt
│   │   │   │   │   └── RecentPlaceDao.kt
│   │   │   │   ├── 📂 entities/              # Entidades de Room
│   │   │   │   │   ├── FavoritePlace.kt
│   │   │   │   │   └── RecentPlace.kt
│   │   │   │   ├── 📂 repository/            # Repositorios
│   │   │   │   │   ├── FavoritesRepository.kt
│   │   │   │   │   └── RecentPlacesRepository.kt
│   │   │   │   └── AppDatabase.kt
│   │   │   │
│   │   │   ├── 📂 screens/                   # Pantallas por módulo
│   │   │   │   │
│   │   │   │   ├── 📂 modulo1/               # Autenticación
│   │   │   │   │   ├── InicioSesion.kt
│   │   │   │   │   ├── Registro.kt
│   │   │   │   │   └── RecuperarContrasena.kt
│   │   │   │   │
│   │   │   │   ├── 📂 modulo2/               # Preferencias
│   │   │   │   │   ├── PreferenciasViaje.kt
│   │   │   │   │   └── PreferenciasManager.kt
│   │   │   │   │
│   │   │   │   ├── 📂 modulo3/               # Navegación
│   │   │   │   │   ├── Home.kt
│   │   │   │   │   ├── PlaneaViaje.kt
│   │   │   │   │   ├── RutaOpciones.kt
│   │   │   │   │   └── NavegacionActiva.kt
│   │   │   │   │
│   │   │   │   ├── 📂 modulo4/               # Perfil
│   │   │   │   │   ├── PerfilAjustes.kt
│   │   │   │   │   └── Guardados.kt
│   │   │   │   │
│   │   │   │   └── 📂 modulo5/               # Planificación
│   │   │   │       ├── PlanesViaje.kt
│   │   │   │       └── CrearPlan.kt          # Con calendario
│   │   │   │
│   │   │   ├── 📂 ui/theme/                  # Tema
│   │   │   │   ├── Color.kt                  # Paleta de colores
│   │   │   │   ├── Theme.kt
│   │   │   │   └── Type.kt                   # Tipografía Nunito
│   │   │   │
│   │   │   ├── 📂 utils/                     # Utilidades
│   │   │   │   └── UsuarioTemporal.kt
│   │   │   │
│   │   │   └── MainActivity.kt               # Actividad principal
│   │   │
│   │   ├── 📂 res/
│   │   │   ├── 📂 drawable/                  # Iconos e imágenes
│   │   │   ├── 📂 font/                      # Fuentes
│   │   │   ├── 📂 values/
│   │   │   │   ├── strings.xml               # Textos de la app
│   │   │   │   ├── colors.xml
│   │   │   │   └── themes.xml
│   │   │   └── 📂 xml/
│   │   │       └── network_security_config.xml
│   │   │
│   │   └── AndroidManifest.xml
│   │
│   ├── build.gradle.kts                      # Configuración del módulo
│   └── google-services.json                  # Credenciales Firebase
│
├── 📂 gradle/
├── build.gradle.kts                          # Configuración raíz
├── gradle.properties
├── settings.gradle.kts
├── local.properties                          # API Keys 
├── .gitignore
└── README.md                                 # Este archivo
```

---

## 🎯 Funcionalidades Detalladas

### 1. Sistema de Preferencias Inteligente

```kotlin
// PreferenciasViajeData
data class PreferenciasViajeData(
    val transportesSeleccionados: Set<String> = setOf("driving", "motorcycle", "walking"),
    val transportePreferido: String = "driving",
    val paradasSugeridas: Set<String> = emptySet(),
    val rutaMasRapida: Boolean = false
)
```

**Características:**
- ✅ Guardado automático con delay de 500ms
- ✅ Sincronización con Firebase en colección `preferencias_viaje`
- ✅ Backup local en SharedPreferences
- ✅ Indicador visual de sincronización

### 2. Búsqueda de Lugares con Autocompletado

**Google Places API + Firebase:**
```kotlin
// Detecta si es lugar de Google o Firebase
if (!placeId.startsWith("ChIJ") && !placeId.startsWith("Ei")) {
    // Es lugar de Firebase → buscar en Firestore
} else {
    // Es lugar de Google → usar Places API
}
```

### 3. Calendario de Planificación

**Compatible con API 24+:**
- Usa `Calendar` en lugar de `java.time.LocalDate`
- Selección múltiple de fechas
- Navegación entre meses
- Diseño moderno con círculos de selección

---

## 🏛️ Arquitectura

### Patrón de Diseño: MVVM Simplificado

```
┌─────────────┐
│   Compose   │  ← UI Layer (Jetpack Compose)
│   Screens   │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│ Repositories│  ← Data Layer
│   + Room    │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│  Firebase   │  ← Backend
│   + APIs    │
└─────────────┘
```

### Tecnologías Clave

| Componente | Tecnología | Propósito |
|------------|------------|-----------|
| **UI** | Jetpack Compose | Interfaz moderna y declarativa |
| **Navegación** | Navigation Compose | Gestión de pantallas |
| **Base de datos local** | Room | Historial y favoritos offline |
| **Base de datos remota** | Firestore | Sincronización en la nube |
| **Autenticación** | Firebase Auth | Login seguro |
| **Mapas** | Google Maps SDK | Visualización de mapas |
| **Lugares** | Places API | Búsqueda y autocompletado |
| **Rutas** | Directions API | Cálculo de rutas |
| **Async** | Coroutines | Operaciones asíncronas |

---

## 🐛 Solución de Problemas

### ❌ Error: "API key not found" o "API_KEY_NOT_FOUND"

**Causa:** El archivo `local.properties` no existe o no tiene la API Key.

**Solución:**
```properties
# Verifica que local.properties contenga:
MAPS_API_KEY=AIzaSyDQeDHEuDEajRDtKUyNafoay6LfcRe0oso
```

---

### ❌ Error: "google-services.json is missing"

**Causa:** El archivo `google-services.json` no está en `app/`.

**Solución:**
1. Descarga el archivo desde Firebase Console
2. Colócalo en `MyWay/app/google-services.json`
3. Sincroniza Gradle

---

### ❌ Mapa no se muestra (pantalla gris)

**Causas posibles:**

1. **API Key incorrecta**
   - Verifica que la API Key sea la correcta
   - Confirma que Maps SDK esté habilitado en Google Cloud

2. **SHA-1 no configurado**
   - Genera el SHA-1 con el comando keytool
   - Agrégalo en Google Cloud Console y Firebase

3. **Permisos faltantes**
   - Verifica en `AndroidManifest.xml`:
   ```xml
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
   <uses-permission android:name="android.permission.INTERNET"/>
   ```

---

### ❌ Búsqueda de lugares no funciona

**Solución:**
1. Habilita **Places API** en Google Cloud Console
2. Verifica que la API Key tenga permisos para Places API
3. Revisa las restricciones de la API Key

---

### ❌ Error: "Call requires API level 26"

**Causa:** Uso de `java.time` que requiere API 26+.

**Solución:** Ya solucionado en la última versión. Se usa `Calendar` en lugar de `LocalDate`.

---

### ❌ Error de compilación: "Unresolved reference: BuildConfig"

**Solución:**
```kotlin
// En app/build.gradle.kts, asegúrate de tener:
android {
    buildFeatures {
        buildConfig = true
    }
}
```

---

### ❌ Firebase: "Permission denied"

**Solución:**
Actualiza las reglas de Firestore:
```javascript
match /{document=**} {
  allow read, write: if request.auth != null;
}
```

---

### ❌ Limpiar cache y reconstruir

Si nada funciona, prueba:
```bash
./gradlew clean
./gradlew build --refresh-dependencies

# O desde Android Studio:
# Build → Clean Project
# Build → Rebuild Project
```

---

