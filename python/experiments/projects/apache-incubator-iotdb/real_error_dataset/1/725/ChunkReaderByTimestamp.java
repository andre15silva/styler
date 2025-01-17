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
package org.apache.iotdb.tsfile.read.reader.chunk;

import org.apache.iotdb.tsfile.file.header.PageHeader;
import org.apache.iotdb.tsfile.read.common.Chunk;

public class ChunkReaderByTimestamp extends AbstractChunkReader {

  private long currentTimestamp;

  public ChunkReaderByTimestamp(Chunk chunk) {
    super(chunk, null);
  }

  @Override
  public boolean pageSatisfied(PageHeader pageHeader) {
    long maxTimestamp = pageHeader.getEndTime();
    // if maxTimestamp > currentTimestamp, this page should NOT be skipped
    return maxTimestamp >= currentTimestamp && maxTimestamp > deletedAt;
  }

  public void setCurrentTimestamp(long currentTimestamp) {
    this.currentTimestamp = currentTimestamp;
  }

}
