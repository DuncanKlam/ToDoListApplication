package utils;

import domain.Reminder;
import domain.ToDoItem;
import domain.User;
import org.javatuples.Pair;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UIUtils {

    private static CloudUtils cloudUtils;
    private static DatabaseUtils databaseUtils;
    private static User user;

    public UIUtils(User user1) throws SQLException {
        cloudUtils = new CloudUtils();
        databaseUtils = new DatabaseUtils();
        user = user1;
    }

    public static PieDataset convertPairsToPieDataset(List<Pair<String, Integer>> pairs) {
        DefaultPieDataset result = new DefaultPieDataset();
        for (Pair<String, Integer> p : pairs) {
            result.insertValue(result.getItemCount(), p.getValue0(), p.getValue1());
        }
        return result;
    }

    public static List<Pair<String, Integer>> convertListOfToDosToListOfPairs(List<ToDoItem> toDoItems){
        List<Pair<String, Integer>> pairsResult = new LinkedList<>();
        int numCompleted = 0;
        int numInProgress = 0;
        int numSnoozed = 0;

        for (ToDoItem item : toDoItems){
            String status = item.getStatus().toLowerCase();
            if (status.equals("completed")){
                numCompleted+=1;
            }else if(status.equals("in-progress")){
                numInProgress+=1;
            }else if(status.equals("snoozed")){
                numSnoozed+=1;
            }else{
            }
        }
        //Adding the tuples based on values made above.
        pairsResult.add(0, new Pair<>("completed", numCompleted));
        pairsResult.add(1, new Pair<>("in-progress", numInProgress));
        pairsResult.add(2, new Pair<>("snoozed", numSnoozed));

        return pairsResult;
    }

    public List<String> syncCloudAndLocalToDatabase() {
        List<ToDoItem> itemList = new LinkedList<>();
        List<String> responseList = new LinkedList<>();
        if(cloudUtils.checkConnection()){
            itemList = cloudUtils.readCloud();
        }
        if(!user.getToDoItemList().isEmpty()){
            itemList.addAll(user.getToDoItemList());
        }
        if (itemList == null || itemList.size() == 0){
            responseList.add("No items to sync");
            return responseList;
        }
        for (ToDoItem item : itemList){
            responseList.add(item.id + " " + databaseUtils.addItemToDatabase(item));
        }
        return responseList;
    }

    public void updateTableDataFromSources(DefaultTableModel tableData) {
        List<ToDoItem> list = getCombinedListFromSourceWithDuplicatesRemoved();
        if (list == null){
            return;
        }
        if (tableData.getRowCount() == 0){
            for (ToDoItem item : list){
                tableData.addRow(new Object[]{item.id, item.about, item.itemCategory, item.status, item.formatDueDateNicely()});
            }
        }
        else{
            clearTable(tableData);
            for (ToDoItem item : list){
                tableData.addRow(new Object[]{item.id, item.about, item.itemCategory, item.status, item.formatDueDateNicely()});
            }
        }
        tableData.fireTableStructureChanged();
    }

    public static List<ToDoItem> getCombinedListFromSourceWithDuplicatesRemoved() {
        List<ToDoItem> cloudList = cloudUtils.readCloud();
        List<ToDoItem> list = removeDuplicateToDoItems(cloudList,user.toDoList);
        return removeDuplicateToDoItems(list,databaseUtils.readDatabase());
    }

    public static List<ToDoItem> removeDuplicateToDoItems(List<ToDoItem> list1, List<ToDoItem> list2) {
        ArrayList<ToDoItem> itemList = new ArrayList<>();
        if(list1 == null || list2 == null){
            if(list1 == null && list2 == null){ return null; }
            if(list1 == null){ return list2; }
            else{ return list1; }
        }
        itemList.addAll(list1);
        itemList.addAll(list2);
        ArrayList<ToDoItem> noDuplicateList = new ArrayList<>();
        for (ToDoItem element : itemList){
            if(!noDuplicateList.contains(element)){
                noDuplicateList.add(element);
            }
        }
        return noDuplicateList;
    }

    private void clearTable(DefaultTableModel tableData) {
        int rows = tableData.getRowCount();
        for (int i = 0; i<rows; i++){
            tableData.removeRow(0);
        }
    }

    public String removeSelectedToDoItemFromAll(DefaultTableModel tableData, int selectedRow){
        String itemID = getIDFromItem(getItemFromList(tableData, selectedRow));
        String response = removeSelectedToDoItemFromCloud(itemID);
        response += "\n" + removeSelectedToDoItemFromLocal(itemID);
        response += "\n" + removeSelectedToDoItemFromDatabase(itemID);
        return response;
    }

    public ToDoItem getItemFromList(DefaultTableModel tableData, int selectedRow) {
        int toDoItemID = (Integer) tableData.getValueAt(selectedRow,0);
        return getFromList(getCombinedListFromSourceWithDuplicatesRemoved(),toDoItemID);
    }

    public String getIDFromItem(ToDoItem item) {
        if(item.stringID != null){
            return item.stringID;
        }
        else{
            return Integer.toString(item.id);
        }
    }

    public String removeSelectedToDoItemFromCloud(String toDoItemID){
        return cloudUtils.deleteSingleItem(toDoItemID);
    }

    public String removeSelectedToDoItemFromLocal(String toDoItemID){
        return user.deleteToDoItem(toDoItemID);
    }

    public String removeSelectedToDoItemFromDatabase(String toDoItemID){
        return databaseUtils.deleteSingleItem(toDoItemID);
    }

    public ToDoItem getSelectedToDoItemFromSource(DefaultTableModel tableData, int selectedRow) {
        int toDoItemID = (Integer) tableData.getValueAt(selectedRow,0);
        ToDoItem chosenToDo = null;
        if (cloudUtils.checkConnection()){
            chosenToDo = getFromList(cloudUtils.readCloud(), toDoItemID);
        }
        if (chosenToDo == null){
            chosenToDo = getFromList(user.getToDoItemList(), toDoItemID);
        }
        if (chosenToDo == null){
            chosenToDo = getFromList(databaseUtils.readDatabase(), toDoItemID);
        }
        return chosenToDo;
    }

    private ToDoItem getFromList(List<ToDoItem> list, int identifier) {
        ToDoItem itemToReturn = null;
        for (ToDoItem item : list) {
            if (item.id == identifier) {
                itemToReturn = item;
            }
        }
        return itemToReturn;
    }


    public String makeToDoItemInLocation(ToDoItem newToDo) {
        if(cloudUtils.checkConnection()){
            cloudUtils.uploadItemToCloud(newToDo);
            return "Item saved to cloud";
        } else {
            user.addToDoItemtoList(newToDo);
            return "Item saved to local";
        }
    }

    public String getBothRemindersMessage() {
        String mostUrgent = getMostUrgentReminderMessage();
        String mostOld = getOldestReminderMessage();
        return mostUrgent + "\n\n" + mostOld;
    }

    public String getMostUrgentReminderMessage() {
        List<ToDoItem> list = getCombinedListFromSourceWithDuplicatesRemoved();
        List<Reminder> reminderList = makeRemindersFromToDos(list);
        if(reminderList.size()==0){
            return "No Reminders";
        }
        Reminder mostUrgent = reminderList.get(0);
        for (Reminder reminder : reminderList){
            if(mostUrgent.timeLeft() > reminder.timeLeft()){
                if(reminder.getMessage(" ").contains("ON-TIME")){
                    mostUrgent = reminder;
                }
            }
        }
        return mostUrgent.getMessage(mostUrgent.message);
    }

    public String getOldestReminderMessage() {
        List<ToDoItem> list = getCombinedListFromSourceWithDuplicatesRemoved();
        List<Reminder> reminderList = makeRemindersFromToDos(list);
        Reminder oldestReminder = reminderList.get(0);
        for (Reminder reminder : reminderList){
            if(oldestReminder.timeLeft() > reminder.timeLeft()){
                oldestReminder = reminder;
            }
        }
        return oldestReminder.getMessage(oldestReminder.overdueMessage);
    }

    private List<Reminder> makeRemindersFromToDos(List<ToDoItem> list) {
        List<Reminder> reminders = new LinkedList<>();
        for (ToDoItem item : list){
            if(item.status.contentEquals("n/a")){
                reminders.add(new Reminder());
            }else {
                reminders.add(new Reminder(item));
            }
        }
        return reminders;
    }



}