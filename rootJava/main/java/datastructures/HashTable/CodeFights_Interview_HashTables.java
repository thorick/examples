package _codefights;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: thorick.chow@gmail.com
 * Date: 6/29/2018
 * Time: 2:17 PM
 */
public class CodeFights_Interview_HashTables {

  static String[][] groupingDishes(String[][] dishes) {
    MyHashTable table = new MyHashTable();

    for (int i = 0; i < dishes.length; i++) {
      String[] data = dishes[i];
      String dish = data[0];
      for (int k = 1; k < data.length; k++) {
        String ingredient = data[k];
        table.putSingleValue(ingredient, dish);
      }
    }


    // now read out and sort the dishes for each ingrediant

    MyHashTable.Entry[] entries = table.getAllEntries();   // sorted in key order
    List<String[]> out = new ArrayList<String[]>();
    for (MyHashTable.Entry entry : entries) {
      // only include ingredients that are present in more than one dish, for some odd reason
      if (entry.value.size() > 1) {
        List<String> dishList = new ArrayList<String>();
        String ingrediant = entry.key;
        //dishList.add(ingrediant);

        for (int j = 0; j < entry.value.size(); j++) {
          String dish = entry.value.get(j);
          dishList.add(dish);
        }

        Collections.sort(dishList);
        String[] singleGroup = new String[dishList.size() + 1];
        int index = 0;
        singleGroup[index++] = ingrediant;
        for (String dish : dishList) {
          singleGroup[index++] = dish;
        }
        out.add(singleGroup);
      }
    }
    return out.toArray(new String[0][0]);
  }


  static class MyHashTable {
    static int RADIX = 97;      // use a prime number
    Entry[] entries = new Entry[RADIX];


    List<String> get(String key) {
      int hash = hash(key);
      Entry entry = entries[hash];
      if (entry == null) {
        return null;
      }
      while ((entry != null) && !entry.key.equals(key)) {
        entry = entry.next;
      }
      if (entry == null) {
        return null;
      }
      return entry.value;    // can be null
    }

    // in key order  flattened array of all non-null entries
    Entry[] getAllEntries() {
      List<Entry> l = new ArrayList<Entry>();
      for (Entry e : entries) {
        if (e != null) {
          l.add(e);
          while (e.next != null) {
            e = e.next;
            l.add(e);
          }
        }
      }
      Collections.sort(l);
      Entry[] out = l.toArray(new Entry[0]);
      return out;
    }

    void putSingleValue(String key, String singleValue) {
      int hash = hash(key);
      Entry entry = entries[hash];
      if (entry == null) {
        entry = new Entry(key);
        entry.value.add(singleValue);
        entries[hash] = entry;
      } else {
        // find entry for our key
        Entry prevEntry = entry;
        while ((entry != null) && (!entry.key.equals(key))) {
          prevEntry = entry;
          entry = entry.next;
        }
        if (entry == null) {
          Entry newEntry = new Entry(key);
          prevEntry.next = newEntry;
          newEntry.value.add(singleValue);
          return;
        }
        for (int i = 0; i < entry.value.size(); i++) {
          if (entry.value.get(i).equals(singleValue)) {
            return;
          }
        }
        entry.value.add(singleValue);
      }
    }


    int hash(String key) {
      int sum = 0;
      char[] keyChars = key.toCharArray();
      for (char c : keyChars) {
        sum += c;
      }
      return sum % RADIX;
    }

    static class Entry implements Comparable<Entry> {
      Entry next;
      String key;
      List<String> value;

      Entry(String key) {
        this.key = key;
        value = new ArrayList<String>();
      }

      // sort by key value
      public int compareTo(Entry other) {
        return this.key.compareTo(other.key);
      }
    }
  }

  public static String printStringArray2D(String[][] a) {
     StringBuilder sb = new StringBuilder();
     sb.append("[");
     for (int i = 0; i < a.length; i++) {
       sb.append("[");
       String[] b = a[i];
       for (int j = 0; j<b.length; j++) {
         sb.append("'").append(b[j]).append("'");
         if (j >= (b.length-1)) {
           sb.append("]");
         }
         else {
           sb.append(", ");
         }
       }
       sb.append("]");
       if (i <a.length-1) {
         sb.append(",");
       }
       sb.append("\n");
     }
     return sb.toString();
   }



  public static void main(String[] args) {
    String test = "";
    String[][] input = null;
    String[][] result = null;
    String expected = "";

    test = "test1";
    input = new String[][]{{"Salad", "Tomato", "Cucumber", "Salad", "Sauce"},
        {"Pizza", "Tomato", "Sausage", "Sauce", "Dough"},
        {"Quesadilla", "Chicken", "Cheese", "Sauce"},
        {"Sandwich", "Salad", "Bread", "Tomato", "Cheese"}};
    expected = "[[\"Cheese\",\"Quesadilla\",\"Sandwich\"], \n" +
        " [\"Salad\",\"Salad\",\"Sandwich\"], \n" +
        " [\"Sauce\",\"Pizza\",\"Quesadilla\",\"Salad\"], \n" +
        " [\"Tomato\",\"Pizza\",\"Salad\",\"Sandwich\"]]";

    result = groupingDishes(input);
    System.err.print(test + "  result=" + printStringArray2D(result) + ",  expected=" + expected);
  }
}