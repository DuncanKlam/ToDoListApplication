package domain;

import java.time.*;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName = "todoitem")
public class ToDoItem {


    @DatabaseField(id = true)
    public String about;

    @DatabaseField()
    public String owner;

    @DatabaseField()
    public String dueDate;

    @DatabaseField()
    public String createdDate;

    @DatabaseField()
    public String itemCategory;

    @DatabaseField()
    public String status;

    @DatabaseField()
    public int id;

    public String stringID = null;

    private LocalDate dateNow = LocalDate.now();
    private int dayNow = dateNow.getDayOfMonth();
    private int monthNow = dateNow.getMonthValue();
    private int yearNow = dateNow.getYear();


    public ToDoItem(){
    }

    public ToDoItem(String about, String owner, TimeStamp dueDate, TimeStamp createdDate, String status, String category, int id, String stringID){
        this.about = about;
        this.owner = owner;
        this.dueDate = dueDate.toString();
        this.createdDate = createdDate.toString();
        this.status = status;
        this.itemCategory = category;
        this.id = id;
        this.stringID = stringID;
    }

    public ToDoItem(String about, String owner, TimeStamp dueDate){
        this.about = about;
        this.owner = owner;
        this.dueDate = dueDate.toString();
        this.createdDate = new TimeStamp(yearNow,monthNow,dayNow).toString();
        this.status = "Snoozed";
        this.itemCategory = "Unsorted";
        this.id = 0;
    }


    public String getStatus(){return status; }

    @Override
    public String toString() {
        return "ToDoItem{" +
                "Id='" + id + '\'' +
                "About='" + about + '\'' +
                ", Owner='" + owner + '\'' +
                ", Due Date='" + dueDate + '\'' +
                ", Status='" + status + '\'' +
                ", Category='" + itemCategory + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o){
        if(o == null)                return false;
        if(!(o instanceof ToDoItem)) return false;

        ToDoItem other = (ToDoItem) o;
        if(! (this.id ==other.id))      return false;

        return true;
    }

    public String formatDueDateNicely() {
        TimeStamp timeStamp = new TimeStamp(this.dueDate);
        return String.format("%s %-5s, %d",findMonthFromNumber(timeStamp.getMonth()),
                                             timeStamp.getDay()+getSuffix(timeStamp.getDay()),
                                             timeStamp.getYear());
    }

    private String findMonthFromNumber(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        if(month > 12 || month <= 0){
            return "Unkown";
        }
        return months[month-1];
    }

    private String getSuffix(int number){
        String[] suffixes = {"th","st","nd","rd","th","th","th","th","th","th"};
        if(number < 10){
            return suffixes[number];
        }
        else if(number < 20){
            return "th";
        }
        else if(number < 30){
            number = number - 20;
            return suffixes[number];
        }
        else if(number < 40){
            number = number - 30;
            return  suffixes[number];
        }
        else{
            return "gh";
        }
    }
}
