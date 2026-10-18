package io.github.droidkit.feature.diagnostics

import io.github.droidkit.core.common.TestResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EngineState(
    val current: PhoneDoctorTest? = null,
    val index: Int = 0,
    val total: Int = 0,
    val results: Map<String, TestResultState> = emptyMap(),
    val done: Boolean = false,
)

/**
 * Ordered test runner. Pure Kotlin — persistence stays in the ViewModel layer
 * so the engine is unit-testable without Room or Android.
 */
class SessionEngine(private val tests: List<PhoneDoctorTest>) {

    private var queue: List<PhoneDoctorTest> = tests

    private val _state = MutableStateFlow(EngineState(total = queue.size))
    val state: StateFlow<EngineState> = _state.asStateFlow()

    fun start() {
        queue = tests
        _state.value = EngineState(
            current = queue.firstOrNull(),
            index = 0,
            total = queue.size,
            done = queue.isEmpty(),
        )
    }

    fun record(state: TestResultState, note: String?) {
        val test = _state.value.current ?: return
        _state.update {
            it.copy(results = it.results + (test.id to state))
        }
        advance()
    }

    fun skip() = record(TestResultState.NOT_TESTED, null)

    fun advance() {
        _state.update {
            val next = it.index + 1
            if (next >= queue.size) {
                it.copy(index = next, current = null, done = true)
            } else {
                it.copy(index = next, current = queue[next])
            }
        }
    }

    /** Marks every unanswered test NOT_TESTED and closes the run. */
    fun finish() {
        _state.update {
            val missing = queue.map { t -> t.id }.filter { id -> id !in it.results }
            it.copy(
                current = null,
                done = true,
                results = it.results + missing.associateWith { TestResultState.NOT_TESTED },
            )
        }
    }

    /** Re-runs only the given tests (retry failed / skipped). */
    fun restart(testIds: List<String>) {
        queue = tests.filter { it.id in testIds }
        _state.value = EngineState(
            current = queue.firstOrNull(),
            index = 0,
            total = queue.size,
            done = queue.isEmpty(),
        )
    }
}
