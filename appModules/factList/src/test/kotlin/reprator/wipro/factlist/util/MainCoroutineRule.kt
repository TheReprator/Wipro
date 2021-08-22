package reprator.wipro.factlist.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import reprator.wipro.base.util.network.AppCoroutineDispatchers

class MainCoroutineRule(
    private val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
) : BeforeEachCallback, AfterEachCallback, TestCoroutineScope by TestCoroutineScope(dispatcher) {

    val testDispatcherProvider = object : AppCoroutineDispatchers {
        override val main: CoroutineDispatcher get() = dispatcher

        override val default: CoroutineDispatcher get() = dispatcher

        override val computation: CoroutineDispatcher get() = dispatcher

        override val io: CoroutineDispatcher get() = dispatcher

        override val singleThread: CoroutineDispatcher get() = dispatcher
    }

    override fun beforeEach(context: ExtensionContext?) {
        Dispatchers.setMain(dispatcher)
    }

    override fun afterEach(context: ExtensionContext?) {
        cleanupTestCoroutines()
        Dispatchers.resetMain()
    }
}