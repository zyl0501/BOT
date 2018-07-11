package com.ray.router.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
  @Test
  public void addition_isCorrect() throws Exception {
    List<String> strList = new ArrayList<>();
    strList.add("1");
    List list = strList;
    Object obj ="1";
    list.add(obj);
    list.get(0);
    System.out.println(list.get(0));
    assertEquals(4, 2 + 2);
  }
}