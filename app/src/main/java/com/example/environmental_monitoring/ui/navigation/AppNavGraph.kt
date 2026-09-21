package com.example.environmental_monitoring.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.environmental_monitoring.di.AppContainer
import com.example.environmental_monitoring.di.ViewModelFactory
import com.example.environmental_monitoring.ui.alarms.AlarmsScreen
import com.example.environmental_monitoring.ui.alarms.AlarmsViewModel
import com.example.environmental_monitoring.ui.dashboard.DashboardScreen
import com.example.environmental_monitoring.ui.dashboard.DashboardViewModel
import com.example.environmental_monitoring.ui.devicedetail.DeviceDetailScreen
import com.example.environmental_monitoring.ui.devicedetail.DeviceDetailViewModel
import com.example.environmental_monitoring.ui.settings.SettingsScreen
import com.example.environmental_monitoring.ui.settings.SettingsViewModel
import com.example.environmental_monitoring.ui.thresholds.ThresholdsScreen
import com.example.environmental_monitoring.ui.thresholds.ThresholdsViewModel

private object Routes {
    const val DASHBOARD    = "dashboard"
    const val ALARMS       = "alarms"
    const val SETTINGS     = "settings"
    const val DEVICE_DETAIL = "device/{deviceId}"
    const val THRESHOLDS   = "thresholds/{deviceId}"

    fun deviceDetail(deviceId: String) = "device/$deviceId"
    fun thresholds(deviceId: String)   = "thresholds/$deviceId"
}

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val topLevelDestinations = listOf(
    TopLevelDestination(Routes.DASHBOARD, "Dispositivos", Icons.Default.Sensors),
    TopLevelDestination(Routes.ALARMS,    "Alarmas",      Icons.Default.NotificationsActive),
    TopLevelDestination(Routes.SETTINGS,  "Ajustes",      Icons.Default.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentalMonitoringApp() {
    val navController = rememberNavController()
    val context       = LocalContext.current
    val container     = remember(context) { AppContainer.get(context) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // ViewModel compartido de dashboard para el badge de alarmas
    val dashboardVm: DashboardViewModel = viewModel(
        factory = ViewModelFactory(container) { DashboardViewModel(it.sensorRepository) }
    )
    val unacknowledgedCount by dashboardVm.unacknowledgedCount.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResourceTitle(currentDestination?.route)) },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        },
        bottomBar = {
            NavigationBar {
                topLevelDestinations.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            if (destination.route == Routes.ALARMS && unacknowledgedCount > 0) {
                                BadgedBox(badge = {
                                    Badge { Text(if (unacknowledgedCount > 9) "9+" else unacknowledgedCount.toString()) }
                                }) {
                                    Icon(destination.icon, contentDescription = destination.label)
                                }
                            } else {
                                Icon(destination.icon, contentDescription = destination.label)
                            }
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Routes.DASHBOARD,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    viewModel        = dashboardVm,
                    onDeviceClick    = { deviceId -> navController.navigate(Routes.deviceDetail(deviceId)) },
                    onConfigureAlarms = { deviceId -> navController.navigate(Routes.thresholds(deviceId)) }
                )
            }
            composable(Routes.ALARMS) {
                val vm: AlarmsViewModel = viewModel(
                    factory = ViewModelFactory(container) { AlarmsViewModel(it.sensorRepository) }
                )
                AlarmsScreen(viewModel = vm)
            }
            composable(Routes.SETTINGS) {
                val vm: SettingsViewModel = viewModel(
                    factory = ViewModelFactory(container) {
                        SettingsViewModel(it.settingsRepository, it.sensorRepository)
                    }
                )
                SettingsScreen(viewModel = vm)
            }
            composable(Routes.DEVICE_DETAIL) { backStack ->
                val deviceId = backStack.arguments?.getString("deviceId") ?: return@composable
                val vm: DeviceDetailViewModel = viewModel(
                    key     = "device-detail-$deviceId",
                    factory = ViewModelFactory(container) { DeviceDetailViewModel(it.sensorRepository, deviceId) }
                )
                DeviceDetailScreen(
                    viewModel             = vm,
                    onConfigureThresholds = { navController.navigate(Routes.thresholds(deviceId)) }
                )
            }
            composable(Routes.THRESHOLDS) { backStack ->
                val deviceId = backStack.arguments?.getString("deviceId") ?: return@composable
                val vm: ThresholdsViewModel = viewModel(
                    key     = "thresholds-$deviceId",
                    factory = ViewModelFactory(container) { ThresholdsViewModel(it.sensorRepository, deviceId) }
                )
                ThresholdsScreen(viewModel = vm)
            }
        }
    }
}

private fun stringResourceTitle(route: String?): String = when {
    route == null                  -> "EnviroMonitor"
    route == Routes.DASHBOARD      -> "Dispositivos"
    route == Routes.ALARMS         -> "Alarmas"
    route == Routes.SETTINGS       -> "Ajustes"
    route.startsWith("device/")    -> "Detalle del dispositivo"
    route.startsWith("thresholds/") -> "Umbrales de alarma"
    else                           -> "EnviroMonitor"
}
