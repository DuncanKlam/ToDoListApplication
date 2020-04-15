package domain;

import utils.CloudUtils;
import utils.DatabaseUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class User {

    public String name;
    public List<ToDoItem> toDoList = new LinkedList<>();
    public CloudUtils cloudUtils;
    public DatabaseUtils databaseUtils;

    public User(String name) throws SQLException {
        this.name = name;
        this.cloudUtils = new CloudUtils();
        this.databaseUtils = new DatabaseUtils();

    }

    public void makeToDoItem(String about, String owner, String dueDate){
        toDoList.add(new ToDoItem(about, owner, new TimeStamp(dueDate)));
    }

    public void makeToDoItem(ToDoItem item){
        toDoList.add(item);
    }

    public ToDoItem getToDoItem(int itemID){ return toDoList.get(itemID); }

    public List<ToDoItem> getToDoItemList(){return toDoList;}

    public String deleteToDoItem(String identifier){
        for (ToDoItem item : toDoList){
            if (item.id == Integer.parseInt(identifier)){
                toDoList.remove(item);
                return "Local Delete: Success";
            }
        }
        return "Not in local storage";
    }

    public void removeToDoItem(int itemID){ toDoList.remove(itemID); }

    public String syncItemsToCloud(){
        try {
            return cloudUtils.uploadListToCloud(toDoList);
        } catch (IOException e){
            e.printStackTrace();
            return "Failure";
        }
    }


}
