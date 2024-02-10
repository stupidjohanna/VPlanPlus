package es.jvbabi.vplanplus.domain.usecase.settings.profiles

import android.content.Context
import es.jvbabi.vplanplus.android.notification.Notification
import es.jvbabi.vplanplus.domain.repository.KeyValueRepository
import es.jvbabi.vplanplus.domain.repository.ProfileRepository
import es.jvbabi.vplanplus.domain.repository.SchoolRepository
import es.jvbabi.vplanplus.domain.repository.Keys
import es.jvbabi.vplanplus.domain.usecase.profile.GetSchoolFromProfileUseCase
import kotlinx.coroutines.flow.first
import java.util.UUID

class DeleteSchoolUseCase(
    private val schoolRepository: SchoolRepository,
    private val profileRepository: ProfileRepository,
    private val keyValueRepository: KeyValueRepository,
    private val getSchoolFromProfileUseCase: GetSchoolFromProfileUseCase,
) {
    suspend operator fun invoke(
        context: Context,
        schoolId: Long
    ) {
        val currentProfile = profileRepository.getProfileById(
            UUID.fromString(keyValueRepository.get(Keys.ACTIVE_PROFILE))
        ).first()!!
        if (getSchoolFromProfileUseCase(currentProfile).schoolId == schoolId) {
            keyValueRepository.set(Keys.ACTIVE_PROFILE,
                profileRepository
                    .getProfiles()
                    .first()
                    .firstOrNull {
                        getSchoolFromProfileUseCase(it).schoolId != schoolId
                    }?.id.toString()
            )
        }
        val profiles = profileRepository.getProfilesBySchoolId(schoolId)
        profiles.forEach { profile ->
            Notification.deleteChannel(
                context,
                "PROFILE_${profile.id.toString().lowercase()}"
            )
        }
        schoolRepository.deleteSchool(schoolId)
    }
}