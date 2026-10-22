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
    val notes: Map<String, String> = emptyMap(),
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
            it.copy(
                results = it.results + (test.id to state),
                notes = note?.takeIf(String::isNotBlank)
                    ?.let { n -> it.notes + (test.id to n) } ?: it.notes,
            )
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

    /**
     * Clears in-memory state before an async reseed — keeps done=false so a
     * stale finished run can't trigger navigation while rows load.
     */
    fun reset() {
        queue = emptyList()
        _state.value = EngineState()
    }

    /**
     * Resumes a persisted session: pre-fills recorded results and re-queues at
     * the first unanswered test. done=true when nothing is left to answer.
     */
    fun seed(
        testIds: List<String>,
        results: Map<String, TestResultState>,
        notes: Map<String, String>,
    ) {
        queue = tests.filter { it.id in testIds }
        val next = queue.indexOfFirst { it.id !in results }
        _state.value = EngineState(
            current = next.takeIf { it >= 0 }?.let(queue::get),
            index = if (next < 0) queue.size else next,
            total = queue.size,
            results = results,
            notes = notes,
            done = next < 0,
        )
    }
}
