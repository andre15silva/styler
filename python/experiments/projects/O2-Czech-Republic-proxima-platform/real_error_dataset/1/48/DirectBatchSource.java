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
package cz.o2.proxima.beam.direct.io;

import cz.o2.proxima.direct.batch.BatchLogObservable;
import cz.o2.proxima.direct.core.Partition;
import cz.o2.proxima.repository.AttributeDescriptor;
import cz.o2.proxima.repository.RepositoryFactory;
import cz.o2.proxima.storage.StreamElement;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.io.BoundedSource;
import org.apache.beam.sdk.options.PipelineOptions;

/** An {@link BoundedSource} created from direct operator's {@link BatchLogObservable}. */
@Slf4j
public class DirectBatchSource extends AbstractDirectBoundedSource {

  static DirectBatchSource of(
      RepositoryFactory factory,
      BatchLogObservable reader,
      List<AttributeDescriptor<?>> attrs,
      long startStamp,
      long endStamp) {

    return new DirectBatchSource(factory, reader, attrs, startStamp, endStamp);
  }

  private final BatchLogObservable reader;
  private final List<AttributeDescriptor<?>> attrs;
  private final long startStamp;
  private final long endStamp;
  private final @Nullable Partition split;

  private DirectBatchSource(
      RepositoryFactory factory,
      BatchLogObservable reader,
      List<AttributeDescriptor<?>> attrs,
      long startStamp,
      long endStamp) {

    super(factory);
    this.reader = Objects.requireNonNull(reader);
    this.attrs = Objects.requireNonNull(attrs);
    this.startStamp = startStamp;
    this.endStamp = endStamp;
    this.split = null;
  }

  private DirectBatchSource(DirectBatchSource parent, Partition split) {
    super(parent.factory);
    this.reader = parent.reader;
    this.attrs = parent.attrs;
    this.startStamp = parent.startStamp;
    this.endStamp = parent.endStamp;
    this.split = split;
  }

  @Override
  public List<? extends BoundedSource<StreamElement>> split(
      long desiredBundleSizeBytes, PipelineOptions arg1) throws Exception {

    if (split != null) {
      return Arrays.asList(this);
    }
    return reader
        .getPartitions(startStamp, endStamp)
        .stream()
        .map(p -> new DirectBatchSource(this, p))
        .collect(Collectors.toList());
  }

  @Override
  public BoundedReader<StreamElement> createReader(PipelineOptions options) throws IOException {

    return BeamBatchLogReader.of(this, reader, attrs, split, startStamp, endStamp);
  }
}
