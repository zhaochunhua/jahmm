package com.zhao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
/*
 * �������ϵĽ��ѵ��HMMģ�͵�״̬ת�ƾ���
 * һ�����ĸ�״̬��
 * B��һ���ʵĿ�ʼ
 * E��һ���ʵĽ���
 * M��һ���ʵ��м�
 * S�����ֳɴ�
 * ͳ�ƹ�ʽ�� Aij = P(Cj|Ci)  =  P(Ci,Cj) / P(Ci) = Count(Ci,Cj) / Count(Ci)
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
     * ��  һ��  ����  ֪ʶ  ��  ����  ֪ʶ  Ҳ  û��  ��  ��  ��  ʲô  �߷�  ��
     * ��һ���ı���BEMS������б��룬�����������ִ�������
     *
     * 1���������ֳɴʡ�
     * 2��ֱ�ӹ��ˣ����迼�ǡ�
     * ������Ϊ����2�ȽϺ������ֳɴ��ܵ��ֳ��ֵ��ﾳ��Ӱ�죬����������Զ�ǵ�һ�ġ�
     * */
    private String encode(String content){
        if(content==null||"".equals(content.trim()))return null;
        //�ִʺ���ı���ȥ��������
        content=content.replaceAll("\\pP", " ").trim();
                                                                                                                                                  
        StringBuilder sb=new StringBuilder();
    //  String[] terms=content.split("\\s{1,}");
        int start,end,len;
        start=end=0;len=content.length();
        //���ݿո���ı����зִ�
        while(end<len){
            if(Character.isWhitespace(content.charAt(end))){
                if(end>start){
                    //�õ�һ����
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
        //����freq��count����������ת�ƾ���
                                                                                                                                                  
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
      /*String ss=tr.encode("ż��  ��  ����  ӵ  ����  ��  ��  ��  ���  ��  ��  ����  ��  �⵽  ��  ����  ��");
      //żB��E  ��S  ��B��E  ӵS  ��B��E  ��S  ��S  ��  ��B��E  ��  ��  ��B��E  ��S  ��B��E  ��  ��B��E  ��
      System.out.println(ss);*/
    }
                                                                                                                                              
    /*
     * ���ı�����,���磺�� ���� Ӧ�� ȥ �׶�԰ �ˣ�
     *    ����Ľ��Ϊ����S��B��EӦB��EȥS��B��M԰E��S
     *    ����ʱ��
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