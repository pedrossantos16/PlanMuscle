package com.pedro.planmuscle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.pedro.planmuscle.data.preferences.UserPreferences
import com.pedro.planmuscle.domain.repository.PlanMuscleRepository
import com.pedro.planmuscle.ui.checkin.CheckInScreen
import com.pedro.planmuscle.ui.checkin.CheckInViewModel
import com.pedro.planmuscle.ui.dashboard.DashboardScreen
import com.pedro.planmuscle.ui.dashboard.DashboardViewModel
import com.pedro.planmuscle.ui.history.HistoryScreen
import com.pedro.planmuscle.ui.history.HistoryViewModel
import com.pedro.planmuscle.ui.nutrition.NutritionPlanScreen
import com.pedro.planmuscle.ui.nutrition.NutritionViewModel
import com.pedro.planmuscle.ui.onboarding.OnboardingScreen
import com.pedro.planmuscle.ui.onboarding.OnboardingViewModel
import com.pedro.planmuscle.ui.settings.SettingsScreen
import com.pedro.planmuscle.ui.settings.SettingsViewModel
import com.pedro.planmuscle.ui.theme.PlanMuscleTheme
import com.pedro.planmuscle.ui.workout.WorkoutSessionScreen
import com.pedro.planmuscle.ui.workout.WorkoutViewModel

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : BottomNavItem("dashboard", "Início", Icons.Default.Home)
    object Nutrition : BottomNavItem("nutrition", "Plano", Icons.Default.RestaurantMenu)
    object CheckIn : BottomNavItem("checkin", "Diário", Icons.Default.EditNote)
    object History : BottomNavItem("history", "Evolução", Icons.Default.Analytics)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as PlanMuscleApplication).appContainer
        val repository = appContainer.repository
        val prefsDataStore = appContainer.userPreferencesDataStore

        setContent {
            val userPrefs by prefsDataStore.userPreferencesFlow.collectAsStateWithLifecycle(initialValue = UserPreferences())
            val activeProgram by repository.getActiveProgram().collectAsStateWithLifecycle(initialValue = null)

            PlanMuscleTheme(themeOption = userPrefs.themeOption) {
                if (activeProgram == null) {
                    val onboardingViewModel: OnboardingViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return OnboardingViewModel(repository) as T
                            }
                        }
                    )
                    OnboardingScreen(
                        viewModel = onboardingViewModel,
                        onOnboardingFinished = {}
                    )
                } else {
                    MainAppScreen(
                        repository = repository,
                        userPrefs = userPrefs,
                        onThemeChanged = { _ ->
                            // Theme changes via DataStore
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppScreen(
    repository: PlanMuscleRepository,
    userPrefs: UserPreferences,
    onThemeChanged: (com.pedro.planmuscle.ui.theme.AppThemeOption) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Nutrition,
        BottomNavItem.CheckIn,
        BottomNavItem.History
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                            label = { Text(text = item.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.padding(PaddingValues(bottom = innerPadding.calculateBottomPadding()))
        ) {
            composable(BottomNavItem.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return DashboardViewModel(repository) as T
                        }
                    }
                )
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onStartWorkout = { sessionId ->
                        navController.navigate("workout_session/$sessionId")
                    }
                )
            }

            composable(
                route = "workout_session/{sessionId}",
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 1L
                val context = androidx.compose.ui.platform.LocalContext.current
                val prefsDataStore = (context.applicationContext as PlanMuscleApplication).appContainer.userPreferencesDataStore
                val workoutViewModel: WorkoutViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return WorkoutViewModel(repository, prefsDataStore, sessionId) as T
                        }
                    }
                )
                WorkoutSessionScreen(
                    viewModel = workoutViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(BottomNavItem.Nutrition.route) {
                val nutritionViewModel: NutritionViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return NutritionViewModel(repository) as T
                        }
                    }
                )
                NutritionPlanScreen(viewModel = nutritionViewModel)
            }

            composable(BottomNavItem.CheckIn.route) {
                val checkInViewModel: CheckInViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return CheckInViewModel(repository) as T
                        }
                    }
                )
                CheckInScreen(viewModel = checkInViewModel)
            }

            composable(BottomNavItem.History.route) {
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return HistoryViewModel(repository) as T
                        }
                    }
                )
                HistoryScreen(viewModel = historyViewModel)
            }

            composable("settings") {
                val context = androidx.compose.ui.platform.LocalContext.current
                val prefsDataStore = (context.applicationContext as PlanMuscleApplication).appContainer.userPreferencesDataStore
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return SettingsViewModel(repository, prefsDataStore) as T
                        }
                    }
                )
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onThemeChanged = onThemeChanged
                )
            }
        }
    }
}
