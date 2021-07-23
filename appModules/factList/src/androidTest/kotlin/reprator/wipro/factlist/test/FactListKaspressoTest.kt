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

package reprator.wipro.factlist.test

import androidx.test.espresso.IdlingRegistry
import com.jakewharton.espresso.OkHttp3IdlingResource
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import reprator.wipro.factlist.CustomMockServer
import reprator.wipro.factlist.Factlist
import reprator.wipro.factlist.dispatcherWithCustomBody
import reprator.wipro.factlist.dispatcherWithEmptyBody
import reprator.wipro.factlist.dispatcherWithErrorTimeOut
import reprator.wipro.factlist.screen.FactListKaspressoScreen
import reprator.wipro.factlist.util.launchFragmentInHiltContainer
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidTest
class FactListKaspressoTest : TestCase() {

    companion object {
        const val TOTAL_ITEM = 14
        const val SCREEN_TITLE = "About India"
    }

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    private lateinit var mockWebServer: MockWebServer

    @Inject
    lateinit var okHttp3IdlingResource: OkHttp3IdlingResource

    @Before
    fun setUp() {
        mockWebServer = CustomMockServer().mockWebServer

        hiltRule.inject()

        IdlingRegistry.getInstance().register(okHttp3IdlingResource)

        mockWebServer.dispatcher = dispatcherWithCustomBody()

        launchFragmentInHiltContainer<Factlist>()
    }

    @After
    fun cleanup() {
        mockWebServer.close()
        IdlingRegistry.getInstance().unregister(okHttp3IdlingResource)
    }

    @Test
    fun `load_item_successfully_in_recyclerview`() =
        before {
            testLogger.i("Before section successfull")
        }.after {
            testLogger.i("After section successfull")
        }.run {
            step("1. Open App and show Toolbar") {
                testLogger.i("Main section")

                FactListKaspressoScreen {
                    toolBar {
                        isVisible()
                        isCompletelyDisplayed()
                        hasTitle(SCREEN_TITLE)
                    }

                    flakySafely(timeoutMs = TimeUnit.SECONDS.toMillis(10)) {
                        factList {

                            hasSize(TOTAL_ITEM)

                            firstChild<FactListKaspressoScreen.Item> {
                                title {
                                    isVisible()
                                    hasText("A")
                                }
                                description {
                                    isDisplayed()
                                    hasText("First Item Description")
                                }
                                image {
                                    isDisplayed()
                                }
                            }

                            scrollToEnd()

                            lastChild<FactListKaspressoScreen.Item> {
                                title {
                                    hasText("Last Item")
                                }
                                description {
                                    hasText("Last Description")
                                }
                                image {
                                    isDisplayed()
                                }
                            }
                        }
                    }
                }
            }
        }

    @Test
    fun load_item_successfully_in_recyclerview_with_error_on_pullToRefresh() =
        before {
            testLogger.i("Before section pullToRefresh")
        }.after {
            testLogger.i("After section pullToRefresh")
        }.run {
            step("1. show list items with pull to refresh") {

                FactListKaspressoScreen {

                    flakySafely(timeoutMs = TimeUnit.SECONDS.toMillis(10)) {
                        factList.hasSize(TOTAL_ITEM)

                        mockWebServer.dispatcher = dispatcherWithErrorTimeOut()

                        swipeToRefresh {
                            isDisplayed()
                            swipeDown()
                        }
                    }
                }
            }

            step("2. verify error with snackbar") {

                FactListKaspressoScreen {
                    flakySafely(timeoutMs = TimeUnit.SECONDS.toMillis(10)) {
                        snackbar {
                            isDisplayed()
                            text.hasText("timeout")
                        }
                    }
                }
            }
        }

    @Test
    fun loadErrorViewOnLaunch_withSuccessfulReload() =
        before {
            testLogger.i("Before section loadErrorView")
            mockWebServer.dispatcher = dispatcherWithErrorTimeOut()
        }.after {
            testLogger.i("After section loadErrorView")
        }.run {
            step("1. show error view with reload button click") {

                FactListKaspressoScreen {

                    factList.isNotDisplayed()

                    mockWebServer.dispatcher = dispatcherWithCustomBody()

                    flakySafely(timeoutMs = TimeUnit.SECONDS.toMillis(10)) {
                        errorRetry {
                            isDisplayed()
                            click()
                        }
                    }
                }
            }

            step("2. verify items in recyclerview") {

                FactListKaspressoScreen {
                    factList {
                        hasSize(TOTAL_ITEM)
                        isDisplayed()
                    }
                }
            }
        }

    @Test
    fun loadEmptyViewOnLaunch() =
        before {
            testLogger.i("Before section empty")
            mockWebServer.dispatcher = dispatcherWithEmptyBody()
        }.after {
            testLogger.i("After section empty")
        }.run {
            step("show empty view") {

                FactListKaspressoScreen {
                    flakySafely(timeoutMs = TimeUnit.SECONDS.toMillis(10)) {
                        factList.isNotDisplayed()
                        empty {
                            isDisplayed()
                        }
                    }
                }
            }
        }

    @Test
    fun scroll_check_Position7() =
        before {
            testLogger.i("Before section scroll & check position 7")
        }.after {
            testLogger.i("After section scroll & check position 7")
        }.run {
            step("Open and scroll to position 6") {
                testLogger.i("Main section")

                FactListKaspressoScreen {
                    flakySafely(timeoutMs = TimeUnit.SECONDS.toMillis(10)) {
                        factList {

                            scrollTo(7)
                            childAt<FactListKaspressoScreen.Item>(7) {

                                title {
                                    hasEmptyText()
                                }
                                description {
                                    hasEmptyText()
                                }
                                image {
                                    isDisplayed()
                                }
                            }
                        }
                    }
                }
            }
        }
}
