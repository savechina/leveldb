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

import junit.framework.TestCase;

import org.renyan.leveldb.*;
import org.renyan.leveldb.impl.LevelDBFactory;
import org.renyan.leveldb.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Test the implemenation via the org.iq80.leveldb API.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class ApiTest extends TestCase {

    File databaseDir = FileUtils.createTempDir("leveldb");

    public static byte[] bytes(String value) {
        if( value == null) {
            return null;
        }
        try {
            return value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String asString(byte value[]) {
        if( value == null) {
            return null;
        }
        try {
            return new String(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void assertEquals(byte[] arg1, byte[] arg2) {
        assertTrue(asString(arg1)+" != "+asString(arg2), Arrays.equals(arg1, arg2));
    }

    DBFactory factory = LevelDBFactory.factory;

    File getTestDirectory(String name) throws IOException {
        File rc = new File(databaseDir, name);
        factory.destroy(rc, new Options().createIfMissing(true));
        rc.mkdirs();
        return rc;
    }

    public void testCompaction() throws IOException, DBException {

        Options options = new Options().createIfMissing(true).compressionType(CompressionType.NONE);

        File path = getTestDirectory(getName());
        DB db = factory.open(path, options);

        System.out.println("Adding");
        for( int i=0 ; i < 1000*1000; i++) {
            if(i%100000 == 0 ) {
                System.out.println("  at: "+i);
            }
            db.put(bytes("key"+i), bytes("value"+i));
        }

        db.close();
        db = factory.open(path, options);

        System.out.println("Deleting");
        for( int i=0 ; i < 1000*1000; i++) {
            if(i%100000 == 0 ) {
                System.out.println("  at: "+i);
            }
            db.delete(bytes("key"+i));
        }

        db.close();
        db = factory.open(path, options);

        System.out.println("Adding");
        for( int i=0 ; i < 1000*1000; i++) {
            if(i%100000 == 0 ) {
                System.out.println("  at: "+i);
            }
            db.put(bytes("key"+i), bytes("value"+i));
        }

        db.close();
    }

}
