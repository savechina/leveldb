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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.renyan.leveldb.DB;
import org.renyan.leveldb.DBFactory;
import org.renyan.leveldb.Options;
import org.renyan.leveldb.util.FileUtils;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class LevelDBFactory implements DBFactory {

    public static final int CPU_DATA_MODEL = Integer.getInteger("sun.arch.data.model");

    // We only use MMAP on 64 bit systems since it's really easy to run out of
    // virtual address space on a 32 bit system when all the data is getting mapped
    // into memory.  If you really want to use MMAP anyways, use -Dleveldb.mmap=true
    public static final boolean USE_MMAP = Boolean.parseBoolean(System.getProperty("leveldb.mmap", ""+(CPU_DATA_MODEL>32)));

    public static final String VERSION;
    static {
        String v="unknown";
        InputStream is = LevelDBFactory.class.getResourceAsStream("version.txt");
        try {
            v = new BufferedReader(new InputStreamReader(is, "UTF-8")).readLine();
        } catch (Throwable e) {
        } finally {
            try {
                is.close();
            } catch (Throwable e) {
            }
        }
        VERSION = v;
    }

    public static final LevelDBFactory factory = new LevelDBFactory();
    
    @Override
    public DB open(File path, Options options) throws IOException {
        return new DbImpl(options, path);
    }

    @Override
    public void destroy(File path, Options options) throws IOException {
        // TODO: This should really only delete leveldb created files.
        FileUtils.deleteRecursively(path);                
    }

    @Override
    public void repair(File path, Options options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return String.format("renyan leveldb version %s", VERSION);
    }
}
