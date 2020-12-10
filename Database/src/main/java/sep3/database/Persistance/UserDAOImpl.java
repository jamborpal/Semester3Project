package sep3.database.Persistance;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.types.ObjectId;
import sep3.database.Model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;

import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.include;

public class UserDAOImpl implements UserDAO
{
    private MongoCollection<Document> collection;
    private DBConnection connection;
    private Gson gson;
    private TopicDAO topicDAO;
    private ChatroomDAO chatroomDAO;

    public UserDAOImpl()
    {
        connection = DBConnection.setConnection();
        collection = connection.getDatabase().getCollection("Users");
        gson = new Gson();
        topicDAO = new TopicDAOImpl();
        chatroomDAO = new ChatroomDAOImpl();
    }

    private MongoCursor<Document> cursor(String key, Object obj)
    {
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.append(key, obj);
        return collection.find(whereQuery).iterator();
    }

    public Account createAccount(Document document)
    {
        ObjectId _id = new ObjectId(document.get("_id").toString());
        Account account = new Account(
                document.get("role").toString(), document.get("Pass").toString()
                , _id.toString(), document.get("Username").toString(),
                document.get("Fname").toString(), document.get("Lname").toString(), document.get("email").toString()
        );
        account.setTopics(topicDAO.getUserTopics(_id));
        account.setFriends(getUserFriends(_id.toString()));
        return account;

    }

    @Override
    public Account getAccount(ObjectId userId)
    {
        MongoCursor<Document> cursor = cursor("_id", userId);
        var document = cursor.next();
        return createAccount(document);
    }

    @Override
    public Account getAccount(String username)
    {
        MongoCursor<Document> cursor = cursor("Username", username);
        Document document = null;
        try
        {
            document = cursor.next();
        } catch (NoSuchElementException e)
        {
            System.out.println(e.getMessage());
            return null;

        }
        return createAccount(document);
    }

    public ArrayList<Account> getAllAccount()
    {
        ArrayList<Account> accounts = new ArrayList<>();
        MongoCursor<Document> cursor = collection.find().iterator();
        try
        {
            while (cursor.hasNext())
            {

                Document document = cursor.next();
                Account account = createAccount(document);
                accounts.add(account);
            }
        } finally
        {
            cursor.close();
        }
        return accounts;

    }

    @Override
    public void deleteAccount(String userID)
    {
        ObjectId user_id = new ObjectId(userID);
        collection.deleteOne(new Document("_id", user_id));
        ArrayList<Account> accounts = getAllAccount();
        User user = null;
        /*for (int i = 0; i < accounts.size(); i++)
        {
            if (accounts.get(i).get_id().equals(user_id))
            {
                user = accounts.get(i);
            }
        }
        for (int i = 0; i < accounts.size(); i++)
        {
            for (int j = 0; j < accounts.get(i).getFriends().size(); j++)
            {
                if (accounts.get(i).getFriends().get(j).get_id().equals(user_id))
                {
                    removeFriend(user, accounts.get(i).getFriends().get(j).get_id());
                }
            }
        }*/
        //delete from friends and chatrooms
    }

    @Override
    public void EditAccount(Account account)
    {
        BasicDBObject basicDBObject = new BasicDBObject();
        BasicDBObject basicDBObject1 = new BasicDBObject();
        basicDBObject.append("Username", account.getUsername());
        basicDBObject.append("Fname", account.getFname());
        basicDBObject.append("Lname", account.getLname());
        basicDBObject.append("Pass", account.getPass());
        basicDBObject.append("role", account.getRole());
        basicDBObject.append("email", account.getEmail());
        basicDBObject1.append("_id", new ObjectId(account.get_id()));
        collection.updateOne(basicDBObject, basicDBObject1);

    }

    @Override
    public User getUser(String userID)
    {
        BasicDBObject whereQuery = new BasicDBObject();
        ObjectId objectId = new ObjectId(userID);
        whereQuery.append("_id", objectId);
        MongoCursor<Document> cursor = collection.find(whereQuery).iterator();
        try
        {
            Document document = cursor.next();
            return new User(document.get("_id").toString(), document.get("Username").toString(),
                    document.get("Fname").toString(), document.get("Lname").toString());
        } catch (NoSuchElementException e)
        {

        }
        return null;
    }


    @Override
    public ArrayList<User> getUserFriends(String userId)
    {
        //ChangeTo arrayList and remove UserList class
        UserList list = new UserList();
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.append("_id", userId);
        FindIterable<Document> findIterable = collection.find(whereQuery).projection(include("friends"));
        Document document;
        try
        {
            document = findIterable.cursor().next();
        } catch (NoSuchElementException e)
        {
            return null;
        }
        var friends = document.getList("friends", ObjectId.class);
        if (friends != null)
        {
            for (ObjectId id : friends
            )
            {
                User friend = getUser(id.toString());
                list.addUser(friend);
            }
        }
        return list.getUsers();
    }

    @Override
    public void addFriend(User friend, String userId)
    {
        BasicDBObject newDocument = new BasicDBObject();
        newDocument.append("$push", new BasicDBObject().append("friends", friend.get_id()));
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.append("_id", userId);
        collection.updateOne(searchQuery, newDocument);


    }

    @Override
    public void removeFriend(User user, String userId)
    {
        ObjectId user_id = new ObjectId(user.get_id());
        BasicDBObject update = new BasicDBObject("friends", user_id);
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.append("_id", userId);
        collection.updateOne(searchQuery, new BasicDBObject("$pull", update));
    }

    @Override
    public void addTopicToUser(String Topic, String userId)
    {
        BasicDBObject newDocument = new BasicDBObject();
        ObjectId topicID = new ObjectId(topicDAO.getTopic(Topic).get_id());
        System.out.println(topicID.toString());
        newDocument.append("$push", new BasicDBObject().append("topics", topicID));
        BasicDBObject searchQuery = new BasicDBObject();
        ObjectId user_id = new ObjectId(userId);
        searchQuery.append("_id", user_id);
        collection.updateOne(searchQuery, newDocument);

    }

    @Override
    public void removeUserTopic(String Topic, String userId)
    {
        BasicDBObject newDocument = new BasicDBObject();
        ObjectId topicID = new ObjectId(topicDAO.getTopic(Topic).get_id());
        System.out.println(topicID.toString());
        newDocument.append("$pull", new BasicDBObject().append("topics", topicID));
        BasicDBObject searchQuery = new BasicDBObject();
        ObjectId user_id = new ObjectId(userId);
        searchQuery.append("_id", user_id);
        collection.updateOne(searchQuery, newDocument);
    }

    @Override
    public void addAccount(Account account)
    {


        Document add = new Document();
        ObjectId _id = new ObjectId(account.get_id());
        add.append("_id", _id);
        add.append("Username", account.getUsername());
        add.append("Pass", account.getPass());
        add.append("Fname", account.getFname());
        add.append("Lname", account.getLname());
        add.append("role", account.getRole());
        add.append("email", account.getEmail());
        if (account.getTopics().size() != 0)
        {
            Document topics = new Document();
            for (var topic : account.getTopics()
            )
            {
                ObjectId topicID = new ObjectId(topic.get_id());
                topics.append("$set", new BasicDBObject().append("topics", topicID));
            }
            add.append("topics", Arrays.asList(topics));
        }
        if (account.getFriends().size() != 0)
        {
            Document friends = new Document();
            for (var friend : account.getFriends()
            )
            {
                ObjectId friendId = new ObjectId(friend.get_id());
                friends.append("$set", new BasicDBObject().append("topics", friendId));
            }
            add.append("friends", Arrays.asList(friends));
        }
        collection.insertOne(add);
    }

    @Override
    public Account getUserByName(String firstName, String LastName)
    {
        return null;
    }

}
