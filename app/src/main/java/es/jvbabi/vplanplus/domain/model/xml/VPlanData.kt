package es.jvbabi.vplanplus.domain.model.xml

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.Text
import org.simpleframework.xml.core.Persister

class VPlanData(val xml: String, val schoolId: Long) {
    val wPlanDataObject: VpMobilVpXml
    init {
        val serializer: Serializer = Persister()
        val modified = xml.replace("<Kurse/>", "")
        val reader = modified.reader()
        wPlanDataObject = serializer.read(VpMobilVpXml::class.java, reader, false)
    }
}

@Root(name = "VpMobil", strict = false)
class VpMobilVpXml {
    @field:Element(name = "Kopf")
    var head: WplanVpXmlHead? = null

    @field:ElementList(name = "FreieTage", entry = "ft") var holidays: List<WplanVpXmlHoliday>? = null
    @field:ElementList(name = "Klassen") var classes: List<WplanVpXmlSchoolClass>? = null
    @field:Element(name = "ZusatzInfo", required = false) var info: VPlanVpAdditionalInfoXml? = null
}

@Root(name = "ft")
class WplanVpXmlHoliday {
    @field:Text var date: String = ""
    @field:Attribute(name = "feier", required = false) var isPublicHoliday: String = ""
}

@Root(name = "Kopf", strict = false)
class WplanVpXmlHead {
    @field:Element(name = "zeitstempel") var timestampString: String? = null
    @field:Element(name = "DatumPlan") var date: String? = null
}

@Root(name = "Klasse")
class WplanVpXmlSchoolClass {
    @field:Element(name = "Kurz") var schoolClass: String = ""
    @field:ElementList(name = "Pl") var lessons: List<WplanVpXmlLesson>? = null
    @field:ElementList(name = "Unterricht", entry = "Ue") var defaultLessons: List<WplanVpXmlDefaultLessonWrapper>? = null
}

@Root(name = "Ue")
class WplanVpXmlDefaultLessonWrapper {
    @field:Element(name = "UeNr") var defaultLesson: WplanVpXmlDefaultLesson? = null
}

@Root(name = "UeNr")
class WplanVpXmlDefaultLesson {
    @field:Attribute(name = "UeLe") var teacherShort: String? = null
    @field:Attribute(name = "UeFa") var subjectShort: String? = null
    @field:Text var lessonId: Int? = null
}

@Root(name = "Std")
class WplanVpXmlLesson {
    @field:Element(name = "St") var lesson: Int = 0
    @field:Element(name = "Nr", required = false) var defaultLessonVpId: Int? = null
    @field:Element(name = "Fa") var subject: WplanVpXmlSubject = WplanVpXmlSubject()
    @field:Element(name = "Le") var teacher: VpMobilVpXmlTeacher = VpMobilVpXmlTeacher()
    @field:Element(name = "Ra") var room: VpMobilVpXmlRoom = VpMobilVpXmlRoom()
    @field:Element(name = "If", required = false) var info: String = ""
}

@Root(name = "Fa")
class WplanVpXmlSubject {
    @field:Text(required = false)
    var subject: String = ""
    @field:Attribute(name = "FaAe", required = false) var subjectChanged: String = ""
}

@Root(name = "Le")
class VpMobilVpXmlTeacher {
    @field:Text(required = false) var teacher: String = ""
    @field:Attribute(name = "LeAe", required = false) var teacherChanged: String = ""
}

@Root(name = "Ra")
class VpMobilVpXmlRoom {
    @field:Text(required = false) var room: String = ""
    @field:Attribute(name = "RaAe", required = false) var roomChanged: String = ""
}

@Root(name = "ZusatzInfo")
class VPlanVpAdditionalInfoXml {
    @field:Element(required = false, name = "ZiZeile") var info: String? = null
}