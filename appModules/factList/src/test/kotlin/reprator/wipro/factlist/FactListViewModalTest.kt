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

package reprator.wipro.factlist

import androidx.lifecycle.Observer
import com.google.common.truth.Truth
import io.mockk.mockk
import io.mockk.every
import io.mockk.coEvery
import io.mockk.slot
import io.mockk.MockKAnnotations
import io.mockk.verifySequence
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import reprator.wipro.base.useCases.AppError
import reprator.wipro.base.useCases.AppSuccess
import reprator.wipro.base_android.util.event.Event
import reprator.wipro.factlist.TestFakeData.getFakeManipulatedRemoteDataList
import reprator.wipro.factlist.domain.usecase.FactListUseCase
import reprator.wipro.factlist.modals.FactModals
import reprator.wipro.factlist.util.InstantExecutorExtension
import reprator.wipro.factlist.util.MainCoroutineRule
import reprator.wipro.factlist.util.onChangeExtension

@ExtendWith(value = [InstantExecutorExtension::class])
class FactListViewModalTest {

    @JvmField
    @RegisterExtension
    val coroutinesTestRule = MainCoroutineRule()

    @MockK
    lateinit var factListUseCase: FactListUseCase

    lateinit var viewModal: FactListViewModal

    // create mockk object
    val observerLoad = mockk<Observer<Boolean>>()
    val observerError = mockk<Observer<String>>()
    val observerSuccessList = mockk<Observer<List<FactModals>>>()

    // For refresh
    val observerRefreshLoad = mockk<Observer<Boolean>>()
    val observerRefreshError = mockk<Observer<Event<String>>>()

    // create slot
    val slotLoad = slot<Boolean>()
    val slotError = slot<String>()
    val slotSuccess = slot<List<FactModals>>()

    val slotRefreshLoad = slot<Boolean>()
    val slotRefreshError = slot<Event<String>>()

    // create list to store values
    val listError = arrayListOf<String>()
    val listLoader = arrayListOf<Boolean>()
    val listSuccess = arrayListOf<List<FactModals>>()

    val listRefreshError = arrayListOf<Event<String>>()
    val listRefreshLoader = arrayListOf<Boolean>()

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        viewModal = FactListViewModal(coroutinesTestRule.testDispatcherProvider, factListUseCase)

        observerLoad.onChangeExtension()
        observerError.onChangeExtension()
        observerSuccessList.onChangeExtension()

        observerRefreshLoad.onChangeExtension()
        observerRefreshError.onChangeExtension()

        // start observing
        viewModal.isLoading.observeForever(observerLoad)
        viewModal.errorMsg.observeForever(observerError)
        viewModal._factList.observeForever(observerSuccessList)

        viewModal._swipeLoading.observeForever(observerRefreshLoad)
        viewModal._swipeErrorMsg.observeForever(observerRefreshError)

        every {
            observerLoad.onChanged(capture(slotLoad))
        } answers {
            listLoader.add(slotLoad.captured)
        }

        every {
            observerError.onChanged(capture(slotError))
        } answers {
            listError.add(slotError.captured)
        }

        every {
            observerSuccessList.onChanged(capture(slotSuccess))
        } answers {
            listSuccess.add(slotSuccess.captured)
        }

        every {
            observerRefreshLoad.onChanged(capture(slotRefreshLoad))
        } answers {
            listRefreshLoader.add(slotRefreshLoad.captured)
        }

        every {
            observerRefreshError.onChanged(capture(slotRefreshError))
        } answers {
            listRefreshError.add(slotRefreshError.captured)
        }
    }

    @Test
    fun `get factList successfully on launch`() = coroutinesTestRule.runBlockingTest {

        val output = getFakeManipulatedRemoteDataList()

        coEvery {
            factListUseCase()
        } returns flowOf(AppSuccess(output))

        viewModal.getFactList()

        verifySequence {
            observerLoad.onChanged(any()) // Default Initialization
            observerError.onChanged(any()) // Default Initialization
            observerSuccessList.onChanged(any()) // Default Initialization
            observerLoad.onChanged(any())
            observerLoad.onChanged(any())
            observerSuccessList.onChanged(any())
            observerLoad.onChanged(any())
        }

        Truth.assertThat(listSuccess).isNotEmpty()
        Truth.assertThat(listSuccess).hasSize(output.second.size)
        Truth.assertThat(listRefreshError).isEmpty()
        Truth.assertThat(listRefreshLoader).isEmpty()
    }

    @Test
    fun `get factList fetch failed on launch`() = coroutinesTestRule.runBlockingTest {

        val output = "An error occurred"

        coEvery {
            factListUseCase()
        } returns flowOf(AppError(message = output))

        viewModal.getFactList()

        verifySequence {
            observerLoad.onChanged(any()) // Default Initialization
            observerError.onChanged(any()) // Default Initialization
            observerSuccessList.onChanged(any()) // Default Initialization
            observerLoad.onChanged(any())
            observerLoad.onChanged(any())
            observerError.onChanged(any())
            observerLoad.onChanged(any())
        }

        Truth.assertThat(listSuccess).isEmpty()
        Truth.assertThat(listLoader).isNotEmpty()
        Truth.assertThat(listLoader).hasSize(3)
        Truth.assertThat(listError[0]).isEqualTo(output)

        Truth.assertThat(listRefreshError).isEmpty()
        Truth.assertThat(listRefreshLoader).isEmpty()
    }

    @Test
    fun `retry, getFactlist successfully`() =
        coroutinesTestRule.runBlockingTest {

            val output = getFakeManipulatedRemoteDataList()

            coEvery {
                factListUseCase()
            } returns flowOf(AppSuccess(output))

            viewModal.retryFactList()

            verifySequence {
                observerLoad.onChanged(any())
                observerError.onChanged(any())
                observerSuccessList.onChanged(any())

                observerLoad.onChanged(any())
                observerError.onChanged(any())

                observerLoad.onChanged(any())
                observerLoad.onChanged(any())
                observerSuccessList.onChanged(any())
                observerLoad.onChanged(any())
            }

            Truth.assertThat(listSuccess).isNotEmpty()
            Truth.assertThat(listSuccess).hasSize(output.second.size)

            Truth.assertThat(listLoader).isNotEmpty()
            Truth.assertThat(listLoader).hasSize(4)

            Truth.assertThat(listError).isNotEmpty()
            Truth.assertThat(listError).hasSize(1)
        }

    @Test
    fun `onRefresh, getlist successfully`() =
        coroutinesTestRule.runBlockingTest {

            val output = getFakeManipulatedRemoteDataList()

            coEvery {
                factListUseCase()
            } returns flowOf(AppSuccess(output))

            viewModal.onRefresh()

            verifySequence {
                observerLoad.onChanged(any())
                observerError.onChanged(any())
                observerSuccessList.onChanged(any())

                observerRefreshLoad.onChanged(any())
                observerRefreshError.onChanged(any())

                observerRefreshLoad.onChanged(any())
                observerRefreshLoad.onChanged(any())

                observerSuccessList.onChanged(any())
                observerRefreshLoad.onChanged(any())
            }

            Truth.assertThat(listSuccess).isNotEmpty()
            Truth.assertThat(listSuccess).hasSize(output.second.size)

            Truth.assertThat(listLoader).isEmpty()
            Truth.assertThat(listError).isEmpty()

            Truth.assertThat(listRefreshError).isEmpty()

            Truth.assertThat(listRefreshLoader).isNotEmpty()
            Truth.assertThat(listRefreshLoader).hasSize(3)
        }
}
