package com.example.chat_de;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chat_de.datas.Chat;
import com.example.chat_de.datas.ChatRoom;
import com.example.chat_de.datas.ChatRoomMeta;
import com.example.chat_de.datas.ChatRoomUser;
import com.example.chat_de.datas.User;
import com.example.chat_de.datas.UserChatRoom;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatDB {
    public static final String CHAT_ROOMS = "chatRooms";
    public static final String CHATS = "chats";
    public static final String CHAT_ROOM_META = "chatRoomMeta";
    public static final String LAST_MESSAGE_INDEX = "lastMessageIndex";
    public static final String LAST_MESSAGE_TIME = "lastMessageTime";
    public static final String LAST_READ_INDEX = "lastReadIndex";
    public static final String USERS = "users";
    public static final String CHAT_ROOM_JOINED = "chatRoomJoined";
    public static final String USER_JOINED = "userJoined";
    public static final String DATE = "date";

    private static DatabaseReference ref = null;
    private static final ArrayList<Pair<String, ChildEventListener>> eventListeners = new ArrayList<>();
    private static String rootPath;
    private static String currentUserKey = null;

    public static void setReference(String root, String userKey) { // 앱 시작할때 딱 1번만 호출할 것
        if (ref == null) {
            ref = FirebaseDatabase.getInstance().getReference(root);
            rootPath = root;
            currentUserKey = userKey;
        }
    }
    public static DatabaseReference getReference() {
        return ref;
    }
    public static String getRootPath() {
        return rootPath;
    }
    //TODO : intent로 본인의 키를 넘겨주는 부분 있으면 전부 이쪽으로 바꿔야 함
    public static String getCurrentUserKey() {
        return currentUserKey;
    }

    public static void getUsersCompleteEventListener(IEventListener<HashMap<String, User>> listener) {
        ref.child(USERS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                HashMap<String, User> item = new HashMap<>();
                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    item.put(userSnapshot.getKey(), userSnapshot.getValue(User.class));
                }
                listener.eventListener(item);
            } else {
                Log.e("FRD", "Can not get users");
                listener.eventListener(new HashMap<>());
            }
        });
    }
    public static void setChatRoomCompleteListener(String chatRoomName, ArrayList<User> userList, User userMe, IEventListener<String> listener) {
        final ChatRoomMeta chatRoomMeta = new ChatRoomMeta(chatRoomName, ChatRoomMeta.Type.BY_USER,"");
        ChatRoom chatRoom = new ChatRoom(new HashMap<>(), chatRoomMeta);
        ref.child(CHAT_ROOMS).push().setValue(chatRoom, (error, rf) -> {
            if (error == null) {
                inviteUserListCompleteListener(rf.getKey(), chatRoomMeta, userList, userMe, listener);
            } else {
                Log.e("FRD", "Make chat room error: " + error.toString());
            }
        });
    }
    public static void setPersonalChatRoom(User userMe, User userOther, IEventListener<String> listener) {
        String chatRoomName = userMe.getName() + ", " + userOther.getName();
        final ChatRoomMeta chatRoomMeta = new ChatRoomMeta(chatRoomName, ChatRoomMeta.Type.BY_USER,"");
        ChatRoom chatRoom = new ChatRoom(new HashMap<>(), chatRoomMeta);
        ref.child(CHAT_ROOMS).push().setValue(chatRoom, (error, rf) -> {
            if (error == null) {
                final String chatRoomKey = rf.getKey();
                HashMap<String, Object> result = new HashMap<>();
                // chatRoomJoined의 chatRoomKey에 새로운 user들 추가
                result.put(makePath(CHAT_ROOM_JOINED, chatRoomKey, userMe.getUserKey()), new ChatRoomUser(userMe));
                result.put(makePath(CHAT_ROOM_JOINED, chatRoomKey, userOther.getUserKey()), new ChatRoomUser(userOther));
                // userJoined의 userKey들에 새로운 chatRoom 추가
                result.put(makePath(USER_JOINED, userMe.getUserKey(), chatRoomKey), new UserChatRoom(chatRoomMeta));
                result.put(makePath(USER_JOINED, userOther.getUserKey(), chatRoomKey), new UserChatRoom(chatRoomMeta));

                // 종합한 값들을 최종적으로 update
                ref.updateChildren(result).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String message = userMe.getName() + "님이 새 채팅방을 생성하셨습니다.";
                        uploadMessage(message, -2, Chat.Type.SYSTEM, chatRoomKey, "SYSTEM", new HashMap<>());
                    } else {
                        Log.e("FRD", "Can not update data of users and the new chat room");
                    }
                });

                listener.eventListener(chatRoomKey);
            } else {
                Log.e("FDB", "Make chat room error: " + error.toString());
            }
        });
    }
    public static void inviteUserListCompleteListener(String chatRoomKey, ChatRoomMeta chatRoomMeta, ArrayList<User> userList, User userMe, IEventListener<String> listener) {
        HashMap<String, Object> result = new HashMap<>();
        for (User item : userList) {
            // chatRoomJoined의 chatRoomKey에 새로운 user들 추가
            result.put(makePath(CHAT_ROOM_JOINED, chatRoomKey, item.getUserKey()), new ChatRoomUser(item.userMeta()));
            // userJoined의 userKey들에 새로운 chatRoom 추가
            result.put(makePath(USER_JOINED, item.getUserKey(), chatRoomKey), new UserChatRoom(chatRoomMeta));
        }
        // 종합한 값들을 최종적으로 update
        ref.updateChildren(result).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                StringBuilder builder = new StringBuilder(userMe.getName() + "님이 ");
                for(User user: userList) {
                    if(!user.getUserKey().equals(userMe.getUserKey())) {
                        builder.append(user.getName()).append("님, ");
                    }
                }
                String message = builder.substring(0, builder.length() - 2) + "을 초대하셨습니다.";
                uploadMessage(message, -2, Chat.Type.SYSTEM, chatRoomKey, "SYSTEM", new HashMap<>());
            } else {
                Log.e("FRD", "Can not update data of users and the new chat room");
            }
        });

        listener.eventListener(chatRoomKey);
    }

    public static void uploadMessage(String message, int index, Chat.Type messageType, String chatRoomKey, String userKey, HashMap<String, ChatRoomUser> chatRoomUserList) {
        // upload message
        ref.child(CHAT_ROOMS).child(chatRoomKey).child(CHATS).push().setValue(new Chat(message, index, userKey, messageType), (error, rf) -> {
            if (error == null && messageType != Chat.Type.SYSTEM) {
                rf.child(DATE).get().addOnCompleteListener(task -> {
                    Object serverTime;
                    if (task.isSuccessful()) {
                        serverTime = task.getResult().getValue();
                    } else {
                        serverTime = ServerValue.TIMESTAMP;
                        Log.e("FRD", "Can not get a server time");
                    }
                    HashMap<String, Object> result = new HashMap<>();
                    // update user read last message
                    result.put(makePath(CHAT_ROOM_JOINED, chatRoomKey, userKey, LAST_READ_INDEX), index);
                    // update chat room's last message index and time
                    result.put(makePath(CHAT_ROOMS, chatRoomKey, CHAT_ROOM_META, LAST_MESSAGE_INDEX), index);
                    result.put(makePath(CHAT_ROOMS, chatRoomKey, CHAT_ROOM_META, LAST_MESSAGE_TIME), serverTime);
                    // update last message index and time of all users in the chat room
                    for (String key : chatRoomUserList.keySet()) {
                        result.put(makePath(USER_JOINED, key, chatRoomKey, CHAT_ROOM_META, LAST_MESSAGE_INDEX), index);
                        result.put(makePath(USER_JOINED, key, chatRoomKey, CHAT_ROOM_META, LAST_MESSAGE_TIME), serverTime);
                    }
                    // update
                    ref.updateChildren(result);
                });
            } else if (error != null) {
                Log.e("FRD", "Upload message error:" + error.toString());
            }
        });
    }

    public static void userReadLatestMessage(String chatRoomKey, String userKey) {
        ref.child(CHAT_ROOMS).child(chatRoomKey).child(CHAT_ROOM_META).child(LAST_MESSAGE_INDEX).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ref.child(makePath(CHAT_ROOM_JOINED, chatRoomKey, userKey, LAST_READ_INDEX)).setValue(task.getResult().getValue(Integer.class));
            } else {
                Log.e("FRD", "Can not get a lastMessageIndex of: " + chatRoomKey);
            }
        });
    }

    public static void getChatRoomUserListCompleteListener(String chatRoomKey, IEventListener<HashMap<String, ChatRoomUser>> listener) {
        ref.child(CHAT_ROOM_JOINED).child(chatRoomKey).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                HashMap<String, ChatRoomUser> chatRoomUserList = new HashMap<>();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    chatRoomUserList.put(snapshot.getKey(), snapshot.getValue(ChatRoomUser.class));
                }
                listener.eventListener(chatRoomUserList);
            } else {
                Log.e("FRD", "Can not get " + chatRoomKey + "'s user list");
            }
        });
    }
    public static void getLastChatCompleteListener(String chatRoomKey, IKeyValueEventListener<String, Chat> listener) {
        ref.child(makePath(CHAT_ROOMS, chatRoomKey, CHATS)).limitToLast(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String resultKey = null;
                Chat resultValue = null;
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    resultKey = snapshot.getKey();
                    resultValue = snapshot.getValue(Chat.class);
                    break;
                }
                listener.eventListener(resultKey, resultValue);
            } else {
                Log.e("FRD", "Can not get last chat of the " + chatRoomKey);
            }
        });
    }
    public static void getPrevChatListCompleteListener(String chatRoomKey, String frontChatKey, int chatLimit, IKeyValueEventListener<String, ArrayList<Chat>> listener) {
        if (frontChatKey != null) {
            ref.child(makePath(CHAT_ROOMS, chatRoomKey, CHATS)).orderByKey().endBefore(frontChatKey).limitToLast(chatLimit).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ArrayList<Chat> chatList = new ArrayList<>();
                    String chatKey = null;
                    for (DataSnapshot snapshot : task.getResult().getChildren()) {
                        if (chatKey == null) {
                            chatKey = snapshot.getKey();
                        }
                        chatList.add(snapshot.getValue(Chat.class));
                    }
                    listener.eventListener(chatKey, chatList);
                } else {
                    Log.e("FRD", "Can not get chats of the" + chatRoomKey);
                }
            });
        } else {
            listener.eventListener(null, new ArrayList<>());
        }
    }

    public static void messageAddedEventListener(String chatRoomKey, String lastChatKey, IKeyValueEventListener<String, Chat> listener) {
        class myChildEventListener implements ChildEventListener {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                listener.eventListener(snapshot.getKey(), snapshot.getValue(Chat.class));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }
        myChildEventListener myListener = new myChildEventListener();
        String path = makePath(CHAT_ROOMS, chatRoomKey, CHATS);

        if (lastChatKey != null) {   // 빈 채팅방이 아닐 때
            ref.child(path).orderByKey().startAfter(lastChatKey).addChildEventListener(myListener);
        } else {                    // 빈 채팅방일 때
            ref.child(path).addChildEventListener(myListener);
        }
        eventListeners.add(new Pair<>(path, myListener));
    }
    public static void userListChangedEventListener(String chatRoomKey, IKeyValueEventListener<String, ChatRoomUser> listener) {
        String path = makePath(CHAT_ROOM_JOINED, chatRoomKey);
        ref.child(path).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                listener.eventListener(snapshot.getKey(), snapshot.getValue(ChatRoomUser.class));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                listener.eventListener(snapshot.getKey(), snapshot.getValue(ChatRoomUser.class));
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public static void chatRoomListChangedEventListener(String userKey, ChatRoomListAdapter chatRoomListAdapter) {
        ref.child(makePath(USER_JOINED, userKey)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatRoomMeta chatRoomMeta = snapshot.getChildren().iterator().next().getValue(ChatRoomMeta.class);
                chatRoomListAdapter.addChatRoom(snapshot.getKey(), chatRoomMeta.getPictureURL(), chatRoomMeta.getName());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatRoomMeta chatRoomMeta = snapshot.getChildren().iterator().next().getValue(ChatRoomMeta.class);
                chatRoomListAdapter.changeChatRoom(snapshot.getKey(), chatRoomMeta.getPictureURL(), chatRoomMeta.getName());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                chatRoomListAdapter.removeChatRoom(snapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void getChatRoomMeta(String chatRoomKey, IEventListener<ChatRoomMeta> listener) {
        ref.child(makePath(CHAT_ROOMS,chatRoomKey,CHAT_ROOM_META)).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                listener.eventListener(dataSnapshot.getValue(ChatRoomMeta.class));
            } else {
                Log.e("FRD", "Can not get meta data of the" + chatRoomKey);
            }
        });
    }

    @NonNull
    private static String makePath(@NonNull String... strings) {
        StringBuilder ret = new StringBuilder();
        for (String str : strings) {
            ret.append("/").append(str);
        }

        return ret.toString();
    }

    public static void removeEventListenerBindOnThis() {
        for (Pair<String, ChildEventListener> i : eventListeners) {
            ref.child(i.first).removeEventListener(i.second);
        }
        eventListeners.clear();
    }
}
