import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class PartnerResult(
    val id: String,
    val username: String,
    val interests: List<String>? = null,
    @SerialName("learning_status")
    val learningStatus: String? = null
)

enum class RelationStatus {
    NONE,        // belum ada hubungan
    PENDING_OUT, // lu yang kirim request, belum dibalas
    PENDING_IN,  // dia yang kirim request ke lu
    FRIEND       // sudah berteman
}

data class PartnerWithStatus(
    val profile: PartnerResult,
    val relationStatus: RelationStatus
)