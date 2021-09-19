package edu.brown.cs.student.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import freemarker.template.Configuration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.checkerframework.checker.units.qual.A;
import spark.ExceptionHandler;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.template.freemarker.FreeMarkerEngine;

import javax.naming.NameNotFoundException;

/**
 * The Main class of our project. This is where execution begins.
 */
public final class Main {

  // use port 4567 by default when running server
  private static final int DEFAULT_PORT = 4567;

  private List<Integer> starIds = new ArrayList<Integer>();
  private List<String> starNames = new ArrayList<String>();
  private List<Double> starXValues = new ArrayList<Double>();
  private List<Double> starYValues = new ArrayList<Double>();
  private List<Double> starZValues = new ArrayList<Double>();

  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   */
  public static void main(String[] args) {
    new Main(args).run();
  }

  private String[] args;

  private Main(String[] args) {
    this.args = args;
  }

  private void run() {
    // set up parsing of command line flags
    OptionParser parser = new OptionParser();

    // "./run --gui" will start a web server
    parser.accepts("gui");

    // use "--port <n>" to specify what port on which the server runs
    parser.accepts("port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_PORT);

    OptionSet options = parser.parse(args);
    if (options.has("gui")) {
      runSparkServer((int) options.valueOf("port"));
    }

    // TODO: Add your REPL here!
    String methodName = "";
    try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
      String input;
      while ((input = br.readLine()) != null) {
        try {
          input = input.trim();
          String[] arguments = input.split(" ");
          String[] name = input.split("\"");

          // TODO: complete your REPL by adding commands for addition "add" and subtraction
          //  "subtract"
          if (arguments[0].equals("add")) {
            System.out.println(new MathBot().add(Double.parseDouble(arguments[1]),
                Double.parseDouble(arguments[2])));
          } else if (arguments[0].equals("subtract")) {
            System.out.println(new MathBot().subtract(Double.parseDouble(arguments[1]),
                Double.parseDouble(arguments[2])));
          } else if (arguments[0].equals("stars")) {
            methodName = "stars";
            loadStars(arguments[1]));
          } else if (arguments[0].equals("naive_neighbors")) {
            methodName = "naive_neighbors";
            if (name.length > 1) {
              /*
              for (String item: name) {
                System.out.println(item);
              }
              System.out.println("Finding neighbors from " + name[1]);
               */
              naiveNeighbors(Integer.parseInt(arguments[1]), name[1]);
            } else if (arguments.length == 5) {
              /*
              System.out.println("Finding neighbors from position "
                  + Double.parseDouble(arguments[2]) + " "
                  + Double.parseDouble(arguments[3]) + " "
                  + Double.parseDouble(arguments[4]));
               */
              naiveNeighbors(
                  Integer.parseInt(arguments[1]), Double.parseDouble(arguments[2]),
                  Double.parseDouble(arguments[3]), Double.parseDouble(arguments[4]));
            }
          } else if (arguments[0].equals("star_data")) {
            for (int i = 0; i < starIds.size(); i++) {
              System.out.println(starIds.get(i) + " " + starNames.get(i) + " "
                  + starXValues.get(i) + " " + starYValues.get(i) + " " + starZValues.get(i));
            }
          } else {
            throw new IllegalArgumentException();
          }
        } catch (NameNotFoundException e) {
          // e.printStackTrace();
          System.out.println("ERROR: Invalid star name");
        } catch (IllegalArgumentException e) {
          System.out.println("ERROR: Invalid input for REPL");
        } catch (ArrayIndexOutOfBoundsException e) {
          if (methodName.equals("stars")) {
            System.out.println("ERROR: stars method is missing file name");
          } else if (methodName.equals("naive_neighbors")) {
            System.out.println("ERROR: naive_neighbors is missing"
                + "either full star position or name");
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("ERROR: Invalid input for REPL");
    }

  }

  private static FreeMarkerEngine createEngine() {
    Configuration config = new Configuration(Configuration.VERSION_2_3_0);

    // this is the directory where FreeMarker templates are placed
    File templates = new File("src/main/resources/spark/template/freemarker");
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out.printf("ERROR: Unable use %s for template loading.%n",
          templates);
      System.exit(1);
    }
    return new FreeMarkerEngine(config);
  }

  private void runSparkServer(int port) {
    // set port to run the server on
    Spark.port(port);

    // specify location of static resources (HTML, CSS, JS, images, etc.)
    Spark.externalStaticFileLocation("src/main/resources/static");

    // when there's a server error, use ExceptionPrinter to display error on GUI
    Spark.exception(Exception.class, new ExceptionPrinter());

    // initialize FreeMarker template engine (converts .ftl templates to HTML)
    FreeMarkerEngine freeMarker = createEngine();

    // setup Spark Routes
    Spark.get("/", new MainHandler(), freeMarker);
  }

  /**
   * Display an error page when an exception occurs in the server.
   */
  private static class ExceptionPrinter implements ExceptionHandler<Exception> {
    @Override
    public void handle(Exception e, Request req, Response res) {
      // status 500 generally means there was an internal server error
      res.status(500);

      // write stack trace to GUI
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }
  }

  /**
   * A handler to serve the site's main page.
   *
   * @return ModelAndView to render.
   * (main.ftl).
   */
  private static class MainHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      // this is a map of variables that are used in the FreeMarker template
      Map<String, Object> variables = ImmutableMap.of("title",
          "Go go GUI");

      return new ModelAndView(variables, "main.ftl");
    }
  }

  private void loadStars(String fileName) {
    starIds.clear();
    starNames.clear();
    starXValues.clear();
    starYValues.clear();
    starZValues.clear();
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] values = line.split(",");
        if (count > 0) {
          starIds.add(Integer.parseInt(values[0]));
          starNames.add(values[1]);
          starXValues.add(Double.parseDouble(values[2]));
          starYValues.add(Double.parseDouble(values[3]));
          starZValues.add(Double.parseDouble(values[4]));
        } else if (count == 0) {
          if (!(values[0].equals("StarID") && values[1].equals("ProperName")
              && values[2].equals("X") && values[3].equals("Y") && values[4].equals("Z"))) {
            throw new Exception();
          }
        }
        count++;
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("ERROR: Empty CSV file");
    } catch (NumberFormatException e) {
      System.out.println("ERROR: At line " + count + " invalid field type");
    } catch (Exception e) {
      System.out.println("ERROR: Invalid CSV header");
    }
    System.out.println("Read " + (count - 1) + " stars from " + fileName);
  }


  private void naiveNeighbors(int kValue, double xValue, double yValue, double zValue) {

    int realKValue = Math.min(kValue, starIds.size());

    List<Double> distances = calculateDistances(xValue, yValue, zValue);

    sortDistances(distances, realKValue, true, "");
  }

  private void naiveNeighbors(int kValue, String name) throws Exception {

    int realKValue = Math.min(kValue, starIds.size() - 1);

    double xValue = 0.0;
    double yValue = 0.0;
    double zValue = 0.0;
    boolean foundStar = false;

    for (int i = 0; i < starNames.size(); i++) {
      if (starNames.get(i).equals(name)) {
        xValue = starXValues.get(i);
        yValue = starYValues.get(i);

        zValue = starZValues.get(i);
        foundStar = true;
      }
    }
    if (!foundStar) {
      throw new NameNotFoundException();
    }

    List<Double> distances = calculateDistances(xValue, yValue, zValue);
    sortDistances(distances, realKValue, false, name);

  }

  private List<Double> calculateDistances(double fromX, double fromY, double fromZ) {
    List<Double> distances = new ArrayList<>();
    for (int i = 0; i < starIds.size(); i++) {
      distances.add(Math.sqrt(
          Math.pow(fromX - starXValues.get(i), 2)
              + Math.pow(fromY - starYValues.get(i), 2)
              + Math.pow(fromZ - starZValues.get(i), 2)
      ));
    }
    return distances;
  }

  private void sortDistances(List<Double> distances, int numNeighbors, boolean includeName,
                               String name) {

    List<Integer> neighbors = new ArrayList<>();
    List<Double> smallestDistance = new ArrayList<>();
    StringBuilder neighborList = new StringBuilder();
    for (int i = 0; i < numNeighbors; i++) {
      smallestDistance.add((double) Integer.MAX_VALUE);
    }
    for (int i = 0; i < starNames.size(); i++) {
      double distanceFrom = distances.get(i);
      /*
      System.out.println("Star Id: " + starIds.get(i));
      System.out.println("Star Distance: " + distanceFrom);
      System.out.println("Star Name: " + starNames.get(i));
       */
      if (starNames.get(i).equals(name) && !includeName) {
        continue;
      } else {
        for (int j = 0; j < numNeighbors; j++) {
          if (distanceFrom < smallestDistance.get(j)) {
            smallestDistance.add(j, distanceFrom);
            neighbors.add(j, starIds.get(i));
            break;
          } else if (distanceFrom == (smallestDistance.get(j)) && j == numNeighbors - 1) {
            int random = (int) Math.floor(Math.random() * 2);
            if (random == 1) {
              smallestDistance.add(j, distanceFrom);
              neighbors.add(j, starIds.get(i));
              break;
            }
          }

        }


      }
    }
    if (neighbors.isEmpty() || numNeighbors == 0) {
      System.out.print("");
    }
    for (int i = 0; i < numNeighbors; i++) {
      System.out.println("k value: " + numNeighbors);
      System.out.println(neighbors.get(i));
    }

  }


  /*for (int i = 0; i < realKValue; i++) {
      smallestDistance.add((double) Integer.MAX_VALUE);
    }

    for (int i = 0; i < starLocations.size(); i++) {
      double distanceFrom = distances.get(i);
      System.out.println("Star Id: " + starIds.get(i));
      System.out.println("Star Distance: " + distanceFrom);
      System.out.println("Star Name: " + starNames.get(i));
      if (!starNames.get(i).equals(name)) {
        for (int j = 0; j < realKValue; j++) {

          if (distanceFrom < smallestDistance.get(j)) {
            smallestDistance.add(j, distanceFrom);
            neighbors.add(j, starIds.get(i));
            break;
          } else if (distanceFrom == (smallestDistance.get(j)) && j == realKValue - 1) {
            int random = (int) Math.floor(Math.random() * 2);
            if (random == 1) {
              smallestDistance.add(j, distanceFrom);
              neighbors.add(j, starIds.get(i));
              break;
            }
          }

        }
      }
    }

    //System.out.println("Size of neighbors list is " + neighbors.size() + " and realKValue is " + realKValue);
    for (int i = 0; i < realKValue; i++) {
      neighborList.append(neighbors.get(i)).append("\n");
    }
    */

  /*for (int i = 0; i < realKValue; i++) {
      smallestDistance.add((double) Integer.MAX_VALUE);
    }

    for (int i = 0; i < starLocations.size(); i++) {
      double distanceFrom = distances.get(i);
      for (int j = 0; j < realKValue; j++) {
        if (distanceFrom < smallestDistance.get(j)) {
          smallestDistance.add(j, distanceFrom);
          neighbors.add(j, starIds.get(i));
          break;
        }
        if (distanceFrom == (smallestDistance.get(i)) && j == kValue - 1) {
          int random = (int) Math.floor(Math.random() * 2);
          if (random == 1) {
            smallestDistance.add(j, distanceFrom);
            neighbors.add(j, starIds.get(i));
            break;
          }
        }
      }

    }

    for (int i = 0; i < realKValue; i++) {
      neighborList.append(neighbors.get(i)).append("\n");
    }
    return neighborList.toString();
   */

}
