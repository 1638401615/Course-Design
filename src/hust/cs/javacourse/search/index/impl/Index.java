package hust.cs.javacourse.search.index.impl;

import hust.cs.javacourse.search.index.*;

import java.io.*;
import java.util.*;

/**
 * AbstractIndex的具体实现类
 */
public class Index extends AbstractIndex {
    /**
     * 返回索引的字符串表示
     *
     * @return 索引的字符串表示
     */
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        if(docIdToDocPathMapping.size() == 0 && termToPostingListMapping.size() ==0)
            return null;
        else
        {
            return "docIdToDocPathMapping: " + docIdToDocPathMapping + "\ntermToPostingListMapping: " + termToPostingListMapping;
        }
    }

    /**
     * 添加文档到索引，更新索引内部的HashMap
     *
     * @param document ：文档的AbstractDocument子类型表示
     */
    @Override
    public void addDocument(AbstractDocument document) {

        docIdToDocPathMapping.put(document.getDocId(),document.getDocPath());
        HashMap<AbstractTerm, List<Integer>> termToListMapping = new HashMap<>();
        for(AbstractTermTuple termTuple : document.getTuples())//先把文件里面的单词和对应的位置给读出来
        {
            if(termToListMapping.containsKey(termTuple.term))
                termToListMapping.get(termTuple.term).add(termTuple.curPos);
            else
            {
                termToListMapping.put(termTuple.term,new ArrayList<>());
                termToListMapping.get(termTuple.term).add(termTuple.curPos);
            }
        }
        for(Map.Entry<AbstractTerm,List<Integer>> entry : termToListMapping.entrySet())//再往每个单词对应的posting里面放文件id，freq和单词索引
        {
            if(termToPostingListMapping.containsKey(entry.getKey()))
                termToPostingListMapping.get(entry.getKey()).add(new Posting(document.getDocId(),entry.getValue().size(),entry.getValue()));
            else
            {
                termToPostingListMapping.put(entry.getKey(),new PostingList());
                termToPostingListMapping.get(entry.getKey()).add(new Posting(document.getDocId(),entry.getValue().size(), entry.getValue()));
            }
        }
    }

    /**
     * <pre>
     * 从索引文件里加载已经构建好的索引.内部调用FileSerializable接口方法readObject即可
     * @param file ：索引文件
     * </pre>
     */
    @Override
    public void load(File file) {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
            readObject(inputStream);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * <pre>
     * 将在内存里构建好的索引写入到文件. 内部调用FileSerializable接口方法writeObject即可
     * @param file ：写入的目标索引文件
     * </pre>
     */
    @Override
    public void save(File file) {
        try{
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            writeObject(outputStream);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 返回指定单词的PostingList
     *
     * @param term : 指定的单词
     * @return ：指定单词的PostingList;如果索引字典没有该单词，则返回null
     */
    @Override
    public AbstractPostingList search(AbstractTerm term) {
        return termToPostingListMapping.getOrDefault(term , null);
    }

    /**
     * 返回索引的字典.字典为索引里所有单词的并集
     *
     * @return ：索引中Term列表
     */
    @Override
    public Set<AbstractTerm> getDictionary() {
        return termToPostingListMapping.keySet();
    }

    /**
     * <pre>
     * 对索引进行优化，包括：
     *      对索引里每个单词的PostingList按docId从小到大排序
     *      同时对每个Posting里的positions从小到大排序
     * 在内存中把索引构建完后执行该方法
     * </pre>
     */
    @Override
    public void optimize() {
        for(Map.Entry<AbstractTerm,AbstractPostingList> map_entry : termToPostingListMapping.entrySet()){
            map_entry.getValue().sort();//给每个PostingList里面的docId排序
            for(int i = 0; i < map_entry.getValue().size();i++)
                Collections.sort(map_entry.getValue().get(i).getPositions());//排序每个posting的positions
        }
    }

    /**
     * 根据docId获得对应文档的完全路径名
     *
     * @param docId ：文档id
     * @return : 对应文档的完全路径名
     */
    @Override
    public String getDocName(int docId) {
        return docIdToDocPathMapping.get(docId);
    }

    /**
     * 写到二进制文件
     *
     * @param out :输出流对象
     */
    @Override
    public void writeObject(ObjectOutputStream out) {
        try{
            out.writeObject(docIdToDocPathMapping);
            out.writeObject(termToPostingListMapping);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 从二进制文件读
     *
     * @param in ：输入流对象
     */
    @Override
    @SuppressWarnings("unchecked")//用于让下面的强制类型转换不显示警告消息
    public void readObject(ObjectInputStream in) {
        try{
            docIdToDocPathMapping = (Map<Integer, String>) in.readObject();
            termToPostingListMapping = (Map<AbstractTerm, AbstractPostingList>) in.readObject();
        }catch (ClassNotFoundException | IOException e){
            e.printStackTrace();
        }
    }
}
