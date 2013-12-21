package algorithms.NPComplete.travelingSalesman;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 10/6/13
 * Time: 4:18 PM
 */
public interface City {

  public int getId();

  public double distance(City other);
}
