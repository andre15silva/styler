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
package org.apache.iotdb.tsfile.file.metadata.statistics;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.apache.iotdb.tsfile.read.reader.TsFileInput;
import org.apache.iotdb.tsfile.utils.Binary;
import org.apache.iotdb.tsfile.utils.BytesUtils;
import org.apache.iotdb.tsfile.utils.ReadWriteIOUtils;

/**
 * Statistics for string type.
 */
public class BinaryStatistics extends Statistics<Binary> {

  private Binary min = new Binary("");
  private Binary max = new Binary("");
  private Binary first = new Binary("");
  private Binary last = new Binary("");
  private double sum;// FIXME sum is meaningless

  @Override
  public void setMinMaxFromBytes(byte[] minBytes, byte[] maxBytes) {
    min = new Binary(minBytes);
    max = new Binary(maxBytes);
  }

  @Override
  public Binary getMin() {
    return min;
  }

  @Override
  public Binary getMax() {
    return max;
  }

  @Override
  public Binary getFirst() {
    return first;
  }

  @Override
  public Binary getLast() {
    return last;
  }

  @Override
  public double getSum() {
    return sum;
  }

  /**
   * initialize Statistics.
   *
   * @param min minimum value
   * @param max maximum value
   * @param first the first value
   * @param last the last value
   * @param sum sum
   */
  private void initializeStats(Binary min, Binary max, Binary first, Binary last, double sum) {
    this.min = min;
    this.max = max;
    this.first = first;
    this.last = last;
    this.sum = sum;
  }

  @Override
  protected void mergeStatisticsValue(Statistics<?> stats) {
    BinaryStatistics stringStats = (BinaryStatistics) stats;
    if (isEmpty) {
      initializeStats(stringStats.getMin(), stringStats.getMax(), stringStats.getFirst(),
          stringStats.getLast(), stringStats.getSum());
      isEmpty = false;
    } else {
      updateStats(stringStats.getMin(), stringStats.getMax(), stringStats.getFirst(),
          stringStats.getLast(), stringStats.getSum());
    }
  }

  @Override
  public void updateStats(Binary value) {
    if (isEmpty) {
      initializeStats(value, value, value, value, 0);
      isEmpty = false;
    } else {
      updateStats(value, value, value, value, 0);
      isEmpty = false;
    }
  }

  @Override
  public void updateStats(Binary[] values) {
    for (Binary value : values) {
      if (isEmpty) {
        initializeStats(value, value, value, value, 0);
        isEmpty = false;
      } else {
        updateStats(value, value, value, value, 0);
        isEmpty = false;
      }
    }
  }

  private void updateStats(Binary minValue, Binary maxValue, Binary firstValue, Binary lastValue,
      double sum) {
    if (minValue.compareTo(min) < 0) {
      min = minValue;
    }
    if (maxValue.compareTo(max) > 0) {
      max = maxValue;
    }
    this.last = lastValue;
  }

  @Override
  public byte[] getMinBytes() {
    return min.getValues();
  }

  @Override
  public byte[] getMaxBytes() {
    return max.getValues();
  }

  @Override
  public byte[] getFirstBytes() {
    return first.getValues();
  }

  @Override
  public byte[] getLastBytes() {
    return last.getValues();
  }

  @Override
  public byte[] getSumBytes() {
    return BytesUtils.doubleToBytes(sum);
  }

  @Override
  public ByteBuffer getMinBytebuffer() {
    return ByteBuffer.wrap(min.getValues());
  }

  @Override
  public ByteBuffer getMaxBytebuffer() {
    return ByteBuffer.wrap(max.getValues());
  }

  @Override
  public ByteBuffer getFirstBytebuffer() {
    return ByteBuffer.wrap(first.getValues());
  }

  @Override
  public ByteBuffer getLastBytebuffer() {
    return ByteBuffer.wrap(last.getValues());
  }

  @Override
  public ByteBuffer getSumBytebuffer() {
    return ReadWriteIOUtils.getByteBuffer(sum);
  }

  @Override
  public int sizeOfDatum() {
    return -1;
  }

  @Override
  public String toString() {
    return "[min:" + min + ",max:" + max + ",first:" + first + ",last:" + last + ",sum:" + sum
        + "]";
  }

  @Override
  void fill(InputStream inputStream) throws IOException {
    this.min = new Binary(ReadWriteIOUtils.readBytesWithSelfDescriptionLength(inputStream));
    this.max = new Binary(ReadWriteIOUtils.readBytesWithSelfDescriptionLength(inputStream));
    this.first = new Binary(ReadWriteIOUtils.readBytesWithSelfDescriptionLength(inputStream));
    this.last = new Binary(ReadWriteIOUtils.readBytesWithSelfDescriptionLength(inputStream));
    this.sum = ReadWriteIOUtils.readDouble(inputStream);
  }

  @Override
  void fill(ByteBuffer byteBuffer) throws IOException {
    this.min = new Binary(
        ReadWriteIOUtils.readByteBufferWithSelfDescriptionLength(byteBuffer).array());
    this.max = new Binary(
        ReadWriteIOUtils.readByteBufferWithSelfDescriptionLength(byteBuffer).array());
    this.first = new Binary(
        ReadWriteIOUtils.readByteBufferWithSelfDescriptionLength(byteBuffer).array());
    this.last = new Binary(
        ReadWriteIOUtils.readByteBufferWithSelfDescriptionLength(byteBuffer).array());
    this.sum = ReadWriteIOUtils.readDouble(byteBuffer);
  }

  @Override
  protected void fill(TsFileInput input, long offset) throws IOException {
    int size = getSerializedSize();
    ByteBuffer buffer = ByteBuffer.allocate(size);
    ReadWriteIOUtils.readAsPossible(input, offset, buffer);
    buffer.flip();
    fill(buffer);
  }
}
