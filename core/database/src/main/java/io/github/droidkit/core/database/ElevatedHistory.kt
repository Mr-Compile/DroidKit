package io.github.droidkit.core.database

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/** Read/clear surface for the elevated-action audit log. */
@Singleton
class ElevatedHistoryRepository @Inject constructor(
    private val dao: ElevatedActionLogDao,
) {
    fun recent(limit: Int = 100): Flow<List<ElevatedActionLogEntity>> =
        dao.observeRecent(limit)

    suspend fun clear() = dao.deleteAll()
}
