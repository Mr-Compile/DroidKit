package io.github.droidkit.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.droidkit.core.database.DiagnosticResultDao
import io.github.droidkit.core.database.DiagnosticResultEntity
import io.github.droidkit.core.database.DiagnosticSessionDao
import io.github.droidkit.core.database.DiagnosticSessionEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ReportRow(
    val session: DiagnosticSessionEntity,
    val results: List<DiagnosticResultEntity>,
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    sessionDao: DiagnosticSessionDao,
    resultDao: DiagnosticResultDao,
) : ViewModel() {

    val rows: StateFlow<List<ReportRow>> =
        combine(sessionDao.observeAll(), resultDao.observeAll()) { sessions, results ->
            sessions.map { s -> ReportRow(s, results.filter { it.sessionId == s.id }) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
