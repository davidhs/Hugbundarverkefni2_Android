package hi.hugbo.verywowchat.Models;

import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hi.hugbo.verywowchat.entities.ChatMessage;

/**
 * @Author Róman
 * This service is Responsible for Making HTTP Requests that have anything to do
 * with the user beeing in a certain chatroom such as sending messages to that chatroom,
 * getting chat messages from the chatroom, getting information of the chatroom etc...
 * We also want to ensure this service is a singleton instance in our app.
 * */
public class ChatRoomMessageService {
    private static final ChatRoomMessageService ourInstance = new ChatRoomMessageService();
    private API_caller api_caller = API_caller.getInstance(); // need for making HTTP calls

    // need this for making singletons
    public static ChatRoomMessageService getInstance() {
        return ourInstance;
    }
    private ChatRoomMessageService() {
    }


    /**
     * <pre>
     *      Usage : ChatRoomMessageService.SendChatMessage( chatID,String ,  body)
     *        FOR : chatID is a string      (Cannot be empty/null)
     *              token is a string       (cannot be null)
     *              dody is a  Map          (cannot be null)
     *      After : performs a HTTP Request to the API with 'body' as REQUEST BODY
     *              and 'token' under AUTHORIZATION header on chatID
     *
     * </pre>
     *
     * @param chatID is the id of the chat
     * @param token users JWT token
     * @param body Map<String,String> with one  key that is declared as 'message'
     */
    public void SendChatMessage(String chatID,String token, Map body) throws Exception {
        /* Send the HTTP request to send txt message to the chatroom */
        try {
            // Make the HTTP Request
            Map<String, String> result = api_caller.HttpRequest("auth/chatroom/"+chatID+"/message","POST",token,body);
            // Parse the HTTP status code
            int status = Integer.parseInt(result.get("status"));

            /* The way the API is written is that there is no possible way to get an error when sending a message expect
             *  when its a server error or client error such as invalid token etc .. so if we receive an error
             *  we log out the user and display the login Page to return the user to a "Safe state " of the app */
            if(status >= 400 && status < 500){
                throw new Exception("Something unexpected happened");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <pre>
     *      Usage : ChatRoomMessageService.GetCountChatLogs( chatID, token)
     *        FOR : chatID is a string       (Cannot be empty/null)
     *              token is a string       (cannot be null)
     *      After : performs a HTTP Request on the API with 'token' under AUTHORIZATION header
     *              and return the count of the chatID
     * </pre>
     *
     * @param chatID is the id of the chat
     * @param token users JWT token
     */
    public int GetCountChatLogs(String chatID,String token) throws Exception {
        int chatSize = 0;
        /* Send the HTTP request to send txt message to the chatroom */
        try {
            // Make the HTTP Request
            Map<String, String> result = api_caller.HttpRequest("auth/chatroom/"+chatID+"/messages/count","GET",token,null);
            // Parse the HTTP status code
            int status = Integer.parseInt(result.get("status"));

            if(status >= 200 && status < 300 ){
                int reps = Integer.parseInt(result.get("response"));
                if(reps-20 > 0) {
                    chatSize = reps-20;
                }
            }
            /* The way the API is written is that there is no possible way to get an error when sending a message expect
             *  when its a server error or client error such as invalid token etc .. so if we receive an error
             *  we log out the user and display the login Page to return the user to a "Safe state " of the app */
            if(status >= 400 && status < 500){
                throw new Exception("Something unexpected happened");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chatSize;
    }


    /**
     * <pre>
     *      Usage : ChatRoomMessageService.GetChatLogs( chatId, token, offset, LoggeInUser, lastTimeStamp)
     *        FOR : chatID is a string       (Cannot be empty/null)
     *              token is a string        (Cannot be empty/null)
     *              offset is a int          (Cannot be empty/null)
     *              LoggeInUser is a String  (Cannot be empty/null)
     *              lastTimeStamp is a int   (-1 to indicate null)
     *      After : performs a HTTP Request on the API with 'token' under AUTHORIZATION header
     *      *       and return a List of new Chat messages
     * </pre>
     *
     * @param chatId  is the id of the chat
     * @param token  users JWT token
     * @param offset offset on the chatlogs that need to be fetched
     * @param LoggeInUser the username of the logged in user
     * @param lastTimeStamp the last message that this chat recived
     * @return a List of new Messages in the chatroom
     */
    public List<ChatMessage> GetChatLogs(String chatId,String token,int offset,String LoggeInUser,int lastTimeStamp) throws Exception {
        List<ChatMessage> newChatMessages = new ArrayList<>();
        try {
            Map<String, String> result = api_caller.HttpRequest("auth/chatroom/"+chatId+"/messages/"+offset,"GET",token,null);
            // Parse the HTTP status code
            int status = Integer.parseInt(result.get("status"));

            /* The way the API is written is that there is no possible way to get an error when sending a message expect
             *  when its a server error or client error such as invalid token etc .. so if we receive an error
             *  we log out the user and display the login Page to return the user to a "Safe state " of the app */
            if(status >= 400 && status < 500){
                throw new Exception("Something unexpected happened");
            }

            if(status >= 200 && status < 300 ){
                // get the Json Array of chats
                JSONArray resultJson = new JSONArray(result.get("response"));
                // if the array is empty we are up to date on our messages
                if(resultJson.length() == 0) {
                    return newChatMessages;
                }

                for(int i = 0; i < resultJson.length(); i++){
                    JSONObject currMsg = resultJson.getJSONObject(i);
                    int timestamp = currMsg.getInt("timestamp");
                    // check if we already have this message displayed in the chat if so we skip mapping
                    if(lastTimeStamp != -1  && timestamp <= lastTimeStamp) {
                        continue;
                    }
                    else {
                        ChatMessage newMsg = new ChatMessage(
                                currMsg.getString("senderUsername"),
                                currMsg.getString("senderDisplayName"),
                                currMsg.getString("message"),
                                timestamp,
                                LoggeInUser
                        );
                        newChatMessages.add(newMsg);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newChatMessages;
    }

}
