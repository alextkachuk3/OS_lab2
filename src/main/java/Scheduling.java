import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.Vector;

public class Scheduling {

  private static int numberOfTickets = 0;
  private static int quantum = 0;
  private static int processNumber = 5;
  private static int meanDev = 1000;
  private static int standardDev = 100;
  private static int runtime = 1000;
  private static int countOfTickets = 0;

  private static Results result = new Results("null","null",0);
  private static String resultsFile;
  private static String logFile;

  private static final Vector<Process> processVector = new Vector<>();

  private static void Init(String file) {

    File f = new File(file);
    String line;

    int CPUTime;
    int IOBlocking;
    double X;


    try {
      DataInputStream in = new DataInputStream(new FileInputStream(f));

      while ((line = in.readLine()) != null) {

        if (line.startsWith("numprocess")) {
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken();
          processNumber = Common.s2i(st.nextToken());
        }
        if (line.startsWith("meandev")) {
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken();
          meanDev = Common.s2i(st.nextToken());
        }
        if (line.startsWith("standdev")) {
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken();
          standardDev = Common.s2i(st.nextToken());
        }
        if (line.startsWith("quantum")) {
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken();
          quantum = Common.s2i(st.nextToken());
        }
        if (line.startsWith("process")) {
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken();
          IOBlocking = Common.s2i(st.nextToken());

          X = Common.R1();
          while (X == -1.0) {
            X = Common.R1();
          }
          X = X * standardDev;
          CPUTime = (int) X + meanDev;

          String nTickets = st.nextToken();
          numberOfTickets = Common.s2i(nTickets);
          countOfTickets += numberOfTickets;

          processVector.addElement(new Process(CPUTime, IOBlocking, 0, 0, 0,0, numberOfTickets));
        }
        if (line.startsWith("runtime")) {
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken();
          runtime = Common.s2i(st.nextToken());
        }
        if (line.startsWith("summary_file")) {
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken();
          resultsFile = st.nextToken();
        }
        if (line.startsWith("log_file")) {
          StringTokenizer st = new StringTokenizer(line);
          st.nextToken();
          logFile = st.nextToken();
        }
      }

      in.close();

    } catch (IOException e) { /* Handle exceptions */ }
  }

  public static void main(String[] args) {
    int i;

    if (args.length != 1) {
      System.out.println("Usage: 'java Scheduling <INIT FILE>'");
      System.exit(-1);
    }

    File f = new File(args[0]);

    if (!(f.exists())) {
      System.out.println("Scheduling: error, file '" + f.getName() + "' does not exist.");
      System.exit(-1);
    }
    if (!(f.canRead())) {
      System.out.println("Scheduling: error, read of " + f.getName() + " failed.");
      System.exit(-1);
    }

    System.out.println("Working...");
    Init(args[0]);

    if (processVector.size() < processNumber) {
      i = 0;

      while (processVector.size() < processNumber) {
        double X = Common.R1();
        while (X == -1.0) {
          X = Common.R1();
        }
        X = X * standardDev;
        int cputime = (int) X + meanDev;

        processVector.addElement(new Process(cputime,i*100,0,0,0,0, numberOfTickets));
        i++;
      }
    }

    SchedulingAlgorithm.run(quantum, runtime, processVector, result, countOfTickets, logFile);

    try {

      PrintStream out = new PrintStream(new FileOutputStream(resultsFile));
      out.println("Scheduling Type: " + result.schedulingType);
      out.println("Scheduling Name: " + result.schedulingName);
      out.println("Simulation Run Time: " + result.computationTime);
      out.println("Mean: " + meanDev);
      out.println("Standard Deviation: " + standardDev);
      out.println("Quantum: " + quantum);
      out.println("Process #\t\tArrival Time\t\tCPU Time\t\tIO Blocking\t\tCPU Completed\t\tCPU Blocked");

      for (i = 0; i < processVector.size(); i++) {
        Process process = processVector.elementAt(i);

        out.print(i);
        if (i < 10) { out.print("\t\t\t\t"); }else if (i < 100) { out.print("\t\t\t"); } else { out.print("\t\t"); }
        out.print(process.arrivaltime);
        if (i < 10) { out.print(" (ms)\t\t\t"); }else if (i < 100) { out.print(" (ms)\t\t"); } else { out.print("\t"); }
        out.print(process.cputime);
        if (i < 10) { out.print(" (ms)\t\t\t\t"); }else if (i < 100) { out.print(" (ms)\t\t\t"); } else { out.print("\t\t"); }
        out.print(process.ioblocking);
        if (i < 10) { out.print(" (ms)\t\t\t\t"); }else if (i < 100) { out.print(" (ms)\t\t\t"); } else { out.print("\t\t"); }
        out.print(process.cpudone);
        if (i < 10) { out.print("\t\t\t\t"); }else if (i < 100) { out.print("\t\t\t"); } else { out.print("\t\t"); }
        out.println(process.numblocked + " times");
      }

      out.close();

    } catch (IOException e) {}

    System.out.println("Completed.");
  }
}

