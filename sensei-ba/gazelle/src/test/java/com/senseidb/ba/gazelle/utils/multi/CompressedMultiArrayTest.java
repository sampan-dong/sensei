package com.senseidb.ba.gazelle.utils.multi;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.senseidb.ba.gazelle.utils.ReadMode;

public class CompressedMultiArrayTest {

  private CompressedMultiArray compressedMultiArray;
  private int[] buffer;

  @Before
  public void setUp() throws Exception {
    compressedMultiArray = new CompressedMultiArray(10, 1000);
    compressedMultiArray.setMaxNumOfElementsPerChunk(909);
     for (int i = 0 ; i < 10000; i ++) {
       int length = i % 10;
       int[] arr = new int[length];
       Arrays.fill(arr, i % 10);
       compressedMultiArray.add(arr);      
     }  
     compressedMultiArray.initSkipLists(); 
     buffer = new int[10];
  }

  @After
  public void tearDown() throws Exception {
  }

 
 
  @Test
  public void test3ReadAllValues() {    
    MultiFacetIterator iterator = compressedMultiArray.iterator();
    
    for (int i = 0; i < 10000; i++) {
    
     assertTrue(iterator.advance(i));
      int length = i % 10;
      assertEquals(length, iterator.readValues(buffer));
      for (int j = 0; j < length; j++) {
        assertEquals(length, buffer[j]);
      }
    }
    assertEquals(9, compressedMultiArray.getMaxNumValuesPerDoc());
  }
  @Test
  public void test4ReadAllValuesWith1500Increment() {    
    MultiFacetIterator iterator = compressedMultiArray.iterator();    
    for (int i = 0; i < 10000; i+=500) {     
      assertTrue(iterator.advance(i));
      int length = i % 10;
      assertEquals(length, iterator.readValues(buffer));
      for (int j = 0; j < length; j++) {
        assertEquals(length, buffer[j]);
      }
    }
  }
  @Test
  public void test2ReadNonExistingValue() {    
    MultiFacetIterator iterator = compressedMultiArray.iterator();
    assertFalse(iterator.advance(10001));  
  }
  
  @Test
  public void test6ReadAllValuesWith1000IncrementAfterPersistingAndReadingThefile() throws Exception {    
    File dir = new File("temp");
    try {
    FileUtils.deleteDirectory(dir);
    dir.mkdirs();
   
    compressedMultiArray.flushToFile(dir, "multiValueColumnName");
    compressedMultiArray = CompressedMultiArray.readFromFile( dir, "multiValueColumnName", 10, ReadMode.DBBuffer);
   
    MultiFacetIterator iterator = compressedMultiArray.iterator();
    
    for (int i = 0; i < 10000; i+=1) {
     
      assertTrue(iterator.advance(i));
      int length = i % 10;      
      assertEquals(length, iterator.readValues(buffer));
      for (int j = 0; j < length; j++) {
        assertEquals(length, buffer[j]);
      }
    }
    } finally {
      FileUtils.deleteDirectory(dir);
    }
  }
  public int getBufferLength(int i) {
    int length = i % 10;
    if (length == 0) {
      length = 1;
    }
    return length;
  }
 
}
