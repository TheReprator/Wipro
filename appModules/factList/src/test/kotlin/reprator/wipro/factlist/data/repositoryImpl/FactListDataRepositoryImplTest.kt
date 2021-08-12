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

package reprator.wipro.factlist.data.repositoryImpl

import com.google.common.truth.Truth
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import reprator.wipro.base.useCases.AppError
import reprator.wipro.base.useCases.AppSuccess
import reprator.wipro.base.util.interent.ConnectionDetector
import reprator.wipro.factlist.TestFakeData.getFakeManipulatedRemoteDataList
import reprator.wipro.factlist.data.datasource.FactListRemoteDataSource
import reprator.wipro.factlist.domain.repository.FactListRepository
import reprator.wipro.factlist.util.InstantExecutorExtension
import reprator.wipro.factlist.util.MainCoroutineRule

@ExtendWith(value = [InstantExecutorExtension::class])
class FactListDataRepositoryImplTest {

    @JvmField
    @RegisterExtension
    val coroutinesTestRule = MainCoroutineRule()

    @MockK
    lateinit var factListRemoteDataSource: FactListRemoteDataSource

    @MockK
    lateinit var connectionDetector: ConnectionDetector

    lateinit var factListRepository: FactListRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this, true)

        factListRepository = FactListDataRepositoryImpl(
            connectionDetector, factListRemoteDataSource
        )
    }

    @Test
    fun `get fact list from server, on internet connection available`() =
        coroutinesTestRule.runBlockingTest {

            val output = getFakeManipulatedRemoteDataList()

            coEvery {
                connectionDetector.isInternetAvailable
            } returns true

            coEvery {
                factListRemoteDataSource.getFacListRemoteDataSource()
            } returns AppSuccess(output)

            val result = factListRepository.getFactListRepository().single()

            Truth.assertThat(result).isInstanceOf(AppSuccess::class.java)
            Truth.assertThat(result.get()!!.second).hasSize(output.second.size)

            coVerifySequence {
                connectionDetector.isInternetAvailable
                factListRemoteDataSource.getFacListRemoteDataSource()
            }

            coVerify(atMost = 1) {
                connectionDetector.isInternetAvailable
                factListRemoteDataSource.getFacListRemoteDataSource()
            }
        }

    @Test
    fun `No internet available`() =
        coroutinesTestRule.runBlockingTest {

            val output = "No internet connection detected."

            coEvery {
                connectionDetector.isInternetAvailable
            } returns false

            val result = factListRepository.getFactListRepository().single()

            Truth.assertThat(result).isInstanceOf(AppError::class.java)
            Truth.assertThat((result as AppError).message).isEqualTo(output)
        }
}
