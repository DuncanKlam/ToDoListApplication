package domain;

import java.time.LocalDate;
import java.time.Period;

public class Reminder {

    private LocalDate dateNow = LocalDate.now();
    private LocalDate dueDate;
    public String message;
    public String overdueMessage;
    private Period period;
    private String status;

    public Reminder(){
        this.dueDate=LocalDate.of(2000,1,1);
        this.message="";
        this.overdueMessage="";
        this.period = Period.ZERO;
        this.status="";
    }

    public Reminder(ToDoItem item){
        TimeStamp timeStamp = new TimeStamp(item.dueDate);
        this.dueDate = LocalDate.of(timeStamp.getYear(), timeStamp.getMonth(), timeStamp.getDay());
        this.message = "Your '" + item.about + "' is almost due!";
        this.overdueMessage = "Your '" + item.about + "' is past due!";
        this.period = Period.between(dateNow, dueDate);
        this.status = item.status;
    }

    public int timeLeft() {
        int timeLeft = daysLeft();
        timeLeft += monthsLeft()*30;
        timeLeft += yearsLeft()*365;
        return timeLeft;
    }

    public String getMessage(String message){
        String unitOfMeasure;
        int measurement;
        String pluralizer = "s";
        if(Math.abs(yearsLeft()) > 0){
            unitOfMeasure = "year";
            measurement = yearsLeft();
        }
        else if(Math.abs(monthsLeft()) > 0){
            unitOfMeasure = "month";
            measurement = monthsLeft();
        }
        else{
            unitOfMeasure = "day";
            measurement = daysLeft();
        }
        if (measurement != 1){
            unitOfMeasure += pluralizer;
        }
        String timeStatus = getTimeStatus(measurement);
        return String.format("%s\nYou have %d %s to complete this item\nThis item is currently %s and %s\n", message, measurement, unitOfMeasure,status,timeStatus);
    }

    public String getTimeStatus(int measurement) {
        if(measurement > 0){
            return "ON-TIME";
        }
        else{
            return "OVERDUE";
        }
    }

    public int daysLeft(){
        return period.getDays();
    }

    public int monthsLeft(){
        return  period.getMonths();
    }

    public int yearsLeft(){
        return period.getYears();
    }



}
