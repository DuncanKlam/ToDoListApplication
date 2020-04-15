import domain.TimeStamp;
import domain.ToDoItem;
import domain.User;
import exceptions.ParameterIsNotJsonStringException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;


public class Main {

    public static void main(String[] args){
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("What is your name: ");
            String name = br.readLine();
            User user = new User(name);
            while (true) {
                System.out.println("\nWhat would you like to access "+ name +"?\n[L]ocal utils?\n[C]loud utils?\n[D]atabase utils?\n[Q]uit utils?");
                String action = br.readLine().toLowerCase();

                //Access Local utilities
                if (action.contentEquals("l")) {
                    System.out.println(name + "'s Local Utilities\n[M]ake a To Do item\n[V]iew To Do item list\n[E]dit a To Do item\n[D]elete a To Do item");
                    String action2 = br.readLine().toLowerCase();
                    if (action2.contentEquals("m")) { // make to-do item
                        System.out.println("What is the memo? ");
                        String memo = br.readLine();
                        System.out.println("What is the Due date year? ex: 2017");
                        int dueDateYear = Integer.parseInt(br.readLine());
                        System.out.println("What is the Due date month? ex: 05");
                        int dueDateMonth = Integer.parseInt(br.readLine());
                        System.out.println("What is the Due date day? ex: 14");
                        int dueDateDay = Integer.parseInt(br.readLine());
                        user.toDoList.add(new ToDoItem(memo, user.name, new TimeStamp(dueDateYear, dueDateMonth, dueDateDay)));
                    } else if (action2.contentEquals("v")) { // View to-do item list
                        List<ToDoItem> list = user.getToDoItemList();
                        int i = 1;
                        for (ToDoItem item : list) {
                            System.out.printf("%d. %s\n", i++, item.toString());
                        }
                    } else if (action2.contentEquals("e")) { // Edit to-do item
                        List<ToDoItem> list = user.getToDoItemList();
                        int i = 0;
                        for (ToDoItem item : list) {
                            System.out.printf("ID %d %s\n", i++, item.toString());
                        }
                        System.out.println("Input ID number of item you wish to change: ");
                        ToDoItem tempItem = user.getToDoItem(Integer.parseInt(br.readLine()));
                        System.out.println("What would you like to change?\n[A]bout the item\n[D]ue date of item\n[C]ategory of item\n[S]tatus of item");
                        String action3 = br.readLine().toLowerCase();
                        if (action3.contentEquals("a")) { //Change about of to-do item
                            System.out.println("Current about: " + tempItem.about);
                            System.out.println("What is the new About?\n");
                            tempItem.changeAbout(br.readLine());
                        } else if (action3.contentEquals("d")) { //Change date of to-do item
                            System.out.println("Current due date: " + tempItem.dueDate);
                            System.out.println("What is the Due date year? ex: 2017");
                            int dueDateYear = Integer.parseInt(br.readLine());
                            System.out.println("What is the Due date month? ex: 05");
                            int dueDateMonth = Integer.parseInt(br.readLine());
                            System.out.println("What is the Due date day? ex: 14");
                            int dueDateDay = Integer.parseInt(br.readLine());
                            tempItem.changeDueDate(new TimeStamp(dueDateYear, dueDateMonth, dueDateDay));
                        } else if (action3.contentEquals("c")) { //Change category
                            System.out.println("Current category: " + tempItem.itemCategory);
                            System.out.println("What is the new category? ");
                            tempItem.changeCategory(br.readLine());
                        } else if (action3.contentEquals("s")) { //change status
                            System.out.println("Current status: " + tempItem.getStatus());
                            System.out.println("Change status to:\n[C]omplete\n[S]noozed\n[I]n progress");
                            String choice = br.readLine().toLowerCase();
                            if (choice.contentEquals("c")) {
                                tempItem.setComplete();
                            } else if (choice.contentEquals("s")) {
                                tempItem.setSnoozed();
                            } else if (choice.contentEquals("i")) {
                                tempItem.setInProgress();
                            } else {
                                System.out.println("mmmmmm try again there big dawg");
                            }
                        } else { //incorrect input
                            System.out.println("mmmmmm try again there big dawg");
                        }
                    }
                    else if (action2.contentEquals("d")){ //Delete to-do item
                        List<ToDoItem> list = user.getToDoItemList();
                        int i = 0;
                        for (ToDoItem item : list) {
                            System.out.printf("ID %d %s\n", i++, item.toString());
                        }
                        System.out.println("Input ID number of item you wish to delete: ");
                        try {
                            user.removeToDoItem(Integer.parseInt(br.readLine()));
                            System.out.println("Removed ToDo item");
                        } catch (IndexOutOfBoundsException e){
                            System.out.println("Try a different number there big dawg");
                        }
                    }
                    else {
                        System.out.println("mmmmmm try again there big dawg");
                    }
                }

                //Access Cloud Utilities
                else if (action.contentEquals("c")) {
                    System.out.println(name +"'s Cloud Utilities\n[S]ync local items to cloud\n[R]ead the cloud\n[C]lear the cloud (not reccommended)");
                    String action4 = br.readLine().toLowerCase();
                    if (action4.contentEquals("s")) { //Sync local items to cloud
                        System.out.println(user.syncItemsToCloud() + "\n");
                    }
                    else if (action4.contentEquals("r")) { //Read the cloud
                        List<ToDoItem> list = user.cloudUtils.readCloud();
                        for (ToDoItem item : list) {
                            System.out.println(item);
                        }
                    }
                    else if (action4.contentEquals("c")){
                        user.cloudUtils.clearTheCloud();
                    }
                    else {
                        System.out.println("mmmmm try again big dawg");
                    }
                }

                //Access Database Utilities
                else if (action.contentEquals("d")) {
                    System.out.println(name +"'s Database Utilities\n[S]ync cloud items to database\n[R]ead the database\n[C]lear the database");
                    String action5 = br.readLine().toLowerCase();
                    if (action5.contentEquals("s")) { //Sync from cloud to database
                        List<ToDoItem> list = user.cloudUtils.readCloud();
                        for (ToDoItem item : list) {
                            System.out.println(user.databaseUtils.addItemToDatabase(item));
                        }
                    }
                    else if (action5.contentEquals("r")) { //Read database
                        List<ToDoItem> list = user.databaseUtils.readDatabase();
                        for (ToDoItem item : list) {
                            System.out.println(item);
                        }
                    }
                    else if (action5.contentEquals("c")){
                        user.databaseUtils.clearDatabase();
                    }
                    else {
                        System.out.println("mmmmmm try again big dawg");
                    }
                }
                //Access quit utilities
                else if (action.contentEquals("q")){
                    break;
                }
            }
        }
        catch (IOException | SQLException e) {
            System.out.println("Something went really wrong. Have fun finding it!");
            e.printStackTrace();
        }
    }
}
