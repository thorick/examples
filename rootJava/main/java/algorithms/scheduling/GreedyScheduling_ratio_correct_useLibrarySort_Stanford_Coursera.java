package algorithms.scheduling;

import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 9/3/13
 * Time: 6:37 PM
 *
 *
 * Question 1
 In this programming problem and the next you'll code up the greedy algorithms from lecture for minimizing the weighted sum of completion times..

 Download the text file here. This file describes a set of jobs with positive and integral weights and lengths. It has the format
 [number_of_jobs]
 [job_1_weight] [job_1_length]
 [job_2_weight] [job_2_length]
 ...
 For example, the third line of the file is "74 59", indicating that the second job has weight 74 and length 59.

 You should NOT assume that edge weights or lengths are distinct.

 Your task in this problem is to run the greedy algorithm that schedules jobs in decreasing order of the difference (weight - length).

 Recall from lecture that this algorithm is not always optimal.
 IMPORTANT: if two jobs have equal difference (weight - length), you should schedule the job with higher weight first.
 Beware: if you break ties in a different way, you are likely to get the wrong answer.

 You should report the sum of weighted completion times of the resulting schedule --- a positive integer --- in the box below.

 ADVICE: If you get the wrong answer, try out some small test cases to debug your algorithm (and post your test cases to the discussion forum)!



 RESULT: 67311454237   <<  latest ratio !



 */
public class GreedyScheduling_ratio_correct_useLibrarySort_Stanford_Coursera extends
  GreedyScheduling_base_Stanford_Coursera
{

  private Logger log =
            Logger.getLogger(GreedyScheduling_ratio_correct_useLibrarySort_Stanford_Coursera.class);



  private double zeroCheck = 0.00000000001;

  protected JobObject_weightRatio newJobObject(int weight, int length) {
    return new JobObject_weightRatio(weight, length);
  }

  protected class JobObject_weightRatio extends GreedyScheduling_base_Stanford_Coursera.JobObject {

    protected double value;

    public JobObject_weightRatio(int weight, int length)  {
      sequence = sequenceGenerator.incrementAndGet();
      this.weight = weight;
      this.length = length;
      computeValue();
    }

    @Override
    public void computeValue() {
      value = (double)weight / (double)length;
    }

    @Override
    public int compareFunction(JobObject other) {
      double oValue = ((JobObject_weightRatio)other).value;

      if ((oValue == 0.0 && value == 0.0)
        || (oValue == value))
      {
        log.debug(" ????  oValue'"+oValue+" == value="+value);
        return 0;
      }


      double diff = oValue - this.value;
      double percent = Math.abs(diff / oValue);

      //log.debug(" ????  oValue'"+oValue+", value="+value+", percent="+percent);


      if (percent < zeroCheck) {
        log.debug(" ????  value'"+value+" ==  oValue="+oValue+", percent="+percent);

        //log.debug(" ?????    less than: "+zeroCheck+", treat as equal, return 0");
        return 0;
      }


        if (value < oValue) {
          log.debug(" ????  value'"+value+" <  oValue="+oValue);

          return 1;
        }

      log.debug(" ????  value'"+value+" >  oValue="+oValue);

        return -1;
        //if (value > oValue) return -1;
        //return 0;
    }

    public String valueString() {
      return Double.toString(value);
    }
  }

  public static void main(String[] args) {
    GreedyScheduling_ratio_correct_useLibrarySort_Stanford_Coursera prog =
            new GreedyScheduling_ratio_correct_useLibrarySort_Stanford_Coursera();

    prog.run();
  }

  /*

  ratio range:

GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 0 v=99.0, w=99, l=1
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 1 v=98.0, w=98, l=1
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 2 v=95.0, w=95, l=1
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 3 v=95.0, w=95, l=1
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 4 v=93.0, w=93, l=1
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 5 v=93.0, w=93, l=1
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 6 v=92.0, w=92, l=1
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 7 v=88.0, w=88, l=1
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 8 v=87.0, w=87, l=1
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 9 v=86.0, w=86, l=1
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 10 v=86.0, w=86, l=1
...
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 9986 v=0.01098901098901099, w=1, l=91
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 9987 v=0.010869565217391304, w=1, l=92
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 9988 v=0.010869565217391304, w=1, l=92
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 9989 v=0.010869565217391304, w=1, l=92
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 9990 v=0.010752688172043012, w=1, l=93
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 9991 v=0.010638297872340425, w=1, l=94
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 9992 v=0.010638297872340425, w=1, l=94
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 9993 v=0.010526315789473684, w=1, l=95
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 9994 v=0.010416666666666666, w=1, l=96
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 9995 v=0.010416666666666666, w=1, l=96
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 9996 v=0.010309278350515464, w=1, l=97
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 9997 v=0.010309278350515464, w=1, l=97
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 9998 v=0.010309278350515464, w=1, l=97
GreedyScheduling_base_Stanford_Coursera,main:65 -  sorted entry: 9999 v=0.010309278350515464, w=1, l=97

   */
}
