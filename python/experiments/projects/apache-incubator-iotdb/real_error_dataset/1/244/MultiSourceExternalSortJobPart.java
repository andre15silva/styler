/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.db.query.externalsort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.iotdb.tsfile.read.IPointReader;

public class MultiSourceExternalSortJobPart extends ExternalSortJobPart {

  private String tmpFilePath;
  private List<ExternalSortJobPart> source;
  private long queryId;

  public MultiSourceExternalSortJobPart(long queryId, String tmpFilePath,
      List<ExternalSortJobPart> source) {
    super(ExternalSortJobPartType.MULTIPLE_SOURCE);
    this.source = source;
    this.tmpFilePath = tmpFilePath;
    this.queryId = queryId;
  }


  @Override
  public IPointReader executeForIPointReader() throws IOException {
    List<IPointReader> prioritySeriesReaders = new ArrayList<>();
    for (ExternalSortJobPart part : source) {
      prioritySeriesReaders.add(part.executeForIPointReader());
    }
    LineMerger merger = new LineMerger(queryId, tmpFilePath);
    return merger.merge(prioritySeriesReaders);
  }
}
