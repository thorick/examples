package datastructures.MST;

import datastructures.MST.prims_ArrayHeap.MST_Prims_naive_Stanford_Coursera;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/7/13
 * Time: 3:04 PM
 */
public class MST_test_naive extends MST_base_test {


  private Logger log =
          Logger.getLogger(MST_test_naive.class);


  public MST_testable getMST_testable() {
    return new MST_Prims_naive_Stanford_Coursera();
  }
}
