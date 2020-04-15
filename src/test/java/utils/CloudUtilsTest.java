package utils;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import domain.TimeStamp;
import domain.ToDoItem;
import exceptions.ParameterIsNotJsonStringException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CloudUtilsTest {

    CloudUtils cloudUtils = new CloudUtils();

    HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();

    String todosURL = "https://todoserver222.herokuapp.com/todos";

    ToDoItem todoItem1 = new ToDoItem("Reminder for grilled cheese", "Klemm", new TimeStamp("2020-04-04T18:35:23.669Z"));
    ToDoItem todoItem2 = new ToDoItem("Don't forget the pana cotta", "Klemm", new TimeStamp("2020-04-12T14:43:54.669Z"));

    List<ToDoItem> list = new LinkedList<>();
    List<ToDoItem> list2 = new LinkedList<>();

    String currentTime = java.time.Clock.systemUTC().instant().toString();


    @Test
    void checkConnection(){
        assertEquals(true, cloudUtils.checkConnection());
    }

    @Test
    void canUploadCustomID() throws IOException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("memo", "custom test item");
        data.put("owner", "teamone");
        data.put("due_date", new TimeStamp(currentTime));
        data.put("created_date", new TimeStamp(currentTime));
        data.put("status", "nonexistent");
        data.put("category", "Black Ops");
        data.put("id", 111);
        HttpContent content = new UrlEncodedContent(data);
        HttpRequest postRequest = requestFactory.buildPostRequest(
                new GenericUrl(todosURL), content);
        //postRequest.execute().parseAsString();
        //Kind of screws up the whole ID system on the cloud so tread lightly
    }

    @Test
    void uploadItemWithoutID() {
        cloudUtils.uploadItemToCloud(todoItem1);
        cloudUtils.uploadItemToCloud(todoItem2);
    }


    @Test
    void cloudUploadsAndDownloadsOneItem() throws IOException, ParameterIsNotJsonStringException {
        list.add(todoItem1);


        cloudUtils.uploadListToCloud(list);
        list2 = cloudUtils.parseCloudJSONString(cloudUtils.retrieveCloud());


        assertEquals(list.get(0).getUniqueItemID(), list2.get(0).getUniqueItemID());

        cloudUtils.deleteTodoItem("1");
    }

    @Test
    void cloudUploadsNoItems() throws IOException, ParameterIsNotJsonStringException {
        list.add(null);

        assertEquals(0, cloudUtils.uploadListToCloud(list));
        list2 = cloudUtils.parseCloudJSONString(cloudUtils.retrieveCloud());

        assertEquals("Cloud is emptyYou big dummy0000-00-00T00:00:00.0000", list2.get(0).getUniqueItemID());
    }

    @Test
    void cloudUploadsMultipleItems() throws IOException, ParameterIsNotJsonStringException {
        list.add(todoItem1);
        list.add(todoItem2);


        cloudUtils.uploadListToCloud(list);
        list2 = cloudUtils.parseCloudJSONString(cloudUtils.retrieveCloud());


        assertEquals(list.get(0).getUniqueItemID(), list2.get(0).getUniqueItemID());
        assertEquals(list.get(1).getUniqueItemID(), list2.get(1).getUniqueItemID());


        cloudUtils.deleteTodoItem("1");
        cloudUtils.deleteTodoItem("2");
    }

    @Test
    void deleteCloudWorks() throws ParameterIsNotJsonStringException {
        cloudUtils.clearTheCloud();

        list2 = cloudUtils.parseCloudJSONString(cloudUtils.retrieveCloud());

        assertEquals(null, list2);

    }

    @Test
    void deleteSpecificCloudEntries(){
        cloudUtils.deleteCloudEntriesSpecific(12,13);
    }


    @Test
    void getPieDataNormalTest() {
        //Stuff here
    }
}