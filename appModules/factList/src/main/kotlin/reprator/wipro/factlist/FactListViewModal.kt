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

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.americanexpress.busybee.BusyBee
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import reprator.wipro.base.extensions.computationalBlock
import reprator.wipro.base.useCases.AppError
import reprator.wipro.base.useCases.AppSuccess
import reprator.wipro.base.util.network.AppCoroutineDispatchers
import reprator.wipro.base_android.util.event.Event
import reprator.wipro.factlist.domain.usecase.FactListUseCase
import reprator.wipro.factlist.modals.FactModals
import javax.inject.Inject

@HiltViewModel
class FactListViewModal @Inject constructor(
    private val coroutineDispatcherProvider: AppCoroutineDispatchers,
    private val factListUseCase: FactListUseCase
) : ViewModel() {

    private val BUSYBEE_OPERATION_NAME = "Network Call"
    private val busyBee = BusyBee.singleton()

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMsg = MutableLiveData("")

    val errorMsg: LiveData<String> = _errorMsg

    private val _title = MutableLiveData<String?>()
    val title: LiveData<String?> = _title

    @VisibleForTesting
    val _factList = MutableLiveData(emptyList<FactModals>())

    val _swipeErrorMsg = MutableLiveData(Event(""))
    val _swipeLoading = MutableLiveData(false)

    fun getFactList() {
        useCaseCall(
            {
                _isLoading.value = it
            },
            {
                _errorMsg.value = it
            }
        )
    }

    fun retryFactList() {
        _isLoading.value = true
        _errorMsg.value = ""
        getFactList()
    }

    fun onRefresh() {
        useCaseCall(
            {
                _swipeLoading.value = it
            },
            {
                _swipeErrorMsg.value = Event(it)
            }
        )
    }

    private fun useCaseCall(
        blockLoader: (Boolean) -> Unit,
        blockError: (String) -> Unit
    ) {
        computationalBlock {

            busyBee.busyWith(BUSYBEE_OPERATION_NAME)

            factListUseCase().flowOn(coroutineDispatcherProvider.io)
                .catch { e ->
                    blockError(e.localizedMessage ?: "")
                }.onStart {
                    blockLoader(true)
                }.onCompletion {
                    blockLoader(false)
                    busyBee.completed(BUSYBEE_OPERATION_NAME)
                }.flowOn(coroutineDispatcherProvider.main)
                .collect {
                    withContext(coroutineDispatcherProvider.main) {
                        blockLoader(false)

                        when (it) {
                            is AppSuccess -> {
                                _title.value = it.data.first
                                _factList.value = it.data.second
                            }
                            is AppError -> {
                                blockError(it.message ?: it.throwable?.message ?: "")
                            }
                            else -> throw IllegalArgumentException("Illegal State")
                        }
                    }
                }
        }
    }

    private fun computationalBlock(
        coroutineExceptionHandler: CoroutineExceptionHandler? = null,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.computationalBlock(
            coroutineDispatcherProvider,
            coroutineExceptionHandler,
            block
        )
    }
}
