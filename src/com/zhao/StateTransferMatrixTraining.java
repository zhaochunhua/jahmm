package com.zhao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import be.ac.ulg.montefiore.run.jahmm.ViterbiCalculator;
/*
 * �������ϵĽ��ѵ��HMMģ�͵�״̬ת�ƾ���
 * һ�����ĸ�״̬��
 * B��һ���ʵĿ�ʼ
 * E��һ���ʵĽ���
 * M��һ���ʵ��м�
 * S�����ֳɴ�
 * ͳ�ƹ�ʽ�� Aij = P(Cj|Ci)  =  P(Ci,Cj) / P(Ci) = countC(Ci,Cj) / countC(Ci)
 * */
public class StateTransferMatrixTraining {
    public StateTransferMatrixTraining(){
        readFile("msr_training.utf8");
        hmm=buildHMM();
    }
    Hmm<ObservationInteger> hmm;
    //private String fileName;
    private final static HashMap<Character,Integer> map=new HashMap<Character,Integer>();
    private final static HashMap<Integer,Character> remap=new HashMap<Integer,Character>();
    //�Ժ��ֽ��б���
    private final static HashMap<String, Integer> cceMap=(new ChineseCharacterEncoding()).getEncoding();
    static{
        map.put('B', 0);map.put('M', 1);
        map.put('E', 2);map.put('S', 3);
        remap.put(0, 'B');
        remap.put(1, 'M');
        remap.put(2, 'E');
        remap.put(3, 'S');
    }
    private long freqC[][]=new long[4][4];
    //ͳ�ƻ��������õ���
    private long freqCO[][]=new long[4][7004];
    private long countC[]=new long[4];
                                                       
    private double[][] transferMatrix=new double[4][4];
    private double[][] mixedMatrix=new double[4][7004];
    //M��E�����ܳ����ھ��ӵ���λ
    private double[] Pi = {0.5, 0.0, 0.0, 0.5};
                                                       
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
    /*
     * ��  һ��  ����  ֪ʶ  ��  ����  ֪ʶ  Ҳ  û��  ��  ��  ��  ʲô  �߷�  ��
     * ��һ���ı���BEMS������б��룬�����������ִ�������
     *
     * 1���������ֳɴʡ�
     * 2��ֱ�ӹ��ˣ����迼�ǡ�
     * ������Ϊ����2�ȽϺ������ֳɴ��ܵ��ֳ��ֵ��ﾳ��Ӱ�죬����������Զ�ǵ�һ�ġ�
     * ��ѵ���Ĺ����У���ʵ��content.split("\\s{1,}");����򵥣������������˾���
     * �����ַ������������������£����ܲ�զ��
     * @param content,��Ҫ������ı�
     * @param withContent,�������ı��Ƿ��ԭ��
     * @return �������ı�
     * */
    private String encode(String content,boolean withContent){
        if(content==null||"".equals(content.trim()))return null;
        //�ִʺ���ı���ȥ��������
        content=content.replaceAll("\\pP", " ").trim();
                                                           
        StringBuilder sb=new StringBuilder();
        int start,end,len;
        start=end=0;len=content.length();
        //���ݿո���ı����зִ�
        while(end<len){
            if(Character.isWhitespace(content.charAt(end))){
                if(end>start){
                    //�õ�һ����
                    if(withContent)
                        insertWithContent(content,sb,start,end);
                    else
                        insert(sb,start,end);
                    ++end;start=end;
                                                                       
                }else{++start;++end;}
                                                                   
            }else{++end;}
        }
        if(end>start){
            if(withContent)
                insertWithContent(content,sb,start,end);
            else
                insert(sb,start,end);
        }
                                                           
        return sb.toString();
    }
    //����״̬ת�ƾ���
    private void calStatus(){
        int i,j;
        for(i=0;i<4;i++){
            for(j=0;j<4;j++){
                transferMatrix[i][j]=(double)freqC[i][j]/countC[i];
            }
        }
    }
                                                       
    public double[][] getStatus(){
                                                           
        return transferMatrix;
    }
    //�����������
    private void calMixed(){
        int i,j;
        for(i=0;i<4;i++){
            for(j=0;j<7002;j++){
                mixedMatrix[i][j]=(double)(freqCO[i][j]+1)/countC[i];
            }
        }
    }
    public double[][] getMixed(){
                                                           
        return mixedMatrix;
    }
    //����ѵ���ı�
    public void readFile(String fileName){
        BufferedReader br=null;
        String line,temp;
        try {
            br=new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)),"utf-8"));
            while((line=br.readLine())!=null){
                if("".equals(line.trim()))continue;
                //ͳ�Ʒ����ǩ������Ҫ���ַ�
                temp=encode(line,false);
                stmStatus(temp);
                //ͳ�ƻ���������Ҫ���ַ�
                temp=encode(line,true);
                stmMixed(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {br.close(); }catch (IOException e) {e.printStackTrace();}
        }
        //����freq��count����������ת�ƾ���
        calStatus();
        calMixed();
    }
    /*
     * ͳ��ÿһ�б���
     * */
    private void stmStatus(String encodeStr){
        int i,j,len;
        len=encodeStr.length();
        if(len<=0)return;
        for(i=0;i<len-1;++i){
            ++countC[map.get(encodeStr.charAt(i))];
            j=i+1;
            ++freqC[map.get(encodeStr.charAt(i))][map.get(encodeStr.charAt(j))];
        }
        ++countC[map.get(encodeStr.charAt(len-1))];
    }
    /*
     * ����Ļ�����Ҫ�����ַ������ַ�һ��,��Ϊÿ���ַ�������һ��
     * ��S��B��EӦB��EȥS��B��M԰E��S
     * */
    private void stmMixed(String encodeStr){
        int i,j,len;
        len=encodeStr.length();
        //�д���ľ��ӣ�ֱ�Ӻ���
        if(len%2!=0)return;
        Integer c,o;
        for(i=0;i<len;i+=2){
            j=i+1;
            c=map.get(encodeStr.charAt(j));
            o=cceMap.get(encodeStr.charAt(i));
            if(c==null||o==null){
                //System.out.println(encodeStr.charAt(i));
                continue;
            }
            ++freqCO[c][o-1];
        }
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
    //�Թ۲��ַ����б��룬ע��������Ҫ��һ����Ŀ������ʹ�±��0��ʼ
    private List<ObservationInteger> getOseq(String sen){
        List<ObservationInteger> oseq=new ArrayList<ObservationInteger>();
        for (int i = 0; i < sen.length(); i++) {
            oseq.add(new ObservationInteger(cceMap.get(sen.charAt(i))-1));
        }
        return oseq;
    }
    //���ı��ִʺ���ı����н���
    private String decode(String sen,int[] seqrs){
        StringBuilder sb=new StringBuilder();
        char ch;
        for(int i=0;i<sen.length();i++){
            sb.append(sen.charAt(i));
            ch=remap.get(seqrs[i]);
            if(ch=='E'||ch=='S')
                sb.append("/");
        }
        return sb.toString();
    }
    //ѵ��hmmģ��
    private Hmm<ObservationInteger> buildHMM(){
        Hmm<ObservationInteger> hmm=new Hmm < ObservationInteger >(4 ,new OpdfIntegerFactory (7004) );
        int i,j;
        for( i=0;i<4;i++){
            hmm.setPi(i, Pi[i]);
        }
        for(i=0;i<4;i++){
            for(j=0;j<4;j++){
                hmm.setAij(i, j, transferMatrix[i][j]);
            }
            hmm.setOpdf(i, new OpdfInteger(mixedMatrix[i]));
        }
        return hmm;
    }
    public String seg(String sen){
        List<ObservationInteger> oseq=getOseq( sen);
        ViterbiCalculator vc=new ViterbiCalculator(oseq, hmm);
        int[] segrs= vc.stateSequence();
        return decode(sen,segrs);
    }
    public static void main(String[] args) {
        StateTransferMatrixTraining tr=new StateTransferMatrixTraining();
        /*
         * ��������ֻ�Ǽ򵥵�ʵ��HMMģ�ͣ���ϸ���ϲ�û��������Ĵ������Բ����������������ڷִʡ�
         * ������ı��ǲ��ܼӱ����ŵģ�ԭ�����ڱ����Ų�û�б���
         * */
        String[] segs={"���Ժ��������쳤","���ǳ�˵������һ���̿���","���б�������������ִ��",
                "���ĺ���δ���Ķ���Ҫ�Ǽ�","��ӱ����ǰʹ�ù�����Ʒ"};
        for (String string : segs) {
            System.out.println(tr.seg(string));
        }
    }
}
