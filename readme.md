Copyright (C) 2025 Francisco Iván San Segundo Álvarez, Daniel Garcia Salinas, Belid Mejia Largo, Marcos Martinez Antón, Marcos Paula González Martín, Sofía Yuste Garzón  y [NextApp]

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

# Monasterio de Santa María la Real de las Huelgas (Valladolid) - App Móvil

![Monasterio de Santa María la Real de las Huelgas, Valladolid](https://upload.wikimedia.org/wikipedia/commons/d/d8/Valladolid_-_Monasterio_de_Las_Huelgas_Reales.jpg)

Aplicación móvil oficial para el **Monasterio de Santa María la Real de las Huelgas** en Valladolid, desarrollada por **nextapp**.

## Descripción

Esta aplicación busca potenciar el patrimonio local del Monasterio de Santa María la Real de las Huelgas, ofreciendo una experiencia digital inmersiva y una herramienta útil tanto para visitantes generales como para el sector educativo. El objetivo es dar mayor visibilidad al monasterio, mejorar la forma de concertar citas y presentar la información de una manera más atractiva e interactiva.

## Funcionalidades Principales

La aplicación se centra en dos funcionalidades clave:

### 1. Visita Virtual Interactiva
*   **Mapa Digital del Monasterio:** Permite explorar un mapa dividido por zonas (Claustro, Iglesia, Museo, etc.).
*   **Puntos de Interés:** Incluye imágenes, descripciones y elementos 360° para ofrecer una experiencia de descubrimiento inmersiva.
*   **Progreso del Usuario:** Registra los lugares visitados por el usuario y guarda su progreso en una galería personal.

### 2. Sistema de Citas y Gestión de Visitas
*   **Solicitud de Citas:** Permite a los usuarios solicitar una cita para visitar el monasterio mediante un formulario fácil de usar.
*   **Notificaciones Automáticas:** Envía una notificación o correo electrónico al responsable del monasterio con los datos de la solicitud para confirmar la visita.

## Requisitos Técnicos

*   **Plataforma:** Aplicación nativa para Android, descargable desde la Play Store.
*   **Contenido Multimedia:** Capacidad para cargar y mostrar imágenes en 360°.
*   **Conectividad:** Acceso a internet para la descarga de contenido y la gestión de citas.
*   **Persistencia de Datos:** Guardado del progreso del usuario.
*   **Interfaz Responsiva:** Diseño adaptable a diferentes tamaños de pantalla (móviles y tablets).
*   **Navegación:** Mapa interactivo del monasterio para un fácil desplazamiento.

## Arquitectura y Tecnologías

El desarrollo de la aplicación seguirá una arquitectura moderna y escalable.

*   **Patrón de Arquitectura:** MVVM (Model-View-ViewModel) para separar la lógica de negocio de la interfaz de usuario.
    *   **Vista (View):** Activities y Fragments que muestran la UI.
    *   **Vista-Modelo (ViewModel):** Maneja la lógica de presentación y actualiza la vista.
    *   **Modelo (Model):** Gestiona los datos, el acceso a servicios externos y la lógica de negocio (gestión de reservas, contenido multimedia, etc.).
*   **Tecnologías:**
    *   **Lenguaje de Programación:** Kotlin
    *   **IDE:** Android Studio
    *   **Plataforma en la Nube:** Firebase (para base de datos, almacenamiento y notificaciones).
    *   **Diseño de UI/UX:** Figma (para bocetos y prototipos).
    *   **Gestión de Proyecto:** Taiga.