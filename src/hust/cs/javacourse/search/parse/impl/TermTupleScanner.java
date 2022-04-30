package hust.cs.javacourse.search.parse.impl;

import hust.cs.javacourse.search.index.AbstractTerm;
import hust.cs.javacourse.search.index.AbstractTermTuple;
import hust.cs.javacourse.search.index.impl.Term;
import hust.cs.javacourse.search.index.impl.TermTuple;
import hust.cs.javacourse.search.parse.AbstractTermTupleScanner;
import hust.cs.javacourse.search.util.Config;
import hust.cs.javacourse.search.util.StringSplitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TermTupleScanner extends AbstractTermTupleScanner {

    private int pos = 0; //记录单词位置
    private List<String> buf;
    private StringSplitter string_splitter = new StringSplitter();

    /**
     * 缺省构造函数
     */
    public TermTupleScanner() {
        buf = new ArrayList<>();
        string_splitter.setSplitRegex(Config.STRING_SPLITTER_REGEX);//将字符串切分成单词
    }
    /**
     * 构造函数
     *
     * @param input ：指定输入流对象，应该关联到一个文本文件
     */
    public TermTupleScanner(BufferedReader input) {
        super(input);
        buf = new ArrayList<>();
        string_splitter.setSplitRegex(Config.STRING_SPLITTER_REGEX);
    }

    /**
     *获得下一个三元组
     * @return :下一个三元组；如果到了流的末尾，返回null
     */
    @Override
    public AbstractTermTuple next() {
        try{
            if(buf.isEmpty()){//还未读取文本
                String s;
                StringBuilder strbuilder = new StringBuilder();//可变的字符串把所有的文件内容都存到strbuilder里面
                while ((s = input.readLine()) != null){
                    strbuilder.append(s).append("\n");//一定要添加换行符号，否则无缝衔接下一行了!!!
                }
                s = strbuilder.toString().trim();
                s = s.toLowerCase();
                buf = string_splitter.splitByRegex(s);//把字符串分割成单词列表
            }
            if(buf.size() == 0)
                return  null;
            AbstractTerm term = new Term(buf.get(0));
            buf.remove(0);
            return new TermTuple(term,pos++);
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public void close(){
        super.close();
    }
}
