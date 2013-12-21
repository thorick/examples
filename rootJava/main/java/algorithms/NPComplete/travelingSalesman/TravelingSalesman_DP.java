package algorithms.NPComplete.travelingSalesman;

import bits.BitUtils;
import org.apache.log4j.Logger;
import utils.Combinations;
import utils.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 10/5/13
 * Time: 8:04 PM
 * <p/>
 * In this assignment you will implement one or more algorithms for the traveling salesman problem,
 * such as the dynamic programming algorithm covered in the video lectures.
 * <p/>
 * Here is a data file describing a TSP instance.
 * <p/>
 * The first line indicates the number of cities.
 * <p/>
 * Each city is a point in the plane, and each subsequent line indicates the x- and y-coordinates of a single city.
 * <p/>
 * The distance between two cities is defined as the Euclidean distance --- that is,
 * two cities at locations (x,y) and (z,w) have distance SQRT (x−z)2+(y−w)2 between them.
 * <p/>
 * In the box below, type in the minimum cost of a traveling salesman tour for this instance,
 * rounded down to the nearest integer.
 * <p/>
 * <p/>
 * OPTIONAL: If you want bigger data sets to play with, check out the TSP instances from around the world here.
 * The smallest data set (Western Sahara) has 29 cities, and most of the data sets are much bigger than that.
 * What's the largest of these data sets that you're able to solve --- using dynamic programming or,
 * if you like, a completely different method?
 * <p/>
 * HINT: You might experiment with ways to reduce the data set size.
 * For example, trying plotting the points.
 * <p/>
 * Can you infer any structure of the optimal solution? Can you use that structure to speed up your algorithm?
 * <p/>
 * (me:  how about minimum cuts ?)
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * num cities=25
 * read 25 cities.
 * ---- result: 29189.236283910068
 * <p/>
 * round down:  29189
 */
public class TravelingSalesman_DP {

  private Logger log =
          Logger.getLogger(TravelingSalesman_DP.class);

  protected City[] citiesOrig;    // saved copy of original input
  protected City[] cities;        // working copy that is being computed upon
  protected int numCities;
  protected int vertexLimit = 32;

  //
  //  dp result array
  //
  //    A[S, j]
  //
  //    source city implicit '0'
  //
  //    rows    = size of S:  tour including all vertices 0,..,m only once
  //    columns = destination city 'j'
  //
  //
  //

  // our DP will alternate between which is previous and which is current
  protected Map<Integer, Double>[] visitedSetsA;
  protected Map<Integer, Double>[] visitedSetsB;
  protected Map<Integer, Double>[] currVisitedSet;
  protected Map<Integer, Double>[] prevVisitedSet;
  protected boolean currVisitedSetsIsA = true;

  // meant to hold the evolving paths
  // you only really need to save the 'chosen' paths of 'curr'
  // but to do this, you have to remember the candidate paths of 'prev'
  // there might not be enough memory available to do this !
  //  Q; what about presized  short arrays ?   arrays are Objects
  protected Map<Integer, String> currVisitedVertexPath;
  protected Map<Integer, String> prevVisitedVertexPath;

  protected int originCity;     // single compute origin city index
  protected double minPathLength;   // single compute result
  protected String minPath;         // single compute cycle path

  protected double[] minPathLengths;  // indexed by city index
  protected String[] minPaths;        // indexed by city index

  public TravelingSalesman_DP() {

  }

  public TravelingSalesman_DP(City[] c) {
    setCity(c);
  }

  public void setCity(City[] c) {
    citiesOrig = c;
    numCities = citiesOrig.length;
    if (numCities > vertexLimit) {
      throw new RuntimeException("Error found " + numCities + " cities, DP parentsPrev checker can only handle up to " +
              vertexLimit);
    }
  }


  public double compute() {
    return compute(0);
  }

  public double compute(int cityIndex) {
    originCity = cityIndex;

    // set the origin city as the zero-th cities element
    cities = new City[numCities];
    for (int i = 0; i < numCities; i++) {
      cities[i] = citiesOrig[i];
    }
    if (originCity != 0) {
      if (originCity >= numCities) throw new RuntimeException(" specified zero based origin city index " + originCity +
              " exceeds number of cities: " + numCities);
      City temp = cities[originCity];
      cities[originCity] = cities[0];
      cities[0] = temp;
    }

    init();

    doDP();

    return findMinCycle();
  }

  public void doDP() {


    // solve all subproblems by increasing total selected vertex cardinality order
    for (int m = 3; m <= numCities; m++) {
      if (currVisitedSetsIsA) {
        currVisitedSet = visitedSetsB;
        prevVisitedSet = visitedSetsA;
        for (Map map : currVisitedSet) {
          //for (Map map : visitedSetsB) {
          map.clear();
        }
        currVisitedSetsIsA = false;
      } else {
        currVisitedSet = visitedSetsA;
        prevVisitedSet = visitedSetsB;
        for (Map map : currVisitedSet) {
          //for (Map map : visitedSetsA) {
          map.clear();
        }
        currVisitedSetsIsA = true;
      }

      // all paths ending at 'j', 'j' going through all vertices 1,..,m
      // the '0' origin vertex is implicit
      //
      // the last city is numbered 'numCities - 1'
      //
      for (int j = 1; j < numCities; j++) {
        if (isP()) {
          log.debug(" ");
          log.debug("    ########  BEGIN m=" + m + ", j=" + j +
                  "\n");
        }
        findMin(m, j);
      }
    }
  }

  /**
   * For the given subproblem
   * find the minimum weight path from 0 to j  going through the set of vertices 0,..,j exactly once
   * <p/>
   * min for  k in S{0,..,m},  k != j,
   * A[ (S - j), k] + weight(k,j)
   * <p/>
   * <p/>
   * try all the 'pre-terminal vertices' k.
   * k cannot be '0' because that's the implicit starting vertex of all paths
   * k can range from 1 to m  (but cannot be 'j' as that is the last hop terminal)
   * <p/>
   * so we go through and check all the values 1,m  except j.
   * the pre-calculated values are all minimums from subproblem calculations
   *
   * @param m
   * @param j
   * @return
   */
  public void findMin(int m, int j) {

    int numVia = m - 2;   // convenience:  the number of 'via' vertices in this set
    // e.g.: when m = 2   the path is a direct route from origin to destination
    // 'via'  NO intermediaries, so 'via' in this case is 0.

    // candidate vertex total set size for the 'k' hop vertex is:
    //  number of cities -
    //  origin city  (1)  -
    //  dest city 'j' (1)
    //
    // we will be choosing all combinations of 'numVia' from this set
    int setSize = numCities - 2;


    // get the List of vertex combinations that need to be set for this total
    // number 'm' of vertices visited (including the origin)
    // this will be all the combinations of the available set taken 'numVia' at a time
    //
    // set the candidate list
    int[] candidateSet = new int[setSize];
    int index = 0;
    for (int i = 1; i < numCities; i++) {
      if (i == j) continue;
      candidateSet[index++] = i;
    }
    List<List<Integer>> combList = Combinations.combinations(candidateSet, numVia);

    if (isP()) {
      log.debug("for m=" + m + ", j=" + j + ", setSize=" + setSize + " numVia=" + numVia + ",  there are " + combList.size() + " 'via' entries to compute.");
      log.debug(Combinations.printListList(combList));
    }
    System.err.println("for m=" + m + ", j=" + j + ", setSize=" + setSize + " numVia=" + numVia + ",  there are " + combList.size() + " 'via' entries to compute.");
    //System.err.println(Combinations.printListList(combList));

    // process each combination
    for (List<Integer> list : combList) {
      int listSize = list.size();
      int[] newJKeyInts = new int[listSize];
      String newJKeyString = "";   // for diagnostics
      int newJKey = 0;     // this will be the key for the new entry for this 'j' and this 'via set'
      index = 0;
      for (Integer i : list) {
        newJKeyInts[index++] = i;
        newJKey = setCity(i, newJKey);
        newJKeyString = newJKeyString + "," + i;
      }

      // now go through each individual vertex in the new j key and do DP
      // selecting from the min hops choosing 'k' from each individual vertex
      // in turn
      double minWeight = Double.MAX_VALUE;
      for (int i = 0; i < newJKeyInts.length; i++) {
        int chosenK = -1;
        int k = newJKeyInts[i];   // compute taking this value as 'k'
        if (k == j) {
          if (isP()) {
            //log.debug("skipping candidate k=" + k + " because k=j=" + j);
          }
          continue;
        }
        String keyString = "";    // for diagnostics only
        int kKey = 0;    // now compute the key value that we want for this 'k'
        for (int v = 0; v < newJKeyInts.length; v++) {
          int city = newJKeyInts[v];
          if (city == k) continue;
          kKey = setCity(city, kKey);
          keyString = keyString + "," + city;
        }

        // lookup value getting to 'k' via 'the other vertices' from last time
        Double prevKLength = prevVisitedSet[k].get(kKey);
        if (prevKLength == null)
          throw new RuntimeException("m=" + m + ", j=" + j + " NO value is set for k=" + k + ", lookup key=" + keyString);

        double thisWeight = prevKLength + cities[k].distance(cities[j]);
        if (isP()) {
          /*
          log.debug(" -- m=" + m + ", k=" + k +
                  " prev weight for k=" + k + " key=" + keyString + ", w=" + prevKLength +
                  ", dist " + k + "-" + j + "=" + cities[k].distance(cities[j]) +
                  " "
          );
          */
        }

        if (thisWeight < minWeight) {
          minWeight = thisWeight;
          chosenK = k;
          if (isP()) {
            //log.debug("    --  chosenK=" + chosenK + ", minWeight=" + minWeight);
          }
        }
      }

      // we've now got the minimum value for this 'j' and 'via' key value
      currVisitedSet[j].put(newJKey, minWeight);
      if (isP()) {
        /*
        log.debug(" ");
        log.debug("****   for m=" + m + ",    j=" + j + ", jKey=" + newJKeyString + ", minWeight=" + minWeight + "\n");
        */
      }
    }
  }

  /**
   * Now that we've computed the min paths from 0 to all vertices
   * <p/>
   * Finish by finding the last hop back to '0' that yields the lowest cost cycle !
   *
   * @return
   */
  public double findMinCycle() {
    int chosenJ = -1;
    double minCycleWeight = Double.MAX_VALUE;

    if (isP()) {
      log.debug("\n\n");
      log.debug(" +++  find minCycle now  +++\n");
    }
    for (int j = 1; j < numCities; j++) {

      // final minweighted paths through all cities from the final calculation DP iteration
      Iterator it = currVisitedSet[j].values().iterator();

      double preWeight = (Double) it.next();

      preWeight = preWeight + cities[0].distance(cities[j]);
      if (isP()) {
        int n = numCities;
        log.debug(" -- j=" + j + ",  dist 0-j = " + cities[0].distance(cities[j]) +
                ",  preWeight=" + preWeight);
      }
      if (preWeight < minCycleWeight) {
        chosenJ = j;
        minCycleWeight = preWeight;
        if (isP()) {
          log.debug("    --  NEW  minCycleWeight " + j + ", = " + minCycleWeight);
        }
      }
    }
    //minPath = pathsNext[chosenJ] + "-0";
    System.err.println(" ----- result: " + minCycleWeight + ",  " + minPath);
    minPathLength = minCycleWeight;
    return minPathLength;
  }


  /**
   * Initialize the base case  m = 0   empty
   * and
   * m = 1   only the origin city
   * and
   * m = 2   direct from origin to first city 'via' no other city
   * <p/>
   * <p/>
   * the city mask considers the start city to be index = '0'
   * so the value of index = '0' will contain a single entry key='0', value '0.0'
   * <p/>
   * after that we can begin DP at m = 2:  start off and visit the first city
   * <p/>
   * m = 0  == prev
   * m = 1  == curr
   * <p/>
   * <p/>
   * if S = {0}  ->  0          at origin gone no where
   * else        ->  infinity   (can't go from 0 -> -> any other vertex, there is no other..  empty set}
   * <p/>
   * A[S, j==0]
   */
  public void init() {

    // nonsense  m = 1  case.  Here only for completeness
    // meaningless hash with distance = 0;
    visitedSetsB = new HashMap[numCities];
    Map map = new HashMap();
    map.put(new Integer(0), new Double(0.0));
    visitedSetsB[0] = map;
    for (int i = 1; i < numCities; i++) {
      map = new HashMap();
      map.put(new Integer(0), Double.MAX_VALUE);
      visitedSetsB[i] = map;
    }


    // m = 2 entries:
    //       direct hop from '0' to destination city
    //       'via' vertices hash is '0'
    //
    visitedSetsA = new HashMap[numCities];
    // special case zero length self loop, origin city back to itself
    map = new HashMap();
    map.put(new Integer(0), Double.MAX_VALUE);
    visitedSetsA[0] = map;
    for (int i = 1; i < numCities; i++) {
      map = new HashMap();
      map.put(new Integer(0), cities[0].distance(cities[i]));

      // BUG it is the map for [k] via, in this case via is '0'  back to origin !
      //map.put(new Integer(i), cities[0].distance(cities[i]));
      visitedSetsA[i] = map;
    }
    currVisitedSetsIsA = true;
  }


  public void loadCities(City[] c) {
    cities = c;
  }

  protected boolean isSet(int theCityToCheck, int parentCityBitMap) {
    if (theCityToCheck == 0) return true;    // zero is ALWAYS included
    int mask = 1;
    mask = mask << (theCityToCheck - 1);
    if (isP()) {
      //log.debug(" ----  theCityToCheck" + theCityToCheck + " mask=" + BitUtils.printBits(mask));
    }
    return (parentCityBitMap & mask) > 0;
  }

  protected int setCity(int theCityToSet, int parentCityBitMap) {
    if (theCityToSet == 0) return parentCityBitMap;   // '0' is always set
    int mask = 1;
    mask = mask << (theCityToSet - 1);
    if (isP()) {
      //log.debug(" theCityToSet=" + theCityToSet + ", bitMap before=" + BitUtils.printBits(parentCityBitMap));
    }
    parentCityBitMap = parentCityBitMap | mask;
    if (isP()) {
      //log.debug(" theCityToSet=" + theCityToSet + ", bitMap after=" + BitUtils.printBits(parentCityBitMap));
    }
    return parentCityBitMap;
  }

  protected City[] readDataFile(String inputFName) {
    City[] retVal = null;

    FileReader fileR = null;
    String f = "tsp_DP.txt";
    String d = "G:\\b\\notes\\java\\my_examples\\src\\main\\resources\\algorithms\\NPComplete\\travelingSalesman";

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

      line = br.readLine();
      int numCities = Integer.valueOf(line);
      retVal = new City_DP[numCities];

      System.err.println(" num cities=" + numCities);

      int i = 0;
      while ((line = br.readLine()) != null) {
        //System.err.println("read line '" + line + "'");
        String[] s = line.split("\\s+");
        double x = Double.valueOf(s[0]);
        double y = Double.valueOf(s[1]);
        City_DP c = new City_DP(i, x, y);
        retVal[i] = c;
        i++;
      }
      br.close();

      System.err.println(" read " + i + " cities.");
    } catch (IOException e) {
      System.err.println("exception " + e.getMessage());
    }
    return retVal;
  }

  private boolean isP() {
    return log.isDebugEnabled();
  }


  public static void main(String[] args) {
    TravelingSalesman_DP prog = new TravelingSalesman_DP();
    City[] c = prog.readDataFile(null);
    prog.setCity(c);
    //prog.findMinPathOverAllCities();
    prog.compute();
  }
}
