package org.appenders.log4j2.elasticsearch.mock;

/*-
 * #%L
 * log4j2-elasticsearch
 * %%
 * Copyright (C) 2019 Rafal Foltynski
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.atomic.AtomicBoolean;

public class LifecycleTestHelper {

    public static Answer<Boolean> trueOnlyOnce() {
        return new Answer<Boolean>() {
            final AtomicBoolean state = new AtomicBoolean(true);

            @Override
            public Boolean answer(InvocationOnMock invocation) {
                return state.compareAndSet(true, false);
            }
        };
    }

    public static Answer<Boolean> falseOnlyOnce() {
        return new Answer<Boolean>() {
            final AtomicBoolean state = new AtomicBoolean();

            @Override
            public Boolean answer(InvocationOnMock invocation) {
                return !state.compareAndSet(false, true);
            }
        };
    }

}
