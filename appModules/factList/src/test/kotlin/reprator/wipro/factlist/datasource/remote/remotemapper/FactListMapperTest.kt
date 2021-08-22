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

package reprator.wipro.factlist.datasource.remote.remotemapper

import com.google.common.truth.Truth
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.spyk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reprator.wipro.factlist.TestFakeData.getFakeManipulatedRemoteDataList
import reprator.wipro.factlist.TestFakeData.getFakeRemoteDataList
import reprator.wipro.factlist.util.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class FactListMapperTest {

    @Test
    fun `create the parsed json fact into FactModals class with title`() = runBlockingTest {
        val input = getFakeRemoteDataList()
        val output = getFakeManipulatedRemoteDataList()

        val mapper = spyk(FactListMapper())

        val result = mapper.map(input)

        Truth.assertThat(output).isEqualTo(result)

        coVerify(atMost = 1) { mapper.map(input) }

        confirmVerified(mapper)
    }
}
