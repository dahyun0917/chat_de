package com.example.chat_de;

import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.chat_de.databinding.ItemElementCenterSystemBinding;
import com.example.chat_de.databinding.ItemElementLeftFileBinding;
import com.example.chat_de.databinding.ItemElementLeftImageBinding;
import com.example.chat_de.databinding.ItemElementLeftTextBinding;
import com.example.chat_de.databinding.ItemElementLeftVideoBinding;
import com.example.chat_de.databinding.ItemElementLoadingBinding;
import com.example.chat_de.databinding.ItemElementRightFileBinding;
import com.example.chat_de.databinding.ItemElementRightImageBinding;
import com.example.chat_de.databinding.ItemElementRightTextBinding;
import com.example.chat_de.databinding.ItemElementRightVideoBinding;
import com.example.chat_de.datas.Chat;
import com.example.chat_de.datas.ChatRoomUser;
import com.example.chat_de.datas.IndexDeque;
import com.example.chat_de.datas.ViewType;
import com.google.android.exoplayer2.C;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

public class RoomElementAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private IndexDeque<Chat> myDataList;
    private HashMap<String, ChatRoomUser> myUserList;
    private ChatRoomUser myCurrentUser;


    public RoomElementAdapter(IndexDeque<Chat> dataList,HashMap<String, ChatRoomUser> chatRoomUser, ChatRoomUser currentUser){
        myUserList = chatRoomUser;
        myDataList = dataList;
        myCurrentUser = currentUser;
    }

    public void setCurrentUser(ChatRoomUser chatRoomUser) {
        myCurrentUser = chatRoomUser;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        switch(viewType) {
            case ViewType.LOADING:
                view = inflater.inflate(R.layout.item_element_loading,parent,false);
                return new LoadingViewHolder(view);
            case ViewType.CENTER_CONTENT:
                view = inflater.inflate(R.layout.item_element_center_system,parent,false);
                return new CenterViewHolder(view);
            case ViewType.LEFT_CONTENT_TEXT:
                view = inflater.inflate(R.layout.item_element_left_text,parent,false);
                return new LeftTextViewHolder(view);
            case ViewType.LEFT_CONTENT_IMAGE:
                view = inflater.inflate(R.layout.item_element_left_image,parent,false);
                return new LeftImageViewHolder(view);
            case ViewType.LEFT_CONTENT_VIDEO:
                view = inflater.inflate(R.layout.item_element_left_video,parent,false);
                return new LeftVideoViewHolder(view);
            case ViewType.LEFT_CONTENT_FILE:
                view = inflater.inflate(R.layout.item_element_left_file,parent,false);
                return new LeftFileViewHolder(view);
            case ViewType.RIGHT_CONTENT_TEXT:
                view = inflater.inflate(R.layout.item_element_right_text,parent,false);
                return new RightTextViewHolder(view);
            case ViewType.RIGHT_CONTENT_IMAGE:
                view = inflater.inflate(R.layout.item_element_right_image,parent,false);
                return new RightImageViewHolder(view);
            case ViewType.RIGHT_CONTENT_VIDEO:
                view = inflater.inflate(R.layout.item_element_right_video,parent,false);
                return new RightVideoViewHolder(view);
            case ViewType.RIGHT_CONTENT_FILE:
                view = inflater.inflate(R.layout.item_element_right_file,parent,false);
                return new RightFileViewHolder(view);
            default:
                Log.e("VIEW_TYPE", "ViewType must be -1 to 4");
                view = inflater.inflate(R.layout.item_element_right_text,parent,false);
                return new RightTextViewHolder(view);
        }


    }

    // 실제 각 뷰 홀더에 데이터를 연결해주는 함수
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        //각 xml의 데이터 set
        if(viewHolder instanceof LoadingViewHolder){
            showLoadingView((LoadingViewHolder)viewHolder,position);
        }
        else {
            Chat item = myDataList.get(position);

            //각 xml의 데이터 set
            if(viewHolder instanceof CenterViewHolder){
                ((CenterViewHolder) viewHolder).bind(item);
            } else if(viewHolder instanceof LeftTextViewHolder){
                ((LeftTextViewHolder) viewHolder).bind(item);
            } else if(viewHolder instanceof LeftImageViewHolder){
                ((LeftImageViewHolder) viewHolder).bind(item);
            } else if(viewHolder instanceof LeftVideoViewHolder){
                ((LeftVideoViewHolder) viewHolder).bind(item);
            } else if(viewHolder instanceof LeftFileViewHolder){
                ((LeftFileViewHolder) viewHolder).bind(item);
            } else if(viewHolder instanceof RightTextViewHolder){
                ((RightTextViewHolder) viewHolder).bind(item);
            } else if(viewHolder instanceof RightImageViewHolder){
                ((RightImageViewHolder) viewHolder).bind(item);
            } else if(viewHolder instanceof RightVideoViewHolder){
                ((RightVideoViewHolder) viewHolder).bind(item);
            } else if(viewHolder instanceof RightFileViewHolder){
                ((RightFileViewHolder) viewHolder).bind(item);
            }
        }
    }

    public void showLoadingView(LoadingViewHolder holder, int position) {

    }

    // 리사이클러뷰안에서 들어갈 뷰 홀더의 개수
    @Override
    public int getItemCount() {
        return myDataList.size();
    }

    // 위에 3개만 오버라이드가 기본 셋팅임,
    // 이 메소드는 ViewType때문에 오버라이딩 했음(구별할려고)
    @Override
    public int getItemViewType(int position) {

        Chat item = myDataList.get(position);

        if (item == null)
            return ViewType.LOADING;

        //채팅 위치 타입 (왼, 중간, 오) 과 메세지 타입(이미지, 텍스트) 정하기
        if (item.getType().equals(Chat.Type.SYSTEM)) {
            return ViewType.CENTER_CONTENT;
        } else if ((item.getFrom().equals(ChatDB.getCurrentUserKey()))) {
            if ((item.getType().equals(Chat.Type.TEXT)))
                return ViewType.RIGHT_CONTENT_TEXT;
            else if ((item.getType().equals(Chat.Type.IMAGE)))
                return ViewType.RIGHT_CONTENT_IMAGE;
            else if((item.getType().equals(Chat.Type.VIDEO)))
                return ViewType.RIGHT_CONTENT_VIDEO;
            else if((item.getType().equals(Chat.Type.FILE)))
                return ViewType.RIGHT_CONTENT_FILE;
        } else {
            if ((item.getType().equals(Chat.Type.TEXT)))
                return ViewType.LEFT_CONTENT_TEXT;
            else if ((item.getType().equals(Chat.Type.IMAGE)))
                return ViewType.LEFT_CONTENT_IMAGE;
            else if ((item.getType().equals(Chat.Type.VIDEO)))
                return ViewType.LEFT_CONTENT_VIDEO;
            else if((item.getType().equals(Chat.Type.FILE)))
                return ViewType.LEFT_CONTENT_FILE;

        }
        return ViewType.RIGHT_CONTENT_TEXT;
    }

    // "리사이클러뷰에 들어갈 뷰 홀더", 그리고 "그 뷰 홀더에 들어갈 아이템들을 셋팅"
    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        ItemElementLoadingBinding itemElementLoadingBinding;
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            itemElementLoadingBinding = ItemElementLoadingBinding.bind(itemView);
        }
    }


    public class CenterViewHolder extends RecyclerView.ViewHolder{
        ItemElementCenterSystemBinding centerSystemBinding;
        public CenterViewHolder(@NonNull View itemView) {
            super(itemView);
            centerSystemBinding = ItemElementCenterSystemBinding.bind(itemView);
        }
        void bind(Chat item){
            centerSystemBinding.textv.setText(item.getText());
        }
    }

    public class LeftImageViewHolder extends RecyclerView.ViewHolder{
        ItemElementLeftImageBinding leftImageBinding;
        ChatRoomUser chatRoomUser=null; //채팅을 보낸 사람
        String chatDate; //채팅을 보낸 날짜, 시간
        public LeftImageViewHolder(@NonNull View itemView) {
            super(itemView);
            leftImageBinding = ItemElementLeftImageBinding.bind(itemView);
            leftImageBinding.imagevMsg.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Chat item = myDataList.get(getBindingAdapterPosition());
                    SimpleDateFormat passDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
                    String passDate= passDateFormat.format(item.normalDate());

                    if(chatRoomUser==null){
                        Log.e("USERKEY EROOR","해당하는 유저의 키가 현재 채팅방에 존재하지 않습니다.");
                    }
                    else {
                        Intent intent = new Intent(itemView.getContext(), ImageFrameActivity.class);
                        intent.putExtra("fromName", chatRoomUser.getUserMeta().getName());
                        intent.putExtra("passDate", passDate);
                        intent.putExtra("imageView", item.getText());
                        view.getContext().startActivity(intent);
                    }
                }
            });
            leftImageBinding.imgv.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //1대1 채팅방 만들기
                    Intent intent = new Intent(itemView.getContext(), UserProfileActivity.class);
                    if(chatRoomUser==null){
                        Log.e("USERKEY EROOR","해당하는 유저의 키가 현재 채팅방에 존재하지 않습니다.");
                    }
                    else {
                        //선택한 사용자 정보 전송
                        intent.putExtra("userOther", chatRoomUser.getUserMeta());
                        //로그인된 사용자 정보 전송
                        intent.putExtra("userMe", myCurrentUser.getUserMeta());
                    }
                    view.getContext().startActivity(intent);
                }
            });
        }
        void bind(Chat item){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("a hh:mm", Locale.KOREA);
            chatDate = simpleDateFormat.format(item.normalDate());
            for (String i : myUserList.keySet()) {
                if (item.getFrom().equals(myUserList.get(i).getUserMeta().getUserKey())) {
                    chatRoomUser = myUserList.get(i);
                }
            }
            Glide.with(itemView.getContext()).load(item.getText()).thumbnail(Glide.with(itemView.getContext()).load(R.drawable.loading)).into(leftImageBinding.imagevMsg);
            leftImageBinding.textvNicname.setText(chatRoomUser.getUserMeta().getName());
            leftImageBinding.textvTime.setText(chatDate);
            Glide.with(itemView.getContext()).load(chatRoomUser.getUserMeta().getPictureURL()).error(R.drawable.knu_mark_white).into(leftImageBinding.imgv);
        }
    }
    public class LeftVideoViewHolder extends RecyclerView.ViewHolder{
        ItemElementLeftVideoBinding leftVideoBinding;
        ChatRoomUser chatRoomUser=null; //채팅을 보낸 사람
        String chatDate; //채팅을 보낸 날짜, 시간
        public LeftVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            leftVideoBinding = ItemElementLeftVideoBinding.bind(itemView);
            leftVideoBinding.imagevMsg.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Chat item = myDataList.get(getBindingAdapterPosition());
                    SimpleDateFormat passDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
                    String passDate= passDateFormat.format(item.normalDate());

                    if(chatRoomUser==null){
                        Log.e("USERKEY EROOR","해당하는 유저의 키가 현재 채팅방에 존재하지 않습니다.");
                    }
                    else {
                        Intent intent = new Intent(itemView.getContext(), VideoFrameActivity.class);
                        intent.putExtra("fromName", chatRoomUser.getUserMeta().getName());
                        intent.putExtra("passDate", passDate);
                        intent.putExtra("imageView", item.getText());
                        view.getContext().startActivity(intent);
                    }
                }
            });
            leftVideoBinding.imgv.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //1대1 채팅방 만들기
                    Intent intent = new Intent(itemView.getContext(), UserProfileActivity.class);
                    if(chatRoomUser==null){
                        Log.e("USERKEY EROOR","해당하는 유저의 키가 현재 채팅방에 존재하지 않습니다.");
                    }
                    else {
                        //선택한 사용자 정보 전송
                        intent.putExtra("userOther", chatRoomUser.getUserMeta());
                        //로그인된 사용자 정보 전송
                        intent.putExtra("userMe", myCurrentUser.getUserMeta());
                    }
                    view.getContext().startActivity(intent);
                }
            });
        }
        void bind(Chat item){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("a hh:mm", Locale.KOREA);
            chatDate = simpleDateFormat.format(item.normalDate());
            for (String i : myUserList.keySet()) {
                if (item.getFrom().equals(myUserList.get(i).getUserMeta().getUserKey())) {
                    chatRoomUser = myUserList.get(i);
                }
            }
            Glide.with(itemView.getContext()).load(R.drawable.player).override(200,200).centerCrop().thumbnail(Glide.with(itemView.getContext()).load(R.drawable.loading)).into(leftVideoBinding.imagevMsg);
            leftVideoBinding.textvNicname.setText(chatRoomUser.getUserMeta().getName());
            leftVideoBinding.textvTime.setText(chatDate);
            Glide.with(itemView.getContext()).load(chatRoomUser.getUserMeta().getPictureURL()).error(R.drawable.knu_mark_white).into(leftVideoBinding.imgv);
        }
    }
    public class LeftFileViewHolder extends RecyclerView.ViewHolder{
        ItemElementLeftFileBinding leftFileBinding;
        ChatRoomUser chatRoomUser=null; //채팅을 보낸 사람
        String chatDate; //채팅을 보낸 날짜, 시간
        public LeftFileViewHolder(@NonNull View itemView) {
            super(itemView);
            leftFileBinding = ItemElementLeftFileBinding.bind(itemView);
            leftFileBinding.imagevMsg.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Chat item = myDataList.get(getBindingAdapterPosition());

                    if(chatRoomUser==null){
                        Log.e("USERKEY EROOR","해당하는 유저의 키가 현재 채팅방에 존재하지 않습니다.");
                    }
                    else {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getText()));
                        view.getContext().startActivity(intent);
                    }
                }
            });
            leftFileBinding.imgv.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //1대1 채팅방 만들기
                    Intent intent = new Intent(itemView.getContext(), UserProfileActivity.class);
                    if(chatRoomUser==null){
                        Log.e("USERKEY EROOR","해당하는 유저의 키가 현재 채팅방에 존재하지 않습니다.");
                    }
                    else {
                        //선택한 사용자 정보 전송
                        intent.putExtra("otherName", chatRoomUser.getUserMeta().getName());
                        intent.putExtra("otherPictureURL", chatRoomUser.getUserMeta().getPictureURL());
                        intent.putExtra("otherGeneration", chatRoomUser.getUserMeta().getGeneration());
                        intent.putExtra("otherUserKey", chatRoomUser.getUserMeta().getUserKey());
                        intent.putExtra("otherLastReadIndex", chatRoomUser.getLastReadIndex());
                        //로그인된 사용자 정보 전송
                        intent.putExtra("myLastReadIndex", myCurrentUser.getLastReadIndex());
                        intent.putExtra("myName", myCurrentUser.getUserMeta().getName());
                        intent.putExtra("myPictureURL", myCurrentUser.getUserMeta().getPictureURL());
                        intent.putExtra("myGeneration", myCurrentUser.getUserMeta().getGeneration());
                        intent.putExtra("myUserKey", myCurrentUser.getUserMeta().getUserKey());
                    }
                    view.getContext().startActivity(intent);
                }
            });
        }
        void bind(Chat item){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("a hh:mm", Locale.KOREA);
            chatDate = simpleDateFormat.format(item.normalDate());
            for (String i : myUserList.keySet()) {
                if (item.getFrom().equals(myUserList.get(i).getUserMeta().getUserKey())) {
                    chatRoomUser = myUserList.get(i);
                }
            }
            Glide.with(itemView.getContext()).load(R.drawable.file_icon).override(200,200).centerCrop().thumbnail(Glide.with(itemView.getContext()).load(R.drawable.loading)).into(leftFileBinding.imagevMsg);
            leftFileBinding.textvNicname.setText(chatRoomUser.getUserMeta().getName());
            leftFileBinding.textvTime.setText(chatDate);
            Glide.with(itemView.getContext()).load(chatRoomUser.getUserMeta().getPictureURL()).error(R.drawable.knu_mark_white).into(leftFileBinding.imgv);
        }
    }

    public class LeftTextViewHolder extends RecyclerView.ViewHolder{
        ItemElementLeftTextBinding leftTextBinding;
        ChatRoomUser chatRoomUser=null; //채팅을 보낸 사람
        String chatDate; //채팅을 보낸 날짜, 시간

        public LeftTextViewHolder(@NonNull View itemView) {
            super(itemView);
            leftTextBinding = ItemElementLeftTextBinding.bind(itemView);
            leftTextBinding.imgv.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //1대1 채팅방 만들기
                    Intent intent = new Intent(itemView.getContext(), UserProfileActivity.class);
                    if(chatRoomUser==null){
                        Log.e("USERKEY EROOR","해당하는 유저의 키가 현재 채팅방에 존재하지 않습니다.");
                    }
                    else {
                        //선택한 사용자 정보 전송
                        intent.putExtra("userOther", chatRoomUser.getUserMeta());
                        //로그인된 사용자 정보 전송
                        intent.putExtra("userMe", myCurrentUser.getUserMeta());
                    }
                    view.getContext().startActivity(intent);
                }
            });
        }
        void bind(Chat item){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("a hh:mm", Locale.KOREA);
            chatDate = simpleDateFormat.format(item.normalDate());
            for (String i : myUserList.keySet()) {
                if (item.getFrom().equals(myUserList.get(i).getUserMeta().getUserKey())) {
                    chatRoomUser = myUserList.get(i);
                }
            }

            leftTextBinding.textvMsg.setText(item.getText());
            leftTextBinding.textvNicname.setText(chatRoomUser.getUserMeta().getName());
            leftTextBinding.textvTime.setText(chatDate);
            Glide.with(itemView.getContext()).load(chatRoomUser.getUserMeta().getPictureURL()).error(R.drawable.knu_mark_white).into(leftTextBinding.imgv);

        }
    }
    public class RightImageViewHolder extends RecyclerView.ViewHolder{
        ItemElementRightImageBinding rightImageBinding;
        public RightImageViewHolder(@NonNull View itemView) {
            super(itemView);
            rightImageBinding = ItemElementRightImageBinding.bind(itemView);
            rightImageBinding.imagevMsg.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    int pos = getBindingAdapterPosition();
                    Chat item = myDataList.get(pos);
                    SimpleDateFormat passDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
                    String passDate = passDateFormat.format(item.normalDate());
                    Intent intent = new Intent(itemView.getContext(), ImageFrameActivity.class);
                    intent.putExtra("fromName", myCurrentUser.getUserMeta().getName());
                    intent.putExtra("passDate", passDate);
                    intent.putExtra("imageView", item.getText());
                    view.getContext().startActivity(intent);
                }
            });
        }
        void bind(Chat item){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("a hh:mm", Locale.KOREA);
            String chatDate = simpleDateFormat.format(item.normalDate());
            Glide.with(itemView.getContext()).load(item.getText()).thumbnail(Glide.with(itemView.getContext()).load(R.drawable.loading)).into(rightImageBinding.imagevMsg);
            rightImageBinding.textvNicname.setText(myCurrentUser.getUserMeta().getName());
            rightImageBinding.textvTime.setText(chatDate);
        }
    }
    public class RightVideoViewHolder extends RecyclerView.ViewHolder{
        ItemElementRightVideoBinding rightVideoBinding;
        public RightVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            rightVideoBinding = ItemElementRightVideoBinding.bind(itemView);
            rightVideoBinding.imagevMsg.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    int pos = getBindingAdapterPosition();
                    Chat item = myDataList.get(pos);
                    SimpleDateFormat passDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
                    String passDate = passDateFormat.format(item.normalDate());
                    Intent intent = new Intent(itemView.getContext(), VideoFrameActivity.class);
                    intent.putExtra("fromName", myCurrentUser.getUserMeta().getName());
                    intent.putExtra("passDate", passDate);
                    intent.putExtra("imageView", item.getText());
                    view.getContext().startActivity(intent);

                }
            });
        }
        void bind(Chat item){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("a hh:mm", Locale.KOREA);
            String chatDate = simpleDateFormat.format(item.normalDate());
            Glide.with(itemView.getContext()).load(R.drawable.player).centerCrop().thumbnail(Glide.with(itemView.getContext()).load(R.drawable.loading)).into(rightVideoBinding.imagevMsg);
            rightVideoBinding.textvNicname.setText(myCurrentUser.getUserMeta().getName());
            rightVideoBinding.textvTime.setText(chatDate);
        }
    }
    public class RightFileViewHolder extends RecyclerView.ViewHolder{
        ItemElementRightFileBinding rightFileBinding;
        public RightFileViewHolder(@NonNull View itemView) {
            super(itemView);
            rightFileBinding = ItemElementRightFileBinding.bind(itemView);
            rightFileBinding.imagevMsg.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    int pos = getBindingAdapterPosition();
                    Chat item = myDataList.get(pos);

                    /*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getText()));
                    view.getContext().startActivity(intent);*/

                    Intent intent = new Intent(itemView.getContext(), FileFrameActivity.class);
                    intent.putExtra("file", item.getText());
                    view.getContext().startActivity(intent);

                }
            });
        }
        void bind(Chat item){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("a hh:mm", Locale.KOREA);
            String chatDate = simpleDateFormat.format(item.normalDate());
            Glide.with(itemView.getContext()).load(R.drawable.file_icon).thumbnail(Glide.with(itemView.getContext()).load(R.drawable.loading)).into(rightFileBinding.imagevMsg);
            rightFileBinding.textvNicname.setText(myCurrentUser.getUserMeta().getName());
            rightFileBinding.textvTime.setText(chatDate);
        }
    }
    public class RightTextViewHolder extends RecyclerView.ViewHolder{
        ItemElementRightTextBinding rightTextBinding;
        String chatDate; //채팅을 보낸 날짜, 시간

        public RightTextViewHolder(@NonNull View itemView) {
            super(itemView);
            rightTextBinding = ItemElementRightTextBinding.bind(itemView);
        }
        void bind(Chat item){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("a hh:mm", Locale.KOREA);
            chatDate = simpleDateFormat.format(item.normalDate());

            rightTextBinding.textvMsg.setText(item.getText());
            rightTextBinding.textvNicname.setText(myCurrentUser.getUserMeta().getName());
            rightTextBinding.textvTime.setText(chatDate);
        }
    }

}
