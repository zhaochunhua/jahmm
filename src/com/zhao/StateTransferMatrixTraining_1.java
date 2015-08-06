package com.zhao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
/*
 * 根据语料的结果训练HMM模型的状态转移矩阵
 * 一共有四个状态：
 * B：一个词的开始
 * E：一个词的结束
 * M：一个词的中间
 * S：单字成词
 * 统计公式： Aij = P(Cj|Ci)  =  P(Ci,Cj) / P(Ci) = Count(Ci,Cj) / Count(Ci)
 * */
public class StateTransferMatrixTraining_1 {
    //private String fileName;
    private final static HashMap<Character,Integer> map=new HashMap<Character,Integer>();
    static{
        map.put('B', 0);map.put('M', 1);
        map.put('E', 2);map.put('S', 3);
    }
    private long freq[][]=new long[4][4];
    private long count[]=new long[4];
                                                                                                                                              
    private void insert(StringBuilder sb,int start,int end){
        if(end-start>1){
            sb.append('B');
            for(int i=0;i<end-start-2;++i){
                sb.append('M');
            }
            sb.append('E');
        }else{
            sb.append('S');
        }
    }
                                                                                                                                              
    /*
     * “  一点  外语  知识  、  数理化  知识  也  没有  ，  还  攀  什么  高峰  ？
     * 对一段文本按BEMS规则进行编码，标点符号有两种处理方法：
     *
     * 1、算作单字成词。
     * 2、直接过滤，不予考虑。
     * 个人认为方案2比较合理，单字成词受到字出现的语境有影响，而标点符号永远是单一的。
     * */
    private String encode(String content){
        if(content==null||"".equals(content.trim()))return null;
        //分词后的文本，去掉标点符号
        content=content.replaceAll("\\pP", " ").trim();
                                                                                                                                                  
        StringBuilder sb=new StringBuilder();
    //  String[] terms=content.split("\\s{1,}");
        int start,end,len;
        start=end=0;len=content.length();
        //根据空格对文本进行分词
        while(end<len){
            if(Character.isWhitespace(content.charAt(end))){
                if(end>start){
                    //得到一个词
                    //  insertWithContent(content,sb,start,end);
                    insert(sb,start,end);
                    ++end;start=end;
                                                                                                                                                              
                }else{++start;++end;}
                                                                                                                                                          
            }else{++end;}
        }
        //insertWithContent(content,sb,start,end);
        insert(sb,start,end);
                                                                                                                                                  
        return sb.toString();
    }
                                                                                                                                              
    private double[][] cal(){
        double[][] transferMatrix=new double[4][4];
        int i,j;
        for(i=0;i<4;i++){
            for(j=0;j<4;j++){
                transferMatrix[i][j]=(double)freq[i][j]/count[i];
            }
        }
        return transferMatrix;
    }
                                                                                                                                              
    public double[][] readFile(String fileName){
        BufferedReader br=null;
        String line;
        try {
            br=new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)),"utf-8"));
            while((line=br.readLine())!=null){
                if("".equals(line.trim()))continue;
                line=encode(line);
                stmTraining(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {br.close(); }catch (IOException e) {e.printStackTrace();}
        }
        //根据freq和count矩阵来计算转移矩阵
                                                                                                                                                  
        return cal();
    }
                                                                                                                                              
    private void stmTraining(String encodeStr){
        int i,j,len;
        len=encodeStr.length();
        for(i=0;i<len-1;++i){
            ++count[map.get(encodeStr.charAt(i))];
            j=i+1;
            ++freq[map.get(encodeStr.charAt(i))][map.get(encodeStr.charAt(j))];
        }
        ++count[map.get(encodeStr.charAt(i))];
    }
    private void print(double[][] A){
        int i,j;
        char[] chs={'B','M','E','S'};
        System.out.println("\t\t"+"B"+"\t\t\t"+"M"+"\t\t\t"+"E"+"\t\t\t"+"S");
        for(i=0;i<4;i++){
            System.out.print(chs[i]+"\t");
            for(j=0;j<4;j++){
                System.out.format("%.12f\t\t",A[i][j]);
                                                                                                                                                          
            }
            System.out.println();
        }
    }
    public static void main(String[] args) {
        StateTransferMatrixTraining_1 tr=new StateTransferMatrixTraining_1();
        double A[][]=tr.readFile("msr_training.utf8");
        tr.print(A);
      /*String ss=tr.encode("偶尔  有  老乡  拥  上来  想  看  “  大官  ”  ，  立即  会  遭到  “  闪开  ！");
      //偶B尔E  有S  老B乡E  拥S  上B来E  想S  看S  “  大B官E  ”  ，  立B即E  会S  遭B到E  “  闪B开E  ！
      System.out.println(ss);*/
    }
                                                                                                                                              
    /*
     * 带文本内容,比如：你 现在 应该 去 幼儿园 了，
     *    输出的结果为：你S现B在E应B该E去S幼B儿M园E了S
     *    测试时用
     * */
    private void insertWithContent(String content,StringBuilder sb,int start,int end){
        if(end-start>1){
            sb.append(content.charAt(start));
            sb.append('B');
            for(int i=0;i<end-start-2;++i){
                sb.append(content.charAt(start+i+1));
                sb.append('M');
            }
            sb.append(content.charAt(end-1));
            sb.append('E');
        }else{
            sb.append(content.charAt(end-1));
            sb.append('S');
        }
    }
}