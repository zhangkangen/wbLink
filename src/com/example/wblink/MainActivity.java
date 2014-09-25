package com.example.wblink;

import java.util.ArrayList;
import java.util.List;

import com.example.wblink.R.color;
import com.sina.weibo.sdk.api.share.ApiUtils.WeiboInfo;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;

import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private Button button;
	private ListView listView;

	private WeiboAuth mWeiboAuth;
	private Oauth2AccessToken mAccessToken;
	private SsoHandler mSsoHandler;
	private StatusesAPI mStatusesAPI;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mWeiboAuth = new WeiboAuth(this, Constants.APP_KEY,
				Constants.REDIRECT_URL, Constants.SCOPE);

		button = (Button) findViewById(R.id.btn_login);
		listView = (ListView) findViewById(R.id.listView1);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				mAccessToken = getToken();
				if (null == mAccessToken) {
					mSsoHandler = new SsoHandler(MainActivity.this, mWeiboAuth);
					mSsoHandler.authorize(new AuthListener());
				} else {
					Toast.makeText(MainActivity.this, "用户授权已经成功",
							Toast.LENGTH_LONG).show();
				}

				mStatusesAPI = new StatusesAPI(mAccessToken);
				mStatusesAPI.friendsTimeline(0L, 0L, 10, 1, false,
						StatusesAPI.AUTHOR_FILTER_ALL, false, mListener);
			}
		});

		mAccessToken = getToken();
		if (null != mAccessToken) {
			mStatusesAPI = new StatusesAPI(mAccessToken);
			mStatusesAPI.friendsTimeline(0L, 0L, 10, 1, false,
					StatusesAPI.AUTHOR_FILTER_ALL, false, mListener);
		}
	}

	private void bindListView(ListView lv, StatusList statusList) {
		// TODO Auto-generated method stub
		List<Status> list = statusList.statusList;

		
		// lv.setAdapter(adapter);

	}

	/**
	 * 获取 SheardPreferences 中 Token
	 * 
	 * @return
	 */
	public Oauth2AccessToken getToken() {
		mAccessToken = AccessTokenKeeper
				.readAccessToken(getApplicationContext());
		if (mAccessToken.isSessionValid()) {
			return mAccessToken;
		} else {
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id) {
		case R.id.action_settings:
			return true;
		case R.id.action_search:
			return true;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * 授权回调
	 * 
	 * @author Administrator
	 *
	 */
	class AuthListener implements WeiboAuthListener {

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			Toast.makeText(MainActivity.this, "取消", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onComplete(Bundle arg0) {
			// TODO Auto-generated method stub
			// 从Bundle中解析Token
			mAccessToken = Oauth2AccessToken.parseAccessToken(arg0);
			if (mAccessToken.isSessionValid()) {
				// 保存Token到SharedPreferences
				AccessTokenKeeper.writeAccessToken(MainActivity.this,
						mAccessToken);
				Toast.makeText(MainActivity.this, "OK", Toast.LENGTH_LONG)
						.show();
			} else {
				String code = arg0.getString("code");
				String message = "fail";
				if (!TextUtils.isEmpty(code)) {
					message = message + "\nObtained the code:" + code;
				}
				Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG)
						.show();
			}
		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			// TODO Auto-generated method stub
			Toast.makeText(MainActivity.this,
					"Auth exception : " + arg0.getMessage(), Toast.LENGTH_LONG)
					.show();
		}

	}

	/**
	 * 微博 OpenAPI 回调接口
	 */
	private RequestListener mListener = new RequestListener() {

		@Override
		public void onWeiboException(WeiboException arg0) {
			// TODO Auto-generated method stub
			ErrorInfo info = ErrorInfo.parse(arg0.getMessage());
			Toast.makeText(MainActivity.this, info.toString(),
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onComplete(String response) {
			// TODO Auto-generated method stub
			if (!TextUtils.isEmpty(response)) {
				if (response.startsWith("{\"statuses\"")) {// 获取到的微博的开头
					StatusList statusList = StatusList.parse(response);
					if (statusList != null && statusList.total_number > 0) {
						Toast.makeText(MainActivity.this,
								"获取微博成功，条数：" + statusList.statusList.size(),
								Toast.LENGTH_LONG).show();

						bindListView(listView, statusList);
					}
				} else if (response.startsWith("{\"created_at\"")) {// 发送微博的开头
					Status status = Status.parse(response);
					Toast.makeText(MainActivity.this,
							"发送一条微博成功,id = " + status.id, Toast.LENGTH_LONG)
							.show();
				} else {
					Toast.makeText(MainActivity.this, response,
							Toast.LENGTH_LONG).show();
				}
			}
		}
	};
}
