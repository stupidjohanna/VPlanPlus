package es.jvbabi.vplanplus.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import es.jvbabi.vplanplus.data.model.DbRoomBooking
import es.jvbabi.vplanplus.data.model.combined.CRoomBooking
import java.util.UUID

@Dao
abstract class RoomBookingDao {

    @Upsert
    abstract fun upsert(roomBooking: DbRoomBooking)

    @Query("DELETE FROM room_booking WHERE `from` < :now")
    abstract fun deleteOld(now: Long)

    @Transaction
    @Query("SELECT * FROM room_booking WHERE class = :classId")
    abstract fun getRoomBookings(classId: UUID): List<CRoomBooking>
}