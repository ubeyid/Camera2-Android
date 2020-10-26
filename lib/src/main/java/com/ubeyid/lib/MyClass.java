package com.ubeyid.lib;

import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.IOException;



public class MyClass
{
    public  static  void main(String[] args)
    {
        File file=new File("C:\\Users\\selam\\Desktop\\E-Ticaret\\images\\deneme.txt");
        File fileOut=new File("C:\\Users\\selam\\Desktop\\E-Ticaret\\images\\"+ (int) (Math.random() * 1000000) +".txt");
        {
            if(!fileOut.exists())
            {
                try
                {
                    fileOut.createNewFile();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        {
            try
            {


                FileInputStream fileInputStream=new FileInputStream(file);
                FileOutputStream fileOutputStream=new FileOutputStream(fileOut);

                while (fileInputStream.read()!=-1)
                {
                     System.out.println((char)fileInputStream.read());
                     //Thread.sleep(500);

                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
