package com.xoease.snowstorm.xmp.handler;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Scanner;

import org.eclipse.jetty.server.Request;

import com.xoease.snowstorm.server.SnowAbstractServer;

public class AppConverHandler implements LastHandler {

	@Override
	public Object parse(Object atta, List<Object> streamObj) {
		System.out.println("this is app msg handler ");
		System.out.println(atta);
		//atta instalce from xmpRequest
		return null;
	}

	@Override
	public void onStart(SnowAbstractServer server) {
	 	echo(null);
	}
	public   void echo(String[] args)
	{
		try
		{
		//	System.out.println("请输入要显示的文字:");
 			String old="Snow";
			Font font = new Font("黑体", Font.PLAIN, 15);
			AffineTransform at = new AffineTransform();
			FontRenderContext frc = new FontRenderContext(at, true, true);
			GlyphVector gv = font.createGlyphVector(frc, old); //要显示的文字(文字的字形)
			Shape shape = gv.getOutline(0, 11);
			int weith = 100;
			int height = 13;
			boolean[][] view = new boolean[weith][height];
			for (int i = 0; i < weith; i++)
			{
				for (int j = 0; j < height; j++)
				{
					if (shape.contains(i, j))
					{
						view[i][j] = true;
					} else
					{
						view[i][j] = false;
					}
				}
			}
		//	System.out.println("转换后的字符文字:");
			for (int j = 0; j < height; j++)
			{
				for (int i = 0; i < weith; i++)
				{
					if (view[i][j])
					{
						System.out.print("#");//替换成你喜欢的图案
					} else
					{
						System.out.print(" ");
					}
				}
				System.out.println();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
