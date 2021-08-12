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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import reprator.wipro.base.util.network.bodyOrThrow
import reprator.wipro.factlist.dispatcherWithCustomBody
import reprator.wipro.factlist.dispatcherWithErrorTimeOut
import reprator.wipro.factlist.util.InstantExecutorExtension
import retrofit2.Retrofit
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@ExtendWith(value = [InstantExecutorExtension::class])
class FactListApiServiceTest {

    companion object {
        private const val COUNT = 14
        private const val TITLE = "About India"
        private const val ITEM_TITLE = "A"
        private const val ITEM_DESCRIPTION = "First Item Description"
        private const val ITEM_URL =
            "http://upload.wikimedia.org/wikipedia/commons/thumb/6/6b/American_Beaver.jpg/220px-American_Beaver.jpg"
        private const val REQUEST_PATH = "/2iodh4vg0eortkl/facts.json"
    }

    private lateinit var service: FactListApiService

    private lateinit var mockWebServer: MockWebServer

    @BeforeEach
    fun createService() {
        mockWebServer = MockWebServer()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS) // For testing purposes
            .readTimeout(2, TimeUnit.SECONDS) // For testing purposes
            .writeTimeout(2, TimeUnit.SECONDS)
            .build()

        service = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(createJacksonConverterFactory())
            .build()
            .create(FactListApiService::class.java)
    }

    @AfterEach
    fun stopService() {
        mockWebServer.shutdown()
    }

    @Test
    fun `get fact list successfully`() = runBlocking {
        mockWebServer.dispatcher = dispatcherWithCustomBody()

        val factListEntity =
            service.factList().body()

        Truth.assertThat(factListEntity).isNotNull()

        val title = factListEntity!!.title
        Truth.assertThat(title).isEqualTo(TITLE)

        val factList = factListEntity.rows
        Truth.assertThat(factList.size).isEqualTo(COUNT)

        val factListItem = factListEntity.rows[0]
        Truth.assertThat(factListItem.title).isEqualTo(ITEM_TITLE)
        Truth.assertThat(factListItem.description).isEqualTo(ITEM_DESCRIPTION)
        Truth.assertThat(factListItem.imageHref).isEqualTo(ITEM_URL)
    }

    @Test
    fun `get fact list request check`() = runBlocking {
        mockWebServer.dispatcher = dispatcherWithCustomBody()

        service.factList().body()
        val request = mockWebServer.takeRequest()

        Truth.assertThat(request.requestUrl!!.pathSegments.size).isEqualTo(2)
        Truth.assertThat(request.requestUrl!!.pathSegments[0]).isEqualTo("2iodh4vg0eortkl")
        Truth.assertThat(request.requestUrl!!.pathSegments[1]).isEqualTo("facts.json")

        val queryParams = request.requestUrl?.queryParameterNames
        Truth.assertThat(queryParams).isEmpty()

        Truth.assertThat(request.path)
            .isEqualTo(REQUEST_PATH)
        Truth.assertThat(request.method).isEqualTo("GET")
    }

    @Test
    fun `Timeout example`(): Unit = runBlocking {
        mockWebServer.dispatcher = dispatcherWithErrorTimeOut()

        assertThrows<SocketTimeoutException> {
            service.factList().bodyOrThrow()
        }
    }
}
