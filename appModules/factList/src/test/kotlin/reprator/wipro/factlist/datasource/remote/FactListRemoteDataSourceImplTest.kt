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

package reprator.wipro.factlist.datasource.remote

import com.google.common.truth.Truth
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import reprator.wipro.base.useCases.AppError
import reprator.wipro.base.useCases.AppSuccess
import reprator.wipro.factlist.TestFakeData.getFakeManipulatedRemoteDataList
import reprator.wipro.factlist.TestFakeData.getFakeManipulatedUIListItem
import reprator.wipro.factlist.TestFakeData.getFakeManipulatedUITitle
import reprator.wipro.factlist.TestFakeData.getFakeRemoteDataList
import reprator.wipro.factlist.data.datasource.FactListRemoteDataSource
import reprator.wipro.factlist.datasource.remote.remotemapper.FactListMapper
import reprator.wipro.factlist.util.InstantExecutorExtension
import reprator.wipro.factlist.util.MainCoroutineRule
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
@ExtendWith(value = [InstantExecutorExtension::class])
class FactListRemoteDataSourceImplTest {

    @JvmField
    @RegisterExtension
    val coroutinesTestRule = MainCoroutineRule()

    @MockK
    lateinit var factListApiService: FactListApiService

    @MockK
    lateinit var factListMapper: FactListMapper

    private lateinit var factListRemoteDataSource: FactListRemoteDataSource

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, true)

        factListRemoteDataSource = FactListRemoteDataSourceImpl(
            factListApiService,
            factListMapper
        )
    }

    @Test
    fun `fetch list successfully from server and map it to UI pojo`() =
        coroutinesTestRule.runBlockingTest {

            val output = getFakeManipulatedRemoteDataList()
            coEvery {
                factListApiService.factList()
            } returns Response.success(getFakeRemoteDataList())

            coEvery {
                factListMapper.map(any())
            } returns output

            val result = factListRemoteDataSource.getFacListRemoteDataSource()

            Truth.assertThat(result).isInstanceOf(AppSuccess::class.java)
            Truth.assertThat(result.get()).isEqualTo(output)

            Truth.assertThat(result.get()!!.first).isEqualTo(getFakeManipulatedUITitle())
            Truth.assertThat(result.get()!!.second).isEqualTo(getFakeManipulatedUIListItem())

            coVerifySequence {
                factListApiService.factList()
                factListMapper.map(any())
            }
        }

    @Test
    fun `fetch list failed with errorBody`() = coroutinesTestRule.runBlockingTest {

        coEvery {
            factListApiService.factList()
        } returns Response.error(404, mockk(relaxed = true))

        val resp = factListRemoteDataSource.getFacListRemoteDataSource()

        Truth.assertThat(resp).isInstanceOf(AppError::class.java)
        Truth.assertThat((resp as AppError).throwable).isInstanceOf(HttpException::class.java)
    }
}
