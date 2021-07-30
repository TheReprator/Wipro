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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.platform.runner.JUnitPlatform
import org.junit.platform.suite.api.SelectClasses
import org.junit.runner.RunWith
import reprator.wipro.factlist.data.repositoryImpl.FactListDataRepositoryImplTest
import reprator.wipro.factlist.datasource.remote.FactListApiServiceTest
import reprator.wipro.factlist.datasource.remote.FactListRemoteDataSourceImplTest
import reprator.wipro.factlist.datasource.remote.remotemapper.FactListMapperTest
import reprator.wipro.factlist.domain.usecase.FactListUseCaseTest

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnitPlatform::class)
@SelectClasses(
    FactListDataRepositoryImplTest::class,
    FactListMapperTest::class,
    FactListApiServiceTest::class,
    FactListRemoteDataSourceImplTest::class,
    FactListUseCaseTest::class,
    FactListViewModalTest::class
)
class FactListTestSuite