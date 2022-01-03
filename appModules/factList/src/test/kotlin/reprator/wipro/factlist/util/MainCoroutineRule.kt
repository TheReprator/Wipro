/*
 * Copyright 2021 Vikram LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package reprator.wipro.factlist.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import reprator.wipro.base.util.network.AppCoroutineDispatchers

class MainCoroutineRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
) : BeforeEachCallback, AfterEachCallback {

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
        Dispatchers.resetMain()
    }
}
