package hust.cs.javacourse.search.query.impl;

import hust.cs.javacourse.search.index.AbstractPosting;
import hust.cs.javacourse.search.index.AbstractPostingList;
import hust.cs.javacourse.search.index.AbstractTerm;
import hust.cs.javacourse.search.index.impl.Posting;
import hust.cs.javacourse.search.index.impl.Term;
import hust.cs.javacourse.search.query.AbstractHit;
import hust.cs.javacourse.search.query.AbstractIndexSearcher;
import hust.cs.javacourse.search.query.Sort;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IndexSearcher extends AbstractIndexSearcher {
    /**
     * 从指定索引文件打开索引，加载到index对象里. 一定要先打开索引，才能执行search方法
     *
     * @param indexFile ：指定索引文件
     */
    @Override
    public void open(String indexFile) {
        index.load(new File(indexFile));
        index.optimize();//优化索引
    }
    /**
     * 根据单个检索词进行搜索
     *
     * @param queryTerm ：检索词
     * @param sorter    ：排序器
     * @return ：命中结果数组
     */
    @Override
    public AbstractHit[] search(AbstractTerm queryTerm, Sort sorter) {
        AbstractPostingList postingList =index.search(queryTerm);//获取所要搜索的term对应的PostingList
        if(postingList == null)
            return null;
        List<AbstractHit>list = new ArrayList<>();
        for(int i = 0;i < postingList.size();i++)
        {
            AbstractPosting posting = postingList.get(i);
            String docPath = index.getDocName(posting.getDocId());
            HashMap<AbstractTerm,AbstractPosting> map = new HashMap<>();
            map.put(queryTerm,posting);
            AbstractHit hit = new Hit(posting.getDocId(),docPath,map);
            hit.setScore(sorter.score(hit));
            list.add(hit);
        }
        sorter.sort(list);
        return list.toArray(new AbstractHit[0]);//结果要的是Hit的数组，所以要把list这个ArrayList对象转换成数组，参数用来指定返回AbstractHit类型的数组

    }
    /**
     * 根据二个检索词进行搜索
     *
     * @param queryTerm1 ：第1个检索词
     * @param queryTerm2 ：第2个检索词
     * @param sorter     ：    排序器
     * @param combine    ：   多个检索词的逻辑组合方式
     * @return ：命中结果数组
     */
    @Override
    public AbstractHit[] search(AbstractTerm queryTerm1, AbstractTerm queryTerm2, Sort sorter, LogicalCombination combine) {
        AbstractPostingList postingList1 = index.search(queryTerm1);
        AbstractPostingList postingList2 = index.search(queryTerm2);
        List<AbstractHit> list = new ArrayList<>();
        if(postingList1 == null && postingList2 == null)
            return null;
        int len1 = 0;
        int len2 = 0;
        if(postingList1 != null)
            len1 = postingList1.size();
        if(postingList2 != null)
            len2 = postingList2.size();
        Posting nullposting = new Posting(-1,-1,null);
        int i = 0 , j = 0;
        while (i < len1 || j < len2){
            AbstractPosting posting1;
            if (i < len1) {
                assert postingList1 != null;
                posting1 = postingList1.get(i);
            } else {
                posting1 = nullposting;
            }
            AbstractPosting posting2;
            if (j < len2) {
                assert postingList2 != null;
                posting2 = postingList2.get(j);
            } else {
                posting2 = nullposting;
            }
            int docId1 = posting1.getDocId(), docId2 = posting2.getDocId();
            if(docId1 == docId2 && docId1 != -1){//两个索引词在一个文件中
                String docPath = index.getDocName(docId1);
                HashMap<AbstractTerm,AbstractPosting> map = new HashMap<>();
                map.put(queryTerm1,posting1);
                map.put(queryTerm2,posting2);
                AbstractHit hit = new Hit(docId1,docPath,map);
                hit.setScore(sorter.score(hit));
                list.add(hit);
                i++;
                j++;
            }
            //此处不能用docId1 != docId1直接判断，因为这样同时++i和++j有可能导致某个文件同时含有两个索引词但是被跳过了
            //所以应该让id小的文件慢慢增加到和id大的文件一样
            else if(docId1 < docId2 && docId1 != -1){
                if(combine == LogicalCombination.OR){
                    //若是或则把docId1的查询结果加入list
                    HashMap<AbstractTerm, AbstractPosting> map = new HashMap<>();
                    String docPath = index.getDocName(docId1);
                    map.put(queryTerm1, posting1);
                    AbstractHit hit = new Hit(docId1, docPath, map);
                    hit.setScore(sorter.score(hit));
                    list.add(hit);
                }
                i++;//不要忘记增加索引！
                }else {
                    //docId2 < docId1情况
                    if(combine == LogicalCombination.OR && docId2 != -1){
                        HashMap<AbstractTerm, AbstractPosting> map = new HashMap<>();
                        String docPath = index.getDocName(docId2);
                        map.put(queryTerm1, posting1);
                        AbstractHit hit = new Hit(docId1, docPath, map);
                        hit.setScore(sorter.score(hit));
                        list.add(hit);
                    }
                    j++;
                }
            }
        sorter.sort(list);
        return list.toArray(new AbstractHit[0]);
    }
    /**
     * 根据二个相邻的检索词进行搜索
     *
     * @param queryTerm1 ：第1个检索词
     * @param queryTerm2 ：第2个检索词
     * @param sorter     ：    排序器
     * @return ：命中结果数组
     */
    public AbstractHit[] search(AbstractTerm queryTerm1, AbstractTerm queryTerm2, Sort sorter){
        AbstractPostingList postingList1 = index.search(queryTerm1);
        AbstractPostingList postingList2 = index.search(queryTerm2);
        List<AbstractHit> list = new ArrayList<>();
        if(postingList1 == null || postingList2 == null)
            return null;
        int len1 = postingList1.size();
        int len2 = postingList2.size();
        int i = 0 , j = 0;
        while (i < len1 && j < len2){
            AbstractPosting posting1 = postingList1.get(i);
            AbstractPosting posting2 = postingList2.get(j);
            int docId1 = posting1.getDocId();
            int docId2 = posting2.getDocId();
            if(docId1 == docId2 && docId1 != 1){
                List<Integer> positions1 = posting1.getPositions();
                List<Integer> positions2 = posting2.getPositions();
                List<Integer> Phrase_positions = new ArrayList<>();
                int pos1 = 0;
                int pos2 = 0;
                while (pos1 < positions1.size() && pos2 < positions2.size()){
                    int curPos1 = positions1.get(pos1);
                    int curPos2 = positions2.get(pos2);
                    if(Math.abs(curPos1 - curPos2) == 1){//两单词位置相邻
                        Phrase_positions.add(Math.min(curPos1,curPos2));
                        pos1++;
                        pos2++;
                    }else if(curPos1 < curPos2 - 2){
                        pos1++;
                    }else if(curPos2 < curPos1 - 2){
                        pos2++;
                    }
                }
                if(!Phrase_positions.isEmpty()){
                    String path = index.getDocName(docId1);
                    HashMap<AbstractTerm,AbstractPosting> map = new HashMap<>();
                    map.put(new Term(queryTerm1.getContent() + " " + queryTerm2.getContent()),new Posting(docId1,Phrase_positions.size(),Phrase_positions));
                    AbstractHit hit = new Hit(docId1,path,map);
                    hit.setScore(sorter.score(hit));
                    list.add(hit);
                }
                i++;
                j++;
            }else if(docId1 < docId2){
                i++;
            }else{
                j++;
            }

        }
        sorter.sort(list);
        return list.toArray(new AbstractHit[0]);
    }
}
