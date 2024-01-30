package es.jvbabi.vplanplus.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "room_booking",
    primaryKeys = ["roomId", "bookedBy", "from", "to"],
    indices = [
        Index(value = ["roomId", "bookedBy", "from", "to", "class"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbSchoolEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DbSchoolEntity::class,
            parentColumns = ["id"],
            childColumns = ["class"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbRoomBooking(
    val roomId: UUID,
    val bookedBy: Int,
    val from: LocalDateTime,
    val to: LocalDateTime,
    val `class`: UUID
)
