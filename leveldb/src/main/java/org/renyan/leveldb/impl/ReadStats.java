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

public class ReadStats
{
    private int seekFileLevel = -1;
    private FileMetaData seekFile;

    public void clear() {
        seekFileLevel = -1;
        seekFile = null;
    }

    public int getSeekFileLevel()
    {
        return seekFileLevel;
    }

    public void setSeekFileLevel(int seekFileLevel)
    {
        this.seekFileLevel = seekFileLevel;
    }

    public FileMetaData getSeekFile()
    {
        return seekFile;
    }

    public void setSeekFile(FileMetaData seekFile)
    {
        this.seekFile = seekFile;
    }
}