package es.jvbabi.vplanplus.data.repository

import es.jvbabi.vplanplus.data.model.DbLesson
import es.jvbabi.vplanplus.data.model.ProfileType
import es.jvbabi.vplanplus.data.source.database.dao.LessonDao
import es.jvbabi.vplanplus.domain.model.Classes
import es.jvbabi.vplanplus.domain.model.Lesson
import es.jvbabi.vplanplus.domain.repository.LessonRepository
import es.jvbabi.vplanplus.domain.repository.ProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

@ExperimentalCoroutinesApi
class LessonRepositoryImpl(
    private val lessonDao: LessonDao,
    private val profileRepository: ProfileRepository
) : LessonRepository {

    override fun getLessonsForClass(classId: UUID, date: LocalDate, version: Long): Flow<List<Lesson>?> {
        return lessonDao.getLessonsByClass(classId, date, version)
            .map { lessons ->
                if (lessons.isEmpty()) null
                else lessons.map { lesson ->
                    lesson.toModel()
                }
            }
    }

    override fun getLessonsForTeacher(teacherId: UUID, date: LocalDate, version: Long): Flow<List<Lesson>?> {
        return lessonDao.getLessonsByTeacher(teacherId, date, version)
            .map { lessons ->
                if (lessons.isEmpty()) null
                else lessons.map { lesson ->
                    lesson.toModel()
                }
            }
    }

    override fun getLessonsForRoom(roomId: UUID, date: LocalDate, version: Long): Flow<List<Lesson>?> {
        return lessonDao.getLessonsByRoom(roomId, date, version)
            .map { lessons ->
                if (lessons.isEmpty()) null
                else lessons.map { lesson ->
                    lesson.toModel()
                }
            }
    }

    override suspend fun getLessonsForProfile(
        profileId: UUID,
        date: LocalDate,
        version: Long
    ): Flow<List<Lesson>?> {
        val profile = profileRepository.getProfileById(profileId).first() ?: return emptyFlow()

        return when (profile.type) {
            ProfileType.STUDENT -> getLessonsForClass(profile.referenceId, date, version)
            ProfileType.TEACHER -> getLessonsForTeacher(profile.referenceId, date, version)
            ProfileType.ROOM -> getLessonsForRoom(profile.referenceId, date, version)
        }
    }

    override suspend fun deleteLessonForClass(`class`: Classes, date: LocalDate, version: Long) {
        lessonDao.deleteLessonsByClassAndDate(`class`.classId, date, version)
    }

    override suspend fun insertLesson(dbLesson: DbLesson): Long {
        return lessonDao.insertLesson(dbLesson)
    }

    override suspend fun deleteAllLessons() {
        lessonDao.deleteAll()
    }

    override suspend fun deleteLessonsByVersion(version: Long) {
        lessonDao.deleteLessonsByVersion(version)
    }

    override suspend fun insertLessons(lessons: List<DbLesson>) {
        lessonDao.insertLessons(lessons)
    }
}