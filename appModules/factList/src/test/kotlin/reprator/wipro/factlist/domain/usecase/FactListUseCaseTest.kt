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

package reprator.wipro.factlist.domain.usecase

import com.google.common.truth.Truth
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reprator.wipro.base.useCases.AppError
import reprator.wipro.base.useCases.AppSuccess
import reprator.wipro.factlist.TestFakeData.getFakeManipulatedRemoteDataList
import reprator.wipro.factlist.domain.repository.FactListRepository
import reprator.wipro.factlist.util.InstantExecutorExtension

@ExtendWith(value = [InstantExecutorExtension::class])
class FactListUseCaseTest {

    @MockK
    lateinit var factListRepository: FactListRepository

    lateinit var factListUseCase: FactListUseCase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this, true)

        factListUseCase = FactListUseCase(factListRepository)
    }

    @Test
    fun `fetch factlist from remote data source`() = runBlockingTest {
        val output = getFakeManipulatedRemoteDataList()

        coEvery {
            factListRepository.getFactListRepository()
        } returns flowOf(AppSuccess(output))

        val result = factListUseCase().single()

        Truth.assertThat(result).isInstanceOf(AppSuccess::class.java)
        Truth.assertThat((result as AppSuccess).data).isEqualTo(output)
    }

    @Test
    fun `failed to load data, as internet is not available`() = runBlockingTest {

        val output = "No internet connection detected."

        coEvery {
            factListRepository.getFactListRepository()
        } returns flowOf(AppError(message = output))

        val result = factListUseCase().single()

        Truth.assertThat(result).isInstanceOf(AppError::class.java)
        Truth.assertThat((result as AppError).message).isEqualTo(output)
    }
}
