/* Code for COMP103 - 2021T2, Assignment 3
 * Name: Annie Cho
 * Username: choanni
 * ID: 300575457
 */

import ecs100.*;
import java.util.*;

/**
 * A treatment Department (Surgery, X-ray room,  ER, Ultrasound, etc)
 * Each department will need
 * - A name,
 * - A maximum number of patients that can be treated at the same time
 * - A Set of Patients that are currently being treated
 * - A Queue of Patients waiting to be treated.
 *    (ordinary queue, or priority queue, depending on argument to constructor)
 */

public class Department{

    private String name;
    private int maxPatients;
    private Queue<Patient> waitingRoom = new ArrayDeque<Patient>();
    private Set<Patient> treatmentRoom = new HashSet<Patient>();
    private boolean priority;
    
    private int waitTime = 0;
    private int totalPatients = 0;

    /**
     * Construct a new Department object
     */
    public Department(String name, int max, boolean priority){
        this.name = name;
        this.maxPatients = max;
        this.priority = priority;
        treatmentRoom = new HashSet(maxPatients);
        if (this.priority){waitingRoom = new PriorityQueue<Patient>();}
        else{waitingRoom = new ArrayDeque<Patient>();}
    } 
    
    /**
     * Adds a patient into the department
     */
    public void addPatient(Patient p){
        if(treatmentRoom.size() < maxPatients){
            treatmentRoom.add(p); // if there's any room in the treatment
        }
        else{
            waitingRoom.offer(p); // otherwise they go to the waiting room
        }
    }
    
    /**
     * Moves next waiting patient in waiting room to treatment if there is space
     */
    public void movePatient(){
        while(treatmentRoom.size() < maxPatients && waitingRoom.size() > 0){
            treatmentRoom.add(waitingRoom.poll());
        }
    }
    
    /**
     * The number of patients that have come through the department
     */
    public void increaseTotalPatients(){
        totalPatients++;
    }
    
    /**
     * Collective minutes that patients have needed to wait for the department
     */
    public void increaseWaitTime(){
        waitTime++;
    }
    
    /**
     * Returns wait time
     */
    public int getWaitTime(){
        return waitTime;
    }
    
    /**
     * Returns wait time
     */
    public int getTotalPatients(){
        return totalPatients;
    }
    
    /**
     * Returns the waiting room
     */
    public Queue getWaitingRoom(){
        return waitingRoom;
    }
    
    /**
     * Returns the number of Pri 1 patients in the waiting room
     */
    public int getPri1Wait(){
        int count = 0;
        for(Patient p: waitingRoom){
            if (p.getPriority() == 1){
                count++;
            }
        }
        return count;
    }
    
    /**
     * Returns the treatment room
     */
    public Set getTreatmentRoom(){
        return treatmentRoom;
    }
    
    /**
     * Returns the max patients
     */
    public int getMaxPatients(){
        return maxPatients;
    }
    
    public String getName(){
        return name;
    }
    
    /**
     * Updates the waiting and treatment rooms
     */
    public void updateRooms(Set treatment, Queue waiting){
        treatmentRoom = treatment;
        waitingRoom = waiting;
    }
    
    
    /**
     * Draw the department: the patients being treated and the patients waiting
     * You may need to change the names if your fields had different names
     */
    public void redraw(double y){
        UI.setFontSize(14);
        UI.drawString(name, 0, y-35);
        double x = 10;
        UI.drawRect(x-5, y-30, maxPatients*10, 30);  // box to show max number of patients
        for(Patient p : treatmentRoom){
            p.redraw(x, y);
            x += 10;
        }
        x = 200;
        for(Patient p : waitingRoom){
            p.redraw(x, y);
            x += 10;
        }
    }
}
