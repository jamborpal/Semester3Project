package sep3.database.Persistance;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import sep3.database.Model.Chatroom;
import sep3.database.Model.Message;
import sep3.database.Model.Topic;
import sep3.database.Model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Projections.include;

public class ChatroomDAOImpl implements ChatroomDAO {
    private MongoCollection<Document> collection;
    private DBConnection connection;
    private Gson gson;
    private UserDAO userDAO;
    private TopicDAO topicDAO;

    public ChatroomDAOImpl() {
        connection = DBConnection.setConnection();
        collection = connection.getDatabase().getCollection("Chatrooms");
        gson = new Gson();
        userDAO = new UserDAOImpl();
        topicDAO = new TopicDAOImpl();
    }

    private Chatroom createChatroom(Document document) {
        Chatroom room = new Chatroom();
        String id = document.get("_id").toString();
        room.set_id(id);
        room.setName(document.get("name").toString());
        room.setOwner(document.get("owner").toString());
        room.setType(document.get("type").toString());
        List<Document> messages = (List<Document>) document.get("messages");
        if(messages!=null) {

            for (var DBmessage : messages
            ) {
                Message message = new Message(
                        DBmessage.get("message").toString(),
                        DBmessage.get("AuthorId").toString(),
                        DBmessage.get("messageId").toString(),DBmessage.get("Username").toString());
                room.addMessage(message);
            }
        }

        ArrayList<User> participants = getChatroomUsers(id);
        room.setParticipants(participants);
        ArrayList<Topic> topics = getChatroomTopic(id);
        room.setTopics(topics);
        return room;
    }

    private ArrayList<Topic> getChatroomTopic(String id) {
        ArrayList<Topic> topicList = new ArrayList<>();
        BasicDBObject whereQuery = new BasicDBObject();
        ObjectId _id = new ObjectId(id);
        whereQuery.append("_id", _id);
        FindIterable<Document> findIterable = collection.find(whereQuery).projection(include("topics"));
        var document = findIterable.cursor().next();
        try {
            List<Document> topics = (List<Document>) document.get("topics");
            for (var DBtopic : topics
            ) {
                ObjectId topicid = new ObjectId(DBtopic.get("topicId").toString());
                Topic topic = topicDAO.getTopic(topicid);
                topicList.add(topic);


            }
        }catch (NullPointerException e)
        {

        }
        return topicList;
    }


    private ArrayList<User> getChatroomUsers(String id) {
        ArrayList<User> users = new ArrayList<>();
        BasicDBObject whereQuery = new BasicDBObject();
        ObjectId _id = new ObjectId(id);
        whereQuery.append("_id", _id);
        FindIterable<Document> findIterable = collection.find(whereQuery).projection(include("participants"));
        var document = findIterable.cursor().next();

        List<Document> participants = (List<Document>) document.get("participants");
        for (var DBparticipant : participants
        ) {
            ObjectId participantId = new ObjectId(DBparticipant.get("participantId").toString());
            User participant = userDAO.getUser(participantId.toString());
            users.add(participant);


        }
        return users;
    }

    @Override
    public ArrayList<Chatroom> getAllChatrooms() {
        ArrayList<Chatroom> chatrooms = new ArrayList<>();
        MongoCursor<Document> cursor = collection.find().iterator();
        try {
            while (cursor.hasNext()) {

                Document document = cursor.next();
                Chatroom chatroom = createChatroom(document);
                chatrooms.add(chatroom);
            }
        } finally {
            cursor.close();
        }
        return chatrooms;
    }

    @Override
    public void AddChatroom(Chatroom chatroom) {
        Document add = new Document();
        ObjectId _id = new ObjectId(chatroom.get_id());
        add.append("_id", _id);
        add.append("name", chatroom.getName());
        add.append("owner",new ObjectId(chatroom.getOwner()));
        add.append("type",chatroom.getType());

        if (chatroom.getTopics()!=null) {
            Document topics = new Document();
            for (var topic : chatroom.getTopics()
            ) {
                ObjectId topicID = new ObjectId(topic.get_id());
                topics.append("topics", topicID);
            }
            add.append("topics", Arrays.asList(topics));
        }
        if (chatroom.getParticipants().size() != 0) {
            List<DBObject> participants = new ArrayList<>();
            for (var friend : chatroom.getParticipants()
            ) {
                ObjectId participantId = new ObjectId(friend.get_id());
                participants.add(new BasicDBObject("participantId", participantId));

            }
            add.append("participants", participants);
        }
        if (chatroom.getMessages().size() != 0) {
            List<DBObject> messages = new ArrayList<>();
            for (var message : chatroom.getMessages()
            ) {
                BasicDBObject DBmessage = new BasicDBObject();
                ObjectId authorId = new ObjectId(message.getAuthorID());
                ObjectId messageId = new ObjectId(message.get_id());
                DBmessage.append("AuthorId", authorId);
                DBmessage.append("messageId", messageId);
                DBmessage.append("Username", message.getUsername());
                DBmessage.append("message", message.getMessage());
                messages.add(DBmessage);
            }
            add.append("messages", messages);
        }
        collection.insertOne(add);

    }

    @Override
    public void deleteChatroom(Chatroom chatroom) {

    }


    @Override
    public void addMessageToChatroom(String chatroomId, Message message) {
        BasicDBObject chatroomObject = new BasicDBObject();
        BasicDBObject updateMessage = new BasicDBObject();
        BasicDBObject messageObject = new BasicDBObject();

        ObjectId authorId = new ObjectId(message.getAuthorID());
        ObjectId messageId = new ObjectId();
        messageObject.append("AuthorId", authorId);
        messageObject.append("messageId", messageId);
        messageObject.append("message", message.getMessage());
        messageObject.append("Username", message.getUsername());

        updateMessage.append("$push", new BasicDBObject().append("messages",messageObject));
        ObjectId chatroom_id = new ObjectId(chatroomId);
        chatroomObject.append("_id", chatroom_id);
        collection.updateOne(chatroomObject,updateMessage);
    }

    @Override
    public void joinChatroom(String userId, String chatroomId) {
        BasicDBObject chatroomObject = new BasicDBObject();
        BasicDBObject updatePList = new BasicDBObject();
        BasicDBObject participantObject = new BasicDBObject();

        ObjectId participantId = new ObjectId(userId);
        participantObject.append("participantId", participantId);
        System.out.println("JOINNNNNNNNNNNNNNNNNNNNNNNNNNNNN");

        updatePList.append("$push", new BasicDBObject().append("participants",participantObject));
        ObjectId chatroom_id = new ObjectId(chatroomId);
        chatroomObject.append("_id", chatroom_id);
        collection.updateOne(chatroomObject,updatePList);

    }

    @Override
    public void leaveChatroom(String userId, String chatroomId) {
        System.out.println(chatroomId);
        BasicDBObject chatroomObject = new BasicDBObject();
        BasicDBObject updatePList = new BasicDBObject();
        BasicDBObject participantObject = new BasicDBObject();
        ObjectId participantId = new ObjectId(userId);
        participantObject.append("participantId", participantId);
        updatePList.append("$pull", new BasicDBObject().append("participants",participantObject));
        ObjectId chatroom_id = new ObjectId(chatroomId);
        chatroomObject.append("_id", chatroom_id);
        collection.updateOne(chatroomObject,updatePList);


    }

    public Chatroom getPrivateChatroom(String userId1,String userId2)
    {
        BasicDBObject object = new BasicDBObject();
      ObjectId user1 = new ObjectId(userId1);
        object.append("participantId",user1);
        BasicDBObject object1 = new BasicDBObject();
        ObjectId user2 = new ObjectId(userId2);
       object1.append("participantId",user2);
        Bson users = Filters.and(Filters.eq("participants",object),
                Filters.eq("participants",object1),
                Filters.eq("type","private")
                );
        Document document = collection.find(users).first();

        assert document != null;
        return createChatroom(document);

    }

    @Override
    public Chatroom getChatroom(String id) {
        BasicDBObject whereQuery = new BasicDBObject();
        ObjectId _id = new ObjectId(id);
        whereQuery.append("_id", _id);
        Chatroom chat = null;
        try {
            Document cursor = collection.find(whereQuery).first();
            String json = cursor.toJson();
            chat = gson.fromJson(json, Chatroom.class);
        } catch (Exception e) {
            return null;
        }
        return chat;

    }

    @Override
    public void removeChatroom(String id) {
        ObjectId _id = new ObjectId(id);
        BasicDBObject remove = new BasicDBObject("_id",_id);
        collection.deleteOne(remove);
    }

    @Override
    public void deletePrivateChatroom(String userId1, String userId2) {
        BasicDBObject object = new BasicDBObject();
        ObjectId user1 = new ObjectId(userId1);
        object.append("participantId",user1);
        BasicDBObject object1 = new BasicDBObject();
        ObjectId user2 = new ObjectId(userId2);
        object1.append("participantId",user2);
        Bson users = Filters.and(Filters.eq("participants",object),
                Filters.eq("participants",object1),
                Filters.eq("type","private")
        );
        collection.deleteOne(users);
    }


    @Override
    public ArrayList<Chatroom> getChatroomByUserId(String userId) {
        ArrayList<Chatroom> chatRooms = new ArrayList<>();
        BasicDBObject whereQuery = new BasicDBObject();
        BasicDBObject participant = new BasicDBObject();

        ObjectId _id = new ObjectId(userId);
        participant.append("participantId",_id);
        whereQuery.append("participants", participant);
        whereQuery.append("type","public");
        MongoCursor<Document> documents = collection.find(whereQuery).iterator();


        while (documents.hasNext()) {
            Document json = documents.next();
            Chatroom room = createChatroom(json);
            chatRooms.add(room);
        }
        return chatRooms;
    }

    @Override
    public void deleteUserFromChatrooms(String userId) {
        BasicDBObject whereQuery = new BasicDBObject();
        BasicDBObject participant = new BasicDBObject();
        ObjectId _id = new ObjectId(userId);
        participant.append("participantId",_id);
        whereQuery.append("$pull", new BasicDBObject().append("participants",participant));
        collection.updateMany(new BasicDBObject(),whereQuery);
    }

    @Override
    public ArrayList<Chatroom> getChatroomsByTopic(String topic) {
        ArrayList<Chatroom> chatRooms = new ArrayList<>();
        BasicDBObject whereQuery = new BasicDBObject();
        BasicDBObject participant = new BasicDBObject();

       Topic topicObject = topicDAO.getTopic(topic);
        participant.append("topics",new ObjectId(topicObject.get_id()));
        whereQuery.append("topics", participant);
        MongoCursor<Document> documents = collection.find(whereQuery).iterator();
        while (documents.hasNext()) {
            Document json = documents.next();
            Chatroom room = createChatroom(json);
            chatRooms.add(room);
        }
        System.out.println(chatRooms.size());
        return chatRooms;
    }
}
