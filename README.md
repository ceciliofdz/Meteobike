# Meteobike

Aplicación Android para planificar salidas en bicicleta consultando la predicción meteorológica de AEMET para localidades de España.

<img src="app/src/main/ic_launcher-playstore.png" alt="Meteobike: bicicleta, sol, nube y lluvia" width="192" />

La imagen de marca combina ciclismo y previsión meteorológica. El original y las indicaciones de generación se conservan en `artwork/`.

## Funciones

- Búsqueda de localidades por nombre, con su provincia en los resultados.
- Predicción diaria y consulta del pronóstico por horas.
- Información de temperatura, estado del cielo, precipitación, viento y humedad.
- Gráficos de temperatura y precipitación en el detalle diario, según los datos disponibles.
- Valoración orientativa de las condiciones para ciclismo a partir de temperatura, probabilidad de lluvia, viento y humedad. Esta valoración se calcula en la aplicación; no es una recomendación emitida por AEMET.
- Recuerdo de la última localidad seleccionada.
- Modo claro y oscuro, con preferencia guardada.
- Interfaz en español e inglés.
- Acceso a donaciones mediante PayPal desde la pantalla de información.

## Uso

1. Abre la aplicación y busca una localidad. En el primer inicio se utiliza Getafe como localidad predeterminada.
2. Consulta la previsión diaria y la valoración de las condiciones para ciclismo.
3. Selecciona un día para ver su detalle o accede al pronóstico horario desde la cabecera.
4. En Ajustes puedes cambiar el tema y el idioma.

La consulta meteorológica necesita conexión a Internet. La disponibilidad de cada dato depende de la información que publique AEMET para la localidad y el periodo seleccionados.

## Estado del proyecto

| Parámetro | Valor |
| --- | --- |
| Versión (`versionName`) | `1.4` |
| Código de versión (`versionCode`) | `5` |
| Android mínimo | Android 8.0, API 26 |
| SDK de compilación / objetivo | 36 / 36 |
| Namespace | `com.cfsd.meteocadaaveres` |
| Application ID | `com.cfsd.meteocadaaveres` |

El nombre visible de la aplicación es **Meteobike**, tanto en español como en inglés, incluida la etiqueta bajo el icono y la cabecera de la pantalla principal. El namespace y el application ID se mantienen como `com.cfsd.meteocadaaveres`.

La etiqueta [`v1.4-pre-meteobike`](https://github.com/ceciliofdz/Meteobike/tree/v1.4-pre-meteobike) conserva el código previo al cambio de nombre, con la versión `1.4` y el código `5`. Es un punto de referencia del código, no una publicación de un APK o AAB.

## Tecnologías

- Kotlin y Jetpack Compose para la interfaz.
- Navigation Compose para la navegación entre pantallas.
- Coroutines y OkHttp para las consultas de red.
- XmlPullParser para las predicciones XML de AEMET.
- Gson para los datos de localidades y su serialización.
- SharedPreferences para guardar localidad, idioma y tema.

Las versiones de las dependencias están centralizadas en `gradle/libs.versions.toml`.

## Compilar y ejecutar

### Requisitos

- Android Studio compatible con Android Gradle Plugin `8.13.0`.
- Android SDK Platform 36.
- Entorno Java conforme a `gradle/gradle-daemon-jvm.properties`: el proyecto solicita un JDK JetBrains 21 para el daemon de Gradle. El código configura compatibilidad Java/Kotlin 11.
- Un dispositivo o emulador con Android API 26 o superior.
- Conexión a Internet para descargar las dependencias y consultar la meteorología.

El repositorio incluye Gradle Wrapper `8.13`; no es necesario instalar Gradle por separado.

### Desde Android Studio

1. Clona el repositorio:

   ```bash
   git clone https://github.com/ceciliofdz/Meteobike.git
   cd Meteobike
   ```

2. Abre la carpeta del proyecto en Android Studio.
3. Configura el SDK de Android y sincroniza el proyecto con Gradle. La ruta local del SDK se guarda en `local.properties`, que no se versiona.
4. Selecciona el módulo `app`, un dispositivo o emulador y pulsa **Run**.

### Desde la terminal

Generar un APK de depuración:

```bash
./gradlew assembleDebug
```

El resultado se genera en `app/build/outputs/apk/debug/app-debug.apk`.

Ejecutar las pruebas unitarias y el análisis de Android Lint:

```bash
./gradlew testDebugUnitTest lintDebug
```

Ejecutar las pruebas instrumentadas con un dispositivo o emulador conectado:

```bash
./gradlew connectedDebugAndroidTest
```

En Windows, utiliza `gradlew.bat` en lugar de `./gradlew`.

Para distribuir una versión firmada, utiliza **Generate Signed App Bundle / APK** en Android Studio con la clave de firma correspondiente. El repositorio no incluye claves ni una configuración de firma de release.

La variante `release` activa R8 (reducción de código, optimización y ofuscación) y la reducción de recursos. Las reglas de `app/proguard-rules.pro` protegen únicamente los modelos que Gson lee por reflexión, para conservar el catálogo, las preferencias guardadas y los datos de navegación.

Para generar un bundle de comprobación sin configurar una firma de publicación:

```bash
./gradlew bundleRelease
```

Se genera en `app/build/outputs/bundle/release/app-release.aab`. La firma de publicación sigue siendo necesaria para distribuirlo. Conserva el archivo `app/build/outputs/mapping/release/mapping.txt` de cada versión publicada para interpretar los errores ofuscados. El bundle incorpora los metadatos de R8 y el mapping; puedes inspeccionar las métricas locales con:

```bash
unzip -p app/build/outputs/bundle/release/app-release.aab BUNDLE-METADATA/com.android.tools/r8.json
```

## Estructura

```text
app/
  build.gradle.kts                 Configuración Android y versión
  src/main/
    AndroidManifest.xml            Permisos, actividad y etiqueta de la app
    java/com/cfsd/meteocadaaveres/
      MainActivity.kt              Pantallas, consultas y tratamiento de datos
      ui/theme/                    Recursos del tema Compose
    assets/localidades.json        Catálogo de localidades
    res/
      values/strings.xml           Textos en español
      values-en/strings.xml        Textos en inglés
      drawable/                    Recursos gráficos
      mipmap-*/                    Iconos de la aplicación
gradle/
  libs.versions.toml               Catálogo de dependencias
  wrapper/                        Gradle Wrapper
```

## Datos y permisos

La aplicación consulta los XML de predicción diaria y horaria de AEMET mediante el código de la localidad. La implementación actual no requiere configurar una clave de API.

El manifiesto declara el permiso de Internet. La localidad se selecciona manualmente: no se solicita acceso al GPS. La localidad elegida, el idioma y el tema se guardan en preferencias locales.

## Control de versiones

El código se mantiene en la rama `main`. Los cambios pueden identificarse mediante commits y etiquetas para conservar puntos de recuperación.

El archivo `.gitignore` excluye cachés, configuración local del entorno, resultados de compilación, APK/AAB y claves de firma. Estos archivos permanecen fuera del historial de código.

## Autor y soporte

Proyecto de [ceciliofdz](https://github.com/ceciliofdz).

Puedes comunicar errores o proponer mejoras en [Issues](https://github.com/ceciliofdz/Meteobike/issues). La aplicación también incluye una opción de donación mediante PayPal.

## Licencia

El repositorio no incluye actualmente un archivo de licencia.
