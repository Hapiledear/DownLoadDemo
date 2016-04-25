package com.example.downloadtest;

import java.util.HashMap;
import java.util.Map;







import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.Toast;

public class DownloadServe extends Service {
	
	class MyBroadCast extends BroadcastReceiver{
	       @Override
	       public void onReceive(Context context, Intent intent)
	       {
	             
	           Log.e("通知下载栏", "点击事件");
	           if (downloaders.get(URL+File_Name).getState()==Downloader.DOWNLOADING) {
				Log.e("通知下载栏", "暂停任务");
				pauseDownload();
			}else{
				Log.e("通知下载栏", "继续任务");
				startDownload();
			}
	       }
	   }

	
	 String TAG="DownloadServe";
	
	private static final String File_Name="My2048.apk";
	// 固定下载的资源路径，这里可以设置网络上的地址
    private static final String URL = "http://192.168.199.129:8080/MyWebServer/";
    // 固定存放下载的音乐的路径：SD卡目录下
    private static final String SD_PATH = "/mnt/sdcard/updateApkFile/";
    // 存放各个下载器
    private Map<String, Downloader> downloaders = new HashMap<String, Downloader>();
    // 存放与下载器对应的进度条
    private Map<String, Notification> ProgressBars = new HashMap<String, Notification>();
	
	private NotificationManager mNotificationManager;
	private int progessNum=0;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.v(TAG, "DownloadServe启动");
		
		//注册广播监听
	    MyBroadCast receiver = new MyBroadCast();
	    IntentFilter filter=new IntentFilter("a");
	    registerReceiver(receiver, filter);
	       
		download.start();
	}
	Thread download=new Thread(new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			startDownload();
		}
	});
	private void startDownload() {
		// TODO Auto-generated method stub
		String urlstr = URL + File_Name;
		String localfile = SD_PATH + File_Name;
		int threadcount = 4;
		// 初始化一个downloader下载器
		Downloader downloader = downloaders.get(urlstr);
		if (downloader == null) {
			downloader = new Downloader(urlstr, localfile, threadcount, this,
					mHandler);
			downloaders.put(urlstr, downloader);
		}
		if (downloader.isdownloading())
			return;
		// 得到下载信息类的个数组成集合
		LoadInfo loadInfo = downloader.getDownloaderInfors();
		// 显示进度信息
		creatNotification(loadInfo, urlstr);
		// 调用方法开始下载
		downloader.download();
	}
	/**
     * 响应暂停下载按钮的点击事件
     */
    public void pauseDownload() {
            String urlstr = URL + File_Name;
            downloaders.get(urlstr).pause();
    }
	private void creatNotification(LoadInfo loadInfo, String url) {
		mNotificationManager = (NotificationManager) getSystemService(  
		            android.content.Context.NOTIFICATION_SERVICE);  
		Notification mNotification = ProgressBars.get(url);
		if (mNotification == null) {
		//notification初始化，不可少
		mNotification = new Notification();  
		mNotification.icon=R.drawable.ic_launcher;
		mNotification.when=System.currentTimeMillis();
		
		
		//点击的事件处理  
        Intent buttonIntent = new Intent("a");  
        //这里加了广播，所及INTENT的必须用getBroadcast方法  
        PendingIntent pendingIntent= PendingIntent.getBroadcast(this, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);  
		mNotification.contentIntent=pendingIntent;//绑定到该通知上去
        
          RemoteViews mRemoteViews =new RemoteViews(this.getPackageName(), R.layout.remote_view_layout);
          mRemoteViews.setImageViewResource(R.id.image	, R.drawable.ic_launcher);
          mRemoteViews.setTextViewText(R.id.text, "正在下载...");
          mRemoteViews.setProgressBar(R.id.progress_horizontal, loadInfo.getFileSize(),loadInfo.getComplete(), false);
	
			System.out.println(loadInfo.getFileSize() + "--"
					+ loadInfo.getComplete());
			
			mNotification.contentView=mRemoteViews;
			mNotification.flags=Notification.FLAG_NO_CLEAR;
			mNotificationManager.notify(1, mNotification);
			
			ProgressBars.put(url, mNotification);
			
		}
	}
	 /**
     ** 利用消息处理机制适时更新进度条 
     */
	
    private Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                            String url = (String) msg.obj;
                            int length = msg.arg1;
                            int filesize=msg.arg2;
                            Notification mNotification = ProgressBars.get(url);
                            if (mNotification != null) {
                                    // 设置进度条按读取的length长度更新
                            	progessNum+=length;
                            	RemoteViews mRemoteViews=mNotification.contentView;
                            	Log.v(TAG, "下载进度:"+progessNum);
                            	mRemoteViews.setProgressBar(R.id.progress_horizontal, filesize, progessNum, false);
            					mNotificationManager.notify(1, mNotification);
                                    if (progessNum>=filesize) {
                                    	Log.v(TAG, "下载完成");
                                    	mRemoteViews.setTextViewText(R.id.text, "下载完成");
                     	    			mNotification.flags=Notification.FLAG_AUTO_CANCEL;
                     	    			mNotificationManager.notify(1, mNotification);
                     	    			
                     	    			 downloaders.get(url).delete(url);
                                         downloaders.get(url).reset();
                                         downloaders.remove(url);

                     	    			onDestroy();
									}
                            }
                    }
            }
    };
    public void onDestroy() {
    	Log.v(TAG, "service结束");
    	
		mNotificationManager.cancel(1);
    };
}
