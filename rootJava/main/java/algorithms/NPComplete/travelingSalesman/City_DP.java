package algorithms.NPComplete.travelingSalesman;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 10/6/13
 * Time: 4:20 PM
 */
public class City_DP implements City {

    public int id;
    public double x;
    public double y;

    public City_DP(int id, double x, double y) {
      this.id = id;
      this.x = x;
      this.y = y;
    }

    public double distance(City o) {
      City_DP other = (City_DP) o;
      return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
    }

    public int getId() {
      return id;
    }

  public String toString() {
    return "id="+id+", ("+x+", "+y+")";
  }

}
