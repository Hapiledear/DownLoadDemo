package com.example.downloadtest;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	
	private Button btn_download;
	private Button btn_many_download;
	private  ArrayList<String> urls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		  //实际上应该传递完整的地址
		 urls=new ArrayList<String>();
		 urls.add("My2048.apk");
		
		btn_download=(Button) findViewById(R.id.btn_downlaod);
		btn_download.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				 Bundle bundle=new Bundle();
				 bundle.putStringArrayList("urls", urls);
				//启动server执行下载
				Intent intent=new Intent();
				intent.putExtras(bundle);
				
				intent.setClass(MainActivity.this, DownloadServe.class);
				startService(intent);
			}
		});
		
		btn_many_download=(Button) findViewById(R.id.btn_many_downlaod);
		btn_many_download.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			
				 urls.add("My2049.apk");
				 urls.add("My2047.apk");
				 
				 Bundle bundle=new Bundle();
				 bundle.putStringArrayList("urls", urls);
				 
				 Intent intent=new Intent();
				 intent.putExtras(bundle);
				 
			 	intent.setClass(MainActivity.this, DownloadServe.class);
			   	startService(intent);
			}
		});
		
	}
}
