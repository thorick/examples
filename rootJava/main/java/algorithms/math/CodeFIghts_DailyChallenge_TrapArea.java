package _codefights;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 7/17/2018
 * Time: 11:52 AM
 */

/**
 * https://app.codesignal.com/challenge/sGDJsXcFYhkoejcrq
 * <p>
 * For a regular polygon with n sides and perimeter p, we can find a new similar polygon by extending the radius from the center to each vertex. Given the length of the new radius r, we'd like to find the difference in area between the two shapes.
 * <p>
 * To avoid precision issues, we'll round the final answer to the nearest integer.
 * <p>
 * Note: in the case that the given radius is smaller than that of the original polygon, the difference in area should be negative.
 * <p>
 * Example
 * <p>
 * For n = 5, p = 40, and r = 16, the output should be trapArea(n, p, r) = 499.
 * <p>
 * The difference between the two areas is about 498.565617, which is rounded to 499.
 * <p>
 * given  N  sides
 * p  length of perimeter of original
 * r  radius of new
 * <p>
 * triangle base is p/N    find R radius of original
 * know:  angle, base  2 R  the 2 sides are equal
 * side angles are  pi - ((2pi/N) / 2)
 * <p>
 * orig R  is
 * (p/N / 2)  /  cosine( pi - ((2pi/N) / 2) )
 * <p>
 * <p>
 * <p>
 * <p>
 * N sided polygon  angle = 2 * pi / N
 * <p>
 * triangle area:
 * a b sin (c) / 2
 * <p>
 * area diff
 * AD = r**2 sin(c)/2  -  R**2 sin(c)/2
 * <p>
 * total diff =
 * N * AD
 */
public class CodeFIghts_DailyChallenge_TrapArea {

  static int trapArea1(int n, double p, double r) {
    // find the original value of R  the radius of polygon vertex.
    // use the right triangle formed by the original radius R
    // 1/2 of the base of the triangle:  p/n * 1/2
    //  the angle between R and (p/n * 1/2) which will be:  theta =  pi - ( 1/2 ( 2pi/n ) )
    //
    //   R  =  (p/2n)  /   cosine ( pi - ( 1/2 ( 2pi/n ) ) )
    //
    // The area of the isoceles triangle formed by the polygon sector will then be:
    //
    //    AT = (R**2  sin ( 2pi/N )) *  1/2
    //
    // This makes the solid area of the polygon to be:
    //
    //     N * AT
    //
    // The difference in area between the R and r polygons is then:
    //
    //     N * 1/2  * sin ( 2pi/N ) * (R**2 -  r**2)
    //

    double pDiv2n = p / (2 * n);
    double pi2Divn = Math.PI * 2 / n;
    double angleTheta = Math.PI - (pi2Divn / 2);

    double originalR = pDiv2n / Math.cos(angleTheta);

    double originalArea = n * originalR * originalR * Math.sin(pi2Divn) / 2;
    double newArea = n * r * r * Math.sin(pi2Divn) / 2;

    double diffArea = newArea - originalArea;
    int intDiffArea = (int) Math.round(diffArea);
    return intDiffArea;


  }

  /**
   * http://www.web-formulas.com/Math_Formulas/Geometry_Area_of_Polygon.aspx
   * <p>
   * Area   S**2 *  N  /   4 tan(PI/N)
   * <p>
   * Area   1/2 ( R**2 N sin( 2 PI / N ) )
   *
   * @param n
   * @param p
   * @param r
   * @return
   */
  static int trapArea(int n, double p, double r) {
    double S = p / n;
    double areaOrig = (S * S * n) / (4 * (Math.tan(Math.PI / n)));
    double areaNew = (r * r * n * Math.sin(2 * Math.PI / n)) / 2;
    double areaDiff = areaNew - areaOrig;
    int areaDiffInt = (int) Math.round(areaDiff);
    return areaDiffInt;
  }

  public static void main(String[] args) {
    String test = "";
    int n = 0;
    int p = 0;
    int r = 0;
    int expected = 0;
    int result = 0;

    test = "test1";
    n = 5;
    p = 40;
    r = 16;
    expected = 499;
    result = trapArea(n, p, r);
    System.err.println(test + "  result=" + result + ", expected=" + expected);

    test = "test2";
    n = 6;
    p = 12;
    r = 10;
    expected = 249;
    result = trapArea(n, p, r);
    System.err.println(test + "  result=" + result + ", expected=" + expected);


  }
}
