package es.jvbabi.vplanplus.domain.usecase.onboarding

import es.jvbabi.vplanplus.domain.repository.SchoolRepository

/**
 * Test if schoolId can be an actual school
 */
class CheckSchoolIdSyntax(
    private val schoolRepository: SchoolRepository
) {
    operator fun invoke(schoolId: String): Boolean {
        return schoolRepository.checkSchoolIdSyntax(schoolId)
    }
}