package com.yang.download;

import java.io.Serializable;

/**
 * 总的一个 任务的下载信息
 * @author Administrator
 *
 */
public class LoadInfo implements Serializable {
	
	 /**
	 * 
	 */
	private static final long serialVersionUID = 3993995987454632185L;
	public int fileSize;// 文件大小
     private int complete;// 完成度
     private String urlstring;// 下载器标识
     private int position;//任务编号
     private String localPath;//存放路径
     private String name;//文件名称
  
	public LoadInfo() {
             super();
             // TODO Auto-generated constructor stub
     }
     /**
      * 
      * @param fileSize 文件大小
      * @param complete 完成度
      * @param urlstring 下载地址
      * @param localPath 存放路径
      * @param name 文件名称
      */
     public LoadInfo(int fileSize, int complete, String urlstring,int position,String localPaht,String name) {
             super();
             this.fileSize = fileSize;
             this.complete = complete;
             this.urlstring = urlstring;
             this.position=position;
             
             this.localPath=localPaht;
             this.name=name;
     }
     
     public String getLocalPath() {
		return localPath;
	}
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
				+ ", urlstring=" + urlstring + ", position=" + position
				+ ", localPath=" + localPath + ", name=" + name + "]";
	}
     

}
