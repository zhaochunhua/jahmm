package com.zhao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
public class ChineseCharacterEncoding {
    private  HashMap<String,Integer> chinese7000=new HashMap<String,Integer>();
    public HashMap<String,Integer> getEncoding(){
        BufferedReader br=null;
        try {
            br=new BufferedReader(new InputStreamReader(new FileInputStream(new File("CCE.txt")),"utf-8"));
            String line ;
            while((line=br.readLine())!=null){
                String[] cc=line.trim().split("\\s{1,}");
                chinese7000.put(cc[0],Integer.parseInt(cc[1]));
                                                               
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return chinese7000;
    }
                                                   
    public static void main(String[] args) {
        ChineseCharacterEncoding cce=new ChineseCharacterEncoding();
       // System.out.println(cce.chinese7000.size());
    }
}
