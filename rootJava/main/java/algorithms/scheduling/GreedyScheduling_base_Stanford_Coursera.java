package algorithms.scheduling;

import org.apache.log4j.Logger;
import utils.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/2/13
 * Time: 2:15 PM
 */
public abstract class GreedyScheduling_base_Stanford_Coursera {

  private Logger log =
          Logger.getLogger(GreedyScheduling_base_Stanford_Coursera.class);

  protected AtomicInteger sequenceGenerator = new AtomicInteger();
  protected int numJobs;
  protected int[] weights;      // the 'priority':  higher value == higher priority
  protected int[] lengths;      //  running time of the job
  protected JobObject[] orderedJobs;

  public void run() {
    readDataFile(null);

    long result = compute();
    System.err.println("\n\nRESULT: "+result+"\n\n");
  }

  /**
   * create array of JobObjects
   * <p/>
   * sort in descending order
   * <p/>
   * resolve ties:  heavy weights first
   * <p/>
   * compute weighted sum
   *
   * @return
   */
  public long compute() {

    // create array of JobObjects
    orderedJobs = new JobObject[numJobs];
    for (int i = 0; i < numJobs; i++) {
      //JobObject_weightDiff jw = new JobObject_weightDiff(weights[i], lengths[i]);
      JobObject jw = newJobObject(weights[i], lengths[i]);
      orderedJobs[i] = jw;
    }


    // sort in descending order
    // the JobObject Comparator takes care of inverting the order
    Arrays.sort(orderedJobs);


    //for (int i=0; i<numJobs; i++) {
    //  log.debug(" sorted entry: "+i+" v="+orderedJobs[i].valueString()+", w="+orderedJobs[i].weight+", l="+orderedJobs[i].length);
    //}


    // now find ties and resolve them
    int tieCount = 0;
    int firstTieIndex = -1;
    int lastTieIndex = -1;
    int currIndex = 0;

    //int prevValue = orderedJobs[prevIndex].value;
    JobObject prevJobObject = orderedJobs[currIndex];
    currIndex++;

    while (currIndex < numJobs) {
      JobObject currJobObject = orderedJobs[currIndex];
      //int currValue = orderedJobs[currIndex].value;

      //if (currValue != prevValue) {
      //  prevValue = currValue;

      //log.debug("== index="+currIndex+", prev="+prevJobObject.valueString()+", curr="+currJobObject.valueString());

      if (prevJobObject.compareFunction(currJobObject) != 0) {
        prevJobObject = currJobObject;
        currIndex++;
        continue;
      }
      tieCount++;

      log.debug(" -- tie "+tieCount+":  detected at currIndex="+currIndex+", value="+currJobObject.valueString());
      // find tie boundaries and resolve that portion of the array
      firstTieIndex = currIndex - 1;
      lastTieIndex = currIndex;
      while (currIndex < numJobs &&
        currJobObject.compareFunction(prevJobObject) == 0) {

      //        currValue == prevValue) {
        lastTieIndex = currIndex;
        prevJobObject = currJobObject;
        //prevValue = currValue;
        currIndex++;

        // only set currValue if we're still within the jobs array
        if (currIndex < numJobs) {
          //currValue = orderedJobs[currIndex].value;
          currJobObject = orderedJobs[currIndex];
        }
      }

      // got our window
      resolveTies(firstTieIndex, lastTieIndex);
    }

    long cumulativeJobLength = orderedJobs[0].length;
    long weightedSum = orderedJobs[0].weight * cumulativeJobLength;

    for (int i = 1; i < numJobs; i++) {
      cumulativeJobLength = cumulativeJobLength + orderedJobs[i].length;
      weightedSum = weightedSum + (orderedJobs[i].weight * cumulativeJobLength);
    }
    return weightedSum;


    //return 0;
  }

  /**
   *
   * duplicate weights for ties:  it does not matter wrt to the weighted function
   * computation which one will go first since the values:
   *   w-l are identical
   *   the weight is identical
   *
   * so the summed quantity: ( w-i * C-i )  +  ( w-j * C-j )
   *   are commutative.

   weight-diff ties, sorted by decreasing weight:

   GreedyScheduling_base_Stanford_Coursera,main:147 -  ----------- ordered tie 2: value=92, weight=98
   GreedyScheduling_base_Stanford_Coursera,main:147 -  ----------- ordered tie 3: value=92, weight=96
   GreedyScheduling_base_Stanford_Coursera,main:147 -  ----------- ordered tie 4: value=92, weight=95
   GreedyScheduling_base_Stanford_Coursera,main:147 -  ----------- ordered tie 5: value=92, weight=95
   GreedyScheduling_base_Stanford_Coursera,main:147 -  ----------- ordered tie 6: value=92, weight=93
   GreedyScheduling_base_Stanford_Coursera,main:147 -  ----------- ordered tie 7: value=92, weight=93
   GreedyScheduling_base_Stanford_Coursera,main:150 -  ---  resolveTies: DONE



   * @param first
   * @param last
   */
  protected void resolveTies(int first, int last) {
    int tieLength = last - first + 1;
    log.debug(" ---  resolveTies: first="+first+", last="+last);

    JobObject_weight[] jw = new JobObject_weight[tieLength];
    int sortIndex = 0;
    for (int i = first; i <= last; i++) {
      jw[sortIndex] = new JobObject_weight(orderedJobs[i]);
      sortIndex++;
    }

    Arrays.sort(jw);
    sortIndex = 0;
    for (int i = first; i <= last; i++) {
      orderedJobs[i] = jw[sortIndex].wrapped;

      log.debug(" ----------- ordered tie "+sortIndex+": value="+orderedJobs[i].valueString()+", weight="+orderedJobs[i].weight);
      sortIndex++;
    }
    log.debug(" ---  resolveTies: DONE\n\n");
  }


  // compute weighted sum

  protected void readDataFile(String inputFName) {
    FileReader fileR = null;
    String f = "coursera_scheduling_jobs";
    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\algorithms\\scheduling";
    String fileName = d + "\\" + f;
    if (inputFName != null && inputFName.length() > 0) {
      fileName = inputFName;
    }
    try {
      fileR = new FileReader(fileName);
    } catch (FileNotFoundException e) {
      System.err.println(" cannot open data file " + fileName);
    }

    // get count so that we can build only the array we need
    try {
      BufferedReader br = new BufferedReader(fileR);
      String line;

      // first record is the job count
      line = br.readLine();
      numJobs = Integer.parseInt(line);
      weights = new int[numJobs];
      lengths = new int[numJobs];
      System.err.println("\nnumJobs="+numJobs+"\n");
      // numJobs=10000


      int i = 0;
      while ((line = br.readLine()) != null) {
        //System.err.println("read line '" + line + "'");
        String[] s = line.split("\\s+");
        if (i < numJobs) {
          weights[i] = Integer.parseInt(s[0]);
          lengths[i] = Integer.parseInt(s[1]);
        } else {
          System.err.println("Warning:  skipping excess input at index=" + i);
        }

        i++;
        if (i % 10 == 0) {
          //  System.err.println("read vertex #" + i + " = " + i);
        }
      }
      br.close();
    } catch (IOException e) {
      System.err.println("exception " + e.getMessage());
    }
  }

  /**
   * For sorting job ties by weight only.
   * <p/>
   * Descending order, inverse of ascending
   */
  protected class JobObject_weight implements Comparable {
    public final JobObject wrapped;

    public JobObject_weight(JobObject j) {
      wrapped = j;
    }

    @Override
    public int compareTo(Object o) {
      JobObject_weight other = (JobObject_weight) o;
      int oWeight = other.wrapped.weight;
      if (this.wrapped.weight < other.wrapped.weight) return 1;
      if (this.wrapped.weight > other.wrapped.weight) return -1;
      return 0;
    }
  }


  protected abstract JobObject newJobObject(int weight, int length);

  protected abstract class JobObject implements Comparable {
    public int sequence;   // convenient id
    public int weight;
    public int length;
    //public int value;


    /**
     * The 'value' of this Job wrt the Scheduling Algorithm
     *
     * @return
     */
    public abstract void computeValue();

    /**
     * Fill in for the Comparator based Java Sort
     *
     * @param other
     * @return
     */
    public abstract int compareFunction(JobObject other);

    public abstract String valueString();

    @Override
    public int compareTo(Object o) {
      JobObject other = (JobObject) o;
      return compareFunction(other);
    }
  }


}
