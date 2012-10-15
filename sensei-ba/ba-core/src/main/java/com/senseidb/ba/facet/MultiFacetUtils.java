package com.senseidb.ba.facet;

import java.io.IOException;

import org.apache.lucene.search.DocIdSetIterator;

import com.browseengine.bobo.api.BrowseSelection;
import com.browseengine.bobo.api.FacetSpec;
import com.browseengine.bobo.docidset.RandomAccessDocIdSet;
import com.browseengine.bobo.facets.data.FacetDataCache;
import com.browseengine.bobo.facets.impl.DefaultFacetCountCollector;
import com.senseidb.ba.MultiValueForwardIndex;
import com.senseidb.ba.gazelle.utils.multi.MultiFacetIterator;

public class MultiFacetUtils {
    public static final class MultiForwardIndexCountCollector extends DefaultFacetCountCollector {
        private final MultiFacetIterator iterator;
        private final int[] buffer;
        private final MultiValueForwardIndex forwardIndex;

        @SuppressWarnings("rawtypes")
        public MultiForwardIndexCountCollector(String name, FacetDataCache dataCache, MultiValueForwardIndex forwardIndex, int docBase, BrowseSelection sel, FacetSpec ospec) {
            super(name, dataCache, docBase, sel, ospec);
            iterator = forwardIndex.getIterator();
            buffer = new int[forwardIndex.getMaxNumValuesPerDoc()];
            this.forwardIndex = forwardIndex;
        }

        @Override
          public void collect(int docid) {
            iterator.advance(docid);
            int count = iterator.readValues(buffer);
            while (count > 0) {
                int valueIndex = buffer[--count];
                _count.add(valueIndex, _count.get(valueIndex) + 1);
            }
          }

        @Override
          public void collectAll() {
            for (int i = 0; i < forwardIndex.getLength(); i++) {
                collect(i);
            }
            
          }
    }
   
    public static class MultiForwardIndexIterator extends DocIdSetIterator {
        int doc = -1;
        private final MultiValueForwardIndex forwardIndex;
        private final int index;
        private int length;
        private MultiFacetIterator iterator;
        private int[] buffer;
        public MultiForwardIndexIterator(MultiValueForwardIndex forwardIndex, int index) {
          this.forwardIndex = forwardIndex;
          this.index = index;
           length = forwardIndex.getLength();
           iterator = forwardIndex.getIterator();
           buffer = new int[forwardIndex.getMaxNumValuesPerDoc()];
        }
        @Override
        public int nextDoc() throws IOException {
          while (true) {
            doc++;
            if (!iterator.advance(doc)) return NO_MORE_DOCS;
              int count = iterator.readValues(buffer);
              while(count > 0) {
                  if (index == buffer[--count]) {
                      return doc;
                  }
              }
            }
        }
        @Override
        public int docID() {
          return doc;
        }

        @Override
        public int advance(int target) throws IOException {
          doc = target - 1;
          return nextDoc();
        }
      }
      public static class MultiForwardDocIdSet extends RandomAccessDocIdSet {
        private MultiValueForwardIndex forwardIndex;
        private int index;
        private int[] buffer;

        public MultiForwardDocIdSet(MultiValueForwardIndex forwardIndex, int index) {
          this.forwardIndex = forwardIndex;
          this.index = index;
        }
        @Override
        public DocIdSetIterator iterator() throws IOException {
          return new MultiForwardIndexIterator(forwardIndex, index);
        }
        
        @Override
        public boolean get(int docId) {
            buffer = new int[forwardIndex.getMaxNumValuesPerDoc()];
            int count = forwardIndex.randomRead(buffer, docId);
            while(count > 0) {
                if (index == buffer[--count]) {
                    return true;
                }
            }
            return false;
        }
      }
}
