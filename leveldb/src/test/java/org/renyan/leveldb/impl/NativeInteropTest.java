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
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class NativeInteropTest extends TestCase {

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

    DBFactory iq80factory = LevelDBFactory.factory;
    DBFactory jnifactory = LevelDBFactory.factory;

    public NativeInteropTest() {
        try {
            ClassLoader cl = NativeInteropTest.class.getClassLoader();
            jnifactory = (DBFactory) cl.loadClass("org.fusesource.leveldbjni.JniDBFactory").newInstance();
        } catch (Throwable e) {
            // We cannot create a JniDBFactory on windows :( so just use a LevelDBFactory for both
            // to avoid test failures.
        }
    }

    File getTestDirectory(String name) throws IOException {
        File rc = new File(databaseDir, name);
        iq80factory.destroy(rc, new Options().createIfMissing(true));
        rc.mkdirs();
        return rc;
    }

    public void testCRUDviaIQ80() throws IOException, DBException {
        crud(iq80factory, iq80factory);
    }

    public void testCRUDviaJNI() throws IOException, DBException {
        crud(jnifactory, jnifactory);
    }

    public void testCRUDviaIQ80thenJNI() throws IOException, DBException {
        crud(iq80factory, jnifactory);
    }

    public void testCRUDviaJNIthenIQ80() throws IOException, DBException {
        crud(jnifactory, iq80factory);
    }

    public void crud(DBFactory firstFactory, DBFactory secondFactory) throws IOException, DBException {

        Options options = new Options().createIfMissing(true);

        File path = getTestDirectory(getName());
        DB db = firstFactory.open(path, options);

        WriteOptions wo = new WriteOptions().sync(false);
        ReadOptions ro = new ReadOptions().fillCache(true).verifyChecksums(true);
        db.put(bytes("Tampa"), bytes("green"));
        db.put(bytes("London"), bytes("red"));
        db.put(bytes("New York"), bytes("blue"));

        db.close();
        db = secondFactory.open(path, options);

        assertEquals(db.get(bytes("Tampa"), ro), bytes("green"));
        assertEquals(db.get(bytes("London"), ro), bytes("red"));
        assertEquals(db.get(bytes("New York"), ro), bytes("blue"));

        db.delete(bytes("New York"), wo);

        assertEquals(db.get(bytes("Tampa"), ro), bytes("green"));
        assertEquals(db.get(bytes("London"), ro), bytes("red"));
        assertNull(db.get(bytes("New York"), ro));

        db.close();
        db = firstFactory.open(path, options);

        assertEquals(db.get(bytes("Tampa"), ro), bytes("green"));
        assertEquals(db.get(bytes("London"), ro), bytes("red"));
        assertNull(db.get(bytes("New York"), ro));

        db.close();
    }

}
