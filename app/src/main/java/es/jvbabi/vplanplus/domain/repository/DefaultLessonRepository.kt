package es.jvbabi.vplanplus.domain.repository

import es.jvbabi.vplanplus.data.model.DbDefaultLesson
import es.jvbabi.vplanplus.domain.model.DefaultLesson
import java.util.UUID

interface DefaultLessonRepository {
    suspend fun insert(defaultLesson: DbDefaultLesson): UUID
    suspend fun getDefaultLessonByVpId(vpId: Long): DefaultLesson?
    suspend fun getDefaultLessonByClassId(classId: Long): List<DefaultLesson>
}