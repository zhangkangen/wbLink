package com.example.wblink;

public interface Constants {
	public static final String APP_KEY = "2725214515";//应用程序的appkey
	
	public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";//回调页
	
	public static final String SCOPE = 
            "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";
}
