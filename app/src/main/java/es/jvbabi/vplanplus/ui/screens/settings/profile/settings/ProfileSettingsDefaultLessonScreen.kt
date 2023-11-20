package es.jvbabi.vplanplus.ui.screens.settings.profile.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import es.jvbabi.vplanplus.R
import es.jvbabi.vplanplus.domain.model.DefaultLesson
import es.jvbabi.vplanplus.ui.common.BackIcon
import es.jvbabi.vplanplus.ui.common.SettingsSetting
import es.jvbabi.vplanplus.ui.common.SettingsType

@Composable
fun ProfileSettingsDefaultLessonScreen(
    profileId: Long,
    navController: NavHostController,
    viewModel: ProfileSettingsDefaultLessonsViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    LaunchedEffect(key1 = profileId, block = {
        viewModel.init(profileId)
    })
    ProfileSettingsDefaultLessonContent(
        state = state,
        onBackClicked = { navController.popBackStack() },
        onDefaultLessonChanged = { dl, value ->
            viewModel.onDefaultLessonChanged(dl, value)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsDefaultLessonContent(
    state: ProfileSettingsDefaultLessonsState,
    onBackClicked: () -> Unit,
    onDefaultLessonChanged: (DefaultLesson, Boolean) -> Unit
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(id = R.string.settings_profileManagementDefaultLessonSettingsTitle)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = { onBackClicked() }) {
                        BackIcon()
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (state.profile == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }
            LazyColumn {
                items(items = state.profile.defaultLessons.entries.sortedBy { it.key.subject }) {
                    SettingsSetting(
                        icon = null,
                        title = it.key.subject,
                        subtitle = it.key.teacher?.acronym ?: stringResource(id = R.string.settings_profileDefaultLessonNoTeacher),
                        type = SettingsType.TOGGLE,
                        enabled = true,
                        checked = it.value,
                        doAction = { onDefaultLessonChanged(it.key, !it.value) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileSettingsDefaultLessonScreenPreview() {
    ProfileSettingsDefaultLessonContent(
        state = ProfileSettingsDefaultLessonsState(),
        onBackClicked = {},
        onDefaultLessonChanged = { _, _ -> }
    )
}