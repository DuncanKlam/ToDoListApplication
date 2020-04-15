package utils;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;
import domain.ToDoItem;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class DatabaseUtils {

    private Dao<ToDoItem, String> todoDao;

    public DatabaseUtils() throws SQLException {
        var connectionSource = new JdbcConnectionSource("jdbc:sqlite:todoitem.db");

        TableUtils.createTableIfNotExists(connectionSource, ToDoItem.class);

         todoDao = DaoManager.createDao(connectionSource, ToDoItem.class);
    }


    public String addItemToDatabase(ToDoItem item){
        try{
            todoDao.create(item);
            return "Success";
        } catch (SQLException e){
            return "Item exists in database";
        }
    }

    public List<ToDoItem> readDatabase(){
        try {
            var allItems = todoDao.queryForAll();
            return new LinkedList<>(allItems);
        } catch(SQLException e){
            e.getErrorCode();
            return new LinkedList<>();
        }
    }

    public String deleteSingleItem(String identifier){
        try {
            var customQuery = todoDao.queryBuilder().where().eq("id", identifier).prepare();
            var itemToDelete = todoDao.query(customQuery);
            if (itemToDelete.isEmpty()) {
                return "Not on database";
            } else {
                todoDao.delete(itemToDelete);
                return "Database Delete: Success";
            }
        } catch (SQLException e){
            return "Not on database";
        }
    }

    public void clearDatabase() throws SQLException {
        todoDao.delete(todoDao.queryForAll());
    }
}
