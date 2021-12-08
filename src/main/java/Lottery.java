import java.util.Random;
import java.util.Vector;

class Lottery {
    private int countOfTickets;
    private int winner;

    Lottery(int n) {
        countOfTickets = n;
    }

    int getWinner() {
        return winner;
    }

    int getCountOfTickets(){
        return countOfTickets;
    }

    void updateNumber(int newNumberOfTickets){
        countOfTickets = newNumberOfTickets;
    }

    void run(Vector<Process> jobs) {
        Random rand = new Random();
        int ticket = rand.nextInt(countOfTickets) + 1;
        int counter = 0;

        for (int i = 0; i < jobs.size(); i++){
            counter = counter + jobs.get(i).numTickets;
            if (counter >= ticket && jobs.get(i).cpudone < jobs.get(i).cputime){
                winner = i;
                break;
            }
        }
    }
}