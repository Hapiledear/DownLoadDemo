package com.yang.download;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;












import com.example.downloadtest.R;
import com.example.downloadtest.R.drawable;
import com.example.downloadtest.R.id;
import com.example.downloadtest.R.layout;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
	    	   int postion=intent.getIntExtra("postion", 0);
	    	   String url=intent.getStringExtra("url");
	           Log.e("通知下载栏", "点击事件");
	           if (downloaders.get(URL+urls.get(postion)).getState()==Downloader.DOWNLOADING) {
				Log.e("通知下载栏", "暂停任务"+postion);
				pauseDownload(postion);
			}else{
				Log.e("通知下载栏", "继续任务"+postion);
				startDownload(postion);
			}
	       }
	   }

	
	 String TAG="DownloadServe";
	 ArrayList<String> urls;//多任务接口
	 private static final int MAX_DOWNLOADER=1;
	 
	 
//	private static final String File_Name="My2048.apk";
	// 固定下载的资源路径，这里可以设置网络上的地址
    private static final String URL = "http://192.168.199.129:8080/MyWebServer/";
    // 固定存放下载的音乐的路径：SD卡目录下
    private static final String SD_PATH = "/mnt/sdcard/updateApkFile/";
    //暂停队列，存放暂停的任务
    private Map<String, Downloader> pauses=new HashMap<String, Downloader>();
    // 存放各个下载器，相当于一个下载队列
    private Map<String, Downloader> downloaders = new HashMap<String, Downloader>();
    // 存放与下载器对应的进度条
    private Map<String, Notification> ProgressBars = new HashMap<String, Notification>();
    private Map<Integer,Integer> progress=new HashMap<Integer, Integer>();//进度条累加,与通知栏id有关
	
	private NotificationManager mNotificationManager;
	//private int progessNum=0;//进度条累加
	private int OkTimes=0;//完成任务次数统计,同时也是计算通知栏id的重要基数

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
		
		 urls=new ArrayList<String>();
		
		//注册广播监听
	    MyBroadCast receiver = new MyBroadCast();
	    IntentFilter filter=new IntentFilter("a");
	    registerReceiver(receiver, filter);
	       
	   //  download.start();
		
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		urls.addAll(intent.getStringArrayListExtra("urls"));
		
		System.out.println("接受了数据 url--->"+urls);
	
		download.start();
		
		 return super.onStartCommand(intent, flags, startId);
	};
	Thread download=new Thread(new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//每次只允许一个下载任务
			for (int i = 0; i < urls.size(); ) {
				if (downloaders.size()<MAX_DOWNLOADER) {//控制并发下载数
					startDownload(i);
					i++;
				}
			}
		}
	});
	private void startDownload(int position) {
		// TODO Auto-generated method stub
		String urlstr = URL +urls.get(position);
		String localfile = SD_PATH + urls.get(position);
		
//		String urlstr = URL +File_Name;
//		String localfile = SD_PATH + File_Name;
		int threadcount = 4;
		// 初始化一个downloader下载器
		Downloader downloader = downloaders.get(urlstr);
		if (downloader == null) {
			downloader = new Downloader(urlstr, localfile, threadcount, this,
					mHandler,position,urls.get(position));
			downloaders.put(urlstr, downloader);
		}
		if (downloader.isdownloading())
			return;
		// 得到下载信息类的个数组成集合
		LoadInfo loadInfo = downloader.getDownloaderInfors();
		// 显示进度信息
		creatNotification(loadInfo, urlstr,position);
		// 调用方法开始下载
		downloader.download(); 
	}
	/**
     * 响应暂停下载按钮的点击事件
     */
    public void pauseDownload(int postion) {
            String urlstr = URL + urls.get(postion);
            downloaders.get(urlstr).pause();
    }
	private void creatNotification(LoadInfo loadInfo, String url,int postion) {
		
		int id=postion%MAX_DOWNLOADER+1;
		
		mNotificationManager = (NotificationManager) getSystemService(  
		            android.content.Context.NOTIFICATION_SERVICE);  
		Notification mNotification = ProgressBars.get(url);
		if (mNotification == null) {
		//notification初始化，不可少
		mNotification = new Notification();  
		mNotification.icon=R.drawable.ic_launcher;
		mNotification.when=System.currentTimeMillis();
		
		
		//点击的事件处理 ,广播通信方式
        Intent buttonIntent = new Intent("a");  
        buttonIntent.putExtra("postion", postion);
        buttonIntent.putExtra("url", url);
        //这里加了广播，所及INTENT的必须用getBroadcast方法  
        PendingIntent pendingIntent= PendingIntent.getBroadcast(this, id, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);  
		mNotification.contentIntent=pendingIntent;//绑定到该通知上去
        
          RemoteViews mRemoteViews =new RemoteViews(this.getPackageName(), R.layout.remote_view_layout);
          mRemoteViews.setImageViewResource(R.id.image	, R.drawable.ic_launcher);
          mRemoteViews.setTextViewText(R.id.text, "正在下载第"+(postion+1)+"个文件");
          mRemoteViews.setProgressBar(R.id.progress_horizontal, loadInfo.getFileSize(),loadInfo.getComplete(), false);
	
			System.out.println(loadInfo.getFileSize() + "--"
					+ loadInfo.getComplete());
			
			mNotification.contentView=mRemoteViews;
			mNotification.flags=Notification.FLAG_NO_CLEAR;
			mNotificationManager.notify(id, mNotification);
			
			ProgressBars.put(url, mNotification);
			progress.put(id, 0);
			
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
                            int position=msg.arg2;
                            
                            Downloader downloader = downloaders.get(url);
                            LoadInfo loadInfo = downloader.getDownloaderInfors();
                            int filesize=loadInfo.getFileSize();
                          
                          
                            Notification mNotification = ProgressBars.get(url);
                            
                            int id=position%MAX_DOWNLOADER+1;
                            
                            int progessNum=progress.get(id);
                            
                            Intent intent=new Intent();
                            intent.setAction("DownloadInfo");
                            
                            Bundle bundle=new Bundle();
                            bundle.putSerializable("loadInfo", loadInfo);
                            intent.putExtras(bundle);
                            sendBroadcast(intent);
                            
                            if (mNotification != null) {
                                    // 设置进度条按读取的length长度更新
                            	progessNum+=length;
                            	progress.put(id, progessNum);//提交修改后的值
                            	RemoteViews mRemoteViews=mNotification.contentView;
                            	Log.v(TAG, id+"下载进度:"+progessNum);
                            	
                            	mRemoteViews.setProgressBar(R.id.progress_horizontal, filesize, progessNum, false);
            					mNotificationManager.notify(id,mNotification);
                                    if (progessNum>=filesize) {
                                    	OkTimes+=1;
                                    	Log.v(TAG, OkTimes+"下载完成");
                                    	mRemoteViews.setTextViewText(R.id.text, "下载完成");
                     	    			mNotification.flags=Notification.FLAG_AUTO_CANCEL;
                     	    			mNotificationManager.notify(id, mNotification);
                     	    			
                     	    			 downloaders.get(url).delete(url);//清空数据库中的数据
                                         downloaders.get(url).reset();//重置下载状态
                                         downloaders.remove(url);
                                         
//                                         progessNum=0;
//                                         progress.put(id, progessNum);
                                         
                                         if (OkTimes==urls.size()) {
                                              OkTimes=0;
                                        	  onDestroy();
										}
                     	    			
									}
                            }
                    }
            }
    };
    public void onDestroy() {
    	Log.v(TAG, "service结束");
		//mNotificationManager.cancelAll();
    };
}
