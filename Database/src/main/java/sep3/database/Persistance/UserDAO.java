package sep3.database.Persistance;

import org.bson.types.ObjectId;
import sep3.database.Model.Account;
import sep3.database.Model.User;
import sep3.database.Model.UserList;

import java.util.ArrayList;

public interface UserDAO {
    Account getAccount(ObjectId userId);
    Account getAccount(String username);
    User getUser(String userID);
    ArrayList<User> getUserFriends(String userId);
    void addFriend(User user,String userId);
    void removeFriend(User user,String userId);
    void addTopicToUser(String Topic,String userId);
    void removeUserTopic(String Topic,String userId);
    void addAccount(Account account);
    Account getUserByName(String firstName,String LastName);
    ArrayList<Account> getAllAccount();
    void deleteAccount(String userID);
    void EditAccount(Account account);

}
