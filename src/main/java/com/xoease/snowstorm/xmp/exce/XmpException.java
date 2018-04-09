package com.xoease.snowstorm.xmp.exce;

public class XmpException extends Exception {
	private int code=0;

	public int getCode() {
		return code;
	}
	public XmpException(){
		
	}
	public XmpException(int code){
		this.code=code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	
}
