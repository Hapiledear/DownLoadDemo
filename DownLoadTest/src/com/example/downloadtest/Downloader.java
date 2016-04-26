package com.example.downloadtest;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Downloader {

	private String urlstr;// 下载的地址
	private String localfile;// 保存路径
	private int threadcount;// 线程数
	private Handler mHandler;// 消息处理器
	private DownloadDao dao;// 工具类
	private int fileSize;// 所要下载的文件的大小
	private List<DownloadInfo> infos;// 存放下载信息类的集合
	public static final int INIT = 1;// 定义三种下载的状态：初始化状态，正在下载状态，暂停状态
	public static final int DOWNLOADING = 2;
	public static final int PAUSE = 3;
	private int state = INIT;
	private int position;//下载器的任务编号
	
	
	public int getState(){
		return state;
	}

	/**
	 * 
	 * @param urlstr
	 *            下载地址
	 * @param localfile
	 *            保存路径
	 * @param threadcount
	 *            线程数
	 * @param context
	 *            上下文对象
	 * @param mHandler
	 *            消息处理器
	 */
	public Downloader(String urlstr, String localfile, int threadcount,
			Context context, Handler mHandler,int position) {
		this.urlstr = urlstr;
		this.localfile = localfile;
		this.threadcount = threadcount;
		this.mHandler = mHandler;
		this.position=position;
		dao = new DownloadDao(context);
	}

	/**
	 * 判断是否正在下载
	 */
	public boolean isdownloading() {
		return state == DOWNLOADING;
	}

	/**
	 * 得到downloader里的信息 首先进行判断是否是第一次下载，如果是第一次就要进行初始化，并将下载器的信息保存到数据库中
	 * 如果不是第一次下载，那就要从数据库中读出之前下载的信息（起始位置，结束为止，文件大小等），并将下载信息返回给下载器
	 * 
	 * <h1>error:第一次初始化时，需要联网，因此不能在主线程中掉用该函数，否则无法正确获取fileSize，而导致以后的操作都会出错
	 */
	public LoadInfo getDownloaderInfors() {
		if (isFirst(urlstr)) {
			init();
			int range = fileSize / threadcount;
			infos = new ArrayList<DownloadInfo>();
			for (int i = 0; i < threadcount - 1; i++) {
				DownloadInfo info = new DownloadInfo(i, i * range, (i + 1)
						* range - 1, 0, urlstr);
				infos.add(info);
			}
			// range只取到整数部分，余下的部分做如下处理
			DownloadInfo info = new DownloadInfo(threadcount - 1,
					(threadcount - 1) * range, fileSize - 1, 0, urlstr);
			infos.add(info);
			// 保存infos中的数据到数据库
			dao.saveInfos(infos);
			// 创建一个LoadInfo对象记载下载器的具体信息
			LoadInfo loadInfo = new LoadInfo(fileSize, 0, urlstr,position);
			return loadInfo;
		} else {
			// 得到数据库中已有的urlstr的下载器的具体信息
			infos = dao.getInfos(urlstr);
			Log.v("TAG", "not isFirst size=" + infos.size());
			int size = 0;
			int compeleteSize = 0;
			for (DownloadInfo info : infos) {
				compeleteSize += info.getCompeleteSize();
				size += info.getEndPos() - info.getStartPos() + 1;
			}
			return new LoadInfo(size, compeleteSize, urlstr,position);
		}
	}

	/**
	 * 初始化
	 */
	private void init() {
		try {
			// get方法连接服务器
			URL url = new URL(urlstr);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setConnectTimeout(5000);
			connection.setRequestMethod("GET");
			fileSize = connection.getContentLength();// 获取文件大小
			System.out.println("fileSize---->"+fileSize);
			File file = new File(localfile);// 创建文件夹
			if (!file.exists()) {
				file.createNewFile();
			}
			// 本地访问文件
			RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
			accessFile.setLength(fileSize);
			accessFile.close();
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isFirst(String urlstr2) {
		// TODO Auto-generated method stub
		return dao.isHasInfors(urlstr);
	}

	/**
	 ** 利用线程开始下载数据
	 */
	public void download() {
		if (infos != null) {
			if (state == DOWNLOADING)
				return;
			state = DOWNLOADING;
			for (DownloadInfo info : infos) {
				new MyThread(info.getThreadId(), info.getStartPos(),
						info.getEndPos(), info.getCompeleteSize(),
						info.getUrl()).start();
			}
		}
	}

	public class MyThread extends Thread {
		private int threadId;
		private int startPos;
		private int endPos;
		private int compeleteSize;
		private String urlstr;

		public MyThread(int threadId, int startPos, int endPos,
				int compeleteSize, String urlstr) {
			this.threadId = threadId;
			this.startPos = startPos;
			this.endPos = endPos;
			this.compeleteSize = compeleteSize;
			this.urlstr = urlstr;
		}

		@Override
		public void run() {
			HttpURLConnection connection = null;
			RandomAccessFile randomAccessFile = null;
			InputStream is = null;
			try {
				URL url = new URL(urlstr);
				connection = (HttpURLConnection) url.openConnection();
				
//             	connection.setDoInput(true);          
//				connection.setDoOutput(true);      
//				connection.setUseCaches(false);
				connection.setConnectTimeout(5000);
				
				connection.setRequestMethod("GET");
				// 设置范围，格式为Range：bytes x-y;
				connection.setRequestProperty("Range", "bytes="+ (startPos + compeleteSize) + "-" + endPos);
//				connection.connect();
//			    int code=connection.getResponseCode();
				
				randomAccessFile = new RandomAccessFile(localfile, "rwd");
				randomAccessFile.seek(startPos + compeleteSize);
				// 将要下载的文件写到保存在保存路径下的文件中
				is = connection.getInputStream();
				byte[] buffer = new byte[4096];
				int length = -1;
				while ((length = is.read(buffer)) != -1) {
					randomAccessFile.write(buffer, 0, length);
					compeleteSize += length;
					// 更新数据库中的下载信息
					dao.updataInfos(threadId, compeleteSize, urlstr);
					// 用消息将下载信息传给进度条，对进度条进行更新
					Message message = Message.obtain();
					message.what = 1;
					message.obj = urlstr;
					message.arg1 = length;
					message.arg2=position;
					
					mHandler.sendMessage(message);
					sleep(1000);
					if (state == PAUSE) {
						return;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					is.close();
					randomAccessFile.close();
					connection.disconnect();
					dao.closeDb();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}
	 // 删除数据库中urlstr对应的下载器信息
    public void delete(String urlstr) {
            dao.delete(urlstr);
    }

    // 设置暂停
    public void pause() {
            state = PAUSE;
    }

    // 重置下载状态
    public void reset() {
            state = INIT;
    }

}
