package es.jvbabi.vplanplus

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.animation.doOnEnd
import androidx.core.content.IntentSanitizer
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import es.jvbabi.vplanplus.data.model.ProfileType
import es.jvbabi.vplanplus.domain.repository.NotificationRepository
import es.jvbabi.vplanplus.domain.usecase.general.Identity
import es.jvbabi.vplanplus.domain.usecase.home.Colors
import es.jvbabi.vplanplus.domain.usecase.home.MainUseCases
import es.jvbabi.vplanplus.feature.onboarding.ui.OnboardingViewModel
import es.jvbabi.vplanplus.ui.NavigationGraph
import es.jvbabi.vplanplus.ui.screens.Screen
import es.jvbabi.vplanplus.ui.theme.VPlanPlusTheme
import es.jvbabi.vplanplus.worker.SyncWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val onboardingViewModel: OnboardingViewModel by viewModels()

    @Inject
    lateinit var mainUseCases: MainUseCases

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private var navController: NavHostController? = null
    private var showSplashScreen: Boolean = true

    private var currentIdentity = mutableStateOf<Identity?>(null)

    private var initDone = false

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        processIntent(intent)

        var goToOnboarding: Boolean? = null
        var colors = Colors.DYNAMIC
        lifecycleScope.launch {
            currentIdentity.value = mainUseCases.getCurrentIdentity.invoke().first()
            goToOnboarding = currentIdentity.value?.profile == null

            notificationRepository.createSystemChannels(applicationContext)
            notificationRepository.createProfileChannels(applicationContext, mainUseCases.getProfilesUseCase().first().map { it.value }.flatten())
            mainUseCases.setUpUseCase()

            combine(
                listOf(
                    mainUseCases.getColorSchemeUseCase(),
                    mainUseCases.getCurrentIdentity()
                )
            ) { data ->
                colors = data[0] as Colors
                currentIdentity.value = data[1] as Identity?
                initDone = true
            }.collect {
                Log.d("MainActivity", "Colors: $colors")
                Log.d("MainActivity", "Identity: $currentIdentity")
            }
        }

        if (showSplashScreen) installSplashScreen().apply {
            setKeepOnScreenCondition {
                !initDone
            }
            setOnExitAnimationListener { screen ->
                Log.d("MainActivity", "Exiting splash screen")
                try {
                    val moveIconAnimator = ObjectAnimator.ofFloat(
                        screen.iconView,
                        View.TRANSLATION_X,
                        screen.iconView.paddingStart.toFloat(),
                        0f
                    )
                    val fadeScreenAnimator = ObjectAnimator.ofFloat(
                        screen.view,
                        View.ALPHA,
                        1f,
                        0f
                    )

                    fadeScreenAnimator.interpolator = AccelerateInterpolator()
                    fadeScreenAnimator.duration = 500L
                    moveIconAnimator.interpolator = AccelerateInterpolator()
                    moveIconAnimator.duration = 500L
                    moveIconAnimator.start()
                    fadeScreenAnimator.start()
                    moveIconAnimator.doOnEnd { screen.remove() }
                } catch (e: NullPointerException) {
                    screen.remove()
                }

                doInit(true)
            }
        } else doInit(false)

        lifecycleScope.launch {
            while (!initDone) delay(50)
            setContent {
                VPlanPlusTheme(cs = colors) {
                    navController = rememberNavController()

                    var selectedIndex by rememberSaveable {
                        mutableIntStateOf(0)
                    }
                    val navBarItems = listOfNotNull(
                        NavigationBarItem(
                            onClick = {
                                if (selectedIndex == 0) return@NavigationBarItem
                                selectedIndex = 0
                                navController!!.navigate(Screen.HomeScreen.route) { popUpTo(0) }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null
                                )
                            },
                            label = { Text(text = stringResource(id = R.string.main_home)) },
                            route = Screen.HomeScreen.route
                        ),
                        NavigationBarItem(
                            onClick = {
                                if (selectedIndex == 1) return@NavigationBarItem
                                selectedIndex = 1
                                navController!!.navigate(Screen.TimetableScreen.route) { popUpTo(Screen.HomeScreen.route) }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.FormatListNumbered,
                                    contentDescription = null
                                )
                            },
                            label = { Text(text = stringResource(id = R.string.main_timetable)) },
                            route = Screen.TimetableScreen.route
                        ),
                        if (currentIdentity.value?.profile?.type == ProfileType.STUDENT) NavigationBarItem(
                            onClick = {
                                if (selectedIndex == 2) return@NavigationBarItem
                                selectedIndex = 2
                                navController!!.navigate(Screen.HomeworkScreen.route) { popUpTo(Screen.HomeScreen.route) }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.MenuBook,
                                    contentDescription = null
                                )
                            },
                            label = { Text(text = stringResource(id = R.string.main_homework)) },
                            route = Screen.HomeworkScreen.route
                        ) else null,
                        if (currentIdentity.value?.profile?.type == ProfileType.STUDENT) NavigationBarItem(
                            onClick = {
                                if (selectedIndex == 3) return@NavigationBarItem
                                selectedIndex = 3
                                navController!!.navigate(Screen.GradesScreen.route) { popUpTo(Screen.HomeScreen.route) }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Grade,
                                    contentDescription = null
                                )
                            },
                            label = { Text(text = stringResource(id = R.string.main_grades)) },
                            route = Screen.GradesScreen.route
                        ) else null
                    )

                    val navBar = @Composable {
                        NavigationBar {
                            navBarItems.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    selected = index == selectedIndex,
                                    onClick = item.onClick,
                                    icon = item.icon,
                                    label = item.label
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        if (goToOnboarding != null) {
                            NavigationGraph(
                                navController = navController!!,
                                onboardingViewModel = onboardingViewModel,
                                goToOnboarding = goToOnboarding!!,
                                navBar = navBar,
                                onNavigationChanged = { route ->
                                    val item =
                                        navBarItems.firstOrNull { route?.startsWith(it.route) == true }
                                    if (item != null && navBarItems.indexOf(item) != selectedIndex) {
                                        selectedIndex = navBarItems.indexOf(item)
                                        Log.d("Navigation", "Selected index: $selectedIndex")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
            15,
            TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
            .addTag("SyncWork")
            .addTag("AutomaticSyncWork")
            .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("SyncWork", ExistingPeriodicWorkPolicy.KEEP, syncWork)
    }

    private fun processIntent(intent: Intent) {
        Log.d("MainActivity.Intent", "onNewIntent: $intent")
        Log.d("MainActivity.Intent", "Data: ${intent.data}")
        if (intent.hasExtra("screen")) {
            showSplashScreen = false
            lifecycleScope.launch {
                while (currentIdentity.value == null || navController == null) delay(50)
                when (intent.getStringExtra("screen")) {
                    "grades" -> navController!!.navigate(Screen.GradesScreen.route)
                    else -> navController!!.navigate(intent.getStringExtra("screen") ?: Screen.HomeScreen.route)
                }
            }
        }
        if (intent.hasExtra("profileId")) {
            showSplashScreen = false
            val profileId = intent.getStringExtra("profileId")
            Log.d("MainActivity.Intent", "profileId: $profileId")
            Log.d("MainActivity.Intent", "dateStr: ${intent.getStringExtra("dateStr")}")

            if (intent.getStringExtra("dateStr") != null) {
                val dateStr = intent.getStringExtra("dateStr") ?: return
                val date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                Log.d(
                    "MainActivity.Intent",
                    "Switching to date: $date (Difference: ${
                        Period.between(
                            LocalDate.now(),
                            date
                        ).days
                    })"
                )
                lifecycleScope.launch {
                    while (currentIdentity.value == null) delay(50)
                    navController!!.navigate(Screen.TimetableScreen.route + "/$date")
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.finish()
        if (intent == null) return
        val sanitized = IntentSanitizer.Builder()
            .allowExtra("profileId", String::class.java)
            .allowExtra("dateStr", String::class.java)
            .allowExtra("screen", String::class.java)
            .allowData { true }
            .allowFlags(0x10000000)
            .allowAnyComponent()
            .allowPackage { true }
            .allowAction(Intent.ACTION_VIEW)
            .allowCategory(Intent.CATEGORY_BROWSABLE)
            .build()
            .sanitizeByFiltering(intent)
        startActivity(sanitized)
        processIntent(intent)
    }

    private fun doInit(calledBySplashScreen: Boolean) {
        if (!calledBySplashScreen) setTheme(R.style.Theme_VPlanPlus)
        enableEdgeToEdge()
    }
}

private data class NavigationBarItem(
    val onClick: () -> Unit,
    val route: String,
    val icon: @Composable () -> Unit,
    val label: @Composable () -> Unit
)