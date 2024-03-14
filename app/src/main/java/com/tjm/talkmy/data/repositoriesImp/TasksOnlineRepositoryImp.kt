import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.tjm.talkmy.data.network.TaskApiService
import com.tjm.talkmy.domain.repositories.TasksOnlineRepository
import javax.inject.Inject
class TasksOnlineRepositoryImp @Inject constructor(
    private val context: Context,
    private val apiService: TaskApiService
) : TasksOnlineRepository {
    override suspend fun getTextFromUrls(url: String): String? {
        runCatching {
            apiService.getFromUrl(url)
        }.onSuccess {
            if (it.isSuccessful) {
                return it.body()
            }
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Error al obtener el texto.", Toast.LENGTH_LONG).show()
            }
        }.onFailure {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Error de conexión.", Toast.LENGTH_LONG).show()
            }
        }
        return null
    }
}
