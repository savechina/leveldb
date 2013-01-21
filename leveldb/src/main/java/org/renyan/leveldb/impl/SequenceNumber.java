/**
 * Copyright (C) 2011 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
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
package org.renyan.leveldb.impl;

import com.google.common.base.Preconditions;

public final class SequenceNumber
{
    // We leave eight bits empty at the bottom so a type and sequence#
    // can be packed together into 64-bits.
    public static final long MAX_SEQUENCE_NUMBER = ((0x1L << 56) - 1);

    public static long packSequenceAndValueType(long sequence, ValueType valueType)
    {
        Preconditions.checkArgument(sequence <= MAX_SEQUENCE_NUMBER, "Sequence number is greater than MAX_SEQUENCE_NUMBER");
        Preconditions.checkNotNull(valueType, "valueType is null");

        return (sequence << 8) | valueType.getPersistentId();
    }

    public static ValueType unpackValueType(long num)
    {
        return ValueType.getValueTypeByPersistentId((byte) num);
    }

    public static long unpackSequenceNumber(long num)
    {
        return num >>> 8;
    }
}
