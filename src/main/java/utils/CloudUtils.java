package utils;

import com.google.gson.*;
import domain.TimeStamp;
import domain.ToDoItem;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import exceptions.ParameterIsNotJsonStringException;
import org.javatuples.Pair;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CloudUtils {

    private HttpRequestFactory requestFactory;
    private String baseURL = "https://todoserver222.herokuapp.com/";
    public String todosURL;

    public CloudUtils(String name) {
        todosURL = baseURL + name + "/todos/";
        requestFactory = new NetHttpTransport().createRequestFactory();
    }

    public boolean checkConnection(){
        try {
            HttpRequest getRequest = requestFactory.buildGetRequest(
                    new GenericUrl(todosURL));
            String rawResponse = getRequest.execute().parseAsString();
            return !rawResponse.isEmpty();
        } catch(IOException e){
            return false;
        }
    }

    public String uploadItemToCloud(ToDoItem toDoItem){
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            data.put("memo", toDoItem.about);
            data.put("owner", toDoItem.owner);
            data.put("due_date", toDoItem.dueDate);
            data.put("created_date", toDoItem.createdDate);
            data.put("status", toDoItem.status);
            data.put("category", toDoItem.itemCategory);
            HttpContent content = new UrlEncodedContent(data);
            HttpRequest postRequest = requestFactory.buildPostRequest(
                    new GenericUrl(todosURL), content);
            postRequest.execute();
            return "Success";
        } catch (NullPointerException | IOException e){
            return "Failure";
        }
    }

    public String uploadListToCloud(List<ToDoItem> toDoItemList) throws IOException{
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            for (ToDoItem tdi : toDoItemList) {
                data.put("memo", tdi.about);
                data.put("owner", tdi.owner);
                data.put("due_date", tdi.dueDate);
                data.put("created_date", tdi.createdDate);
                data.put("status", tdi.status);
                data.put("category", tdi.itemCategory);
                HttpContent content = new UrlEncodedContent(data);
                HttpRequest postRequest = requestFactory.buildPostRequest(
                        new GenericUrl(todosURL), content);
                postRequest.execute();
            }
            return "Success";
        } catch (NullPointerException e){
            return "Empty List";
        }
    }

    public List<ToDoItem> readCloud() throws ParameterIsNotJsonStringException {
        return parseCloudJSONString(retrieveCloud());
    }

    public String retrieveCloud(){
        try{
            HttpRequest getRequest = requestFactory.buildGetRequest(new GenericUrl(todosURL));
            return getRequest.execute().parseAsString();
        } catch (IOException e){
            e.printStackTrace();
            return "";
        }
    }

    //WIP
    public PieDataset getPieData() throws Exception{
        String rawData;
        List<ToDoItem> toDoItems;
        List<Pair<String, Integer>> pairs;
        try {
            rawData = retrieveCloud();
            toDoItems = parseCloudJSONString(rawData);
            pairs = UIUtils.convertListOfToDosToListOfPairs(toDoItems);
        } catch (Exception | ParameterIsNotJsonStringException e) {
            JOptionPane.showMessageDialog(null, "Couldn't get data!");
            return new DefaultPieDataset();
        }
        return UIUtils.convertPairsToPieDataset(pairs);
    }


    public List<ToDoItem> parseCloudJSONString(String jsonString) throws ParameterIsNotJsonStringException {
        if (thisIsNotAJSONString(jsonString)) {
            throw new ParameterIsNotJsonStringException();
        }
        List<ToDoItem> list = new LinkedList<>();
        JsonParser jsonParser = new JsonParser();
        JsonElement rootElement = jsonParser.parse(jsonString);
        JsonArray rootObjects = rootElement.getAsJsonArray();
        if(rootObjects.size() > 0) {
            for (JsonElement rootObject : rootObjects) {
                var about = rootObject.getAsJsonObject().getAsJsonPrimitive("memo").getAsString();
                var owner = rootObject.getAsJsonObject().getAsJsonPrimitive("owner").getAsString();
                var dueDateJson = rootObject.getAsJsonObject().getAsJsonPrimitive("due_date").getAsString();
                var createdDateJson = rootObject.getAsJsonObject().getAsJsonPrimitive("created_date").getAsString();
                var status = rootObject.getAsJsonObject().getAsJsonPrimitive("status").getAsString();
                var category = rootObject.getAsJsonObject().getAsJsonPrimitive("category").getAsString();
                var idNumber = rootObject.getAsJsonObject().getAsJsonPrimitive("id").getAsInt();
                list.add(new ToDoItem(about, owner, makeTSfromJsonString(dueDateJson), makeTSfromJsonString(createdDateJson), status, category, idNumber));
            }
        } else {
            list.add(new ToDoItem("Cloud is empty", "You", new TimeStamp("0000-00-00T00:00:00.0000")));
        }
        return list;
    }

    public TimeStamp makeTSfromJsonString(String jsonString) throws ParameterIsNotJsonStringException{
        if (thisIsNotAJSONString(jsonString)) {
            throw new ParameterIsNotJsonStringException();
        }
        JsonParser jsonParser = new JsonParser();
        JsonElement rootElement = jsonParser.parse(jsonString);
        JsonObject rootObject = rootElement.getAsJsonObject();
        var year = rootObject.getAsJsonPrimitive("year").getAsInt();
        var month = rootObject.getAsJsonPrimitive("month").getAsInt();
        var day = rootObject.getAsJsonPrimitive("day").getAsInt();
        return new TimeStamp(year, month, day);
    }


    public void deleteTodoItem(int id) {
        try {
            HttpRequest deleteRequest = requestFactory.buildDeleteRequest(
                    new GenericUrl("https://todoserver222.herokuapp.com/todos/" + id));
            deleteRequest.execute();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private boolean thisIsNotAJSONString(String json){
        return json.charAt(0) == '{' && json.charAt(0) == '[';
    }


    //Extremely destructive to anyone else using the cloud. Completely wipes every to-do item. Only use in extreme cases.
    public void clearTheCloud(){
        JsonParser jsonParser = new JsonParser();
        JsonElement rootElement = jsonParser.parse(retrieveCloud());
        JsonArray rootObjects = rootElement.getAsJsonArray();
        for (JsonElement rootObject : rootObjects){
            var number = rootObject.getAsJsonObject().getAsJsonPrimitive("id").getAsInt();
            deleteTodoItem(number);
        }
    }

    public void deleteCloudEntriesSpecific(int start, int end){
        for (int i = start; i < end; i++){
            deleteTodoItem(i);
        }
    }
}
