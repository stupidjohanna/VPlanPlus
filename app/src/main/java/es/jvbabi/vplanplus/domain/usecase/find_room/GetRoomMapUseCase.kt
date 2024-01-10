package es.jvbabi.vplanplus.domain.usecase.find_room

import es.jvbabi.vplanplus.domain.model.Lesson
import es.jvbabi.vplanplus.domain.model.Room
import es.jvbabi.vplanplus.domain.model.School
import es.jvbabi.vplanplus.domain.repository.ClassRepository
import es.jvbabi.vplanplus.domain.repository.KeyValueRepository
import es.jvbabi.vplanplus.domain.repository.LessonTimeRepository
import es.jvbabi.vplanplus.domain.repository.RoomRepository
import es.jvbabi.vplanplus.domain.usecase.Keys
import es.jvbabi.vplanplus.domain.usecase.LessonUseCases
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class GetRoomMapUseCase(
    private val roomRepository: RoomRepository,
    private val keyValueRepository: KeyValueRepository,
    private val lessonUseCases: LessonUseCases,
    private val lessonTimeRepository: LessonTimeRepository,
    private val classRepository: ClassRepository
) {

    suspend operator fun invoke(school: School): RoomMap {
        val rooms = roomRepository.getRoomsBySchool(school)
        val classes = classRepository.getClassesBySchool(school)
        val version = (keyValueRepository.get(Keys.LESSON_VERSION_NUMBER)?:"0").toLong()
        val lessonTimes = classes.associateWith { lessonTimeRepository.getLessonTimesByClass(it) }
        val maxLessons = lessonTimes.values.maxOf { it.size }
        val roomResult = mutableListOf<RoomRecord>()

        rooms.forEach { room ->
            val lessons = mutableListOf<Lesson?>()
            val roomLessons = lessonUseCases.getLessonsForRoom(room, LocalDate.now(), version).first()
            repeat(maxLessons) { l ->
                lessons.add(roomLessons.lessons.firstOrNull { it.lessonNumber == l })
            }

            roomResult.add(RoomRecord(room, lessons))
        }
        return RoomMap(school, maxLessons, roomResult)
    }
}

data class RoomMap(
    val school: School,
    val maxLessons: Int,
    val rooms: List<RoomRecord>
)

data class RoomRecord (
    val room: Room,
    val lessons: List<Lesson?>,
    val displayed: Boolean = true
)