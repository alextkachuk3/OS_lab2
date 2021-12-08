import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

class SchedulingAlgorithm {

  static Results run(int quantum, int runtime, Vector<Process> processVec, Results result, int numberOfTickets, String resultsFile) {

    result.schedulingType = "Preemptive";
    result.schedulingName = "Lottery Scheduling";

    Vector<Process> processVector = (Vector<Process>) processVec.clone();


    int comptime = 0;
    int currentProcess;
    int size = processVector.size();
    int completed = 0;
    int blockedProcess = -1;

    try {

      PrintStream out = new PrintStream(new FileOutputStream(resultsFile));

      // run first lottery on start
      Lottery lottery = new Lottery(numberOfTickets);
      lottery.run(processVector);
      currentProcess = lottery.getWinner();

      Process process = processVector.elementAt(currentProcess);
      process.arrivaltime = comptime;
      out.println(comptime + ":     process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.arrivaltime + ")");

      while (comptime < runtime) {
        blockedProcess = -1;

        // if process has completed
        if (process.cpudone == process.cputime) {

          completed++;
          out.println(comptime + ":     process: " + currentProcess + " completed... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.arrivaltime + ")");

          // remove process and update number of tickets
          processVector.remove(process);
          lottery.updateNumber(lottery.getCountOfTickets() - process.numTickets);

          // if all processes are done
          if (completed == size) {
            result.computationTime = comptime;
            out.close();
            return result;
          }

          //choose next process in lottery
          int i;
          while (true) {
            lottery.run(processVector); // run lottery to find out which process should run next
            i = lottery.getWinner();
            process = processVector.elementAt(i);
            if (process.cpudone < process.cputime) {
              currentProcess = i;
              break;
            }
          }

          process = processVector.elementAt(currentProcess);
          if (process.cpudone == 0) process.arrivaltime = comptime;
          out.println(comptime + ":     process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.arrivaltime + ")");
        }

        if (process.ioblocking == process.ionext) {

          out.println(comptime + ":     process: " + currentProcess + " I/O blocked... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.arrivaltime + ")");

          blockedProcess = currentProcess;
          process.numblocked++;
          process.ionext = 0;
          process.lotnext = 0;

          // if process is not the only left, choose next in lottery
          if (processVector.size() > 1) {

            int i;
            while (true) {
              lottery.run(processVector);
              i = lottery.getWinner();
              process = processVector.elementAt(i);
              if (process.cpudone < process.cputime && i != blockedProcess) {
                currentProcess = i;
                break;
              }
            }

            process = processVector.elementAt(currentProcess);
            if (process.cpudone == 0) process.arrivaltime = comptime;
            out.println(comptime + ":     process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.arrivaltime + ")");
          }
        }

        if (process.ioblocking > 0) {
          process.ionext++;
        }

        // periodic lottery run
        if (process.lotnext == quantum){
          process.lotnext = 0;

          if (processVector.size() == 1) currentProcess = 0; // if only one process left, start it

          else {
            int i;
            while (true) {
              lottery.run(processVector);
              i = lottery.getWinner();
              process = processVector.elementAt(i);
              if (process.cpudone < process.cputime && i != blockedProcess) {
                currentProcess = i;
                break;
              }
            }
          }

          process = processVector.elementAt(currentProcess);
          if (process.cpudone == 0) process.arrivaltime = comptime;
          out.println(comptime + ":     process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.arrivaltime + ")");
        }
        process.cpudone++;
        process.lotnext++;
        comptime++;
      }

      out.close();
      System.out.println(processVector.size());

    } catch (IOException e) { /* Handle exceptions */ }
    result.computationTime = comptime;
    return result;
  }

}
