package com.example.charityshareapp;

import java.io.Serializable;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface STORE {

    String IP = "192.168.100.10";
//    String IP = "54.202.36.154";

    String BASE_URL_API = "http://" + IP + ":8000/api/";
    String BASE_URL_IMG = "http://" + IP + ":8000/images/";

    String SP = "com.example.charityshareapp.SP";

    interface API_Client {

        // USER

        @FormUrlEncoded
        @POST("user/register")
        Call<ResponseModel>
        register(@Field("email") String email,
                 @Field("password") String password,
                 @Field("name") String name,
                 @Field("mobile") String mobile,
                 @Field("type") String type);

        @FormUrlEncoded
        @POST("user/login")
        Call<ResponseModel>
        login(@Field("email") String email,
              @Field("password") String password);

        @GET("user/list")
        Call<RM_Users>
        user_list();

        @FormUrlEncoded
        @POST("user/update")
        Call<ResponseModel>
        user_update(@Field("email")              String email,
                    @Field("password")           String password,
                    @Field("name")               String name,
                    @Field("mobile")             String mobile,
                    @Field("type")               String type,
                    @Field("account_active")     String account_active);

        @FormUrlEncoded
        @POST("user/delete")
        Call<ResponseModel>
        user_delete(@Field("email") String email);

        @FormUrlEncoded
        @POST("user/reset")
        Call<ResponseModel>
        user_reset(@Field("email") String email);

        // POST

        @Multipart
        @POST("post/add")
        Call<ResponseModel>
        post_add(@Part("poster")      RequestBody poster,
                 @Part("title")       RequestBody title,
                 @Part("description") RequestBody description,
                 @Part("filename")    RequestBody filename,
                 @Part                MultipartBody.Part image);


        @GET("post/list")
        Call<RM_Posts>
        post_list();

        @FormUrlEncoded
        @POST("post/delete")
        Call<ResponseModel>
        post_delete(@Field("id") String id);

        // COMMENT

        @FormUrlEncoded
        @POST("comment/add")
        Call<ResponseModel>
        comment_add(@Field("post_id") String post_id,
                    @Field("poster")  String poster,
                    @Field("content") String content);

        @FormUrlEncoded
        @POST("comment/list")
        Call<RM_Comments>
        comment_list(@Field("post_id") String post_id);

        // NOTIFICATION

        @FormUrlEncoded
        @POST("notification/send")
        Call<ResponseModel>
        notification_send(@Field("title")  String title,
                         @Field("message") String message);
    }

    class ResponseModel {
        int status;
        String message;

        int getStatus() {
            return status;
        }
        String getMessage() {
            return message;
        }
    }

    class User {
        String id;
        String type;
        String email;
        String name;
        String mobile;
        String account_active;
    }

    class RM_Users extends ResponseModel {
        List<User> users;
        public List<User> getUsers() { return users; }
    }

    class Post implements Serializable {
        String id;
        String poster_name;
        String title;
        String description;
        String filename;
        String created_at;
    }

    class RM_Posts extends ResponseModel {
        List<Post> posts;
        public List<Post> getPosts() { return posts; }
    }

    class Comment implements Serializable {
        String id;
        String post_id;
        String poster;
        String poster_name;
        String content;
        String created_at;
    }

    class RM_Comments extends ResponseModel {
        List<Comment> comments;
        public List<Comment> getComments() { return comments; }
    }

}
