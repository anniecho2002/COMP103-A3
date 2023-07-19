/* Code for COMP103 - 2021T2, Assignment 3
 * Name: Annie Cho
 * Username: choanni
 * ID: 300575457
 */

import ecs100.*;
import java.util.*;
import java.io.*;

/**
 * Simple Simulation of a Hospital ER
 * 
 * The Emergency room has a waiting room and a treatment room that has a fixed
 *  set of beds for examining and treating patients.
 * 
 * When a patient arrives at the emergency room, they are immediately assessed by the
 *  triage team who determines the priority of the patient.
 *
 * They then wait in the waiting room until a bed becomes free, at which point
 * they go from the waiting room to the treatment room.
 *
 * When a patient has finished their treatment, they leave the treatment room and are discharged,
 *  at which point information about the patient is added to the statistics. 
 *
 *  READ THE ASSIGNMENT PAGE!
 */

public class HospitalERCore{

    // Fields for recording the patients waiting in the waiting room and being treated in the treatment room
    private Queue<Patient> waitingRoom = new ArrayDeque<Patient>();
    private static final int MAX_PATIENTS = 5;   // max number of patients currently being treated
    private Set<Patient> treatmentRoom = new HashSet<Patient>();

    // fields for the statistics
    private int totPatients = 0;
    private int totWaitTime = 0;
    private int pri1Patients = 0;
    private int pri1WaitTime = 0;
    
    // Fields for the simulation
    private boolean running = false;
    private int time = 0; // The simulated time - the current "tick"
    private int delay = 300;  // milliseconds of real time for each tick

    // fields controlling the probabilities.
    private int arrivalInterval = 5;   // new patient every 5 ticks, on average
    private double probPri1 = 0.1; // 10% priority 1 patients
    private double probPri2 = 0.2; // 20% priority 2 patients
    private Random random = new Random();  // The random number generator.

    /**
     * Construct a new HospitalERCore object, setting up the GUI, and resetting
     */
    public static void main(String[] arguments){
        HospitalERCore er = new HospitalERCore();
        er.setupGUI();
        er.reset(false);   // initialise with an ordinary queue.
    }        

    /**
     * Set up the GUI: buttons to control simulation and sliders for setting parameters
     */
    public void setupGUI(){
        UI.addButton("Reset (Queue)", () -> {this.reset(false); });
        UI.addButton("Reset (Pri Queue)", () -> {this.reset(true);});
        UI.addButton("Start", ()->{if (!running){ run(); }});   //don't start if already running!
        UI.addButton("Pause & Report", ()->{running=false;});
        UI.addSlider("Speed", 1, 400, (401-delay),
            (double val)-> {delay = (int)(401-val);});
        UI.addSlider("Av arrival interval", 1, 50, arrivalInterval,
            (double val)-> {arrivalInterval = (int)val;});
        UI.addSlider("Prob of Pri 1", 1, 100, probPri1*100,
            (double val)-> {probPri1 = val/100;});
        UI.addSlider("Prob of Pri 2", 1, 100, probPri2*100,
            (double val)-> {probPri2 = Math.min(val/100,1-probPri1);});
        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(1000,600);
        UI.setDivider(0.5);
    }

    /**
     * Reset the simulation:
     *  stop any running simulation,
     *  reset the waiting and treatment rooms
     *  reset the statistics.
     */
    public void reset(boolean usePriorityQueue){
        running=false;
        UI.sleep(2*delay);  // to make sure that any running simulation has stopped
        time = 0;           // set the "tick" to zero.

        // reset the waiting room, the treatment room, and the statistics.
        if (usePriorityQueue){
            waitingRoom = new PriorityQueue<Patient>();
        }
        else{
            waitingRoom = new ArrayDeque<Patient>();
        }
        Set<Patient> treatmentRoom = new HashSet<Patient>();

        UI.clearGraphics();
        UI.clearText();
    }

    /**
     * Main loop of the simulation
     */
    public void run(){
        if (running) { return; } // don't start simulation if already running one!
        running = true;
        while (running){         // each time step, check whether the simulation should pause.
            time++;
            List<Patient> removed = new ArrayList<Patient>();
            for (Patient p: treatmentRoom){
                if(p.completedCurrentTreatment()){
                    removed.add(p);
                    UI.println(time + ": Discharge: " + p);
                    totWaitTime = totWaitTime + p.getWaitingTime();
                    totPatients++;
                    if(p.getPriority() == 1){
                        pri1Patients++;
                        pri1WaitTime = pri1WaitTime + p.getWaitingTime();
                    }
                }
                else{p.advanceTreatmentByTick();}
            }
            for (Patient item: removed){
                treatmentRoom.remove(item);
            }
            for (Patient w: waitingRoom){
                w.waitForATick();
            }
            if(treatmentRoom.size() < MAX_PATIENTS && waitingRoom.size() > 0){
                treatmentRoom.add(waitingRoom.poll()); // remove
            }
            

            // Get any new patient that has arrived and add them to the waiting room
            if (time==1 || Math.random()<1.0/arrivalInterval){
                Patient newPatient = new Patient(time, randomPriority());
                UI.println(time+ ": Arrived: "+newPatient);
                waitingRoom.offer(newPatient); // add
            }
            redraw();
            UI.sleep(delay);
        }
        // paused, so report current statistics
        reportStatistics();
    }

    // Additional methods used by run() (You can define more of your own)

    /**
     * Report summary statistics about all the patients that have been discharged.
     * (Doesn't include information about the patients currently waiting or being treated)
     * The run method should have been recording various statistics during the simulation.
     */
    public void reportStatistics(){
        UI.println(); UI.println();
        UI.println("Total patients discharged: " + totPatients);
        if (totPatients > 0){
            UI.println("Average waiting time: " + totWaitTime/totPatients + " minutes");
        }
        UI.println();
        UI.println("Priority 1 patients discharged: " + pri1Patients);
        if(pri1Patients > 0){
            UI.println("Priority 1 patients average waiting time: " + pri1WaitTime/pri1Patients + " minutes");
        }
    }


    // HELPER METHODS FOR THE SIMULATION AND VISUALISATION
    /**
     * Redraws all the departments
     */
    public void redraw(){
        UI.clearGraphics();
        UI.setFontSize(14);
        UI.drawString("Treating Patients", 5, 15);
        UI.drawString("Waiting Queues", 200, 15);
        UI.drawLine(0,32,400, 32);

        // Draw the treatment room and the waiting room:
        double y = 80;
        UI.setFontSize(14);
        UI.drawString("ER", 0, y-35);
        double x = 10;
        UI.drawRect(x-5, y-30, MAX_PATIENTS*10, 30);  // box to show max number of patients
        for(Patient p : treatmentRoom){
            p.redraw(x, y);
            x += 10;
        }
        x = 200;
        for(Patient p : waitingRoom){
            p.redraw(x, y);
            x += 10;
        }
        UI.drawLine(0,y+2,400, y+2);
    }

    /** 
     * Returns a random priority 1 - 3
     * Probability of a priority 1 patient should be probPri1
     * Probability of a priority 2 patient should be probPri2
     * Probability of a priority 3 patient should be (1-probPri1-probPri2)
     */
    private int randomPriority(){
        double rnd = random.nextDouble();
        if (rnd < probPri1) {return 1;}
        if (rnd < (probPri1 + probPri2) ) {return 2;}
        return 3;
    }
}
