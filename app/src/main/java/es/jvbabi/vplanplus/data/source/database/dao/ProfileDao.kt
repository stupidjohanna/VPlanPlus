package es.jvbabi.vplanplus.data.source.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import es.jvbabi.vplanplus.data.model.DbProfile
import es.jvbabi.vplanplus.data.model.ProfileType
import es.jvbabi.vplanplus.data.model.combined.CProfile
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ProfileDao {
    @Query("SELECT * FROM profile")
    abstract fun getProfiles(): Flow<List<CProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(profile: DbProfile): Long

    @Query("SELECT * FROM profile WHERE referenceId = :referenceId AND type = :type")
    abstract suspend fun getProfileByReferenceId(referenceId: Long, type: ProfileType): CProfile

    @Query("SELECT * FROM profile WHERE id = :id")
    abstract fun getProfileById(id: Long): Flow<CProfile>

    @Query("DELETE FROM profile WHERE id = :profileId")
    abstract suspend fun deleteProfile(profileId: Long)

    @Query("SELECT * FROM profile WHERE referenceId IN (SELECT id FROM classes WHERE schoolClassRefId = :schoolId) OR referenceId IN (SELECT id FROM teacher WHERE schoolTeacherRefId = :schoolId) OR referenceId IN (SELECT id FROM room WHERE schoolRoomRefId = :schoolId)")
    abstract suspend fun getProfilesBySchoolId(schoolId: Long): List<CProfile>
}