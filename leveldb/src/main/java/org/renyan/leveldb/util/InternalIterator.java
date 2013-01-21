package org.renyan.leveldb.util;

import org.renyan.leveldb.impl.InternalKey;
import org.renyan.leveldb.impl.SeekingIterator;

/**
 * <p>A common interface for internal iterators.</p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface InternalIterator extends SeekingIterator<InternalKey, Slice> {
}
