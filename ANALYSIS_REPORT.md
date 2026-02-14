# Reporte de Análisis: Sistema de Gestión de Vistas

## Resumen de Problemas
Se han identificado varios problemas críticos que afectan la estabilidad, funcionalidad y eficiencia de la aplicación, principalmente relacionados con la navegación y la gestión de eventos de usuario.

### 1. Vulnerabilidad a Doble Clic (Navegación Múltiple)
**Descripción:** El sistema actual de navegación no protege contra clics rápidos sucesivos.
**Causa Raíz:**
- `MonasteryButton.kt`: Utiliza el componente estándar `Button` de Compose sin ningún mecanismo de "debounce" (tiempo de espera entre clics).
- `AppNavigation.kt` (`DrawerMenuItem`): Utiliza `Modifier.clickable` estándar.
- **Consecuencia:** Si un usuario pulsa dos veces rápidamente un botón de navegación (ej. "Visita Virtual"), se lanzan dos eventos de navegación casi simultáneos, apilando dos veces la misma pantalla o causando transiciones erráticas.

### 2. Condición de Carrera en el Mapa (PlanoScreen)
**Descripción:** Al pulsar un pin o figura en el mapa, la navegación puede fallar o duplicarse.
**Causa Raíz:**
- `PlanoScreen.kt`: Utiliza `postDelayed(..., 200)` dentro del listener de toques (`setOnPhotoTapListener`) para efectos visuales antes de navegar.
- **Consecuencia:** Si el usuario toca otro elemento durante esos 200ms de espera, se programan múltiples navegaciones, causando conflictos o cierres inesperados.

### 3. Ineficiencia en Renderizado (DebugPhotoView)
**Descripción:** El componente que muestra el mapa consume recursos excesivos en el hilo principal.
**Causa Raíz:**
- `DebugPhotoView.kt`: En el método `onDraw` (que se ejecuta hasta 60 veces por segundo durante animaciones), se crean nuevos objetos `Matrix`, `Path` y `Paint` en cada fotograma.
- La animación de parpadeo (`blinkingAlpha`) fuerza la invalidación constante de toda la vista.
- **Consecuencia:** Mayor uso de CPU/Batería y posibles caídas de FPS (lag) en dispositivos de gama media/baja.

## Plan de Acción Recomendado

1.  **Implementar Debounce Global:** Crear un modificador `clickableSingle` que ignore clics repetidos en un intervalo de 500ms-1000ms.
2.  **Refactorizar Botones y Menús:** Aplicar este modificador a `MonasteryButton` y a los elementos del menú lateral.
3.  **Proteger Navegación en Mapa:** Añadir una bandera de estado (`isNavigating`) en `PlanoScreen` para bloquear interacciones adicionales una vez iniciada la navegación.
4.  **Optimizar Renderizado:** Refactorizar `DebugPhotoView` para reutilizar objetos (`Matrix`, `Path`) y evitar la asignación de memoria en el ciclo de dibujo (`onDraw`).
