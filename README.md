# 🗺️ MyWay - Aplicación de Navegación Inteligente

> Aplicación móvil de navegación con IA, preferencias personalizadas y planificación de viajes, desarrollada en Kotlin con Jetpack Compose.

[![Android](https://img.shields.io/badge/Platform-Android%207.0%2B-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com/)

---

## 📋 Tabla de Contenidos
- [Características Principales](#-características-principales)
- [Requisitos](#-requisitos)
- [Instalación Rápida](#-instalación-rápida)
- [Configuración de APIs](#-configuración-de-apis)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Tecnologías Utilizadas](#-tecnologías-utilizadas)
- [Autores](#-autores)

---

## ⭐ Características Principales

### 🔐 Autenticación
- Registro e inicio de sesión con Firebase Auth
- Recuperación de contraseña
- Validación en tiempo real

### 🎯 Preferencias Inteligentes
- Configuración de medios de transporte (carro, moto, caminando)
- Selección de transporte preferido
- Paradas sugeridas (gasolinera, restaurante, tienda)
- **Ruta más rápida** con análisis de tráfico
- Sincronización automática en Firestore

### 🗺️ Navegación Avanzada
- Búsqueda con autocompletado (Google Places API)
- Búsqueda por categorías (restaurantes, gasolineras, hoteles, etc.)
- Cálculo de rutas optimizadas
- Visualización de múltiples rutas en mapa
- Sistema de favoritos
- Historial de lugares (Room Database)

### 📅 Planificación de Viajes
- Creación de planes con múltiples destinos
- Calendario interactivo
- Compatible con Android API 24+

### 👤 Perfil y Ajustes
- Gestión de cuenta
- Configuración de preferencias
- Cierre de sesión

---

## 💻 Requisitos

| Componente | Versión Requerida |
|------------|-------------------|
| Android Studio | Hedgehog (2023.1.1) o superior |
| JDK | 17 |
| Android SDK | API 24 (Android 7.0) mínimo |
| Gradle | 8.4 |

**Permisos necesarios:**
- 📍 Ubicación (GPS)
- 🌐 Internet

---

## 🚀 Instalación Rápida

### 1️⃣ Clonar el Repositorio
```bash
git clone https://github.com/Tiaguitoo09/MyWay.git
cd MyWay
```

### 2️⃣ Configurar Firebase y Google Maps

#### ✅ Firebase ya está configurado
El proyecto incluye el archivo `google-services.json` preconfigurado para el paquete `com.example.myway`.

**Servicios habilitados:**
- Authentication (Email/Password)
- Firestore Database
- Storage

**Nota para el evaluador:** Tiene acceso colaborador al proyecto Firebase enviado previamente por correo institucional.

#### 🔑 Configurar Google Maps API Key

**Por seguridad, la API Key no está en el repositorio público.**

Para habilitar el mapa:

1. Crea/edita el archivo `local.properties` en la raíz del proyecto
2. Agrega la siguiente línea:
```properties
MAPS_API_KEY=AIzaSyDQeDHEuDEajRDtKUyNafoay6LfcRe0oso
```

> ⚠️ **Importante:** Este archivo no se sube a GitHub y es único por desarrollador.

### 3️⃣ Abrir y Ejecutar

1. Abre el proyecto en **Android Studio**
2. Espera a que Gradle sincronice
3. Conecta un dispositivo o inicia un emulador
4. Click en **Run** ▶️

---

## 🔧 Configuración de APIs

### Google Cloud APIs Habilitadas

- **Maps SDK for Android** - Visualización de mapas
- **Places API** - Búsqueda y autocompletado
- **Directions API** - Cálculo de rutas

### Firestore Collections
```
📦 Firestore Database
├── preferencias_viaje/{userId}     # Preferencias por usuario
├── lugares/{placeId}                # Lugares personalizados
├── planes_viaje/{planId}            # Planes de viaje
│   └── lugares/{lugarId}            # Subcolección de destinos
└── usuarios/{userId}
    └── favoritos/{favId}            # Lugares favoritos
```

---

## 📁 Estructura del Proyecto
```
MyWay/
├── app/
│   ├── src/main/java/com/example/myway/
│   │   ├── data/                    # Capa de datos
│   │   │   ├── dao/                 # Room DAOs
│   │   │   ├── entities/            # Entidades
│   │   │   ├── repository/          # Repositorios
│   │   │   └── AppDatabase.kt
│   │   │
│   │   ├── ia/                      # Módulo de Inteligencia Artificial
│   │   │   ├── AiModels.kt          # Modelos de datos para IA
│   │   │   ├── AiRepository.kt      # Repositorio de servicios IA
│   │   │   ├── AiService.kt         # Servicio principal de IA
│   │   │   ├── GooglePlaceNearby.kt # Búsqueda de lugares cercanos
│   │   │   ├── ItineraryGenerator.kt # Generador de itinerarios inteligentes
│   │   │   └── PopulatePlaces.kt    # Población de lugares en Firebase
│   │   │
│   │   ├── screens/                 # Pantallas por módulo
│   │   │   ├── modulo1/             # Autenticación
│   │   │   ├── modulo2/             # Perfil y Preferencias
│   │   │   ├── modulo3/             # Navegación
│   │   │   ├── modulo4/             # Sin Plan
│   │   │   └── modulo5/             # Planificación e Itinerario
│   │   │
│   │   ├── ui/theme/                # Tema y estilos
│   │   ├── utils/                   # Utilidades
│   │   └── MainActivity.kt
│   │
│   ├── build.gradle.kts
│   └── google-services.json
│
├── local.properties                 # ⚠️ Contiene MAPS_API_KEY
└── README.md
```

---

## 🏛️ Tecnologías Utilizadas

### Frontend
- **Jetpack Compose** - UI moderna y declarativa
- **Navigation Compose** - Gestión de navegación
- **Material Design 3** - Componentes UI

### Backend & Datos
- **Firebase Authentication** - Autenticación segura
- **Cloud Firestore** - Base de datos en tiempo real
- **Room Database** - Persistencia local

### APIs & Mapas
- **Google Maps SDK** - Visualización de mapas
- **Places API** - Búsqueda de lugares
- **Directions API** - Cálculo de rutas

### Arquitectura
- **MVVM** - Patrón de diseño
- **Coroutines** - Programación asíncrona
- **Flow** - Manejo de streams de datos

---

## 🐛 Solución de Problemas Comunes

### ❌ Mapa no se muestra

**Solución:**
1. Verifica que `local.properties` contenga `MAPS_API_KEY`
2. Asegúrate de que Maps SDK esté habilitado en Google Cloud Console

### ❌ Error: "google-services.json is missing"

**Solución:**
- El archivo debe estar en la carpeta `app/`
- Ejecuta: `File → Sync Project with Gradle Files`

### ❌ Permisos de ubicación denegados

**Solución:**
- Ve a Configuración del dispositivo → Aplicaciones → MyWay → Permisos
- Habilita "Ubicación"

---

## 👥 Autores

**Desarrollo:**
- [Julian Santiago Hernandez][Sergio Daniel Aza][Santiago Martinez Beltran]- Desarrolladores Principales
- Universidad: [Universidad de Bogotá Jorge Tadeo Lozano]
- Curso: [CONSTRUCCIÓN DE APLICACIONES MÓVILES]
- Semestre: [Sexto Semestre]

---

## 📄 Licencia

Este proyecto es con fines académicos. Desarrollado como proyecto de [CONSTRUCCIÓN DE APLICACIONES MÓVILES].

---

## 📞 Contacto

Para consultas sobre el proyecto:
- **Email:** [julians.hernandezg@utadeo.edu.co][sergiod.azaocampo@utadeo.edu.co][santiago.martinezb@utadeo.edu.co]
- **GitHub:** [@Tiaguitoo09](https://github.com/Tiaguitoo09)

---

<div align="center">
  <p>⭐ Si este proyecto te fue útil, considera darle una estrella en GitHub ⭐</p>
  <p>Hecho con ❤️ y ☕ por [Julian Santiago Hernandez][Sergio Daniel Aza][Santiago Martinez Beltran]</p>
</div>
