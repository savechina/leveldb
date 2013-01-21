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

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Filename
{
    public enum FileType
    {
        LOG,
        DB_LOCK,
        TABLE,
        DESCRIPTOR,
        CURRENT,
        TEMP,
        INFO_LOG  // Either the current one, or an old one
    }

    /**
     * Return the name of the log file with the specified number.
     */
    public static String logFileName(long number)
    {
        return makeFileName(number, "log");
    }

    /**
     * Return the name of the sstable with the specified number.
     */
    public static String tableFileName(long number)
    {
        return makeFileName(number, "sst");
    }

    /**
     * Return the name of the descriptor file with the specified incarnation number.
     */
    public static String descriptorFileName(long number)
    {
        Preconditions.checkArgument(number >= 0, "number is negative");
        return String.format("MANIFEST-%06d", number);
    }

    /**
     * Return the name of the current file.
     */
    public static String currentFileName()
    {
        return "CURRENT";
    }

    /**
     * Return the name of the lock file.
     */
    public static String lockFileName()
    {
        return "LOCK";
    }

    /**
     * Return the name of a temporary file with the specified number.
     */
    public static String tempFileName(long number)
    {
        return makeFileName(number, "dbtmp");
    }

    /**
     * Return the name of the info log file.
     */
    public static String infoLogFileName()
    {
        return "LOG";
    }

    /**
     * Return the name of the old info log file.
     */
    public static String oldInfoLogFileName()
    {
        return "LOG.old";
    }

    /**
     * If filename is a leveldb file, store the type of the file in *type.
     * The number encoded in the filename is stored in *number.  If the
     * filename was successfully parsed, returns true.  Else return false.
     */
    public static FileInfo parseFileName(File file)
    {
        // Owned filenames have the form:
        //    dbname/CURRENT
        //    dbname/LOCK
        //    dbname/LOG
        //    dbname/LOG.old
        //    dbname/MANIFEST-[0-9]+
        //    dbname/[0-9]+.(log|sst|dbtmp)
        String fileName = file.getName();
        if ("CURRENT".equals(fileName)) {
            return new FileInfo(FileType.CURRENT);
        }
        else if ("LOCK".equals(fileName)) {
            return new FileInfo(FileType.DB_LOCK);
        }
        else if ("LOG".equals(fileName)) {
            return new FileInfo(FileType.INFO_LOG);
        }
        else if ("LOG.old".equals(fileName)) {
            return new FileInfo(FileType.INFO_LOG);
        }
        else if (fileName.startsWith("MANIFEST-")) {
            long fileNumber = Long.parseLong(removePrefix(fileName, "MANIFEST-"));
            return new FileInfo(FileType.DESCRIPTOR, fileNumber);
        }
        else if (fileName.endsWith(".log")) {
            long fileNumber = Long.parseLong(removeSuffix(fileName, ".log"));
            return new FileInfo(FileType.LOG, fileNumber);
        }
        else if (fileName.endsWith(".sst")) {
            long fileNumber = Long.parseLong(removeSuffix(fileName, ".sst"));
            return new FileInfo(FileType.TABLE, fileNumber);
        }
        else if (fileName.endsWith(".dbtmp")) {
            long fileNumber = Long.parseLong(removeSuffix(fileName, ".dbtmp"));
            return new FileInfo(FileType.TEMP, fileNumber);
        }
        return null;
    }

    /**
     * Make the CURRENT file point to the descriptor file with the
     * specified number.
     * @return true if successful; false otherwise
     */
    public static boolean setCurrentFile(File databaseDir, long descriptorNumber)
            throws IOException
    {
        String manifest = descriptorFileName(descriptorNumber);
        String temp = tempFileName(descriptorNumber);

        File tempFile = new File(databaseDir, temp);
        Files.write(manifest + "\n", tempFile, Charsets.UTF_8);
        File to = new File(databaseDir, currentFileName());
        boolean ok = tempFile.renameTo(to);
        if (!ok) {
            tempFile.delete();
            Files.write(manifest + "\n", to, Charsets.UTF_8);
        }
        return ok;
    }

    public static List<File> listFiles(File dir)
    {
        File[] files = dir.listFiles();
        if (files == null) {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf(files);
    }

    private static String makeFileName(long number, String suffix)
    {
        Preconditions.checkArgument(number >= 0, "number is negative");
        Preconditions.checkNotNull(suffix, "suffix is null");
        return String.format("%06d.%s", number, suffix);
    }

    private static String removePrefix(String value, String prefix)
    {
        return value.substring(prefix.length());
    }

    private static String removeSuffix(String value, String suffix)
    {
        return value.substring(0, value.length() - suffix.length());
    }

    public static class FileInfo
    {
        private FileType fileType;
        private long fileNumber;

        public FileInfo(FileType fileType)
        {
            this(fileType, 0);
        }

        public FileInfo(FileType fileType, long fileNumber)
        {
            Preconditions.checkNotNull(fileType, "fileType is null");
            this.fileType = fileType;
            this.fileNumber = fileNumber;
        }

        public FileType getFileType()
        {
            return fileType;
        }

        public long getFileNumber()
        {
            return fileNumber;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            FileInfo fileInfo = (FileInfo) o;

            if (fileNumber != fileInfo.fileNumber) {
                return false;
            }
            if (fileType != fileInfo.fileType) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = fileType.hashCode();
            result = 31 * result + (int) (fileNumber ^ (fileNumber >>> 32));
            return result;
        }

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("FileInfo");
            sb.append("{fileType=").append(fileType);
            sb.append(", fileNumber=").append(fileNumber);
            sb.append('}');
            return sb.toString();
        }
    }
}
