/**
 * Copyright 2017-2020 O2 Czech Republic, a.s.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cz.o2.proxima.direct.kafka;

import cz.o2.proxima.direct.core.DataAccessorFactory;
import cz.o2.proxima.direct.core.DirectDataOperator;
import cz.o2.proxima.repository.EntityDescriptor;
import java.net.URI;
import java.util.Map;

/** Storage using {@code KafkaProducer}. */
public class KafkaStorage implements DataAccessorFactory {

  @Override
  public KafkaAccessor createAccessor(
      DirectDataOperator direct, EntityDescriptor entityDesc, URI uri, Map<String, Object> cfg) {

    return new KafkaAccessor(entityDesc, uri, cfg);
  }

  @Override
  public Accept accepts(URI uri) {
    return uri.getScheme().equals("kafka") ? Accept.ACCEPT : Accept.REJECT;
  }
}
