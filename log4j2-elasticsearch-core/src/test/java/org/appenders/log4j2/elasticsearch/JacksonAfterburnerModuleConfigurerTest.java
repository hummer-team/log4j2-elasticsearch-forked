package org.appenders.log4j2.elasticsearch;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class JacksonAfterburnerModuleConfigurerTest {

    @Test
    public void deprecatedConfigureDelegates() {

        // given
        JacksonAfterburnerModuleConfigurer configurer = spy(new JacksonAfterburnerModuleConfigurer());

        ObjectMapper objectMapper = spy(new ObjectMapper());

        // when
        configurer.configure(objectMapper);

        // then
        verify(configurer).applyTo(same(objectMapper));

    }

    @Test
    public void configuresAfterburnerModule() {

        // given
        JacksonAfterburnerModuleConfigurer configurer = new JacksonAfterburnerModuleConfigurer();

        ObjectMapper objectMapper = spy(new ObjectMapper());

        // when
        configurer.applyTo(objectMapper);

        // then
        ArgumentCaptor<AfterburnerModule> captor = ArgumentCaptor.forClass(AfterburnerModule.class);
        verify(objectMapper).registerModule(captor.capture());

        assertEquals(AfterburnerModule.class, captor.getValue().getClass());

    }

}
