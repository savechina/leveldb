package org.renyan.leveldb.util;

import org.renyan.leveldb.impl.FileMetaData;
import org.renyan.leveldb.impl.InternalKey;
import org.renyan.leveldb.impl.InternalKeyComparator;
import org.renyan.leveldb.impl.TableCache;

import java.util.List;
import java.util.Map.Entry;

public final class LevelIterator extends AbstractSeekingIterator<InternalKey, Slice> implements InternalIterator
{
    private final TableCache tableCache;
    private final List<FileMetaData> files;
    private final InternalKeyComparator comparator;
    private InternalTableIterator current;
    private int index;

    public LevelIterator(TableCache tableCache, List<FileMetaData> files, InternalKeyComparator comparator)
    {
        this.tableCache = tableCache;
        this.files = files;
        this.comparator = comparator;
    }

    @Override
    protected void seekToFirstInternal()
    {
        // reset index to before first and clear the data iterator
        index = 0;
        current = null;
    }

    @Override
    protected void seekInternal(InternalKey targetKey)
    {
        // seek the index to the block containing the key
        if (files.size() == 0) {
            return;
        }

        // todo replace with Collections.binarySearch
        int left = 0;
        int right = files.size() - 1;

        // binary search restart positions to find the restart position immediately before the targetKey
        while (left < right) {
            int mid = (left + right) / 2;

            if (comparator.compare(files.get(mid).getLargest(), targetKey) < 0) {
                // Key at "mid.largest" is < "target".  Therefore all
                // files at or before "mid" are uninteresting.
                left = mid + 1;
            }
            else {
                // Key at "mid.largest" is >= "target".  Therefore all files
                // after "mid" are uninteresting.
                right = mid;
            }
        }
        index = right;

        // if the index is now pointing to the last block in the file, check if the largest key
        // in the block is than the the target key.  If so, we need to seek beyond the end of this file
        if (index == files.size() - 1 && comparator.compare(files.get(index).getLargest(), targetKey) < 0) {
            index++;
        }

        // if indexIterator does not have a next, it mean the key does not exist in this iterator
        if (index < files.size()) {
            // seek the current iterator to the key
            current = openNextFile();
            current.seek(targetKey);
        }
        else {
            current = null;
        }
    }

    @Override
    protected Entry<InternalKey, Slice> getNextElement()
    {
        // note: it must be here & not where 'current' is assigned,
        // because otherwise we'll have called inputs.next() before throwing
        // the first NPE, and the next time around we'll call inputs.next()
        // again, incorrectly moving beyond the error.
        boolean currentHasNext = false;
        while (true) {
            if (current != null) {
                currentHasNext = current.hasNext();
            }
            if (!(currentHasNext)) {
                if (index < files.size()) {
                    current = openNextFile();
                }
                else {
                    break;
                }
            }
            else {
                break;
            }
        }
        if (currentHasNext) {
            return current.next();
        }
        else {
            // set current to empty iterator to avoid extra calls to user iterators
            current = null;
            return null;
        }
    }

    private InternalTableIterator openNextFile()
    {
        FileMetaData fileMetaData = files.get(index);
        index++;
        return tableCache.newIterator(fileMetaData);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("ConcatenatingIterator");
        sb.append("{index=").append(index);
        sb.append(", files=").append(files);
        sb.append(", current=").append(current);
        sb.append('}');
        return sb.toString();
    }
}
