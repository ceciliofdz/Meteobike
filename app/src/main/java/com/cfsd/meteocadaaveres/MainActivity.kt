package com.cfsd.meteocadaaveres

//noinspection UsingMaterialAndMaterial3Libraries


import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.StringReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.graphics.Path
//import androidx.compose.material3.isSystemInDarkTheme

// Estructura para el JSON
data class ProvinciaLocalidades(
    val source: String,
    val data: Map<String, String>
)

// Estructura para una localidad individual
data class Localidad(
    val nombre: String,
    val codigo: String,
    val provincia: String
)

// Cargar JSON desde assets
fun loadLocalidadesFromAssets(context: Context): List<ProvinciaLocalidades> {
    val jsonString: String
    try {
        jsonString = context.assets.open("localidades.json").bufferedReader().use { it.readText() }
    } catch (e: IOException) {
        e.printStackTrace()
        return emptyList()
    }

    val gson = Gson()
    val listType = object : TypeToken<List<ProvinciaLocalidades>>() {}.type
    return gson.fromJson(jsonString, listType)
}

// Convertir JSON a lista plana de localidades
fun flattenLocalidades(localidadesPorProvincia: List<ProvinciaLocalidades>): List<Localidad> {
    return localidadesPorProvincia.flatMap { provincia ->
        provincia.data.map { (nombre, codigo) ->
            Localidad(nombre, codigo, provincia.source)
        }
    }
}

// Estado global del tema (puedes mover esto a un ViewModel si prefieres)
object ThemeState {
    val isDarkMode = mutableStateOf(false) // Por defecto, modo claro
}

fun saveThemePreference(context: Context, isDark: Boolean) {
    val prefs = context.getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
    prefs.edit { putBoolean("isDarkMode", isDark) }
}

fun loadThemePreference(context: Context): Boolean {
    val prefs = context.getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("isDarkMode", false) // Por defecto, modo claro
}

// Colores según el tema
@Composable
fun getThemeColors(): ThemeColors {
    val isDark = ThemeState.isDarkMode.value
   // val systemInDark = isSystemInDarkTheme()

    val effectiveDarkMode = isDark

//    val effectiveDarkMode = when {
//        isDark -> true
//        !isDark -> false
//       else -> !systemInDark // Opción para seguir el tema del sistema
//    }


    return if (effectiveDarkMode) {
        ThemeColors(
            primary = Color(0xFF64B5F6),
            secondary = Color(0xFF4FC3F7),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            textPrimary = Color(0xFF64B5F6), // Azul claro para modo oscuro
            textSecondary = Color(0xFFB0BEC5),
            accentGreen = Color(0xFF81C784),    // Añadido
            accentOrange = Color(0xFFFFB300),   // Añadido
            accentRed = Color(0xFFE57373),      // Añadido
            divider = Color(0xFF424242)         // Añadido
        )
    } else {
        ThemeColors(
            primary = Color(0xFF1976D2),
            secondary = Color(0xFF2196F3),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFF5F5F5),
            textPrimary = Color(0xFF1976D2), // Azul oscuro para modo claro
            textSecondary = Color(0xFF757575),
            accentGreen = Color(0xFF43A047),    // Añadido
            accentOrange = Color(0xFFFFA726),   // Añadido
            accentRed = Color(0xFFE53935),      // Añadido
            divider = Color(0xFFE0E0E0)         // Añadido
        )
    }
}

data class ThemeColors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentGreen: Color,
    val accentOrange: Color,
    val accentRed: Color,
    val divider: Color
)

// Elimina las constantes originales de color y usa getThemeColors() en su lugar


// Dimensiones y espaciado
val CardElevation = 4.dp
val CardCornerRadius = 16.dp
val DefaultPadding = 16.dp
val SmallPadding = 8.dp
val TinyPadding = 4.dp
val MediumPadding = 24.dp
val LargePadding = 28.dp

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cargar preferencias antes de setContent
        val prefs1 = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val prefs = getSharedPreferences("LanguagePrefs", MODE_PRIVATE)

        val savedLocation = prefs1.getString("selectedLocation", null)
        val language = prefs.getString("language", "es") ?: "es"

        // Cargar el tema
        ThemeState.isDarkMode.value = loadThemePreference(this)

        updateLanguage(this, language)

        setContent {
            // Inyectar la localidad guardada
            val initialLocation = if (savedLocation != null) {
                Gson().fromJson(savedLocation, Localidad::class.java)
            } else {
                Localidad("Getafe", "28065", "Madrid")
            }
            WeatherApp(initialLocation)
        }
    }
}

data class DiaPrediccion(
    val fecha: String,
    val probPrecipitacion: String,
    val estadoCielo: String,
    val tempMax: String,
    val tempMin: String,
    val humMax: String,
    val humMin: String,
    val vientoDir: String,
    val vientoVel: String,
    val precipitacionPorPeriodo: List<Pair<String, String>> = emptyList() // Nuevo campo: periodo (e.g., "00-06") y valor (e.g., "100")
) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerFechaFormateada(context: Context): String { // Añadimos context como parámetro
        try {
            val fecha = LocalDate.parse(this.fecha)
            val locale = context.resources.configuration.locales[0] // Obtener el Locale actual
            val diaSemana = fecha.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
                .replaceFirstChar { it.uppercase() }
            val formatoFecha = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            return "$diaSemana, $formatoFecha"
        } catch (e: Exception) {
            return fecha
        }
    }

    fun evaluarCondicionesCiclismo(context: Context): Triple<Boolean, String, Int> {
        val temperatura = tempMax.toFloatOrNull() ?: 0f
        val probabilidadLluvia = probPrecipitacion.toFloatOrNull() ?: 0f
        val velocidadViento = vientoVel.toFloatOrNull() ?: 0f
        val humedad = humMax.toFloatOrNull() ?: 0f

        var puntuacion = 100
        var razon = mutableListOf<String>()

        when {
            temperatura < 5 -> {
                puntuacion -= 40; razon.add(context.getString(R.string.very_low_temperature))
            }

            temperatura < 10 -> {
                puntuacion -= 20; razon.add(context.getString(R.string.low_temperature))
            }

            temperatura > 35 -> {
                puntuacion -= 40; razon.add(context.getString(R.string.very_high_temperature))
            }

            temperatura > 30 -> {
                puntuacion -= 20; razon.add(context.getString(R.string.high_temperature))
            }
        }
        when {
            probabilidadLluvia >= 80 -> {
                puntuacion -= 50; razon.add(context.getString(R.string.high_rain_probability))
            }

            probabilidadLluvia >= 60 -> {
                puntuacion -= 30; razon.add(context.getString(R.string.likely_rain))
            }

            probabilidadLluvia >= 40 -> {
                puntuacion -= 15; razon.add(context.getString(R.string.possible_rain))
            }
        }
        when {
            velocidadViento > 40 -> {
                puntuacion -= 40; razon.add(context.getString(R.string.very_strong_wind))
            }

            velocidadViento > 30 -> {
                puntuacion -= 25; razon.add(context.getString(R.string.strong_wind))
            }

            velocidadViento > 20 -> {
                puntuacion -= 10; razon.add(context.getString(R.string.moderate_wind))
            }
        }
        when {
            humedad > 90 -> {
                puntuacion -= 20; razon.add(context.getString(R.string.very_high_humidity))
            }

            humedad > 80 -> {
                puntuacion -= 10; razon.add(context.getString(R.string.high_humidity))
            }
        }

        val esRecomendable = puntuacion >= 60
        val mensaje = when {
            razon.isEmpty() -> context.getString(R.string.perfect_conditions)
            esRecomendable -> context.getString(
                R.string.acceptable_conditions,
                razon.joinToString(", ")
            )

            else -> context.getString(R.string.not_recommended, razon.joinToString(", "))
        }

        return Triple(esRecomendable, mensaje, puntuacion)
    }
}

fun translateSkyState(context: Context, estadoCielo: String): String {
    return when (estadoCielo) {
        "Nuboso" -> context.getString(R.string.cloudy)
        "Poco nuboso" -> context.getString(R.string.partly_cloudy)
        "Nubes altas" -> context.getString(R.string.high_clouds)
        "Muy nuboso" -> context.getString(R.string.very_cloudy)
        "Cubierto" -> context.getString(R.string.cubierto)
        "Despejado" -> context.getString(R.string.clear)
        "Cubierto con lluvia escasa" -> context.getString(R.string.overcast_rain)
        "Cielo despejado" -> context.getString(R.string.cielo_despejado)
        "Nuboso con tormenta" -> context.getString(R.string.nuboso_con_tormenta)
        "Intervalos nubosos con tormenta y lluvia escasa" -> context.getString(R.string.intervalos_nubosos_lluvia_escasa)
        "Muy nuboso con lluvia escasa" -> context.getString(R.string.muy_nuboso_con_lluvia_escasa)
        "Nuboso con lluvia escasa" -> context.getString(R.string.nuboso_con_lluvia_escasa)
        "Niebla " -> context.getString(R.string.niebla)
        "Nieve" -> context.getString(R.string.nieve)
        "Bruma" -> context.getString(R.string.bruma)
        "Intervalos nubosos" -> context.getString(R.string.intervalos_nubosos)
        "Cubierto con tormenta y lluvia escasa" -> context.getString(R.string.cubierto_tormenta_lluvia_escasa)
        "Cubierto con lluvia" -> context.getString(R.string.cubierto_con_lluvia)
        "Cubierto con tormenta" -> context.getString(R.string.cubierto_con_tormenta)
        "Muy nuboso con lluvia" -> context.getString(R.string.muy_nuboso_con_lluvia)
        "Muy nuboso con tormenta" -> context.getString(R.string.muy_nuboso_tormenta)
        "Nuboso con lluvia" -> context.getString(R.string.nuboso_con_lluvia)
        "Cubierto con tormenta con lluvia escasa" -> context.getString(R.string.cubierto_tormenta_lluvia_escasa)
        "Intervalos nubosos con lluvia escasa" -> context.getString(R.string.intervalos_nubosos_lluvia)
        "Intervalos nubosos con lluvia" -> context.getString(R.string.intervalos_nubosos_lluvia)

        else -> estadoCielo // Si no coincide, devuelve el valor original
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WeatherApp(initialLocation: Localidad = Localidad("Getafe", "28065", "Madrid")) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                initialLocation = initialLocation, // <-- Pasar como parámetro
                onHourlyForecastClick = { localidadJson ->
                    navController.navigate("hourly/$localidadJson")
                },
                onDailyDetailClick = { localidadJson, diaPrediccionJson ->
                    navController.navigate("daily/$localidadJson/$diaPrediccionJson")
                },
                navController = navController
            )
        }
        composable("hourly/{localidad}") { backStackEntry ->
            val localidadJson = backStackEntry.arguments?.getString("localidad")
            val localidad = Gson().fromJson(localidadJson, Localidad::class.java)
            HourlyForecastScreen(
                localidad = localidad,
                onBackClick = { navController.navigateUp() }
            )
        }
        composable("daily/{localidad}/{diaPrediccion}") { backStackEntry ->
            val localidadJson = backStackEntry.arguments?.getString("localidad")
            val diaPrediccionJson = backStackEntry.arguments?.getString("diaPrediccion")

            val localidad = Gson().fromJson(localidadJson, Localidad::class.java)
            val diaPrediccion = Gson().fromJson(diaPrediccionJson, DiaPrediccion::class.java)

            DailyDetailScreen(
                diaPrediccion = diaPrediccion,
                localidad = localidad,
                onBackClick = { navController.navigateUp() }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}

@Composable
fun getImageResourceId(estadoCielo: String): Int {
    val context = LocalContext.current
    return when (estadoCielo) {
        context.getString(R.string.cubierto_con_lluvia) -> R.drawable.cubierto_lluvia
        context.getString(R.string.cubierto_con_tormenta) -> R.drawable.cubierto_tormenta
        context.getString(R.string.muy_nuboso_con_lluvia) -> R.drawable.muy_cubierto_lluvia
        context.getString(R.string.cubierto_con_lluvia_escasa) -> R.drawable.lluvia
        context.getString(R.string.cubierto) -> R.drawable.cubierto
        context.getString(R.string.muy_nuboso_tormenta) -> R.drawable.cubierto_tormenta
        context.getString(R.string.intervalos_nubosos) -> R.drawable.intervalos_nubosos
        context.getString(R.string.intervalos_nubosos_lluvia) -> R.drawable.intervalo_nuboso_lluvia
        context.getString(R.string.poco_nuboso) -> R.drawable.poco_nuboso
        context.getString(R.string.cielo_despejado) -> R.drawable.cielo_despejado
        context.getString(R.string.nuboso_con_lluvia) -> R.drawable.nuboso_con_lluvia
        context.getString(R.string.intervalos_nubosos_tormenta_lluvia_escasa) -> R.drawable.intervalo_nuboso_tormenta_lluvia_escasa
        context.getString(R.string.intervalos_nubosos_lluvia_escasa) -> R.drawable.intervalo_nuboso_lluvia_escasa
        context.getString(R.string.nuboso_con_tormenta) -> R.drawable.nuboso_con_tormenta
        context.getString(R.string.muy_nuboso_con_lluvia_escasa) -> R.drawable.cubierto_lluvia
        context.getString(R.string.nuboso_con_lluvia_escasa) -> R.drawable.nuboso_con_lluvia_escasa
        context.getString(R.string.niebla) -> R.drawable.niebla
        context.getString(R.string.nieve) -> R.drawable.lluvia_nieve
        context.getString(R.string.nuboso) -> R.drawable.nuboso
        context.getString(R.string.very_cloudy) -> R.drawable.muy_nuboso
        context.getString(R.string.nubes_altas) -> R.drawable.nubes_altas
        context.getString(R.string.bruma) -> R.drawable.bruma

        else -> R.drawable.nubes_dispersa
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    initialLocation: Localidad, // <--- Nuevo parámetro
    onHourlyForecastClick: (String) -> Unit,
    onDailyDetailClick: (String, String) -> Unit,
    navController: NavController
) {
    val colors = getThemeColors()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current



    fun saveSelectedLocation(context: Context, localidad: Localidad) {
        val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString("selectedLocation", Gson().toJson(localidad))
        }
    }

    fun loadSelectedLocation(context: Context): Localidad? {
        val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val json = prefs.getString("selectedLocation", null)
        return json?.let { Gson().fromJson(it, Localidad::class.java) }
    }

    // Recuperar localidad guardada o usar la predeterminada
    var selectedLocalidad by remember {
        mutableStateOf(loadSelectedLocation(context) ?: initialLocation)
    }



   // var selectedLocalidad by remember { mutableStateOf(Localidad("Getafe", "28065", "Madrid")) }

    var prediccionDias by remember { mutableStateOf<List<DiaPrediccion>>(emptyList()) }
    var cabecera by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    var showDialog by remember { mutableStateOf(false) }

    var isDropdownVisible by remember { mutableStateOf(false) } // Nueva variable para controlar el desplegable
    // Cargar todas las localidades desde el JSON
//    val todasLocalidades by remember {
//        mutableStateOf(flattenLocalidades(loadLocalidadesFromAssets(context)))
//    }
    val todasLocalidades by remember { mutableStateOf(loadAndFlattenLocalidades(context)) }

    // Estado para el texto de búsqueda usando TextFieldValue para mejor control
    var searchText by remember { mutableStateOf(TextFieldValue(selectedLocalidad.nombre)) }
    var suggestions by remember { mutableStateOf(listOf<Localidad>()) }
//En la lógica de LaunchedEffect, agregamos una bandera (isFirstTime) para evitar que el dropdown se active al principio:

    var isFirstTime by remember { mutableStateOf(true) }

    fun actualizarDatos() {
        scope.launch(Dispatchers.IO) {
            val codigo = selectedLocalidad.codigo
            val (header, dias) = fetchWeatherData(codigo, context)
            cabecera = header ?: context.getString(R.string.error_fetching_data)
            prediccionDias = dias
            isDropdownVisible = false
        }
    }

    LaunchedEffect(Unit) {
       // ThemeState.isDarkMode.value = loadThemePreference(context)
        actualizarDatos()
    }
    // Actualizar cuando se seleccione nueva localidad
    LaunchedEffect(selectedLocalidad) {
        saveSelectedLocation(context, selectedLocalidad)
    }

    // Actualizar sugerencias según el texto de búsqueda
    LaunchedEffect(searchText) {
        if (isFirstTime) {
            isFirstTime = false
            return@LaunchedEffect
        }
        suggestions = if (searchText.text.isNotBlank()) {
            todasLocalidades.filter {
                it.nombre.contains(searchText.text, ignoreCase = true) && it.provincia != "provincias"
                        //|| it.provincia.contains(searchText.text, ignoreCase = true)
            }.take(10) // Limitar a 10 sugerencias

        } else {
            emptyList()
        }
        isDropdownVisible =
            searchText.text.isNotBlank() && suggestions.isNotEmpty() // Mostrar solo si hay texto y sugerencias
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            shape = RoundedCornerShape(CardCornerRadius),
            backgroundColor = colors.surface,
            title = {
                Text(
                    text = context.getString(R.string.info_title),
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = SmallPadding)
                ) {
                    Text(
                        text = "_",
                        textAlign = TextAlign.Justify,
                        color = colors.surface,
                        modifier = Modifier.padding(vertical = DefaultPadding),
                        lineHeight = 8.sp
                    )
                    Image(
                        painter = painterResource(id = R.drawable.icono_clima),
                        contentDescription = context.getString(R.string.app_icon_description),
                        modifier = Modifier
                            .size(160.dp)
                            .padding(vertical = DefaultPadding)
                    )
                    Text(
                        text = context.getString(R.string.info_description),
                        textAlign = TextAlign.Justify,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(vertical = DefaultPadding),
                        lineHeight = 22.sp
                    )
                    Text(
                        text = context.getString(R.string.info_donation),
                        textAlign = TextAlign.Justify,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(vertical = DefaultPadding),
                        lineHeight = 22.sp
                    )
                    Button(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    "https://www.paypal.com/donate/?business=NXNTZ9Y9QTPD8&no_recurring=0&item_name=${
                                        context.getString(
                                            R.string.donation_item_name
                                        )
                                    }&currency_code=EUR"
                                )
                            )
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF0070BA)),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = context.getString(R.string.donate_button),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = SmallPadding)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.primary)
                ) {
                    Text(
                        text = context.getString(R.string.close_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DefaultPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DefaultPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = context.getString(R.string.app_name),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SmallPadding)
                ) {
                    IconButton(onClick = {
                        val localidadJson = Uri.encode(Gson().toJson(selectedLocalidad))
                        onHourlyForecastClick(localidadJson)
                    }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = context.getString(R.string.hourly_forecast),
                            tint = colors.primary
                        )
                    }

                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = context.getString(R.string.settings_title),
                            tint = colors.primary
                        )
                    }
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = context.getString(R.string.info_title),
                            tint = colors.primary
                        )
                    }

                }
            }

            // Buscador de localidades
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(vertical = DefaultPadding)
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { newText -> searchText = newText },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DefaultPadding),
                    label = {
                        Text(
                            text = context.getString(R.string.select_location),
                            color = colors.textSecondary
                        )
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = colors.textPrimary,
                        backgroundColor = colors.surface,
                        cursorColor = colors.primary,
                        focusedIndicatorColor = Color.Transparent,  // Quitamos la línea azul cuando tiene foco
                        unfocusedIndicatorColor = Color.Transparent,  // Quitamos la línea cuando no tiene foco
                        focusedLabelColor = colors.textSecondary,
                        unfocusedLabelColor = colors.textSecondary
                    ),
                    shape = RoundedCornerShape(CardCornerRadius),  // Usamos la misma variable de radio que las tarjetas
                    singleLine = true
                )

                // Mostrar sugerencias si hay texto y sugerencias disponibles
                if (isDropdownVisible) { // Usar la variable de estado para controlar visibilidad
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = SmallPadding),
                        backgroundColor = colors.surface,
                        elevation = CardElevation
                    ) {
                        Column {
                            suggestions.forEach { localidad ->
                                Text(
                                    text = "${localidad.nombre} (${localidad.provincia})",
                                    color = colors.textPrimary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (selectedLocalidad.nombre == localidad.nombre )
                                                //&& selectedLocalidad.provincia == localidad.provincia
                                                colors.primary.copy(alpha = 0.3f)
                                            else colors.surface
                                        )
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                        .clickable {
                                            selectedLocalidad = localidad
                                            searchText = TextFieldValue(localidad.nombre)
                                            isDropdownVisible = false // Cerrar el desplegable al seleccionar
                                            actualizarDatos()
                                        }
                                )
                            }
                        }
                    }
                }
            }

            // Lista de predicciones
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                prediccionDias.forEach { dia ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = SmallPadding)
                            .shadow(CardElevation, RoundedCornerShape(CardCornerRadius))
                            .clickable {

                                // Convert both localidad and diaPrediccion to JSON for navigation
                                val localidadJson = Uri.encode(Gson().toJson(selectedLocalidad))
                                val diaPrediccionJson = Uri.encode(Gson().toJson(dia))
                                onDailyDetailClick(localidadJson, diaPrediccionJson)

                            },
                        shape = RoundedCornerShape(CardCornerRadius),
                        backgroundColor = colors.surface,
                        elevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(DefaultPadding)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = SmallPadding),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dia.obtenerFechaFormateada(context),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                                Card(
                                    backgroundColor = colors.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = 0.dp
                                ) {
                                    Text(
                                        text = context.getString(
                                            R.string.rain_label,
                                            dia.probPrecipitacion
                                        ),
                                        color = colors.primary,
                                        modifier = Modifier.padding(
                                            horizontal = SmallPadding,
                                            vertical = TinyPadding
                                        ),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = translateSkyState(context, dia.estadoCielo),
                                    fontSize = 14.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(top = TinyPadding)
                                )
                            }

                            Divider(
                                color = colors.divider,
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = SmallPadding)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(TinyPadding)
                                ) {
                                    Text(
                                        text = "${dia.tempMax}°",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "${dia.tempMin}°",
                                        fontSize = 24.sp,
                                        color = colors.textSecondary,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(SmallPadding))
                                    val imageResourceId = getImageResourceId(dia.estadoCielo)
                                    Image(
                                        painter = painterResource(id = imageResourceId),
                                        contentDescription = translateSkyState(
                                            context,
                                            dia.estadoCielo
                                        ),
                                        modifier = Modifier.size(35.dp)
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = TinyPadding)
                                    ) {
                                        Text(
                                            text = context.getString(R.string.humidity_label2),
                                            color = colors.textSecondary
                                        )
                                        Text(
                                            text = "${dia.humMax}%",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "/${dia.humMin}%",
                                            fontSize = 14.sp,
                                            color = colors.textSecondary,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = context.getString(R.string.wind_label2),
                                            color = colors.textSecondary
                                        )
                                        Text(
                                            text = "${dia.vientoDir} ${dia.vientoVel}km/h",
                                            color = colors.textPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Divider(
                                color = colors.divider,
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = SmallPadding)
                            )

                            val (esRecomendable, mensaje, puntuacion) = dia.evaluarCondicionesCiclismo(
                                context
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = context.getString(R.string.conditions_cycling),
                                        color = colors.textSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = mensaje,
                                        color = when {
                                            puntuacion >= 80 -> colors.accentGreen
                                            puntuacion >= 60 -> colors.accentOrange
                                            else -> colors.accentRed
                                        },
                                        modifier = Modifier.padding(top = TinyPadding)
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = DefaultPadding)
                                ) {
                                    Text(
                                        text = "$puntuacion%",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            puntuacion >= 80 -> colors.accentGreen
                                            puntuacion >= 60 -> colors.accentOrange
                                            else -> colors.accentRed
                                        },
                                        modifier = Modifier.padding(end = SmallPadding)
                                    )
                                    CircularProgressIndicator(
                                        percentage = puntuacion.toFloat(),
                                        color = when {
                                            puntuacion >= 80 -> colors.accentGreen
                                            puntuacion >= 60 -> colors.accentOrange
                                            else -> colors.accentRed
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun loadAndFlattenLocalidades(context: Context): List<Localidad> {
    val json = context.assets.open("localidades.json").bufferedReader().use { it.readText() }
    val localidadesResponse = Gson().fromJson(json, Array<LocalidadWrapper>::class.java)
    return localidadesResponse.flatMap { provincia ->
        provincia.data.map { (nombre, codigo) ->
            Localidad(nombre, codigo, provincia.source) // Añadimos la provincia
        }
    }
}

data class LocalidadWrapper(
    val source: String,
    val data: Map<String, String>
)
@Composable
fun CircularProgressIndicator(
    percentage: Float,
    color: Color
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp)
            .padding(SmallPadding)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Fondo gris con transparencia
            drawArc(
                color = color.copy(alpha = 0.15f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )
            // Arco de progreso con sombra
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = percentage * 3.6f,
                useCenter = false,
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )
        }
        // Texto del porcentaje en el centro
        Text(
            text = "${percentage.toInt()}%",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DailyDetailScreen(
    diaPrediccion: DiaPrediccion,
    localidad: Localidad,
    onBackClick: () -> Unit
) {
    val colors = getThemeColors()
    val context = LocalContext.current
    val (esRecomendable, mensaje, puntuacion) = diaPrediccion.evaluarCondicionesCiclismo(context)
    val scope = rememberCoroutineScope()
    var prediccionHoraria by remember { mutableStateOf<List<PrediccionHoraria>>(emptyList()) }


    // Obtener datos horarios al cargar la pantalla
    LaunchedEffect(localidad) {
        scope.launch(Dispatchers.IO) {
            val codigo = localidad.codigo
            val todasPrediccionesHorarias = fetchHourlyData(codigo)
            // Filtrar solo las predicciones del día seleccionado
            prediccionHoraria =
                todasPrediccionesHorarias.filter { it.dia.contains(diaPrediccion.fecha) }

        }
    }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DefaultPadding)
        ) {
            // Top Bar with Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DefaultPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = context.getString(R.string.button_back),
                        tint = colors.textPrimary
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = diaPrediccion.obtenerFechaFormateada(context),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = localidad.nombre,
                        fontSize = 16.sp,
                        color = colors.primary
                    )
                }
            }

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = DefaultPadding)
            ) {
                // Temperature Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SmallPadding),
                    backgroundColor = colors.surface,
                    shape = RoundedCornerShape(CardCornerRadius),
                    elevation = CardElevation
                ) {
                    Column(
                        modifier = Modifier.padding(DefaultPadding)
                    ) {
                        Text(
                            text = context.getString(R.string.temperature),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = SmallPadding),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "${diaPrediccion.tempMax}°",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.accentOrange
                            )
                            Spacer(modifier = Modifier.width(SmallPadding))
                            val imageResourceId = getImageResourceId(diaPrediccion.estadoCielo)
                            Image(
                                painter = painterResource(id = imageResourceId),
                                contentDescription = translateSkyState(
                                    context,
                                    diaPrediccion.estadoCielo
                                ),
                                modifier = Modifier.size(50.dp)
                            )
                            Text(
                                text = "${diaPrediccion.tempMin}°",
                                fontSize = 24.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
                // Gráfico de temperatura por horas
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SmallPadding),
                    backgroundColor = colors.surface,
                    shape = RoundedCornerShape(CardCornerRadius),
                    elevation = CardElevation
                ) {
                    Column(
                        modifier = Modifier.padding(DefaultPadding)
                    ) {
                        Text(
                            text = context.getString(R.string.temperatura_horas),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(MediumPadding))
                        TemperatureLineChart(prediccionHoraria, colors, context)
                    }
                }

                // Sky Conditions Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SmallPadding),
                    backgroundColor = colors.surface,
                    shape = RoundedCornerShape(CardCornerRadius),
                    elevation = CardElevation
                ) {
                    Column(
                        modifier = Modifier.padding(DefaultPadding)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = SmallPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = context.getString(R.string.sky_conditions),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.width(SmallPadding))
                            Card(
                                backgroundColor = colors.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp),
                                elevation = 0.dp
                            ) {
                                Text(
                                    text = context.getString(
                                        R.string.rain_label,
                                        diaPrediccion.probPrecipitacion
                                    ),
                                    color = colors.primary,
                                    modifier = Modifier.padding(
                                        horizontal = SmallPadding,
                                        vertical = TinyPadding
                                    ),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Text(
                            text = translateSkyState(context, diaPrediccion.estadoCielo),
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )


                    }
                }

                // Humidity and Wind Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SmallPadding),
                    backgroundColor = colors.surface,
                    shape = RoundedCornerShape(CardCornerRadius),
                    elevation = CardElevation
                ) {
                    Column(
                        modifier = Modifier.padding(DefaultPadding)
                    ) {
                        Text(
                            text = context.getString(R.string.humidity_wind),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = SmallPadding),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = context.getString(R.string.humidity_label2),
                                    color = colors.textSecondary
                                )
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(TinyPadding)
                                ) {
                                    Text(
                                        text = "${diaPrediccion.humMax}%",
                                        fontSize = 18.sp,
                                        color = colors.primary
                                    )
                                    Spacer(modifier = Modifier.width(SmallPadding))
                                    Text(
                                        text = "${diaPrediccion.humMin}%",
                                        fontSize = 14.sp,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = context.getString(R.string.wind_label2),
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "${diaPrediccion.vientoDir} ${diaPrediccion.vientoVel} km/h",
                                    fontSize = 18.sp,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }

                // Cycling Conditions Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SmallPadding),
                    backgroundColor = colors.surface,
                    shape = RoundedCornerShape(CardCornerRadius),
                    elevation = CardElevation
                ) {
                    Column(
                        modifier = Modifier.padding(DefaultPadding)
                    ) {
                        Text(
                            text = context.getString(R.string.conditions_cycling),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = SmallPadding),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mensaje,
                                    color = when {
                                        puntuacion >= 80 -> colors.accentGreen
                                        puntuacion >= 60 -> colors.accentOrange
                                        else -> colors.accentRed
                                    },
                                    modifier = Modifier.padding(end = SmallPadding)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$puntuacion%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        puntuacion >= 80 -> colors.accentGreen
                                        puntuacion >= 60 -> colors.accentOrange
                                        else -> colors.accentRed
                                    },
                                    modifier = Modifier.padding(end = SmallPadding)
                                )
                                CircularProgressIndicator(
                                    percentage = puntuacion.toFloat(),
                                    color = when {
                                        puntuacion >= 80 -> colors.accentGreen
                                        puntuacion >= 60 -> colors.accentOrange
                                        else -> colors.accentRed
                                    }
                                )
                            }
                        }
                    }
                }

                // Precipitation Bar Chart Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SmallPadding),
                    backgroundColor = colors.surface,
                    shape = RoundedCornerShape(CardCornerRadius),
                    elevation = CardElevation
                ) {
                    Column(
                        modifier = Modifier.padding(DefaultPadding)
                    ) {
                        Text(
                            text = context.getString(R.string.rain_probability),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(SmallPadding))
                        PrecipitationBarChart(
                            //precipitacionPorPeriodo = diaPrediccion.precipitacionPorPeriodo,
                            //cambio
                            prediccionHoraria = prediccionHoraria,
                            colors = colors,
                            context = context
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TemperatureLineChart(prediccionHoraria: List<PrediccionHoraria>, colors: ThemeColors, context: Context) {
    val maxHeight = 150.dp
    val spacing = 50.dp
    val startOffset = 30.dp // Agregar margen izquierdo
    val points = prediccionHoraria.mapNotNull {
        val temp = it.temperatura.toFloatOrNull()
        val hour = it.hora.split(":")[0].toIntOrNull()
        if (temp != null && temp.isFinite() && hour != null) hour to temp else null
    }

    if (points.isEmpty()) {
        Text(
            text = context.getString(R.string.no_data_temperatura),
            color = colors.textSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    val verticalFractions = temperatureChartFractions(points.map { it.second })

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(maxHeight + 40.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        Canvas(modifier = Modifier
            .width(startOffset + spacing * points.size)
            .height(maxHeight + 40.dp)
        ) {
            val plotTop = 24.dp.toPx()
            val plotBottom = maxHeight.toPx() - 12.dp.toPx()
            val positions = verticalFractions.mapIndexed { index, fraction ->
                Offset(
                    startOffset.toPx() + index * spacing.toPx(),
                    plotTop + fraction * (plotBottom - plotTop)
                )
            }

            val path = Path()
            positions.forEachIndexed { index, position ->
                if (index == 0) {
                    path.moveTo(position.x, position.y)
                } else {
                    path.lineTo(position.x, position.y)
                }
            }
            drawPath(
                path = path,
                color = colors.primary,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            points.forEachIndexed { index, (hour, temp) ->
                val (x, y) = positions[index]
                drawCircle(
                    color = colors.accentOrange,
                    center = Offset(x, y),
                    radius = 6f
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "$temp°",
                    x,
                    y - 10f,
                    Paint().apply {
                        color = colors.textPrimary.toArgb()
                        textSize = 30f
                        textAlign = Paint.Align.CENTER
                    }
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "$hour:00",
                    x,
                    maxHeight.toPx() + 30f,
                    Paint().apply {
                        color = colors.textSecondary.toArgb()
                        textSize = 30f
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

// Componente para el gráfico de barras con scroll horizontal
@Composable
fun PrecipitationBarChart(
    prediccionHoraria: List<PrediccionHoraria>,
    colors: ThemeColors,
    context: Context
) {
    // Verificar si hay datos disponibles
    if (prediccionHoraria.isEmpty()) {
        Text(
            text = stringResource(R.string.no_data_precipitation),
            modifier = Modifier.padding(DefaultPadding),
            color = colors.textSecondary
        )
        return
    }

    // Registrar información sobre los datos disponibles
    Log.d("PrecipitationBarChart", "Total de predicciones horarias: ${prediccionHoraria.size}")
    prediccionHoraria.forEach { prediccion ->
        Log.d("PrecipitationBarChart", "Hora: ${prediccion.hora}, Prob: ${prediccion.probPrecipitacion}")
    }

    // Verificar si hay datos de probabilidad de precipitación válidos
    val hayDatosValidos = prediccionHoraria.any {
        val probValue = it.probPrecipitacion.toIntOrNull() ?: 0
        probValue > 0
    }

    if (!hayDatosValidos) {
        Log.d("PrecipitationBarChart", "No hay datos válidos de probabilidad de precipitación")
        Text(
            text = "No hay datos de probabilidad de precipitación disponibles",
            modifier = Modifier.padding(DefaultPadding),
            color = colors.textSecondary
        )
        return
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DefaultPadding)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .background(colors.surface, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            PrecipitationBarChartContent(prediccionHoraria, colors)
        }
    }
}

@Composable
fun PrecipitationBarChartContent(
    periodos: List<PrediccionHoraria>,
    colors: ThemeColors
) {
    val maxProbValue = 100  // Valor máximo fijo para la escala

    val barWidth = 60.dp
    val spacing = 12.dp
    val maxHeight = 200.dp

    // Filtrar periodos con valores válidos de probabilidad de precipitación
    val periodosValidos = periodos.filter {
        val probValue = it.probPrecipitacion.toIntOrNull() ?: 0
        probValue > 0 // Solo incluir periodos con probabilidad > 0
    }

    // Si no hay datos válidos, mostrar un mensaje
    if (periodosValidos.isEmpty()) {
        Text(
            text = "No hay datos de precipitación disponibles",
            color = colors.textSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    // Usar periodos válidos para el gráfico
    Canvas(
        modifier = Modifier
            .width((barWidth + spacing) * periodosValidos.size - spacing)
            .height(maxHeight + 40.dp)
            .padding(bottom = 12.dp)
    ) {
        periodosValidos.forEachIndexed { index, prediccion ->
            val probValue = prediccion.probPrecipitacion.toIntOrNull() ?: 0
            Log.d("PrecipitationBarChart", "Dibujando barra para hora ${prediccion.hora} con valor $probValue")

            val barHeightPercentage = probValue / 100f  // Siempre escala sobre 100%
            val barHeight = barHeightPercentage * maxHeight.toPx()

            val x = index * (barWidth.toPx() + spacing.toPx())

            // Dibujar la barra
            drawRect(
                color = when {
                    probValue >= 70 -> colors.accentRed
                    probValue >= 40 -> colors.accentOrange
                    else -> colors.accentGreen
                },
                topLeft = Offset(x, maxHeight.toPx() - barHeight),
                size = Size(barWidth.toPx(), barHeight)
            )

            // Etiqueta de la hora debajo
            drawContext.canvas.nativeCanvas.apply {
                save()
                translate(x + barWidth.toPx() / 2, maxHeight.toPx() + 30f)
                rotate(0f)
                drawText(
                    prediccion.hora,
                    0f,
                    0f,
                    Paint().apply {
                        color = colors.primary.toArgb()
                        textSize = 30f
                        textAlign = Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
                restore()
            }

            // Valor encima de la barra
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    "${prediccion.probPrecipitacion}%",
                    x + barWidth.toPx() / 2,
                    if (barHeight > 30) maxHeight.toPx() - barHeight - 10f else maxHeight.toPx() - 40f,
                    Paint().apply {
                        color = colors.primary.toArgb()
                        textSize = 30f
                        textAlign = Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
            }
        }
    }
}

// Función para obtener y parsear los datos del XML

suspend fun fetchWeatherData(codigo: String, context: Context): Pair<String?, List<DiaPrediccion>> {
    val client = OkHttpClient()
    val url = "https://www.aemet.es/xml/municipios/localidad_$codigo.xml"
    val request = Request.Builder().url(url).build()

    return try {
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return Pair(
            "Error al descargar datos: ${response.code}",
            emptyList()
        )

        val xmlData = response.body?.string() ?: return Pair("No se recibieron datos", emptyList())
        parseWeatherData(xmlData, context)
    } catch (e: Exception) {
        Pair("Error procesando datos: ${e.message}", emptyList())
    }
}

// Parsear el XML y construir el resultado


fun parseWeatherData(xml: String, context: Context): Pair<String, List<DiaPrediccion>> {
    val factory = XmlPullParserFactory.newInstance()
    factory.isNamespaceAware = true
    val parser = factory.newPullParser()
    parser.setInput(StringReader(xml))
    var eventType = parser.eventType

    var nombreLocalidad = ""
    var inPrediccion = false
    var inDia = false

    val diasPrediccion = mutableListOf<DiaPrediccion>()
    var diaActual: MutableMap<String, String> = mutableMapOf()
    var probPrecipitacionPeriodos: MutableList<Pair<String, String>> = mutableListOf()


    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> {
                when (parser.name) {
                    "nombre" -> nombreLocalidad = parser.nextText()
                    "prediccion" -> inPrediccion = true
                    "dia" -> {
                        if (inPrediccion) {
                            inDia = true
                            diaActual = mutableMapOf()
                            diaActual["fecha"] = parser.getAttributeValue(null, "fecha")
                            probPrecipitacionPeriodos = mutableListOf() // Reiniciar para cada día
                        }
                    }

                    "prob_precipitacion" -> {
                        if (inDia) {
                            val periodo = parser.getAttributeValue(null, "periodo") ?: ""
                            val valor = parser.nextText().trim()
                            if (valor.isNotEmpty() && valor.all { it.isDigit() }) { // Solo añadir si es un número válido
                                val valoresActuales =
                                    diaActual["probPrecipitacionLista"]?.split(",")?.toMutableList()
                                        ?: mutableListOf()
                                valoresActuales.add(valor)
                                diaActual["probPrecipitacionLista"] =
                                    valoresActuales.joinToString(",")
                                probPrecipitacionPeriodos.add(Pair(periodo, valor))
                            }
                        }
                    }

                    "estado_cielo" -> {
                        if (inDia) {
                            val descripcion = parser.getAttributeValue(null, "descripcion")?.trim()
                            val weatherCondition =
                                descripcion  // Esta es la condición del clima que puedes cambiar según necesites

                            val weatherText = when (weatherCondition) {
                                "Cubierto con lluvia" -> context.getString(R.string.cubierto_con_lluvia)
                                "Cubierto con tormenta" -> context.getString(R.string.cubierto_con_tormenta)
                                "Muy nuboso con lluvia" -> context.getString(R.string.muy_nuboso_con_lluvia)
                                "Cubierto con lluvia escasa" -> context.getString(R.string.cubierto_con_lluvia_escasa)
                                "Cubierto" -> context.getString(R.string.cubierto)
                                "Muy nuboso con tormenta" -> context.getString(R.string.muy_nuboso_tormenta)
                                "Cielo despejado" -> context.getString(R.string.cielo_despejado)
                                "Despejado" -> context.getString(R.string.cielo_despejado)
                                "Nuboso con lluvia" -> context.getString(R.string.nuboso_con_lluvia)
                                "Cubierto con tormenta con lluvia escasa" -> context.getString(R.string.cubierto_tormenta_lluvia_escasa)
                                "Poco nuboso" -> context.getString(R.string.poco_nuboso)
                                "Intervalos nubosos" -> context.getString(R.string.intervalos_nubosos)
                                "Intervalos nubosos con lluvia escasa" -> context.getString(R.string.intervalos_nubosos_lluvia)
                                "Intervalos nubosos con lluvia" -> context.getString(R.string.intervalos_nubosos_lluvia)
                                "Nuboso con tormenta" -> context.getString(R.string.nuboso_con_tormenta)
                                "Muy nuboso" -> context.getString(R.string.very_cloudy)
                                "Intervalos nubosos con tormenta y lluvia escasa" -> context.getString(R.string.intervalos_nubosos_lluvia_escasa)
                                "Muy nuboso con lluvia escasa" -> context.getString(R.string.muy_nuboso_con_lluvia_escasa)
                                "Nuboso con lluvia escasa" -> context.getString(R.string.nuboso_con_lluvia_escasa)
                                "Niebla " -> context.getString(R.string.niebla)
                                "Nieve" -> context.getString(R.string.nieve)
                                "Nuboso" -> context.getString(R.string.nuboso)
                                "Bruma" -> context.getString(R.string.bruma)
                                "Nubes altas" -> context.getString(R.string.nubes_altas)
                                "Cubierto con tormenta y lluvia escasa" -> context.getString(R.string.cubierto_tormenta_lluvia_escasa)
                                else -> context.getString(R.string.condicion_desconocida) // Valor por defecto en caso de que no coincida con ninguna opción
                            }
                            if (!descripcion.isNullOrEmpty()) {
                                diaActual["estadoCieloLista"] =
                                    (diaActual["estadoCieloLista"] ?: "") + "$weatherText,"
                            }
                        }
                    }

                    "temperatura" -> {
                        if (inDia) {
                            while (!(eventType == XmlPullParser.END_TAG && parser.name == "temperatura")) {
                                if (eventType == XmlPullParser.START_TAG) {
                                    when (parser.name) {
                                        "maxima" -> diaActual["tempMax"] = parser.nextText()
                                        "minima" -> diaActual["tempMin"] = parser.nextText()
                                    }
                                }
                                eventType = parser.next()
                            }
                        }
                    }

                    "humedad_relativa" -> {
                        if (inDia) {
                            while (!(eventType == XmlPullParser.END_TAG && parser.name == "humedad_relativa")) {
                                if (eventType == XmlPullParser.START_TAG) {
                                    when (parser.name) {
                                        "maxima" -> diaActual["humMax"] = parser.nextText()
                                        "minima" -> diaActual["humMin"] = parser.nextText()
                                    }
                                }
                                eventType = parser.next()
                            }
                        }
                    }

                    "viento" -> {
                        if (inDia && !diaActual.containsKey("vientoDir")) {
                            var direccion = ""
                            var velocidad = ""
                            var encontrado = false
                            val currentVientoDepth = parser.depth

                            while (!(eventType == XmlPullParser.END_TAG && parser.name == "viento" && parser.depth == currentVientoDepth)) {
                                if (eventType == XmlPullParser.START_TAG) {
                                    when (parser.name) {
                                        "direccion" -> {
                                            direccion = parser.nextText()
                                            if (direccion.isNotEmpty()) {
                                                encontrado = true
                                            }
                                        }

                                        "velocidad" -> {
                                            velocidad = parser.nextText()
                                            if (velocidad.isNotEmpty()) {
                                                encontrado = true
                                            }
                                        }
                                    }
                                }
                                if (encontrado && direccion.isNotEmpty() && velocidad.isNotEmpty()) {
                                    diaActual["vientoDir"] = direccion
                                    diaActual["vientoVel"] = velocidad
                                    break
                                }
                                eventType = parser.next()
                            }
                        }
                        // Si ya tenemos datos de viento, saltamos este nodo
                        else if (inDia && diaActual.containsKey("vientoDir")) {
                            var depth = 1
                            while (depth != 0) {
                                eventType = parser.next()
                                if (eventType == XmlPullParser.START_TAG) depth++
                                else if (eventType == XmlPullParser.END_TAG) depth--
                            }
                        }
                    }
                }
            }

            XmlPullParser.END_TAG -> {
                if (parser.name == "dia" && inDia) {

                    val probPrecipitacionLista = diaActual["probPrecipitacionLista"]
                        ?.split(",")
                        ?.filter { it.isNotEmpty() }
                        ?.mapNotNull { it.toIntOrNull() }

                    val probPrecipitacionPromedio = if (!probPrecipitacionLista.isNullOrEmpty()) {
                        (probPrecipitacionLista.sum()
                            .toDouble() / probPrecipitacionLista.size).toInt().toString()
                    } else {
                        "0"
                    }

                    // Seleccionar el primer estado del cielo que no esté vacío
                    val estadoCieloLista = diaActual["estadoCieloLista"]
                        ?.split(",")
                        ?.filter { it.isNotEmpty() }

                    val estadoCieloSeleccionado =
                        estadoCieloLista?.firstOrNull() ?: context.getString(R.string.sindatos)

                    diasPrediccion.add(
                        DiaPrediccion(
                            fecha = diaActual["fecha"] ?: "",
                            probPrecipitacion = probPrecipitacionPromedio,
                            estadoCielo = estadoCieloSeleccionado,
                            tempMax = diaActual["tempMax"] ?: "",
                            tempMin = diaActual["tempMin"] ?: "",
                            humMax = diaActual["humMax"] ?: "",
                            humMin = diaActual["humMin"] ?: "",
                            vientoDir = diaActual["vientoDir"] ?: "",
                            vientoVel = diaActual["vientoVel"] ?: "",
                            precipitacionPorPeriodo = probPrecipitacionPeriodos // Añadir los períodos
                        )
                    )
                    inDia = false
                }
            }
        }
        eventType = parser.next()
    }

    val cabecera = "Localidad: $nombreLocalidad"
    return Pair(cabecera, diasPrediccion)
}

// Utilidad para obtener el tag padre
fun XmlPullParser.parentTag(): String {
    var depth = this.depth
    while (depth > 0) {
        depth--
        val event = this.eventType
        if (event == XmlPullParser.START_TAG) return this.name
        this.next()
    }
    return ""
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HourlyForecastScreen(
    localidad: Localidad, // Cambiar de String a Localidad
    onBackClick: () -> Unit
) {
    val colors = getThemeColors()
    var prediccionHoraria by remember { mutableStateOf<List<PrediccionHoraria>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDay(dayString: String): String {
        try {
            val datePart = dayString.split(", ")[1] // "01/01/2025"
            val fecha = LocalDate.parse(datePart, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            val locale = context.resources.configuration.locales[0]
            val diaSemana = fecha.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
                .replaceFirstChar { it.uppercase() }
            return "$diaSemana, $datePart"
        } catch (e: Exception) {
            return dayString
        }
    }

    LaunchedEffect(localidad) {
        scope.launch(Dispatchers.IO) {
            val codigo = localidad.codigo // Usar el código directamente
            prediccionHoraria = fetchHourlyData(codigo)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = context.getString(R.string.button_back),
                        tint = colors.textPrimary
                    )
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = context.getString(R.string.hourly_forecast),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = localidad.nombre, // Usar el nombre de la localidad
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                prediccionHoraria
                    .filter { prediccion -> prediccion.temperatura.isNotEmpty() }
                    .forEach { prediccion ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = 2.dp,
                            backgroundColor = colors.surface
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = formatDay(prediccion.dia),
                                    fontSize = 16.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = prediccion.hora,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = translateSkyState(
                                                context,
                                                prediccion.estadoCielo
                                            ),
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                    Text(
                                        text = "${prediccion.temperatura}°",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                }

                                Divider(
                                    color = colors.divider,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = context.getString(
                                                R.string.rain_label2,
                                                prediccion.precipitacion
                                            ),
                                            color = colors.primary
                                        )
                                        Text(
                                            text = context.getString(
                                                R.string.humidity_label,
                                                prediccion.humedad
                                            ),
                                            color = colors.textSecondary
                                        )
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = context.getString(
                                                R.string.wind_label,
                                                prediccion.vientoDir
                                            ),
                                            color = colors.textSecondary
                                        )
                                        Text(
                                            text = "${prediccion.vientoVel} km/h",
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }
}

data class PrediccionHoraria(
    val dia: String,
    val hora: String,
    val temperatura: String,
    val estadoCielo: String,
    val precipitacion: String,
    val probPrecipitacion: String,
    val humedad: String,
    val vientoDir: String,
    val vientoVel: String
)

suspend fun fetchHourlyData(codigo: String): List<PrediccionHoraria> {
    val client = OkHttpClient()
    val url = "https://www.aemet.es/xml/municipios_h/localidad_h_$codigo.xml"
    val request = Request.Builder().url(url).build()

    return try {
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val xmlData = response.body?.string() ?: return emptyList()
        parseHourlyData(xmlData)
    } catch (e: Exception) {
        emptyList()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun parseHourlyData(xml: String): List<PrediccionHoraria> {
    val factory = XmlPullParserFactory.newInstance()
    factory.isNamespaceAware = true
    val parser = factory.newPullParser()
    parser.setInput(StringReader(xml))

    val prediccionesHorarias = mutableListOf<PrediccionHoraria>()
    var diaActual = ""
    var temperaturas = mutableMapOf<String, String>()
    var precipitacion = mutableMapOf<String, String>()
    var probPrecipitacion =
        mutableMapOf<String, String>() // Mapa para probabilidad de precipitación
    var humedades = mutableMapOf<String, String>()
    var vientoDirs = mutableMapOf<String, String>()
    var vientoVels = mutableMapOf<String, String>()
    var estadosCielo = mutableMapOf<String, String>()
    var horasDisponibles = mutableSetOf<String>()

    while (parser.eventType != XmlPullParser.END_DOCUMENT) {
        when (parser.eventType) {
            XmlPullParser.START_TAG -> {
                when (parser.name) {
                    "dia" -> {
                        val fecha = parser.getAttributeValue(null, "fecha")
                        if (fecha != null) {
                            val fechaLocal = LocalDate.parse(fecha)
                            val diaSemana = fechaLocal.dayOfWeek.getDisplayName(
                                TextStyle.FULL,
                                Locale("es", "ES")
                            ).replaceFirstChar { it.uppercase() }
                            diaActual =
                                "$diaSemana, ${fechaLocal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
                            temperaturas.clear()
                            precipitacion.clear()
                            probPrecipitacion.clear()// Limpiamos el mapa
                            humedades.clear()
                            vientoDirs.clear()
                            vientoVels.clear()
                            estadosCielo.clear()
                            horasDisponibles.clear()

                        }
                    }

                    "temperatura" -> {
                        val periodo = parser.getAttributeValue(null, "periodo")
                        if (periodo != null && periodo.length == 2) {
                            val valor = parser.nextText().trim()
                            temperaturas[periodo] = valor
                            horasDisponibles.add(periodo)
                        }
                    }

                    "prob_precipitacion" -> { // Bloque para prob_precipitacion
                        val periodo = parser.getAttributeValue(null, "periodo")
                        if (periodo != null) {
                            val valor = parser.nextText().trim()
                            Log.d("parseHourlyData", "Encontrado prob_precipitacion: periodo=$periodo, valor=$valor")

                            // Asegurarse de que el valor sea un número válido
                            if (valor.isNotEmpty() && valor.all { it.isDigit() }) {
                                // Guardar el periodo exactamente como viene en el XML
                                probPrecipitacion[periodo] = valor
                                Log.d("parseHourlyData", "Añadido al mapa: $periodo -> $valor")

                                // Verificar si es uno de los periodos esperados
                                if (periodo in listOf("0208", "0814", "1420", "2002")) {
                                    Log.d("parseHourlyData", "Periodo estándar encontrado: $periodo")
                                } else {
                                    Log.d("parseHourlyData", "Periodo no estándar: $periodo")
                                }
                            } else {
                                Log.d("parseHourlyData", "Valor no válido para probabilidad: $valor")
                            }
                        } else {
                            Log.d("parseHourlyData", "Periodo nulo o inválido en prob_precipitacion")
                        }
                    }

                    "precipitacion" -> {
                        val periodo = parser.getAttributeValue(null, "periodo")
                        if (periodo != null && periodo.length == 2) {
                            val valor = parser.nextText().trim()
                            precipitacion[periodo] =
                                valor.ifEmpty { "0" }// Asegurar valor por defecto
                            horasDisponibles.add(periodo) // Asegurar que la hora se considere
                        }
                    }

                    "humedad_relativa" -> {
                        val periodo = parser.getAttributeValue(null, "periodo")
                        if (periodo != null && periodo.length == 2) {
                            val valor = parser.nextText().trim()
                            humedades[periodo] = valor

                        }
                    }

                    "viento" -> {
                        val periodo = parser.getAttributeValue(null, "periodo")
                        if (periodo != null && periodo.length == 2) {
                            var direccion = ""
                            var velocidad = ""
                            while (!(parser.eventType == XmlPullParser.END_TAG && parser.name == "viento")) {
                                if (parser.eventType == XmlPullParser.START_TAG) {
                                    when (parser.name) {
                                        "direccion" -> direccion = parser.nextText().trim()
                                        "velocidad" -> velocidad = parser.nextText().trim()
                                    }
                                }
                                parser.next()
                            }
                            if (direccion.isNotEmpty() && velocidad.isNotEmpty()) {
                                vientoDirs[periodo] = direccion
                                vientoVels[periodo] = velocidad

                            }
                        }
                    }

                    "estado_cielo" -> {
                        val periodo = parser.getAttributeValue(null, "periodo")
                        if (periodo != null && periodo.length == 2) {
                            val desc = parser.getAttributeValue(null, "descripcion") ?: "Sin datos"
                            estadosCielo[periodo] = desc

                        }
                    }
                }
            }

            XmlPullParser.END_TAG -> {
                if (parser.name == "dia") {
                    horasDisponibles.sorted().forEach { horaStr ->
                        val prediccion = PrediccionHoraria(
                            dia = diaActual,
                            hora = "$horaStr:00",
                            temperatura = temperaturas[horaStr] ?: "",
                            estadoCielo = estadosCielo[horaStr] ?: "Sin datos",
                            precipitacion = precipitacion[horaStr] ?: "0",
                            probPrecipitacion = obtenerProbPrecipitacion(
                                probPrecipitacion,
                                horaStr
                            ),//Añadimos el nuevo valor
                            humedad = humedades[horaStr] ?: "0",
                            vientoDir = vientoDirs[horaStr] ?: "N/A",
                            vientoVel = vientoVels[horaStr] ?: "0"// Se habia olvidado este valor
                        )

                        prediccionesHorarias.add(prediccion)
                    }
                }
            }

            XmlPullParser.TEXT -> {

            }
        }
        parser.next()
    }

    return prediccionesHorarias
}

fun obtenerProbPrecipitacion(
    probPrecipitacion: MutableMap<String, String>,
    hora: String
): String {
    val horaInt = hora.toIntOrNull() ?: return "0"
    Log.d("obtenerProbPrecipitacion", "Hora: $horaInt, Mapa: $probPrecipitacion")

    // Verificar si hay datos en el mapa
    if (probPrecipitacion.isEmpty()) {
        Log.d("obtenerProbPrecipitacion", "El mapa de probabilidad de precipitación está vacío")
        return "0"
    }

    // Usar los periodos exactos del XML de AEMET
    val resultado = when (horaInt) {
        in 2..7 -> probPrecipitacion["0208"] // 02-08 horas
        in 8..13 -> probPrecipitacion["0814"] // 08-14 horas
        in 14..19 -> probPrecipitacion["1420"] // 14-20 horas
        in 20..23, 0, 1 -> probPrecipitacion["2002"] // 20-02 horas (incluye 0 y 1 de la madrugada)
        else -> null
    }

    // Registrar el resultado
    Log.d("obtenerProbPrecipitacion", "Resultado para hora $horaInt: $resultado")

    // Si no hay datos para el periodo específico, intentar usar cualquier valor disponible
    if (resultado != null) {
        return resultado
    }

    // Si no encontramos un valor específico, usar cualquier valor disponible
    val primerValor = probPrecipitacion.values.firstOrNull()
    Log.d("obtenerProbPrecipitacion", "Usando primer valor disponible: $primerValor")

    return primerValor ?: "0"
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    val colors = getThemeColors()
    val context = LocalContext.current
    var isDarkMode by remember { mutableStateOf(ThemeState.isDarkMode.value) }


    var selectedLanguage by remember { mutableStateOf(context.resources.configuration.locales[0].language) } // "es" o "en"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DefaultPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DefaultPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
                Text(
                    text = context.getString(R.string.settings_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SmallPadding),
                shape = RoundedCornerShape(CardCornerRadius),
                backgroundColor = colors.surface,
                elevation = CardElevation
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DefaultPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = context.getString(R.string.dark_mode),
                        fontSize = 18.sp,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { newValue ->
                            isDarkMode = newValue
                            ThemeState.isDarkMode.value = newValue
                            saveThemePreference(context, newValue)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.primary,
                            uncheckedThumbColor = colors.textSecondary,
                            checkedTrackColor = colors.primary.copy(alpha = 0.5f),
                            uncheckedTrackColor = colors.divider
                        )
                    )
                }
            }

            // Selector de idioma
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SmallPadding),
                shape = RoundedCornerShape(CardCornerRadius),
                backgroundColor = colors.surface,
                elevation = CardElevation
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DefaultPadding)
                ) {
                    Text(
                        text = "Language / Idioma",
                        fontSize = 18.sp,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                selectedLanguage = "es"
                                updateLanguage(context, "es")
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (selectedLanguage == "es") colors.textSecondary else colors.divider
                            )
                        ) {
                            Text("Español", color = colors.accentOrange)
                        }
                        Button(
                            onClick = {
                                selectedLanguage = "en"
                                updateLanguage(context, "en")
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (selectedLanguage == "en") colors.textSecondary else colors.divider
                            )
                        ) {
                            Text("English", color = colors.accentOrange)
                        }
                    }
                }
            }
        }
    }
}

fun updateLanguage(context: Context, language: String) {
    val locale = Locale(language)
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
    // Guardar preferencia (opcional)
    val prefs = context.getSharedPreferences("LanguagePrefs", Context.MODE_PRIVATE)
    prefs.edit { putString("language", language) }
}
