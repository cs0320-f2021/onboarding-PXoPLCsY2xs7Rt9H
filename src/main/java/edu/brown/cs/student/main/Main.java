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

/**
 * The Main class of our project. This is where execution begins.
 */
public final class Main {

  // use port 4567 by default when running server
  private static final int DEFAULT_PORT = 4567;

  private List<String> starIds = new ArrayList<String>();
  private List<String> starNames = new ArrayList<String>();
  private List<String> starLocations = new ArrayList<String>();

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

    //FIX WHEN NAME HAS 2 SPACES
    //FIX FOR INPUT BY NAME AND BY POSITION IF THE INPUT IS PART OF THE STAR DATA IN ORDER TO FILTER ON FINAL RESULT
    // TODO: Add your REPL here!
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
            System.out.println(loadStars(arguments[1]));
          } else if (arguments[0].equals("naive_neighbors")) {
            if (name.length > 1) {
              for (String item: name) {
                System.out.println(item);
              }
              System.out.println("Finding neighbors from " + name[1]);
              System.out.println(naiveNeighbors(
                  Integer.parseInt(arguments[1]), name[1]));
            } else if (arguments.length == 5) {

              System.out.println("Finding neighbors from position "
                  + Double.parseDouble(arguments[2]) + " "
                  + Double.parseDouble(arguments[3]) + " "
                  + Double.parseDouble(arguments[4]));
              System.out.println(naiveNeighbors(
                  Integer.parseInt(arguments[1]), Double.parseDouble(arguments[2]),
                  Double.parseDouble(arguments[3]), Double.parseDouble(arguments[4])));
            }
          } else if (arguments[0].equals("star_data")) {
            for (int i = 0; i < starLocations.size(); i++) {
              System.out.println(starIds.get(i) + " " + starNames.get(i) + " "
                  + starLocations.get(i));
            }
          } else {
            throw new Exception();
          }
        } catch (Exception e) {
          // e.printStackTrace();
          System.out.println("ERROR: We couldn't process your input");
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

  private String loadStars(String fileName) {
    starIds.clear();
    starNames.clear();
    starLocations.clear();
    int count = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (count != 0) {
          String[] values = line.split(",");
          starIds.add(values[0]);
          starNames.add(values[1]);
          starLocations.add(values[2] + " " + values[3] + " " + values[4]);
        }
        count++;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "Read " + (count - 1) + " stars from " + fileName;
  }


  private String naiveNeighbors(int kValue, double xValue, double yValue, double zValue) {
    List<String> neighbors = new ArrayList<>();
    List<Double> smallestDistance = new ArrayList<>();
    List<Double> distances = new ArrayList<>();
    StringBuilder neighborList = new StringBuilder();
    int realKValue = Math.min(kValue, starIds.size());
    int numStars = starIds.size();


    for (String loc: starLocations) {
      String[] values = loc.split(" ");

      distances.add(Math.sqrt(
          Math.pow((Double.parseDouble(values[0]) - xValue), 2)
              + Math.pow((Double.parseDouble(values[1]) - yValue), 2)
              + Math.pow((Double.parseDouble(values[2]) - zValue), 2)
      ));
    }

    for (int i = 0; i < realKValue; i++) {
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
  }

  private String naiveNeighbors(int kValue, String name) throws Exception {

    List<String> neighbors = new ArrayList<>();
    List<Double> smallestDistance = new ArrayList<>();
    List<Double> distances = new ArrayList<>();
    StringBuilder neighborList = new StringBuilder();
    int realKValue = Math.min(kValue, starIds.size());
    int numStars = starIds.size();

    double xValue = 0.0;
    double yValue = 0.0;
    double zValue = 0.0;
    boolean foundStar = false;

    for (int i = 0; i < starNames.size(); i++) {
      if (starNames.get(i).equals(name)) {
        String[] values = starLocations.get(i).split(" ");
        xValue = Double.parseDouble(values[0]);
        yValue = Double.parseDouble(values[1]);

        zValue = Double.parseDouble(values[2]);
        foundStar = true;
      }
    }

    System.out.println(foundStar);
    if (!foundStar) {
      throw new Exception("Given Star Not Found");
    }

    for (String loc: starLocations) {
      String[] values = loc.split(" ");

      distances.add(Math.sqrt(
          Math.pow((Double.parseDouble(values[0]) - xValue), 2)
              + Math.pow((Double.parseDouble(values[1]) - yValue), 2)
              + Math.pow((Double.parseDouble(values[2]) - zValue), 2)
      ));
    }
    for (Double distance: distances) {
      System.out.println(distance);
    }

    for (int i = 0; i < realKValue; i++) {
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
      for (int z = 0; z < smallestDistance.size(); z++) {
        System.out.println("At position " + z + " " + smalls);
      }
    }

    System.out.println("Size of neighbors list is " + neighbors.size()
        + " and realKValue is " + realKValue);
    for (int i = 0; i < realKValue; i++) {
      neighborList.append(neighbors.get(i)).append("\n");
    }
    return neighborList.toString();

  }

}
