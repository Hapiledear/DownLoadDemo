package com.example.downloadtest;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btn_download=(Button) findViewById(R.id.btn_downlaod);
		btn_download.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//启动server执行下载
				Intent intent=new Intent();
				intent.setClass(MainActivity.this, DownloadServe.class);
				startService(intent);
			}
		});
		
	}
}
