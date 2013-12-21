package datastructures.MST;

import datastructures.MST.prims_ArrayHeap.MST_Prims_vertexHeaps_Stanford_Coursera;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/10/13
 * Time: 10:55 AM
 */
public class MST_test_vertexHeaps extends MST_base_test {



  private Logger log =
          Logger.getLogger(MST_test_vertexHeaps.class);


  public MST_testable getMST_testable() {
    return new MST_Prims_vertexHeaps_Stanford_Coursera();
  }
}
