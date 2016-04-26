package com.example.downloadtest;

/**
 * 总的一个 任务的下载信息
 * @author Administrator
 *
 */
public class LoadInfo {
	
	 public int fileSize;// 文件大小
     private int complete;// 完成度
     private String urlstring;// 下载器标识
     private int position;//下载器的任务编号
  
	public LoadInfo() {
             super();
             // TODO Auto-generated constructor stub
     }
     /**
      * 
      * @param fileSize 文件大小
      * @param complete 完成度
      * @param urlstring 下载地址
      */
     public LoadInfo(int fileSize, int complete, String urlstring,int position) {
             super();
             this.fileSize = fileSize;
             this.complete = complete;
             this.urlstring = urlstring;
             this.position=position;
     }
     public int getPosition() {
 		return position;
 	}
 	public void setPosition(int position) {
 		this.position = position;
 	}
	public int getFileSize() {
		return fileSize;
	}
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
	public int getComplete() {
		return complete;
	}
	public void setComplete(int complete) {
		this.complete = complete;
	}
	public String getUrlstring() {
		return urlstring;
	}
	public void setUrlstring(String urlstring) {
		this.urlstring = urlstring;
	}
	@Override
	public String toString() {
		return "LoadInfo [fileSize=" + fileSize + ", complete=" + complete
				+ ", urlstring=" + urlstring + ", position=" + position + "]";
	}
     

}
